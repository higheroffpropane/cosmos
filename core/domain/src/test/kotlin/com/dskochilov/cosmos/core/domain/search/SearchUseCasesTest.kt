package com.dskochilov.cosmos.core.domain.search

import androidx.paging.PagingData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class SearchUseCasesTest {

    private val repository = mockk<MediaSearchRepository>()

    @Test
    fun `search delegates to the repository`() {
        val flow: Flow<PagingData<MediaItem>> = flowOf(PagingData.empty())
        every { repository.search("orion") } returns flow

        assertSame(flow, SearchMediaUseCase(repository).invoke("orion"))
        verify(exactly = 1) { repository.search("orion") }
    }

    @Test
    fun `save trims the query and skips blank input`() = runTest {
        coEvery { repository.saveSearch(any()) } just Runs
        val useCase = SaveSearchQueryUseCase(repository)

        useCase("  apollo 11  ")
        useCase("   ")

        coVerify(exactly = 1) { repository.saveSearch("apollo 11") }
        coVerify(exactly = 1) { repository.saveSearch(any()) }
    }

    @Test
    fun `observe history uses the default limit`() {
        every { repository.observeSearchHistory(10) } returns flowOf(listOf("apollo"))

        ObserveSearchHistoryUseCase(repository).invoke()

        verify(exactly = 1) { repository.observeSearchHistory(10) }
    }

    @Test
    fun `clear history delegates to the repository`() = runTest {
        coEvery { repository.clearSearchHistory() } just Runs

        ClearSearchHistoryUseCase(repository).invoke()

        coVerify(exactly = 1) { repository.clearSearchHistory() }
    }
}
