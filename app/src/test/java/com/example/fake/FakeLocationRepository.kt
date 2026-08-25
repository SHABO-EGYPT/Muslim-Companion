package com.example.fake

import com.example.data.location.AppLocation
import com.example.data.location.LocationRepository

class FakeLocationRepository(
    var currentLocation: AppLocation? = AppLocation(30.0444, 31.2357, "Cairo, Egypt")
) : LocationRepository {
    override suspend fun getCurrentLocation(): AppLocation? = currentLocation
    override suspend fun reverseGeocode(latitude: Double, longitude: Double): String =
        currentLocation?.locationName ?: "Coordinates: %.2f, %.2f".format(java.util.Locale.US, latitude, longitude)
}
