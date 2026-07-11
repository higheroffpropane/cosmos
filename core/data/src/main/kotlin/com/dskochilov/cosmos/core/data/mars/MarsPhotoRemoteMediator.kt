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
import com.dskochilov.cosmos.core.domain.mars.MarsFeedFilter
import com.dskochilov.cosmos.core.network.mars.MarsPhotoDto
import com.dskochilov.cosmos.core.network.mars.NasaMarsApi
import kotlin.coroutines.cancellation.CancellationException

/**
 * Pages the Mars photo feed by walking Martian days downwards (see ADR 0005).
 *
 * The continuation key is `(sol, page)` stored per query in `mars_remote_keys`:
 * REFRESH starts from the manifest's `max_sol`; an empty page moves to the previous
 * sol, scanning at most [MAX_SOL_SCAN_PER_LOAD] sols per load.
 */
@OptIn(ExperimentalPagingApi::class)
internal class MarsPhotoRemoteMediator(
    private val filter: MarsFeedFilter,
    private val api: NasaMarsApi,
    private val database: CosmosDatabase
) : RemoteMediator<Int, MarsPhotoEntity>() {

    private val photoDao = database.marsPhotoDao()
    private val remoteKeyDao = database.marsRemoteKeyDao()
    private val queryKey = listOfNotNull(filter.rover.apiName, filter.cameraApiName).joinToString("|")

    override suspend fun load(loadType: LoadType, state: PagingState<Int, MarsPhotoEntity>): MediatorResult = try {
        val startKey = when (loadType) {
            LoadType.REFRESH -> {
                val manifest = api.getManifest(filter.rover.apiName).photoManifest
                SolPage(sol = manifest.maxSol, page = 1)
            }

            // The feed only grows towards older sols; new photos arrive via REFRESH.
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> {
                val key = remoteKeyDao.get(queryKey)
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                val sol = key.nextSol
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                SolPage(sol = sol, page = key.nextPage)
            }
        }

        val (photos, servedFrom) = fetchNextNonEmptyPage(startKey)
        val nextKey = servedFrom?.copy(page = servedFrom.page + 1)

        database.withTransaction {
            if (loadType == LoadType.REFRESH) {
                photoDao.clearForRover(filter.rover.apiName)
                remoteKeyDao.delete(queryKey)
            }
            remoteKeyDao.upsert(
                MarsRemoteKeyEntity(
                    queryKey = queryKey,
                    nextSol = nextKey?.sol,
                    nextPage = nextKey?.page ?: 1
                )
            )
            photoDao.insertAll(photos.map { it.toEntity(filter.rover) })
        }

        MediatorResult.Success(endOfPaginationReached = nextKey == null)
    } catch (e: CancellationException) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        // Wrapped so the UI receives a typed AppError instead of a raw network exception.
        MediatorResult.Error(AppErrorException(e.toAppError()))
    }

    /**
     * Fetches from [startKey], walking to older sols while pages come back empty.
     * Returns the photos and the key they were served from, or an empty list with
     * a null key when the scan budget is exhausted or sol 0 is passed.
     */
    private suspend fun fetchNextNonEmptyPage(startKey: SolPage): Pair<List<MarsPhotoDto>, SolPage?> {
        var sol = startKey.sol
        var page = startKey.page
        var solsScanned = 0

        while (sol >= 0 && solsScanned < MAX_SOL_SCAN_PER_LOAD) {
            val photos = api.getPhotos(
                rover = filter.rover.apiName,
                sol = sol,
                page = page,
                camera = filter.cameraApiName
            ).photos

            if (photos.isNotEmpty()) {
                return photos to SolPage(sol = sol, page = page)
            }

            sol -= 1
            page = 1
            solsScanned += 1
        }

        return emptyList<MarsPhotoDto>() to null
    }

    private data class SolPage(val sol: Int, val page: Int)

    private companion object {
        const val MAX_SOL_SCAN_PER_LOAD = 15
    }
}
