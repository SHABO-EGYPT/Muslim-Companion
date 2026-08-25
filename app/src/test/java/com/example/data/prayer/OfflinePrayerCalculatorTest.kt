package com.example.data.prayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class OfflinePrayerCalculatorTest {

    @Test
    fun calculate_cairoCoordinates_returnsAllPrayers() {
        val date = LocalDate.of(2026, 8, 25)
        val cairoLat = 30.0444
        val cairoLng = 31.2357
        val cairoZone = ZoneId.of("Africa/Cairo")

        val times = OfflinePrayerCalculator.calculate(
            date = date,
            latitude = cairoLat,
            longitude = cairoLng,
            methodName = "Egyptian General Authority",
            zoneId = cairoZone
        )

        assertNotNull(times["Fajr"])
        assertNotNull(times["Sunrise"])
        assertNotNull(times["Dhuhr"])
        assertNotNull(times["Asr"])
        assertNotNull(times["Maghrib"])
        assertNotNull(times["Isha"])

        // In August in Cairo, Fajr is around 04:xx, Dhuhr around 12:xx or 13:xx (with DST), Maghrib around 19:xx
        assertTrue(times["Fajr"]!!.startsWith("03:") || times["Fajr"]!!.startsWith("04:") || times["Fajr"]!!.startsWith("05:"))
        assertTrue(times["Maghrib"]!!.startsWith("18:") || times["Maghrib"]!!.startsWith("19:") || times["Maghrib"]!!.startsWith("20:"))
    }

    @Test
    fun calculate_makkahCoordinates_ummAlQuraMethod_ishaIs90MinutesAfterMaghrib() {
        val date = LocalDate.of(2026, 5, 15)
        val makkahLat = 21.3891
        val makkahLng = 39.8579
        val makkahZone = ZoneId.of("Asia/Riyadh")

        val times = OfflinePrayerCalculator.calculate(
            date = date,
            latitude = makkahLat,
            longitude = makkahLng,
            methodName = "Umm Al-Qura University, Makkah",
            zoneId = makkahZone
        )

        val maghribStr = times["Maghrib"]!!
        val ishaStr = times["Isha"]!!

        val maghribMinutes = maghribStr.split(":")[0].toInt() * 60 + maghribStr.split(":")[1].toInt()
        val ishaMinutes = ishaStr.split(":")[0].toInt() * 60 + ishaStr.split(":")[1].toInt()

        // Umm Al Qura fixed difference is exactly 90 minutes (1h 30m)
        assertEquals(90, ishaMinutes - maghribMinutes)
    }

    @Test
    fun calculate_isnaMethod_returnsValidChronologicalTimes() {
        val date = LocalDate.of(2026, 10, 10)
        val nyLat = 40.7128
        val nyLng = -74.0060
        val nyZone = ZoneId.of("America/New_York")

        val times = OfflinePrayerCalculator.calculate(
            date = date,
            latitude = nyLat,
            longitude = nyLng,
            methodName = "Islamic Society of North America (ISNA)",
            zoneId = nyZone
        )

        val fajr = times["Fajr"]!!
        val sunrise = times["Sunrise"]!!
        val dhuhr = times["Dhuhr"]!!
        val asr = times["Asr"]!!
        val maghrib = times["Maghrib"]!!
        val isha = times["Isha"]!!

        assertTrue(fajr < sunrise)
        assertTrue(sunrise < dhuhr)
        assertTrue(dhuhr < asr)
        assertTrue(asr < maghrib)
        assertTrue(maghrib < isha)
    }
}
