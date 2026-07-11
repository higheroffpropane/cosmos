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
import com.dskochilov.cosmos.core.domain.mars.MarsFeedFilter
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun refresh_loadsFromManifestMaxSol_andStoresContinuationKey() = runTest {
        val api = FakeNasaMarsApi(
            maxSol = 100,
            pages = mapOf(
                (100 to 1) to listOf(fakePhoto(id = 1, sol = 100), fakePhoto(id = 2, sol = 100))
            )
        )
        val mediator = mediator(api)

        val result = mediator.load(LoadType.REFRESH, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(2, countPhotos())
        val key = database.marsRemoteKeyDao().get("curiosity")
        assertEquals(100, key?.nextSol)
        assertEquals(2, key?.nextPage)
    }

    @Test
    fun append_walksToPreviousSol_whenCurrentSolIsExhausted() = runTest {
        val api = FakeNasaMarsApi(
            maxSol = 100,
            pages = mapOf(
                (100 to 1) to listOf(fakePhoto(id = 1, sol = 100)),
                // (100, 2) is empty -> mediator must walk down; sols 99..98 empty too.
                (97 to 1) to listOf(fakePhoto(id = 2, sol = 97))
            )
        )
        val mediator = mediator(api)

        mediator.load(LoadType.REFRESH, emptyPagingState())
        val result = mediator.load(LoadType.APPEND, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        assertEquals(2, countPhotos())
        val key = database.marsRemoteKeyDao().get("curiosity")
        assertEquals(97, key?.nextSol)
        assertEquals(2, key?.nextPage)
    }

    @Test
    fun append_reportsEndOfPagination_whenScanBudgetFindsNothing() = runTest {
        val api = FakeNasaMarsApi(
            maxSol = 100,
            pages = mapOf((100 to 1) to listOf(fakePhoto(id = 1, sol = 100)))
        )
        val mediator = mediator(api)

        mediator.load(LoadType.REFRESH, emptyPagingState())
        val result = mediator.load(LoadType.APPEND, emptyPagingState())

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        // A further APPEND must not hit the network again.
        val requestsAfterEnd = api.photoRequests
        mediator.load(LoadType.APPEND, emptyPagingState())
        assertEquals(requestsAfterEnd, api.photoRequests)
    }

    @Test
    fun refresh_clearsPreviouslyCachedPhotosForTheRover() = runTest {
        val api = FakeNasaMarsApi(
            maxSol = 100,
            pages = mapOf((100 to 1) to listOf(fakePhoto(id = 10, sol = 100)))
        )
        database.marsPhotoDao().insertAll(
            listOf(
                MarsPhotoEntity(
                    id = 999,
                    rover = "curiosity",
                    cameraName = "NAVCAM",
                    cameraFullName = "Navigation Camera",
                    imageUrl = "https://mars.nasa.gov/old.jpg",
                    earthDate = "2020-01-01",
                    sol = 50
                )
            )
        )

        mediator(api).load(LoadType.REFRESH, emptyPagingState())

        assertEquals(1, countPhotos())
        assertEquals(null, database.marsPhotoDao().getById(999))
    }

    private fun mediator(api: FakeNasaMarsApi) = MarsPhotoRemoteMediator(
        filter = MarsFeedFilter(rover = MarsRover.CURIOSITY),
        api = api,
        database = database
    )

    private fun emptyPagingState() = PagingState<Int, MarsPhotoEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 25),
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
