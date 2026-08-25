package com.example.data.location

import com.example.fake.FakeLocationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LocationRepositoryTest {

    @Test
    fun getCurrentLocation_returnsExpectedLocation() = runTest {
        val fakeRepo = FakeLocationRepository(
            currentLocation = AppLocation(21.3891, 39.8579, "Makkah, Saudi Arabia")
        )

        val loc = fakeRepo.getCurrentLocation()
        assertNotNull(loc)
        assertEquals(21.3891, loc!!.latitude, 0.0001)
        assertEquals(39.8579, loc.longitude, 0.0001)
        assertEquals("Makkah, Saudi Arabia", loc.locationName)
    }

    @Test
    fun reverseGeocode_returnsFormattedName() = runTest {
        val fakeRepo = FakeLocationRepository(
            currentLocation = AppLocation(30.0444, 31.2357, "Cairo, Egypt")
        )

        val name = fakeRepo.reverseGeocode(30.0444, 31.2357)
        assertEquals("Cairo, Egypt", name)
    }
}
