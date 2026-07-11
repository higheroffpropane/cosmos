package com.dskochilov.cosmos.core.domain.search

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

private const val DEFAULT_HISTORY_LIMIT = 10

class ObserveSearchHistoryUseCase @Inject constructor(private val repository: MediaSearchRepository) {
    operator fun invoke(limit: Int = DEFAULT_HISTORY_LIMIT): Flow<List<String>> = repository.observeSearchHistory(limit)
}

class SaveSearchQueryUseCase @Inject constructor(private val repository: MediaSearchRepository) {
    suspend operator fun invoke(query: String) {
        val normalized = query.trim()
        if (normalized.isNotEmpty()) {
            repository.saveSearch(normalized)
        }
    }
}

class ClearSearchHistoryUseCase @Inject constructor(private val repository: MediaSearchRepository) {
    suspend operator fun invoke() = repository.clearSearchHistory()
}
