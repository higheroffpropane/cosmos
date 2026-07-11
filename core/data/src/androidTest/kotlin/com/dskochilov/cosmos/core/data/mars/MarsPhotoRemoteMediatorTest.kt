package com.dskochilov.cosmos.core.data.mars

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dskochilov.cosmos.core.database.CosmosDatabase
import com.dskochilov.cosmos.core.database.entity.MarsPhotoEntity
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalPagingApi::class)
@RunWith(AndroidJUnit4::class)
class MarsPhotoRemoteMediatorTest {

    private lateinit var database: CosmosDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CosmosDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun refresh_loadsFirstPage_andStoresContinuationKey() = runTest {
        val api = FakeNasaMarsApi(
            curiosityPages = mapOf(
                0 to listOf(fakeCuriosityPhoto(id = "a", sol = 100), fakeCuriosityPhoto(id = "b", sol = 100))
            )
        )
        val mediator = mediator(api)

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(2, countPhotos())
        assertEquals(1, database.marsRemoteKeyDao().get("curiosity")?.nextPage)
    }

    @Test
    fun append_continuesFromStoredPage() = runTest {
        val api = FakeNasaMarsApi(
            curiosityPages = mapOf(
                0 to listOf(fakeCuriosityPhoto(id = "a", sol = 100)),
                1 to listOf(fakeCuriosityPhoto(id = "b", sol = 99))
            )
        )
        val mediator = mediator(api)

        mediator.load(LoadType.REFRESH, emptyPagingState())
        val result = mediator.load(LoadType.APPEND, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(2, countPhotos())
        assertEquals(2, database.marsRemoteKeyDao().get("curiosity")?.nextPage)
    }

    @Test
    fun emptyPage_endsPagination_andFurtherAppendsSkipTheNetwork() = runTest {
        val api = FakeNasaMarsApi(
            curiosityPages = mapOf(0 to listOf(fakeCuriosityPhoto(id = "a", sol = 100)))
        )
        val mediator = mediator(api)

        mediator.load(LoadType.REFRESH, emptyPagingState())
        val result = mediator.load(LoadType.APPEND, emptyPagingState())

        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertNull(database.marsRemoteKeyDao().get("curiosity")?.nextPage)

        val requestsAfterEnd = api.photoRequests
        mediator.load(LoadType.APPEND, emptyPagingState())
        assertEquals(requestsAfterEnd, api.photoRequests)
    }

    @Test
    fun refresh_clearsPreviouslyCachedPhotosForTheRover() = runTest {
        val api = FakeNasaMarsApi(
            curiosityPages = mapOf(0 to listOf(fakeCuriosityPhoto(id = "new", sol = 100)))
        )
        database.marsPhotoDao().insertAll(
            listOf(
                MarsPhotoEntity(
                    id = "stale",
                    rover = "curiosity",
                    cameraName = "MAST_LEFT",
                    cameraFullName = "Mastcam Left",
                    imageUrl = "https://mars.nasa.gov/old.jpg",
                    thumbnailUrl = "https://mars.nasa.gov/old.jpg",
                    earthDate = "2020-01-01",
                    sol = 50
                )
            )
        )

        mediator(api).load(LoadType.REFRESH, emptyPagingState())

        assertEquals(1, countPhotos())
        assertNull(database.marsPhotoDao().getById("stale"))
    }

    private fun mediator(api: FakeNasaMarsApi) = MarsPhotoRemoteMediator(
        rover = MarsRover.CURIOSITY,
        api = api,
        database = database
    )

    private fun emptyPagingState() = PagingState<Int, MarsPhotoEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = MARS_PHOTOS_PAGE_SIZE),
        leadingPlaceholderCount = 0
    )

    private suspend fun countPhotos(): Int {
        val source = database.marsPhotoDao().pagingSource(rover = "curiosity", camera = null)
        val page = source.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false)
        )
        return (page as PagingSource.LoadResult.Page).data.size
    }
}
