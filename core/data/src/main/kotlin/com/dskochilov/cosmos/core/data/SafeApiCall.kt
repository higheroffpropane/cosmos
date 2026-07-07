package com.dskochilov.cosmos.core.data

import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.common.result.AppResult
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

private const val HTTP_TOO_MANY_REQUESTS = 429

/**
 * Executes a network call and maps every failure into a typed [AppError],
 * so repositories never leak exceptions across the domain boundary.
 * Coroutine cancellation is always rethrown.
 */
// TooGenericExceptionCaught: the last catch intentionally funnels unexpected errors into AppError.Unknown.
// SwallowedException: IOException is deliberately collapsed into the connectionless NoConnection state.
@Suppress("TooGenericExceptionCaught", "SwallowedException")
suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    AppResult.Failure(e.toHttpError())
} catch (e: IOException) {
    AppResult.Failure(AppError.NoConnection)
} catch (e: Exception) {
    AppResult.Failure(AppError.Unknown(e))
}

private fun HttpException.toHttpError(): AppError = when (code()) {
    HTTP_TOO_MANY_REQUESTS -> AppError.RateLimited
    else -> AppError.Http(code())
}
