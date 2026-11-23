package com.dskochilov.cosmos.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.dskochilov.cosmos.core.domain.search.ClearSearchHistoryUseCase
import com.dskochilov.cosmos.core.domain.search.MediaSearchRepository
import com.dskochilov.cosmos.core.domain.search.ObserveSearchHistoryUseCase
import com.dskochilov.cosmos.core.domain.search.SaveSearchQueryUseCase
import com.dskochilov.cosmos.core.domain.search.SearchMediaUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val repository = mockk<MediaSearchRepository>()
    private val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { repository.search(any()) } returns flowOf(PagingData.empty())
        every { repository.observeSearchHistory(any()) } returns flowOf(emptyList())
        coEvery { repository.saveSearch(any()) } just Runs
        coEvery { repository.clearSearchHistory() } just Runs
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `query changes update SavedStateHandle immediately`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = viewModel(savedStateHandle)

        viewModel.onIntent(SearchIntent.QueryChanged("nebula"))

        assertEquals("nebula", savedStateHandle.get<String>(SearchViewModel.KEY_QUERY))
        assertEquals("nebula", viewModel.query.value)
    }

    @Test
    fun `search fires only after the debounce window`() = runTest {
        val viewModel = viewModel()
        val collector = launch { viewModel.results.collect { } }
        runCurrent()

        viewModel.onIntent(SearchIntent.QueryChanged("apollo"))
        advanceTimeBy(SEARCH_DEBOUNCE_MS - 50)
        runCurrent()
        verify(exactly = 0) { repository.search(any()) }

        advanceTimeBy(51)
        runCurrent()
        verify(exactly = 1) { repository.search("apollo") }
        coVerify(exactly = 1) { repository.saveSearch("apollo") }

        collector.cancel()
    }

    @Test
    fun `rapid typing produces a single search for the final query`() = runTest {
        val viewModel = viewModel()
        val collector = launch { viewModel.results.collect { } }
        runCurrent()

        listOf("a", "ap", "apo", "apol", "apollo").forEach { partial ->
            viewModel.onIntent(SearchIntent.QueryChanged(partial))
            advanceTimeBy(100)
        }
        advanceTimeBy(SEARCH_DEBOUNCE_MS + 1)
        runCurrent()

        verify(exactly = 1) { repository.search(any()) }
        verify(exactly = 1) { repository.search("apollo") }

        collector.cancel()
    }

    @Test
    fun `queries shorter than the minimum never search`() = runTest {
        val viewModel = viewModel()
        val collector = launch { viewModel.results.collect { } }
        runCurrent()

        viewModel.onIntent(SearchIntent.QueryChanged("a"))
        advanceTimeBy(SEARCH_DEBOUNCE_MS * 2)
        runCurrent()

        verify(exactly = 0) { repository.search(any()) }
        coVerify(exactly = 0) { repository.saveSearch(any()) }

        collector.cancel()
    }

    @Test
    fun `history selection sets the query and clear intents work`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(SearchViewModel.KEY_QUERY to "old"))
        val viewModel = viewModel(savedStateHandle)

        viewModel.onIntent(SearchIntent.HistorySelected("hubble"))
        assertEquals("hubble", viewModel.query.value)

        viewModel.onIntent(SearchIntent.ClearQuery)
        assertEquals("", viewModel.query.value)

        viewModel.onIntent(SearchIntent.ClearHistory)
        runCurrent()
        coVerify(exactly = 1) { repository.clearSearchHistory() }
    }

    @Test
    fun `clearing a query allows searching for the same term from history again`() = runTest {
        val viewModel = viewModel()
        val collector = launch { viewModel.results.collect { } }
        runCurrent()

        viewModel.onIntent(SearchIntent.QueryChanged("apollo"))
        advanceTimeBy(SEARCH_DEBOUNCE_MS + 1)
        viewModel.onIntent(SearchIntent.ClearQuery)
        runCurrent()
        viewModel.onIntent(SearchIntent.HistorySelected("apollo"))
        advanceTimeBy(SEARCH_DEBOUNCE_MS + 1)

        verify(exactly = 2) { repository.search("apollo") }
        coVerify(exactly = 0) { repository.saveSearch("") }
        collector.cancel()
    }

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = SearchViewModel(
        searchMedia = SearchMediaUseCase(repository),
        observeSearchHistory = ObserveSearchHistoryUseCase(repository),
        saveSearchQuery = SaveSearchQueryUseCase(repository),
        clearSearchHistory = ClearSearchHistoryUseCase(repository),
        savedStateHandle = savedStateHandle
    )
}
