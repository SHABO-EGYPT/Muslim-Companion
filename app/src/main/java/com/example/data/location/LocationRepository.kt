package com.example.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class AppLocation(
    val latitude: Double,
    val longitude: Double,
    val locationName: String
)

interface LocationRepository {
    suspend fun getCurrentLocation(): AppLocation?
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String
}

@Singleton
class RealLocationRepository @Inject constructor(
    private val context: Context
) : LocationRepository {

    companion object {
        private const val TAG = "LocationRepo"
    }

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    override suspend fun getCurrentLocation(): AppLocation? = withContext(Dispatchers.IO) {
        val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (finePerm != PackageManager.PERMISSION_GRANTED && coarsePerm != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission not granted.")
            return@withContext null
        }

        try {
            // Step 1: Try last known location first with a 3s timeout
            val lastLoc: Location? = withTimeoutOrNull(3_000) {
                suspendCancellableCoroutine { continuation ->
                    try {
                        fusedClient.lastLocation
                            .addOnSuccessListener { loc ->
                                if (continuation.isActive) continuation.resume(loc)
                            }
                            .addOnFailureListener {
                                if (continuation.isActive) continuation.resume(null)
                            }
                            .addOnCanceledListener {
                                if (continuation.isActive) continuation.resume(null)
                            }
                    } catch (e: SecurityException) {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            }

            val location = if (lastLoc != null) {
                lastLoc
            } else {
                // Step 2: Request fresh high accuracy location with a 6s timeout
                withTimeoutOrNull(6_000) {
                    val cts = CancellationTokenSource()
                    suspendCancellableCoroutine { continuation ->
                        continuation.invokeOnCancellation { cts.cancel() }
                        try {
                            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                                .addOnSuccessListener { loc ->
                                    if (continuation.isActive) continuation.resume(loc)
                                }
                                .addOnFailureListener {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                                .addOnCanceledListener {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                        } catch (e: SecurityException) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    }
                }
            }

            if (location != null) {
                val name = reverseGeocode(location.latitude, location.longitude)
                AppLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    locationName = name
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current location", e)
            null
        }
    }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<android.location.Address>) {
                            val name = formatAddress(addresses.firstOrNull(), latitude, longitude)
                            if (continuation.isActive) continuation.resume(name)
                        }

                        override fun onError(errorMessage: String?) {
                            val fallback = "Coordinates: %.2f, %.2f".format(Locale.US, latitude, longitude)
                            if (continuation.isActive) continuation.resume(fallback)
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                formatAddress(addresses?.firstOrNull(), latitude, longitude)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Geocoding failed for ($latitude, $longitude)", e)
            "Coordinates: %.2f, %.2f".format(Locale.US, latitude, longitude)
        }
    }

    private fun formatAddress(address: android.location.Address?, lat: Double, lng: Double): String {
        if (address == null) return "Coordinates: %.2f, %.2f".format(Locale.US, lat, lng)
        val city = address.locality ?: address.subAdminArea ?: address.adminArea
        val country = address.countryName
        return when {
            city != null && country != null -> "$city, $country"
            city != null -> city
            country != null -> country
            else -> "Coordinates: %.2f, %.2f".format(Locale.US, lat, lng)
        }
    }
}
