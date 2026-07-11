package com.dskochilov.cosmos.core.common.result

/**
 * Result of a data-layer operation: either [Success] with data or [Failure]
 * with a typed [AppError]. Repositories never throw across the domain boundary.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

inline fun <T, R> AppResult<T>.fold(onSuccess: (T) -> R, onFailure: (AppError) -> R): R = when (this) {
    is AppResult.Success -> onSuccess(data)
    is AppResult.Failure -> onFailure(error)
}
