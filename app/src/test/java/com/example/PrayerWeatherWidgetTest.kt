package com.example

import com.example.notifications.PrayerWeatherWidgetProvider
import org.junit.Assert.assertNotNull
import org.junit.Test

class PrayerWeatherWidgetTest {

    @Test
    fun testPrayerWeatherWidgetProviderInstantiation() {
        val provider = PrayerWeatherWidgetProvider()
        assertNotNull("PrayerWeatherWidgetProvider instance should not be null", provider)
    }
}
