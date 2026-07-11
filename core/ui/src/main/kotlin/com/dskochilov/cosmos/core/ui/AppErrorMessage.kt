package com.dskochilov.cosmos.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dskochilov.cosmos.core.common.result.AppError

/**
 * Shared user-facing message for a typed [AppError].
 */
@Composable
fun AppError.toUiMessage(): String = when (this) {
    AppError.NoConnection -> stringResource(R.string.core_ui_error_no_connection)
    AppError.RateLimited -> stringResource(R.string.core_ui_error_rate_limited)
    is AppError.Http -> stringResource(R.string.core_ui_error_http, code)
    is AppError.Unknown -> stringResource(R.string.core_ui_error_unknown)
}
