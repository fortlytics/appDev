package com.example.data.repository

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.collector.*
import com.example.data.db.NetworkReadingDao
import com.example.data.model.NetworkReading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class CoverageRepository(
    private val context: Context,
    private val dao: NetworkReadingDao
) {
    private val telephonyCollector = TelephonyCollector(context)
    private val locationCollector = LocationCollector(context)
    private val speedTestEngine = SpeedTestEngine()

    val allReadings: Flow<List<NetworkReading>> = dao.getAllReadings()
    val latestReading: Flow<NetworkReading?> = dao.getLatestReading()

    fun observeTelephony(forceSimulation: Boolean = false): Flow<TelephonySnapshot> {
        return telephonyCollector.observeTelephonyState(forceSimulation)
    }

    suspend fun getLocation(): LocationSnapshot {
        return locationCollector.getCurrentLocation()
    }

    fun runSpeedTest(): Flow<SpeedTestState> {
        return speedTestEngine.runSpeedTest()
    }

    suspend fun saveReading(reading: NetworkReading): Long = withContext(Dispatchers.IO) {
        dao.insertReading(reading)
    }

    suspend fun deleteReading(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun clearAllReadings() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }

    suspend fun generateSampleReadingsIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getCount() == 0) {
            val sampleList = mutableListOf<NetworkReading>()
            val baseLat = 51.5074
            val baseLng = -0.1278
            val now = System.currentTimeMillis()
            val operators = listOf("Vodafone UK", "EE", "O2", "Three UK")
            val netTypes = listOf("5G", "4G LTE", "4G LTE", "3G")

            for (i in 0 until 24) {
                val timeOffsetMs = now - (23 - i) * 3600_000L - Random.nextLong(1000, 60000)
                val lat = baseLat + (Random.nextDouble() - 0.5) * 0.04
                val lng = baseLng + (Random.nextDouble() - 0.5) * 0.04
                val dbm = Random.nextInt(-115, -60)
                val rsrp = (dbm - 8).coerceIn(-130, -65)
                val rsrq = Random.nextInt(-18, -4)
                val rssnr = Random.nextInt(2, 28)
                val dl = when {
                    dbm >= -85 -> Random.nextDouble(45.0, 180.0)
                    dbm >= -98 -> Random.nextDouble(18.0, 55.0)
                    else -> Random.nextDouble(2.0, 15.0)
                }
                val ul = dl * 0.3 + Random.nextDouble(1.0, 5.0)
                val ping = (1000.0 / Math.abs(dbm)).toLong() + Random.nextLong(10, 30)

                sampleList.add(
                    NetworkReading(
                        timestamp = timeOffsetMs,
                        latitude = lat,
                        longitude = lng,
                        networkType = netTypes[i % netTypes.size],
                        signalStrengthDbm = dbm,
                        rsrp = rsrp,
                        rsrq = rsrq,
                        rssnr = rssnr,
                        cellId = "Cell-#${102000 + i}",
                        downloadMbps = Math.round(dl * 10.0) / 10.0,
                        uploadMbps = Math.round(ul * 10.0) / 10.0,
                        pingMs = ping,
                        operatorName = operators[i % operators.size],
                        isSimulated = true
                    )
                )
            }
            dao.insertAll(sampleList)
        }
    }

    suspend fun exportReadingsToCsvFile(): File? = withContext(Dispatchers.IO) {
        val readings = dao.getAllReadings().first()
        if (readings.isEmpty()) return@withContext null

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val csvBuilder = StringBuilder()

        // CSV Header
        csvBuilder.append("ID,Timestamp,Formatted_Time,Latitude,Longitude,Operator,Network_Type,Signal_dBm,RSRP_dBm,RSRQ_dB,RSSNR_dB,Cell_ID,Download_Mbps,Upload_Mbps,Ping_ms,Is_Simulated\n")

        for (r in readings) {
            val dateStr = dateFormat.format(Date(r.timestamp))
            csvBuilder.append("${r.id},")
            csvBuilder.append("${r.timestamp},")
            csvBuilder.append("\"$dateStr\",")
            csvBuilder.append("${r.latitude},")
            csvBuilder.append("${r.longitude},")
            csvBuilder.append("\"${r.operatorName}\",")
            csvBuilder.append("\"${r.networkType}\",")
            csvBuilder.append("${r.signalStrengthDbm},")
            csvBuilder.append("${r.rsrp ?: ""},")
            csvBuilder.append("${r.rsrq ?: ""},")
            csvBuilder.append("${r.rssnr ?: ""},")
            csvBuilder.append("\"${r.cellId ?: ""}\",")
            csvBuilder.append("${r.downloadMbps},")
            csvBuilder.append("${r.uploadMbps},")
            csvBuilder.append("${r.pingMs},")
            csvBuilder.append("${r.isSimulated}\n")
        }

        val exportDir = File(context.cacheDir, "csv_exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val fileName = "network_coverage_data_${System.currentTimeMillis()}.csv"
        val file = File(exportDir, fileName)
        file.writeText(csvBuilder.toString())
        file
    }

    fun shareCsvFile(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Network Coverage Analyzer - Exported Data")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Coverage Readings CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
