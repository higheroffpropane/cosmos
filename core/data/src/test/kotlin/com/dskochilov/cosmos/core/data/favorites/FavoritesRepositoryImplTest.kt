package com.dskochilov.cosmos.core.data.favorites

import com.dskochilov.cosmos.core.database.CosmosDatabase
import com.dskochilov.cosmos.core.database.dao.FavoriteDao
import com.dskochilov.cosmos.core.database.entity.FavoriteEntity
import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.domain.favorites.ImagePrefetcher
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDate
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FavoritesRepositoryImplTest {

    private val dao = mockk<FavoriteDao>()
    private val database = mockk<CosmosDatabase> {
        every { favoriteDao() } returns dao
    }
    private val prefetchedUrls = mutableListOf<String>()
    private val prefetcher = ImagePrefetcher { url -> prefetchedUrls += url }

    @Test
    fun `toggling a new apod inserts it and prefetches its image`() = runTest {
        coEvery { dao.exists(any()) } returns false
        val inserted = slot<FavoriteEntity>()
        coEvery { dao.insert(capture(inserted)) } just Runs

        repository().toggleApod(imageApod())

        assertEquals("apod:2026-07-11", inserted.captured.id)
        assertEquals(listOf("https://apod.nasa.gov/m24.jpg"), prefetchedUrls)
    }

    @Test
    fun `toggling an existing favorite deletes it and prefetches nothing`() = runTest {
        coEvery { dao.exists("apod:2026-07-11") } returns true
        coEvery { dao.deleteById("apod:2026-07-11") } just Runs

        repository().toggleApod(imageApod())

        coVerify(exactly = 1) { dao.deleteById("apod:2026-07-11") }
        coVerify(exactly = 0) { dao.insert(any()) }
        assertEquals(emptyList<String>(), prefetchedUrls)
    }

    @Test
    fun `favorites without a usable image skip the prefetch`() = runTest {
        coEvery { dao.exists(any()) } returns false
        coEvery { dao.insert(any()) } just Runs

        repository().toggleApod(imageApod().copy(media = ApodMedia.Unsupported))

        assertEquals(emptyList<String>(), prefetchedUrls)
    }

    private fun TestScope.repository() = FavoritesRepositoryImpl(
        database = database,
        imagePrefetcher = prefetcher,
        ioDispatcher = StandardTestDispatcher(testScheduler)
    )

    private fun imageApod() = Apod(
        date = LocalDate.of(2026, 7, 11),
        title = "Messier 24",
        explanation = "A star cloud.",
        copyright = null,
        media = ApodMedia.Image(url = "https://apod.nasa.gov/m24.jpg", hdUrl = null)
    )
}
