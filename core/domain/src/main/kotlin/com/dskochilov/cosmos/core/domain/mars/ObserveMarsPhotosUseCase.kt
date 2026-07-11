package com.dskochilov.cosmos.core.domain.mars

import androidx.paging.PagingData
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Streams the paged Mars photo feed for a rover/camera filter.
 */
class ObserveMarsPhotosUseCase @Inject constructor(private val repository: MarsPhotoRepository) {
    operator fun invoke(filter: MarsFeedFilter): Flow<PagingData<MarsPhoto>> = repository.observeFeed(filter)
}
