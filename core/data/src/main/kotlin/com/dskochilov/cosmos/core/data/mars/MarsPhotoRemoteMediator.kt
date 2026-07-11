package com.dskochilov.cosmos.core.data.mars

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dskochilov.cosmos.core.common.result.AppErrorException
import com.dskochilov.cosmos.core.data.toAppError
import com.dskochilov.cosmos.core.database.CosmosDatabase
import com.dskochilov.cosmos.core.database.entity.MarsPhotoEntity
import com.dskochilov.cosmos.core.database.entity.MarsRemoteKeyEntity
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import com.dskochilov.cosmos.core.network.mars.NasaMarsApi
import kotlin.coroutines.cancellation.CancellationException

internal const val MARS_PHOTOS_PAGE_SIZE = 32

/**
 * Pages a rover's raw-image feed (newest sols first) into the Room cache.
 * The continuation key is a plain page number stored per rover; camera
 * filtering happens locally in the DAO query (see ADR 0006).
 */
@OptIn(ExperimentalPagingApi::class)
internal class MarsPhotoRemoteMediator(
    private val rover: MarsRover,
    private val api: NasaMarsApi,
    private val database: CosmosDatabase
) : RemoteMediator<Int, MarsPhotoEntity>() {

    private val photoDao = database.marsPhotoDao()
    private val remoteKeyDao = database.marsRemoteKeyDao()
    private val queryKey = rover.apiName

    override suspend fun load(loadType: LoadType, state: PagingState<Int, MarsPhotoEntity>): MediatorResult = try {
        val page = when (loadType) {
            LoadType.REFRESH -> 0

            // The feed only grows towards older sols; new photos arrive via REFRESH.
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> {
                val key = remoteKeyDao.get(queryKey)
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                key.nextPage
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        val result = fetchPage(page)

        database.withTransaction {
            if (loadType == LoadType.REFRESH) {
                photoDao.clearForRover(rover.apiName)
                remoteKeyDao.delete(queryKey)
            }
            remoteKeyDao.upsert(
                MarsRemoteKeyEntity(
                    queryKey = queryKey,
                    nextPage = if (result.endReached) null else page + 1
                )
            )
            photoDao.insertAll(result.photos)
        }

        MediatorResult.Success(endOfPaginationReached = result.endReached)
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        // Wrapped so the UI receives a typed AppError instead of a raw network exception.
        MediatorResult.Error(AppErrorException(e.toAppError()))
    }

    private suspend fun fetchPage(page: Int): FetchResult = when (rover) {
        MarsRover.PERSEVERANCE -> {
            val response = api.getPerseverancePhotos(perPage = MARS_PHOTOS_PAGE_SIZE, page = page)
            FetchResult(
                photos = response.images.map { it.toEntity() },
                endReached = response.images.isEmpty() ||
                    (page + 1) * MARS_PHOTOS_PAGE_SIZE >= response.totalResults
            )
        }

        MarsRover.CURIOSITY -> {
            val response = api.getCuriosityPhotos(perPage = MARS_PHOTOS_PAGE_SIZE, page = page)
            FetchResult(
                photos = response.items.map { it.toEntity() },
                endReached = response.items.isEmpty()
            )
        }
    }

    private data class FetchResult(val photos: List<MarsPhotoEntity>, val endReached: Boolean)
}
