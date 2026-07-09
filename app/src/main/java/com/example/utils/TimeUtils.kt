package com.example.utils

import java.time.LocalTime

object TimeUtils {
    /**
     * Parses a prayer time string (e.g. "16:04 (EET)") into a LocalTime.
     * Returns null if parsing fails.
     */
    fun parsePrayerTime(timeString: String): LocalTime? {
        return try {
            val cleanTime = timeString.split(" ")[0]
            val parts = cleanTime.split(":")
            if (parts.size >= 2) {
                val hour = parts[0].trim().toIntOrNull() ?: return null
                val minute = parts[1].trim().toIntOrNull() ?: return null
                LocalTime.of(hour, minute)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
