package com.dskochilov.cosmos.core.data.search

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.dskochilov.cosmos.core.database.CosmosDatabase
import com.dskochilov.cosmos.core.database.entity.SearchHistoryEntity
import com.dskochilov.cosmos.core.domain.search.MediaItem
import com.dskochilov.cosmos.core.domain.search.MediaSearchRepository
import com.dskochilov.cosmos.core.network.images.NasaImagesApi
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class MediaSearchRepositoryImpl @Inject constructor(
    private val api: NasaImagesApi,
    private val database: CosmosDatabase
) : MediaSearchRepository {

    override fun search(query: String): Flow<PagingData<MediaItem>> = Pager(
        config = PagingConfig(
            pageSize = MEDIA_SEARCH_PAGE_SIZE,
            initialLoadSize = MEDIA_SEARCH_PAGE_SIZE,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { MediaSearchPagingSource(api = api, query = query) }
    ).flow

    override fun observeSearchHistory(limit: Int): Flow<List<String>> = database.searchHistoryDao().observeRecent(limit)

    override suspend fun saveSearch(query: String) {
        database.searchHistoryDao().upsert(
            SearchHistoryEntity(query = query, updatedAt = System.currentTimeMillis())
        )
    }

    override suspend fun clearSearchHistory() {
        database.searchHistoryDao().clear()
    }
}
