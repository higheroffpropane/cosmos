package com.dskochilov.cosmos.core.common.result

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppResultTest {

    @Test
    fun `map transforms success data`() {
        val result: AppResult<Int> = AppResult.Success(21)

        val mapped = result.map { it * 2 }

        assertEquals(AppResult.Success(42), mapped)
    }

    @Test
    fun `map keeps failure untouched`() {
        val result: AppResult<Int> = AppResult.Failure(AppError.RateLimited)

        val mapped = result.map { it * 2 }

        assertEquals(AppResult.Failure(AppError.RateLimited), mapped)
    }

    @Test
    fun `fold calls onSuccess for success`() {
        val result: AppResult<String> = AppResult.Success("apod")

        val folded = result.fold(onSuccess = { "got $it" }, onFailure = { "error" })

        assertEquals("got apod", folded)
    }

    @Test
    fun `fold calls onFailure with the typed error`() {
        val result: AppResult<String> = AppResult.Failure(AppError.Http(code = 500))

        val folded = result.fold(
            onSuccess = { "data" },
            onFailure = { error -> (error as AppError.Http).code.toString() }
        )

        assertEquals("500", folded)
    }
}
