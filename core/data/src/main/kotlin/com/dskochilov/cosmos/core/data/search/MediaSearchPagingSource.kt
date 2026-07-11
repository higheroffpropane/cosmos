package com.dskochilov.cosmos.core.data.search

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.dskochilov.cosmos.core.common.result.AppErrorException
import com.dskochilov.cosmos.core.data.toAppError
import com.dskochilov.cosmos.core.domain.search.MediaItem
import com.dskochilov.cosmos.core.network.images.NasaImagesApi
import kotlin.coroutines.cancellation.CancellationException

internal const val MEDIA_SEARCH_PAGE_SIZE = 25

/**
 * Plain network paging over the image-library search (no local cache; results
 * are ephemeral per query). Keys are 1-based page numbers.
 */
internal class MediaSearchPagingSource(private val api: NasaImagesApi, private val query: String) :
    PagingSource<Int, MediaItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaItem> = try {
        val page = params.key ?: FIRST_PAGE
        val response = api.search(query = query, page = page, pageSize = MEDIA_SEARCH_PAGE_SIZE)
        val items = response.collection.items.mapNotNull { it.toDomain() }
        val totalHits = response.collection.metadata?.totalHits ?: 0
        val isLastPage = response.collection.items.isEmpty() || page * MEDIA_SEARCH_PAGE_SIZE >= totalHits

        LoadResult.Page(
            data = items,
            prevKey = if (page == FIRST_PAGE) null else page - 1,
            nextKey = if (isLastPage) null else page + 1
        )
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        LoadResult.Error(AppErrorException(e.toAppError()))
    }

    override fun getRefreshKey(state: PagingState<Int, MediaItem>): Int? = state.anchorPosition?.let { anchorPosition ->
        val closest = state.closestPageToPosition(anchorPosition)
        closest?.prevKey?.plus(1) ?: closest?.nextKey?.minus(1)
    }

    private companion object {
        const val FIRST_PAGE = 1
    }
}
