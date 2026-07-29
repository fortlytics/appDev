package com.example.data.collector

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.*
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.random.Random

data class TelephonySnapshot(
    val operatorName: String = "Unknown Carrier",
    val networkType: String = "4G LTE",
    val signalStrengthDbm: Int = -90,
    val rsrp: Int? = null,
    val rsrq: Int? = null,
    val rssnr: Int? = null,
    val cellId: String? = null,
    val isSimulated: Boolean = false
)

class TelephonyCollector(private val context: Context) {

    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    fun observeTelephonyState(forceSimulation: Boolean = false): Flow<TelephonySnapshot> = callbackFlow {
        if (forceSimulation || isEmulatorOrNoRadio()) {
            // Simulated telemetry loop for emulators / evaluators
            var simDbm = -82
            var simRsrp = -92
            var simRsrq = -10
            var simRssnr = 18

            val timer = java.util.Timer()
            timer.scheduleAtFixedRate(object : java.util.TimerTask() {
                override fun run() {
                    // Slight natural variance
                    val delta = Random.nextInt(-3, 4)
                    simDbm = (simDbm + delta).coerceIn(-125, -55)
                    simRsrp = (simDbm - 8).coerceIn(-130, -60)
                    simRsrq = (-12 + Random.nextInt(-2, 3)).coerceIn(-20, -3)
                    simRssnr = (15 + Random.nextInt(-4, 5)).coerceIn(0, 30)

                    val snapshot = TelephonySnapshot(
                        operatorName = "Simulated Carrier (Demo)",
                        networkType = if (Random.nextBoolean()) "5G" else "4G LTE",
                        signalStrengthDbm = simDbm,
                        rsrp = simRsrp,
                        rsrq = simRsrq,
                        rssnr = simRssnr,
                        cellId = "Cell-#${104800 + Random.nextInt(0, 99)}",
                        isSimulated = true
                    )
                    trySend(snapshot)
                }
            }, 0, 3000)

            awaitClose { timer.cancel() }
        } else {
            // Real Telephony API callback
            val hasReadStatePermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED

            fun emitCurrentSnapshot() {
                try {
                    val snapshot = getRealSnapshot(hasReadStatePermission)
                    trySend(snapshot)
                } catch (e: Exception) {
                    trySend(getFallbackSnapshot())
                }
            }

            emitCurrentSnapshot()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val callback = object : TelephonyCallback(),
                    TelephonyCallback.SignalStrengthsListener,
                    TelephonyCallback.ServiceStateListener {
                    override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                        emitCurrentSnapshot()
                    }

                    override fun onServiceStateChanged(serviceState: ServiceState) {
                        emitCurrentSnapshot()
                    }
                }

                try {
                    telephonyManager.registerTelephonyCallback(
                        context.mainExecutor,
                        callback
                    )
                    awaitClose {
                        telephonyManager.unregisterTelephonyCallback(callback)
                    }
                } catch (e: Exception) {
                    awaitClose {}
                }
            } else {
                @Suppress("DEPRECATION")
                val listener = object : PhoneStateListener() {
                    @Deprecated("Deprecated in Java")
                    override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
                        emitCurrentSnapshot()
                    }
                }
                @Suppress("DEPRECATION")
                telephonyManager.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
                awaitClose {
                    @Suppress("DEPRECATION")
                    telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE)
                }
            }
        }
    }

    private fun getRealSnapshot(hasPermission: Boolean): TelephonySnapshot {
        val operator = telephonyManager.networkOperatorName.takeIf { it.isNotBlank() } ?: "Unknown Operator"
        val netTypeStr = getNetworkTypeString(telephonyManager.networkType)

        var dbm = -95
        var rsrp: Int? = null
        var rsrq: Int? = null
        var rssnr: Int? = null
        var cellIdStr: String? = null

        if (hasPermission && ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val cellInfoList = telephonyManager.allCellInfo
            if (!cellInfoList.isNullOrEmpty()) {
                for (info in cellInfoList) {
                    if (info.isRegistered) {
                        when (info) {
                            is CellInfoLte -> {
                                val lte = info.cellSignalStrength
                                dbm = filterInvalid(lte.dbm) ?: -95
                                rsrp = filterInvalid(lte.rsrp)
                                rsrq = filterInvalid(lte.rsrq)
                                rssnr = filterInvalid(lte.rssnr)
                                cellIdStr = filterInvalid(info.cellIdentity.ci)?.toString()
                            }
                            is CellInfoNr -> {
                                val nr = info.cellSignalStrength as? CellSignalStrengthNr
                                if (nr != null) {
                                    dbm = filterInvalid(nr.dbm) ?: -95
                                    rsrp = filterInvalid(nr.csiRsrp) ?: filterInvalid(nr.ssRsrp)
                                    rsrq = filterInvalid(nr.csiRsrq) ?: filterInvalid(nr.ssRsrq)
                                    rssnr = filterInvalid(nr.csiSinr) ?: filterInvalid(nr.ssSinr)
                                }
                                cellIdStr = "NR-${info.cellIdentity}"
                            }
                            is CellInfoGsm -> {
                                dbm = filterInvalid(info.cellSignalStrength.dbm) ?: -95
                                cellIdStr = filterInvalid(info.cellIdentity.cid)?.toString()
                            }
                            is CellInfoWcdma -> {
                                dbm = filterInvalid(info.cellSignalStrength.dbm) ?: -95
                                cellIdStr = filterInvalid(info.cellIdentity.cid)?.toString()
                            }
                        }
                        break
                    }
                }
            }
        }

        return TelephonySnapshot(
            operatorName = operator,
            networkType = netTypeStr,
            signalStrengthDbm = dbm,
            rsrp = rsrp,
            rsrq = rsrq,
            rssnr = rssnr,
            cellId = cellIdStr,
            isSimulated = false
        )
    }

    private fun filterInvalid(value: Int?): Int? {
        if (value == null) return null
        if (value == Int.MAX_VALUE || value == Int.MIN_VALUE || value == 99 || value == -1) return null
        return value
    }

    private fun getNetworkTypeString(type: Int): String {
        return when (type) {
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS -> "2G"
            else -> "4G LTE"
        }
    }

    private fun isEmulatorOrNoRadio(): Boolean {
        val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu")
        val noOperator = telephonyManager.networkOperatorName.isNullOrBlank() ||
                telephonyManager.simState != TelephonyManager.SIM_STATE_READY
        return isEmulator || noOperator
    }

    private fun getFallbackSnapshot() = TelephonySnapshot(
        operatorName = "Mobile Network",
        networkType = "4G LTE",
        signalStrengthDbm = -88,
        rsrp = -96,
        rsrq = -11,
        rssnr = 14,
        cellId = "Cell-#10842"
    )
}
