package com.dskochilov.cosmos.core.data.search

import androidx.paging.PagingSource
import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.common.result.AppErrorException
import com.dskochilov.cosmos.core.network.images.NasaImagesApi
import com.dskochilov.cosmos.core.network.images.NasaImagesCollectionDto
import com.dskochilov.cosmos.core.network.images.NasaImagesItemDataDto
import com.dskochilov.cosmos.core.network.images.NasaImagesItemDto
import com.dskochilov.cosmos.core.network.images.NasaImagesLinkDto
import com.dskochilov.cosmos.core.network.images.NasaImagesMetadataDto
import com.dskochilov.cosmos.core.network.images.NasaImagesSearchResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MediaSearchPagingSourceTest {

    private val api = mockk<NasaImagesApi>()

    @Test
    fun `first page maps items and points to the next page`() = runTest {
        coEvery { api.search(query = "apollo", page = 1, pageSize = MEDIA_SEARCH_PAGE_SIZE) } returns
            response(items = (1..MEDIA_SEARCH_PAGE_SIZE).map { hit("id-$it") }, totalHits = 100)

        val result = MediaSearchPagingSource(api, "apollo").load(refreshParams())

        val page = result as PagingSource.LoadResult.Page
        assertEquals(MEDIA_SEARCH_PAGE_SIZE, page.data.size)
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `last page has no next key`() = runTest {
        coEvery { api.search(query = "apollo", page = 4, pageSize = MEDIA_SEARCH_PAGE_SIZE) } returns
            response(items = listOf(hit("id-last")), totalHits = 4 * MEDIA_SEARCH_PAGE_SIZE)

        val result = MediaSearchPagingSource(api, "apollo")
            .load(
                PagingSource.LoadParams.Append(key = 4, loadSize = MEDIA_SEARCH_PAGE_SIZE, placeholdersEnabled = false)
            )

        val page = result as PagingSource.LoadResult.Page
        assertEquals(3, page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun `network failure becomes a typed AppError`() = runTest {
        coEvery { api.search(query = any(), page = any(), pageSize = any()) } throws IOException("offline")

        val result = MediaSearchPagingSource(api, "apollo").load(refreshParams())

        val error = result as PagingSource.LoadResult.Error
        assertEquals(AppError.NoConnection, (error.throwable as AppErrorException).error)
    }

    @Test
    fun `unrenderable hits are dropped from the page`() = runTest {
        coEvery { api.search(query = "apollo", page = 1, pageSize = MEDIA_SEARCH_PAGE_SIZE) } returns
            response(
                items = listOf(hit("ok"), NasaImagesItemDto(data = emptyList(), links = emptyList())),
                totalHits = 2
            )

        val result = MediaSearchPagingSource(api, "apollo").load(refreshParams())

        assertEquals(listOf("ok"), (result as PagingSource.LoadResult.Page).data.map { it.nasaId })
    }

    private fun refreshParams() = PagingSource.LoadParams.Refresh<Int>(
        key = null,
        loadSize = MEDIA_SEARCH_PAGE_SIZE,
        placeholdersEnabled = false
    )

    private fun response(items: List<NasaImagesItemDto>, totalHits: Int) = NasaImagesSearchResponseDto(
        collection = NasaImagesCollectionDto(
            items = items,
            metadata = NasaImagesMetadataDto(totalHits = totalHits)
        )
    )

    private fun hit(id: String) = NasaImagesItemDto(
        data = listOf(NasaImagesItemDataDto(nasaId = id, title = id)),
        links = listOf(NasaImagesLinkDto(href = "https://assets.nasa.gov/$id.jpg", rel = "preview", render = "image"))
    )
}
