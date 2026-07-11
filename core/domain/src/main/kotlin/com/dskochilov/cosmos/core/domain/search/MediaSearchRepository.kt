package com.dskochilov.cosmos.core.domain.search

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface MediaSearchRepository {
    /** Paged search over NASA's image library. [query] must already be validated/trimmed. */
    fun search(query: String): Flow<PagingData<MediaItem>>

    /** Most recent search queries, newest first. */
    fun observeSearchHistory(limit: Int): Flow<List<String>>

    /** Records [query] as the most recent search (deduplicated). */
    suspend fun saveSearch(query: String)

    suspend fun clearSearchHistory()
}
