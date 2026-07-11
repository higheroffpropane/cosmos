package com.dskochilov.cosmos.core.domain.search

import androidx.paging.PagingData
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SearchMediaUseCase @Inject constructor(private val repository: MediaSearchRepository) {
    operator fun invoke(query: String): Flow<PagingData<MediaItem>> = repository.search(query)
}
