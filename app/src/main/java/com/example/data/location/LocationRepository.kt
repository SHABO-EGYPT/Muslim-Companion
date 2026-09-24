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
    suspend fun getCoordinatesForLocation(locationName: String): Pair<Double, Double>?
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

    override suspend fun getCoordinatesForLocation(locationName: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        if (locationName.isBlank()) return@withContext null

        // 1. Check if string is formatted coordinates "Coordinates: lat, lng" or "lat, lng"
        val coordRegex = Regex("""(?:Coordinates:\s*)?([+-]?\d+(?:\.\d+)?)\s*,\s*([+-]?\d+(?:\.\d+)?)""")
        val match = coordRegex.find(locationName)
        if (match != null) {
            val lat = match.groupValues[1].toDoubleOrNull()
            val lng = match.groupValues[2].toDoubleOrNull()
            if (lat != null && lng != null) {
                return@withContext Pair(lat, lng)
            }
        }

        // 2. Try Android Geocoder
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val list = geocoder.getFromLocationName(locationName, 1)
            if (!list.isNullOrEmpty()) {
                val address = list[0]
                return@withContext Pair(address.latitude, address.longitude)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Geocoding location name failed for '$locationName'", e)
        }

        // 3. Fallback map for common cities (English & Arabic)
        val normalized = locationName.trim().lowercase()
        return@withContext when {
            normalized.contains("cairo") || normalized.contains("قاهرة") || normalized.contains("مصر") || normalized.contains("egypt") -> Pair(30.0444, 31.2357)
            normalized.contains("alexandria") || normalized.contains("إسكندرية") || normalized.contains("اسكندرية") -> Pair(31.2001, 29.9187)
            normalized.contains("giza") || normalized.contains("جيزة") -> Pair(30.0131, 31.2089)
            normalized.contains("makkah") || normalized.contains("mecca") || normalized.contains("مكة") -> Pair(21.4225, 39.8262)
            normalized.contains("madinah") || normalized.contains("medina") || normalized.contains("مدينة") -> Pair(24.5247, 39.5692)
            normalized.contains("riyadh") || normalized.contains("رياض") -> Pair(24.7136, 46.6753)
            normalized.contains("jeddah") || normalized.contains("جدة") -> Pair(21.5433, 39.1728)
            normalized.contains("dubai") || normalized.contains("دبي") -> Pair(25.2048, 55.2708)
            normalized.contains("abu dhabi") || normalized.contains("أبوظبي") -> Pair(24.4539, 54.3773)
            normalized.contains("amman") || normalized.contains("عمان") -> Pair(31.9454, 35.9284)
            normalized.contains("damascus") || normalized.contains("دمشق") -> Pair(33.5138, 36.2765)
            normalized.contains("beirut") || normalized.contains("بيروت") -> Pair(33.8938, 35.5018)
            normalized.contains("baghdad") || normalized.contains("بغداد") -> Pair(33.3152, 44.3661)
            normalized.contains("kuwait") || normalized.contains("كويت") -> Pair(29.3759, 47.9774)
            normalized.contains("doha") || normalized.contains("دوحة") -> Pair(25.2854, 51.5310)
            normalized.contains("muscat") || normalized.contains("مسقط") -> Pair(23.5880, 58.3829)
            normalized.contains("manama") || normalized.contains("منامة") -> Pair(26.2285, 50.5860)
            normalized.contains("jerusalem") || normalized.contains("quds") || normalized.contains("قدس") -> Pair(31.7683, 35.2137)
            normalized.contains("istanbul") || normalized.contains("إسطنبول") -> Pair(41.0082, 28.9784)
            normalized.contains("london") || normalized.contains("لندن") -> Pair(51.5074, -0.1278)
            normalized.contains("new york") || normalized.contains("نيويورك") -> Pair(40.7128, -74.0060)
            normalized.contains("paris") || normalized.contains("باريس") -> Pair(48.8566, 2.3522)
            normalized.contains("berlin") || normalized.contains("برلين") -> Pair(52.5200, 13.4050)
            normalized.contains("jakarta") || normalized.contains("جاكرتا") -> Pair(-6.2088, 106.8456)
            normalized.contains("kuala lumpur") || normalized.contains("كوالالمبور") -> Pair(3.1390, 101.6869)
            else -> null
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
