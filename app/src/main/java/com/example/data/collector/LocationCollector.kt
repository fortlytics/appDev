package com.example.data.collector

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

data class LocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean = false
)

class LocationCollector(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): LocationSnapshot {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return getDefaultLocation()
        }

        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (location != null) {
                LocationSnapshot(location.latitude, location.longitude, false)
            } else {
                val lastLoc = fusedLocationClient.lastLocation.await()
                if (lastLoc != null) {
                    LocationSnapshot(lastLoc.latitude, lastLoc.longitude, false)
                } else {
                    getDefaultLocation()
                }
            }
        } catch (e: Exception) {
            getDefaultLocation()
        }
    }

    private fun getDefaultLocation(): LocationSnapshot {
        // Default location (e.g. London / Central Academic Campus area with slight random scatter for testing)
        val lat = 51.5074 + (Math.random() - 0.5) * 0.015
        val lng = -0.1278 + (Math.random() - 0.5) * 0.015
        return LocationSnapshot(lat, lng, true)
    }
}
