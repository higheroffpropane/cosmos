package com.dskochilov.cosmos.core.common.result

/**
 * Domain-level failure categories every data-layer error is mapped into.
 * UI layers render these as retryable error states.
 */
sealed interface AppError {
    /** Device is offline or the host is unreachable. */
    data object NoConnection : AppError

    /** Non-2xx HTTP response that is not a rate limit. */
    data class Http(val code: Int) : AppError

    /** HTTP 429 — the NASA API key ran out of quota. */
    data object RateLimited : AppError

    /** Anything unexpected: serialization bugs, programming errors, etc. */
    data class Unknown(val cause: Throwable? = null) : AppError
}
