package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "readings")
data class NetworkReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val networkType: String, // "5G", "4G LTE", "3G", "2G", "Wi-Fi", "No Service"
    val signalStrengthDbm: Int, // e.g. -85 dBm
    val rsrp: Int? = null, // LTE RSRP
    val rsrq: Int? = null, // LTE RSRQ
    val rssnr: Int? = null, // LTE RSSNR / SNR
    val cellId: String? = null,
    val downloadMbps: Double = 0.0,
    val uploadMbps: Double = 0.0,
    val pingMs: Long = 0,
    val operatorName: String = "Unknown Carrier",
    val isSimulated: Boolean = false
) {
    val signalQuality: SignalQuality
        get() = when {
            signalStrengthDbm >= -85 -> SignalQuality.EXCELLENT
            signalStrengthDbm >= -98 -> SignalQuality.GOOD
            signalStrengthDbm >= -110 -> SignalQuality.FAIR
            else -> SignalQuality.POOR
        }
}

enum class SignalQuality(val label: String, val hexColor: Long) {
    EXCELLENT("Excellent", 0xFF10B981), // Emerald
    GOOD("Good", 0xFF3B82F6),      // Blue
    FAIR("Fair", 0xFFF59E0B),      // Amber
    POOR("Poor", 0xFFEF4444)       // Red
}
