package com.dskochilov.cosmos.feature.apod

import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.domain.apod.Apod
import java.time.LocalDate

/**
 * MVI contract of the APOD screen: single sealed state, user intents,
 * and one-off effects that must not be replayed on recomposition.
 */
sealed interface ApodUiState {
    data object Loading : ApodUiState

    data class Content(val apod: Apod) : ApodUiState

    data class Error(val error: AppError) : ApodUiState
}

sealed interface ApodIntent {
    data class SelectDate(val date: LocalDate) : ApodIntent

    data object Retry : ApodIntent

    data object OpenVideo : ApodIntent

    data object ToggleFavorite : ApodIntent
}

sealed interface ApodEffect {
    data class OpenUrl(val url: String) : ApodEffect
}
