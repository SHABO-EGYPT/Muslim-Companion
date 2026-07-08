package com.example.viewmodel

import com.example.data.local.AppSettingEntity
import com.example.data.local.CachedAyahEntity
import com.example.data.local.UserProgressEntity
import com.example.domain.model.Surah
import com.example.fake.FakeCompanionRepository
import com.example.fake.FakeQuranRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@OptIn(ExperimentalCoroutinesApi::class)
class SurahReaderViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeQuranRepo: FakeQuranRepository
    private lateinit var fakeRepo: FakeCompanionRepository
    private lateinit var viewModel: SurahReaderViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeQuranRepo = FakeQuranRepository()
        fakeRepo = FakeCompanionRepository()
        viewModel = SurahReaderViewModel(
            repository = fakeRepo,
            context = androidx.test.core.app.ApplicationProvider.getApplicationContext()
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `currentSurah is null initially`() {
        assertNull(viewModel.currentSurah.value)
    }

    @Test
    fun `setSurah updates currentSurah`() {
        val surah = Surah(1, "Al-Fatiha", "The Opening", 7, "الفاتحة", true)
        viewModel.setSurah(surah)
        assertEquals(surah, viewModel.currentSurah.value)
    }

    @Test
    fun `quranReciter defaults to alafasy`() {
        assertEquals("7", viewModel.quranSettings.value.quranReciter)
    }

    @Test
    fun `updateQuranReciter updates settings`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.quranSettings.collect {}
        }
        viewModel.updateQuranReciter("6")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("6", viewModel.quranSettings.value.quranReciter)
        collectJob.cancel()
    }

    @Test
    fun `recitersList contains 4 reciters`() {
        assertEquals(4, viewModel.recitersList.size)
        assertTrue(viewModel.recitersList.any { it.id == "ar.alafasy" })
        assertTrue(viewModel.recitersList.any { it.id == "ar.husary" })
    }

    @Test
    fun `isPlaying is false initially`() {
        assertFalse(viewModel.isPlaying.value)
    }

    @Test
    fun `toggleBookmark adds bookmark when not bookmarked`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.bookmarks.collect {}
        }
        val surah = Surah(36, "Ya-Sin", "Ya Sin", 83, "يس", true)

        viewModel.toggleBookmark(surah)

        testDispatcher.scheduler.advanceUntilIdle()

        val bookmarks = viewModel.bookmarks.value
        assertTrue(bookmarks.any { it.surahNumber == 36 })
        collectJob.cancel()
    }

    @Test
    fun `toggleBookmark removes bookmark when already bookmarked`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.bookmarks.collect {}
        }
        val surah = Surah(36, "Ya-Sin", "Ya Sin", 83, "يس", true)
        fakeRepo.quranRepository.toggleBookmark(36, "Ya-Sin")

        viewModel.toggleBookmark(surah)

        testDispatcher.scheduler.advanceUntilIdle()

        val bookmarks = viewModel.bookmarks.value
        assertFalse(bookmarks.any { it.surahNumber == 36 })
        collectJob.cancel()
    }

    @Test
    fun `updateProgress saves to repository`() = runTest {
        val surah = Surah(18, "Al-Kahf", "The Cave", 110, "الكهف", true)

        viewModel.updateProgress(surah.number, surah.name, surah.arabicName, 25, 0f)

        testDispatcher.scheduler.advanceUntilIdle()

        val progress = fakeRepo.getUserProgressDirect()
        assertEquals(18, progress?.lastReadSurahNumber)
        assertEquals(25, progress?.lastReadAyahNumber)
    }

    @Test
    fun `quranSettings reflects repository settings`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.quranSettings.collect {}
        }
        fakeRepo.saveSettings(
            AppSettingEntity(
                quranTextSize = 30f,
                quranKeepScreenOn = true
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val settings = viewModel.quranSettings.value
        assertEquals(30f, settings.quranTextSize, 0.01f)
        assertTrue(settings.quranKeepScreenOn)
        collectJob.cancel()
    }

    @Test
    fun `readerLoadState is Idle initially`() {
        assertTrue(viewModel.readerLoadState.value is ReaderLoadState.Idle)
    }
}
