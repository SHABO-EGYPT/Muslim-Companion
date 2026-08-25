package com.example.data.prayer

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.TimeZone
import kotlin.math.*

/**
 * High-accuracy astronomical offline prayer times calculator.
 * Computes Fajr, Sunrise, Dhuhr, Asr, Maghrib, and Isha without internet access
 * using solar positioning equations and recognized Islamic calculation authorities.
 */
object OfflinePrayerCalculator {

    data class PrayerCalculationParams(
        val fajrAngle: Double,
        val ishaAngle: Double? = null,
        val ishaIntervalMinutes: Int? = null, // e.g. 90 minutes after Maghrib for Umm Al-Qura
        val asrShadowMultiplier: Double = 1.0 // 1.0 = Standard (Shafi'i/Maliki/Hanbali), 2.0 = Hanafi
    )

    private val METHOD_PARAMS: Map<String, PrayerCalculationParams> = mapOf(
        "Egyptian General Authority" to PrayerCalculationParams(fajrAngle = 19.5, ishaAngle = 17.5),
        "Umm Al-Qura University, Makkah" to PrayerCalculationParams(fajrAngle = 18.5, ishaIntervalMinutes = 90),
        "Muslim World League" to PrayerCalculationParams(fajrAngle = 18.0, ishaAngle = 17.0),
        "Islamic Society of North America (ISNA)" to PrayerCalculationParams(fajrAngle = 15.0, ishaAngle = 15.0),
        "University of Islamic Sciences, Karachi" to PrayerCalculationParams(fajrAngle = 18.0, ishaAngle = 18.0),
        "Kuwait" to PrayerCalculationParams(fajrAngle = 18.0, ishaAngle = 17.5),
        "Qatar" to PrayerCalculationParams(fajrAngle = 18.0, ishaIntervalMinutes = 90),
        "Majlis Ugama Islam Singapura, Singapore" to PrayerCalculationParams(fajrAngle = 20.0, ishaAngle = 18.0),
        "Union Organization Islamic de France" to PrayerCalculationParams(fajrAngle = 12.0, ishaAngle = 12.0),
        "Diyanet İşleri Başkanlığı, Turkey" to PrayerCalculationParams(fajrAngle = 18.0, ishaAngle = 17.0),
        "Spiritual Administration of Muslims of Russia" to PrayerCalculationParams(fajrAngle = 16.0, ishaAngle = 15.0),
        "Gulf Region" to PrayerCalculationParams(fajrAngle = 19.5, ishaIntervalMinutes = 90),
        "Institute of Geophysics, University of Tehran" to PrayerCalculationParams(fajrAngle = 17.7, ishaAngle = 14.0),
        "Moonsighting Committee Worldwide" to PrayerCalculationParams(fajrAngle = 18.0, ishaAngle = 18.0)
    )

    /**
     * Calculates prayer times for a given date, coordinates, and calculation method.
     * @return Map of prayer name to HH:mm string (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha)
     */
    fun calculate(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        methodName: String = "Egyptian General Authority",
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Map<String, String> {
        val params = METHOD_PARAMS[methodName] ?: METHOD_PARAMS["Egyptian General Authority"]!!
        val zoneOffsetHours = zoneId.rules.getOffset(date.atStartOfDay()).totalSeconds / 3600.0

        val julianDay = toJulianDay(date.year, date.monthValue, date.dayOfMonth) - (longitude / (15.0 * 24.0))
        val (declination, equationOfTime) = solarPosition(julianDay)

        // Solar noon / transit in local standard time hours (0.0 .. 24.0)
        val noon = 12.0 + zoneOffsetHours - (longitude / 15.0) - (equationOfTime / 60.0)

        // Sunrise & Sunset (center of sun is 50 arcminutes = 0.8333 degrees below horizon)
        val sunAlt = -0.8333
        val sunHourAngle = hourAngle(latitude, declination, sunAlt)

        val sunrise = if (sunHourAngle != null) noon - (sunHourAngle / 15.0) else noon - 6.0
        val sunset = if (sunHourAngle != null) noon + (sunHourAngle / 15.0) else noon + 6.0

        // Fajr
        val fajrAngle = -params.fajrAngle
        val fajrHourAngle = hourAngle(latitude, declination, fajrAngle)
        val fajr = if (fajrHourAngle != null) noon - (fajrHourAngle / 15.0) else sunrise - 1.5

        // Asr: shadow length = object + noon shadow
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val noonShadow = abs(tan(latRad - decRad))
        val asrAltRad = atan(1.0 / (params.asrShadowMultiplier + noonShadow))
        val asrAlt = Math.toDegrees(asrAltRad)
        val asrHourAngle = hourAngle(latitude, declination, asrAlt)
        val asr = if (asrHourAngle != null) noon + (asrHourAngle / 15.0) else noon + 3.25

        // Maghrib = Sunset + 1 minute (for safety / ihtiyat)
        val maghrib = sunset + (1.0 / 60.0)

        // Isha
        val isha = if (params.ishaIntervalMinutes != null) {
            maghrib + (params.ishaIntervalMinutes / 60.0)
        } else {
            val ishaAngle = -params.ishaAngle!!
            val ishaHourAngle = hourAngle(latitude, declination, ishaAngle)
            if (ishaHourAngle != null) noon + (ishaHourAngle / 15.0) else sunset + 1.5
        }

        // Add 1 minute buffer to Dhuhr for zawal
        val dhuhr = noon + (1.0 / 60.0)

        return mapOf(
            "Fajr" to formatHours(fajr),
            "Sunrise" to formatHours(sunrise),
            "Dhuhr" to formatHours(dhuhr),
            "Asr" to formatHours(asr),
            "Maghrib" to formatHours(maghrib),
            "Isha" to formatHours(isha)
        )
    }

    private fun hourAngle(lat: Double, dec: Double, alt: Double): Double? {
        val latRad = Math.toRadians(lat)
        val decRad = Math.toRadians(dec)
        val altRad = Math.toRadians(alt)
        val cosH = (sin(altRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        return if (cosH in -1.0..1.0) Math.toDegrees(acos(cosH)) else null
    }

    private fun solarPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d

        val raRad = atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))
        val ra = fixAngle(Math.toDegrees(raRad)) / 15.0

        val decRad = asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l)))
        val declination = Math.toDegrees(decRad)

        var eqT = (q / 15.0) - ra
        if (eqT > 12.0) eqT -= 24.0
        if (eqT < -12.0) eqT += 24.0
        val eqTMinutes = eqT * 60.0

        return Pair(declination, eqTMinutes)
    }

    private fun toJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun formatHours(hours: Double): String {
        var h = hours % 24.0
        if (h < 0) h += 24.0
        val totalMinutes = (h * 60.0).roundToInt()
        val formattedHour = (totalMinutes / 60) % 24
        val formattedMinute = totalMinutes % 60
        return "%02d:%02d".format(java.util.Locale.US, formattedHour, formattedMinute)
    }
}
