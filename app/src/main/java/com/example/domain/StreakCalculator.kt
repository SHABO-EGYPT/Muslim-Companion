package com.example.domain

/**
 * Pure domain logic for tracking user streak based on azkar and prayer completion.
 * No Android dependencies — fully unit-testable.
 */
object StreakCalculator {

    /**
     * Calculates the new streak value based on completion of morning+evening azkar.
     *
     * @param currentStreak the current streak count
     * @param lastStreakDate the date the streak was last updated (ISO format, nullable)
     * @param morningDone count of morning azkar completed today
     * @param eveningDone count of evening azkar completed today
     * @param todayStr today's date as ISO string (e.g. "2026-07-04")
     * @return Pair of (newStreak, newLastStreakDate)
     */
    fun calculateAzkarStreak(
        currentStreak: Int,
        lastStreakDate: String?,
        morningDone: Int,
        eveningDone: Int,
        todayStr: String
    ): Pair<Int, String> {
        // Both morning and evening must be done
        if (morningDone <= 0 || eveningDone <= 0) {
            return currentStreak to (lastStreakDate ?: todayStr)
        }

        if (lastStreakDate.isNullOrEmpty()) {
            return 1 to todayStr
        }

        return try {
            val lastDate = java.time.LocalDate.parse(lastStreakDate)
            val today = java.time.LocalDate.parse(todayStr)

            when {
                lastDate.plusDays(1) == today -> (currentStreak + 1) to todayStr
                lastDate == today -> currentStreak to todayStr
                else -> 1 to todayStr  // Streak broken
            }
        } catch (e: Exception) {
            // If date parsing fails, start fresh
            1 to todayStr
        }
    }

    /**
     * Calculates streak for prayer completion.
     * Streak advances when all 5 prayers are completed.
     */
    fun calculatePrayerStreak(
        currentStreak: Int,
        lastStreakDate: String?,
        allPrayersCompleted: Boolean,
        todayStr: String
    ): Pair<Int, String> {
        if (!allPrayersCompleted) {
            return currentStreak to (lastStreakDate ?: todayStr)
        }

        if (lastStreakDate.isNullOrEmpty()) {
            return 1 to todayStr
        }

        return try {
            val lastDate = java.time.LocalDate.parse(lastStreakDate)
            val today = java.time.LocalDate.parse(todayStr)

            when {
                lastDate == today -> currentStreak to todayStr  // Already counted today
                lastDate.plusDays(1) >= today -> (currentStreak + 1) to todayStr
                else -> 1 to todayStr
            }
        } catch (e: Exception) {
            1 to todayStr
        }
    }
}
