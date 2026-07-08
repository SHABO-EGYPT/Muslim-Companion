package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StreakCalculatorTest {

    // --- Azkar Streak Tests ---

    @Test
    fun `azkarStreak_returns1_when_noPreviousStreak_and_bothDone`() {
        val (streak, date) = StreakCalculator.calculateAzkarStreak(
            currentStreak = 0,
            lastStreakDate = null,
            morningDone = 10,
            eveningDone = 8,
            todayStr = "2026-07-04"
        )
        assertEquals(1, streak)
        assertEquals("2026-07-04", date)
    }

    @Test
    fun `azkarStreak_returns0AndEmpty_when_noPreviousStreak_and_noneDone`() {
        val (streak, date) = StreakCalculator.calculateAzkarStreak(
            currentStreak = 0,
            lastStreakDate = null,
            morningDone = 0,
            eveningDone = 0,
            todayStr = "2026-07-04"
        )
        assertEquals(0, streak)
        assertEquals("2026-07-04", date)
    }

    @Test
    fun `azkarStreak_increments_when_consecutiveDay()`() {
        val (streak, date) = StreakCalculator.calculateAzkarStreak(
            currentStreak = 5,
            lastStreakDate = "2026-07-03",
            morningDone = 15,
            eveningDone = 12,
            todayStr = "2026-07-04"
        )
        assertEquals(6, streak)
        assertEquals("2026-07-04", date)
    }

    @Test
    fun `azkarStreak_preserves_when_sameDay()`() {
        val (streak, date) = StreakCalculator.calculateAzkarStreak(
            currentStreak = 5,
            lastStreakDate = "2026-07-04",
            morningDone = 15,
            eveningDone = 12,
            todayStr = "2026-07-04"
        )
        assertEquals(5, streak)
        assertEquals("2026-07-04", date)
    }

    @Test
    fun `azkarStreak_resets_when_daySkipped()`() {
        val (streak, date) = StreakCalculator.calculateAzkarStreak(
            currentStreak = 5,
            lastStreakDate = "2026-07-01",
            morningDone = 15,
            eveningDone = 12,
            todayStr = "2026-07-04"
        )
        assertEquals(1, streak)
        assertEquals("2026-07-04", date)
    }

    @Test
    fun `azkarStreak_doesNotChange_when_morningNotDone()`() {
        val (streak, date) = StreakCalculator.calculateAzkarStreak(
            currentStreak = 5,
            lastStreakDate = "2026-07-03",
            morningDone = 0,
            eveningDone = 12,
            todayStr = "2026-07-04"
        )
        assertEquals(5, streak)
        assertEquals("2026-07-03", date)
    }

    @Test
    fun `azkarStreak_doesNotChange_when_eveningNotDone()`() {
        val (streak, date) = StreakCalculator.calculateAzkarStreak(
            currentStreak = 5,
            lastStreakDate = "2026-07-03",
            morningDone = 10,
            eveningDone = 0,
            todayStr = "2026-07-04"
        )
        assertEquals(5, streak)
        assertEquals("2026-07-03", date)
    }

    // --- Prayer Streak Tests ---

    @Test
    fun `prayerStreak_increments_when_allDone_and_yesterday()`() {
        val (streak, date) = StreakCalculator.calculatePrayerStreak(
            currentStreak = 3,
            lastStreakDate = "2026-07-03",
            allPrayersCompleted = true,
            todayStr = "2026-07-04"
        )
        assertEquals(4, streak)
        assertEquals("2026-07-04", date)
    }

    @Test
    fun `prayerStreak_preserves_when_allDone_and_sameDay()`() {
        val (streak, date) = StreakCalculator.calculatePrayerStreak(
            currentStreak = 3,
            lastStreakDate = "2026-07-04",
            allPrayersCompleted = true,
            todayStr = "2026-07-04"
        )
        assertEquals(3, streak)
        assertEquals("2026-07-04", date)
    }

    @Test
    fun `prayerStreak_doesNotChange_when_notAllDone()`() {
        val (streak, date) = StreakCalculator.calculatePrayerStreak(
            currentStreak = 3,
            lastStreakDate = "2026-07-03",
            allPrayersCompleted = false,
            todayStr = "2026-07-04"
        )
        assertEquals(3, streak)
        assertEquals("2026-07-03", date)
    }

    @Test
    fun `prayerStreak_resets_when_daySkipped()`() {
        val (streak, date) = StreakCalculator.calculatePrayerStreak(
            currentStreak = 3,
            lastStreakDate = "2026-06-30",
            allPrayersCompleted = true,
            todayStr = "2026-07-04"
        )
        assertEquals(1, streak)
        assertEquals("2026-07-04", date)
    }

    @Test
    fun `prayerStreak_handles_invalidDate_gracefully()`() {
        val (streak, date) = StreakCalculator.calculatePrayerStreak(
            currentStreak = 3,
            lastStreakDate = "not-a-date",
            allPrayersCompleted = true,
            todayStr = "2026-07-04"
        )
        assertEquals(1, streak)
        assertEquals("2026-07-04", date)
    }
}
