package com.example.viewmodel

import com.example.domain.model.ChainDhikrItem
import com.example.domain.model.CustomDhikrChain
import com.example.fake.FakeAzkarRepository
import com.example.fake.FakeCustomDhikrRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomDhikrViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeCustomRepo: FakeCustomDhikrRepository
    private lateinit var fakeAzkarRepo: FakeAzkarRepository
    private lateinit var viewModel: CustomDhikrViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCustomRepo = FakeCustomDhikrRepository()
        fakeAzkarRepo = FakeAzkarRepository()
        viewModel = CustomDhikrViewModel(fakeCustomRepo, fakeAzkarRepo)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `chains seeds default template on initialization`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.chains.collect {}
        }

        val list = viewModel.chains.value
        assertTrue(list.isNotEmpty())
        assertEquals("Daily Core", list[0].title)

        collectJob.cancel()
    }

    @Test
    fun `saveChain creates a new chain successfully`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.chains.collect {}
        }

        viewModel.saveChain(
            title = "Night Chain",
            description = "Nightly dhikr",
            items = listOf(
                ChainDhikrItem(1, "أَسْتَغْفِرُ اللَّهَ", 100)
            )
        )

        val found = viewModel.chains.value.find { it.title == "Night Chain" }
        assertNotNull(found)
        assertEquals(100, found!!.totalCount)

        collectJob.cancel()
    }

    @Test
    fun `deleteChain removes chain from repository`() = runTest {
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.chains.collect {}
        }

        val initialSize = viewModel.chains.value.size
        val firstId = viewModel.chains.value.first().id

        viewModel.deleteChain(firstId)

        assertEquals(initialSize - 1, viewModel.chains.value.size)
        assertNull(viewModel.chains.value.find { it.id == firstId })

        collectJob.cancel()
    }

    @Test
    fun `recitation flow advances steps and marks complete`() = runTest {
        val testChain = CustomDhikrChain(
            id = 99,
            title = "Short Test Chain",
            items = listOf(
                ChainDhikrItem(1, "سُبْحَانَ اللَّهِ", 2),
                ChainDhikrItem(2, "الْحَمْدُ لِلَّهِ", 1)
            ),
            totalCount = 3
        )
        fakeCustomRepo.saveChain(testChain)

        viewModel.startRecitation(99)

        assertEquals(0, viewModel.currentStepIndex.value)
        assertEquals(0, viewModel.currentStepCount.value)
        assertFalse(viewModel.isChainCompleted.value)

        // Step 1: count 1
        viewModel.increment()
        assertEquals(0, viewModel.currentStepIndex.value)
        assertEquals(1, viewModel.currentStepCount.value)

        // Step 1: count 2 -> advances to Step 2
        viewModel.increment()
        assertEquals(1, viewModel.currentStepIndex.value)
        assertEquals(0, viewModel.currentStepCount.value)

        // Step 2: count 1 -> finishes chain
        viewModel.increment()
        assertTrue(viewModel.isChainCompleted.value)
    }
}
