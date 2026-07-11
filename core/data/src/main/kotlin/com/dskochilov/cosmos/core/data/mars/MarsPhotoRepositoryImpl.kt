package com.dskochilov.cosmos.core.data.mars

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.dskochilov.cosmos.core.common.coroutines.CosmosDispatchers
import com.dskochilov.cosmos.core.common.coroutines.Dispatcher
import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.common.result.AppResult
import com.dskochilov.cosmos.core.database.CosmosDatabase
import com.dskochilov.cosmos.core.domain.mars.MarsFeedFilter
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import com.dskochilov.cosmos.core.domain.mars.MarsPhotoRepository
import com.dskochilov.cosmos.core.network.mars.NasaMarsApi
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val PHOTOS_PER_PAGE = 25

@OptIn(ExperimentalPagingApi::class)
internal class MarsPhotoRepositoryImpl @Inject constructor(
    private val api: NasaMarsApi,
    private val database: CosmosDatabase,
    @param:Dispatcher(CosmosDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : MarsPhotoRepository {

    override fun observeFeed(filter: MarsFeedFilter): Flow<PagingData<MarsPhoto>> = Pager(
        config = PagingConfig(
            pageSize = PHOTOS_PER_PAGE,
            enablePlaceholders = false
        ),
        remoteMediator = MarsPhotoRemoteMediator(filter = filter, api = api, database = database),
        pagingSourceFactory = {
            database.marsPhotoDao().pagingSource(
                rover = filter.rover.apiName,
                camera = filter.cameraApiName
            )
        }
    ).flow.map { pagingData -> pagingData.map { entity -> entity.toDomain() } }

    override suspend fun getPhoto(id: Long): AppResult<MarsPhoto> = withContext(ioDispatcher) {
        database.marsPhotoDao().getById(id)
            ?.let { AppResult.Success(it.toDomain()) }
            ?: AppResult.Failure(AppError.Unknown())
    }
}
