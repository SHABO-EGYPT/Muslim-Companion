package com.example.viewmodel

import com.example.data.local.UserProgressEntity
import com.example.domain.model.AzkarCategory
import com.example.domain.model.DhikrItem
import com.example.fake.FakeAzkarRepository
import com.example.fake.FakeCompanionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AzkarViewModelTest {

    private lateinit var fakeAzkarRepo: FakeAzkarRepository
    private lateinit var fakeRepo: FakeCompanionRepository
    private lateinit var viewModel: AzkarViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        fakeRepo = FakeCompanionRepository()
        fakeAzkarRepo = fakeRepo.azkarRepository
        viewModel = AzkarViewModel(
            repository = fakeRepo,
            azkarRepository = fakeAzkarRepo
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `azkarCategories initially empty`() {
        val categories = viewModel.azkarCategories.value
        assertTrue(categories.isEmpty())
    }

    @Test
    fun `selectCategory updates selectedCategory`() {
        val cat = AzkarCategory("evening", "Evening Azkar", "أذكار المساء", 8, 3, "sunset", 0xFFA8F2DC)
        viewModel.selectCategory(cat)
        assertEquals(cat, viewModel.selectedCategory.value)
    }

    @Test
    fun `selectCategory resets flowIndex to 0`() {
        for (i in 0 until 3) viewModel.nextStep()
        assertEquals(3, viewModel.flowIndex.value)

        viewModel.selectCategory(AzkarCategory("morning", "", "", 5, 2, "sunrise", 0L))
        assertEquals(0, viewModel.flowIndex.value)
    }

    @Test
    fun `nextStep increments flowIndex`() {
        viewModel.nextStep()
        assertEquals(1, viewModel.flowIndex.value)
        viewModel.nextStep()
        assertEquals(2, viewModel.flowIndex.value)
    }

    @Test
    fun `prevStep decrements flowIndex`() {
        viewModel.nextStep()
        viewModel.nextStep()
        assertEquals(2, viewModel.flowIndex.value)
        viewModel.prevStep()
        assertEquals(1, viewModel.flowIndex.value)
    }

    @Test
    fun `prevStep does not go below 0`() {
        viewModel.prevStep()
        assertEquals(0, viewModel.flowIndex.value)
    }

    @Test
    fun `searchAzkar returns results for matching category`() = runTest {
        fakeAzkarRepo.setDhikrItems("أذكار الصباح", listOf(
            DhikrItem(1, "سُبْحَانَ اللَّهِ", "Glory be to Allah", "SubhanAllah", 33)
        ))

        viewModel.searchAzkar("Allah")
        advanceUntilIdle()

        assertFalse(viewModel.assistantLoading.value)
        assertTrue(viewModel.assistantAzkar.value.isNotEmpty())
    }

    @Test
    fun `searchAzkar shows error for no match`() = runTest {
        viewModel.searchAzkar("nonexistent_category_xyz")
        advanceUntilIdle()

        assertFalse(viewModel.assistantLoading.value)
        assertTrue(viewModel.assistantAzkar.value.isEmpty())
    }

    @Test
    fun `completeAzkarFlow updates morningDone`() = runTest {
        fakeRepo.setProgress(UserProgressEntity(morningDone = 0))

        viewModel.completeAzkarFlow("أذكار الصباح", 15)
        advanceUntilIdle()

        val progress = fakeRepo.getUserProgressDirect()
        assertEquals(15, progress?.morningDone)
    }
}
