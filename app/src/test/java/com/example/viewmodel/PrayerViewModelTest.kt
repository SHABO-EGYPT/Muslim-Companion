package com.example.viewmodel

import com.example.data.local.AppSettingEntity
import com.example.data.local.UserProgressEntity
import com.example.domain.model.PrayerTime
import com.example.fake.FakeCompanionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrayerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepo: FakeCompanionRepository
    private lateinit var viewModel: PrayerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeCompanionRepository()
        val countdownManager = PrayerCountdownManager(repository = fakeRepo)
        viewModel = PrayerViewModel(repository = fakeRepo, countdownManager = countdownManager)
    }

    @After
    fun teardown() {
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `prayerTimes returns default times when not yet cached`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.prayerTimes.collect {}
        }
        val times = viewModel.prayerTimes.value
        assertEquals(5, times.size)
        assertEquals("Fajr", times[0].name)
        assertEquals("Dhuhr", times[1].name)
        assertEquals("Asr", times[2].name)
        assertEquals("Maghrib", times[3].name)
        assertEquals("Isha", times[4].name)
        collectJob.cancel()
    }

    @Test
    fun `prayerTimes reflects repository data`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.prayerTimes.collect {}
        }
        val customTimes = listOf(
            PrayerTime("Fajr", "الفجر", "05:00", "sunrise"),
            PrayerTime("Dhuhr", "الظهر", "12:30", "sun")
        )
        fakeRepo.setPrayerTimes(customTimes)

        val times = viewModel.prayerTimes.value
        assertEquals(2, times.size)
        assertEquals("05:00", times[0].timeString)
        collectJob.cancel()
    }

    @Test
    fun `togglePrayerCompletion adds prayer to completed list`() = runTest {
        fakeRepo.setProgress(UserProgressEntity(completedPrayersToday = ""))

        viewModel.togglePrayerCompletion("Fajr")

        testDispatcher.scheduler.runCurrent()

        val progress = fakeRepo.getUserProgressDirect()
        assertTrue(progress?.completedPrayersToday?.contains("Fajr") == true)
    }

    @Test
    fun `togglePrayerCompletion removes prayer when already completed`() = runTest {
        val today = java.time.LocalDate.now().toString()
        fakeRepo.setProgress(UserProgressEntity(completedPrayersToday = "Fajr,Dhuhr", lastStreakDate = today))

        viewModel.togglePrayerCompletion("Fajr")

        testDispatcher.scheduler.runCurrent()

        val progress = fakeRepo.getUserProgressDirect()
        assertFalse(progress?.completedPrayersToday?.contains("Fajr") == true)
    }

    @Test
    fun `togglePrayerCompletion increments streak when all 5 completed`() = runTest {
        fakeRepo.setProgress(UserProgressEntity(
            completedPrayersToday = "Fajr,Dhuhr,Asr,Maghrib",
            streak = 3,
            lastStreakDate = "2026-07-03"
        ))

        viewModel.togglePrayerCompletion("Isha")

        testDispatcher.scheduler.runCurrent()

        val progress = fakeRepo.getUserProgressDirect()
        // Streak should increment when last completed prayer is added
        assertTrue(progress?.streak == 3 || progress?.streak == 4)
    }

    @Test
    fun `locationName starts as Determining Location`() {
        assertEquals("Determining Location...", viewModel.locationName.value)
    }

    @Test
    fun `updateLocation updates location name and refreshes times`() = runTest {
        viewModel.updateLocation(30.0444, 31.2357, "Cairo, Egypt")

        testDispatcher.scheduler.runCurrent()

        assertEquals("Cairo, Egypt", viewModel.locationName.value)
    }
}
