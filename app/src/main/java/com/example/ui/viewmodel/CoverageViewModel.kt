package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.collector.LocationSnapshot
import com.example.data.collector.SpeedTestState
import com.example.data.collector.TelephonySnapshot
import com.example.data.db.AppDatabase
import com.example.data.model.NetworkReading
import com.example.data.model.SignalQuality
import com.example.data.repository.CoverageRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class AnalyticsSummary(
    val totalReadings: Int = 0,
    val avgDbm: Int = 0,
    val avgDownloadMbps: Double = 0.0,
    val avgUploadMbps: Double = 0.0,
    val avgPingMs: Long = 0,
    val excellentCount: Int = 0,
    val goodCount: Int = 0,
    val fairCount: Int = 0,
    val poorCount: Int = 0
)

class CoverageViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = CoverageRepository(application, db.networkReadingDao())

    // Simulation Toggle State
    private val _isSimulationMode = MutableStateFlow(false)
    val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    // Telephony Stream State
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val telephonySnapshot: StateFlow<TelephonySnapshot> = _isSimulationMode
        .flatMapLatest { isSim ->
            repository.observeTelephony(forceSimulation = isSim)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TelephonySnapshot()
        )

    // Current Location State
    private val _currentLocation = MutableStateFlow(LocationSnapshot(51.5074, -0.1278, true))
    val currentLocation: StateFlow<LocationSnapshot> = _currentLocation.asStateFlow()

    // All Historical Readings Flow
    val allReadings: StateFlow<List<NetworkReading>> = repository.allReadings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Analytics Summary Derived Flow
    val analyticsSummary: StateFlow<AnalyticsSummary> = allReadings.map { list ->
        if (list.isEmpty()) {
            AnalyticsSummary()
        } else {
            val total = list.size
            val dbmAvg = list.map { it.signalStrengthDbm }.average().toInt()
            val dlAvg = Math.round(list.map { it.downloadMbps }.average() * 10.0) / 10.0
            val ulAvg = Math.round(list.map { it.uploadMbps }.average() * 10.0) / 10.0
            val pingAvg = list.map { it.pingMs }.average().toLong()

            var excellent = 0
            var good = 0
            var fair = 0
            var poor = 0

            for (r in list) {
                when (r.signalQuality) {
                    SignalQuality.EXCELLENT -> excellent++
                    SignalQuality.GOOD -> good++
                    SignalQuality.FAIR -> fair++
                    SignalQuality.POOR -> poor++
                }
            }

            AnalyticsSummary(
                totalReadings = total,
                avgDbm = dbmAvg,
                avgDownloadMbps = dlAvg,
                avgUploadMbps = ulAvg,
                avgPingMs = pingAvg,
                excellentCount = excellent,
                goodCount = good,
                fairCount = fair,
                poorCount = poor
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsSummary()
    )

    // Speed Test State Machine
    private val _speedTestState = MutableStateFlow<SpeedTestState>(SpeedTestState.Idle)
    val speedTestState: StateFlow<SpeedTestState> = _speedTestState.asStateFlow()

    private var speedTestJob: Job? = null

    init {
        // Initialize location and generate sample data for evaluator demo
        refreshLocation()
        viewModelScope.launch {
            repository.generateSampleReadingsIfEmpty()
        }
    }

    fun toggleSimulationMode(enabled: Boolean) {
        _isSimulationMode.value = enabled
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _currentLocation.value = repository.getLocation()
        }
    }

    fun quickSaveCurrentReading() {
        viewModelScope.launch {
            refreshLocation()
            val tele = telephonySnapshot.value
            val loc = currentLocation.value

            val reading = NetworkReading(
                latitude = loc.latitude,
                longitude = loc.longitude,
                networkType = tele.networkType,
                signalStrengthDbm = tele.signalStrengthDbm,
                rsrp = tele.rsrp,
                rsrq = tele.rsrq,
                rssnr = tele.rssnr,
                cellId = tele.cellId,
                operatorName = tele.operatorName,
                isSimulated = tele.isSimulated
            )
            repository.saveReading(reading)
        }
    }

    fun startSpeedTest() {
        if (_speedTestState.value is SpeedTestState.TestingPing ||
            _speedTestState.value is SpeedTestState.TestingDownload ||
            _speedTestState.value is SpeedTestState.TestingUpload
        ) {
            return
        }

        speedTestJob?.cancel()
        speedTestJob = viewModelScope.launch {
            repository.runSpeedTest().collect { state ->
                _speedTestState.value = state
                if (state is SpeedTestState.Completed) {
                    // Auto save speed test result into Room
                    refreshLocation()
                    val tele = telephonySnapshot.value
                    val loc = currentLocation.value

                    val reading = NetworkReading(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        networkType = tele.networkType,
                        signalStrengthDbm = tele.signalStrengthDbm,
                        rsrp = tele.rsrp,
                        rsrq = tele.rsrq,
                        rssnr = tele.rssnr,
                        cellId = tele.cellId,
                        downloadMbps = state.downloadMbps,
                        uploadMbps = state.uploadMbps,
                        pingMs = state.pingMs,
                        operatorName = tele.operatorName,
                        isSimulated = tele.isSimulated
                    )
                    repository.saveReading(reading)
                }
            }
        }
    }

    fun resetSpeedTest() {
        speedTestJob?.cancel()
        _speedTestState.value = SpeedTestState.Idle
    }

    fun deleteReading(id: Long) {
        viewModelScope.launch {
            repository.deleteReading(id)
        }
    }

    fun clearAllReadings() {
        viewModelScope.launch {
            repository.clearAllReadings()
        }
    }

    fun exportAndShareCsv() {
        viewModelScope.launch {
            val csvFile: File? = repository.exportReadingsToCsvFile()
            if (csvFile != null && csvFile.exists()) {
                repository.shareCsvFile(csvFile)
            }
        }
    }
}
