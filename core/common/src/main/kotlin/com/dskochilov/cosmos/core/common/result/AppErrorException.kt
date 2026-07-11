package com.dskochilov.cosmos.core.common.result

/**
 * Carrier for [AppError] through APIs that can only transport [Throwable]
 * (e.g. Paging's LoadState.Error), so UI layers never see raw network exceptions.
 */
class AppErrorException(val error: AppError) : Exception()

/** The [AppError] behind a throwable, or [AppError.Unknown] if it isn't one of ours. */
fun Throwable.asAppError(): AppError = (this as? AppErrorException)?.error ?: AppError.Unknown(this)
