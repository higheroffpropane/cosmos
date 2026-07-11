package com.dskochilov.cosmos.feature.marsrover

import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import com.dskochilov.cosmos.core.domain.mars.MarsRover

/**
 * MVI contract of the Mars Rover feed. The paged photo stream itself is exposed
 * separately as `Flow<PagingData<MarsPhoto>>` — Paging owns its own Loading/Error
 * state machine per load position — while the filter is regular MVI state.
 */
sealed interface MarsRoverIntent {
    data class SelectRover(val rover: MarsRover) : MarsRoverIntent

    data class SelectCamera(val cameraApiName: String?) : MarsRoverIntent
}

/**
 * MVI contract of the photo detail screen.
 */
sealed interface MarsPhotoDetailUiState {
    data object Loading : MarsPhotoDetailUiState

    data class Content(val photo: MarsPhoto) : MarsPhotoDetailUiState

    data class Error(val error: AppError) : MarsPhotoDetailUiState
}

sealed interface MarsPhotoDetailIntent {
    data object Retry : MarsPhotoDetailIntent
}
