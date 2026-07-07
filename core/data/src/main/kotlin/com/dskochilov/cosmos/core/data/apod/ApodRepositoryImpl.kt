package com.dskochilov.cosmos.core.data.apod

import com.dskochilov.cosmos.core.common.coroutines.CosmosDispatchers
import com.dskochilov.cosmos.core.common.coroutines.Dispatcher
import com.dskochilov.cosmos.core.common.result.AppResult
import com.dskochilov.cosmos.core.common.result.map
import com.dskochilov.cosmos.core.data.safeApiCall
import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.apod.ApodRepository
import com.dskochilov.cosmos.core.network.apod.NasaApodApi
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class ApodRepositoryImpl @Inject constructor(
    private val api: NasaApodApi,
    @param:Dispatcher(CosmosDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ApodRepository {

    override suspend fun getApod(date: LocalDate?): AppResult<Apod> = withContext(ioDispatcher) {
        safeApiCall { api.getApod(date = date?.toString(), thumbs = true) }
            .map { dto -> dto.toDomain() }
    }
}
