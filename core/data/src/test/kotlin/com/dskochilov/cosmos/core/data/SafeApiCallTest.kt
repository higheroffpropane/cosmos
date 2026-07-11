package com.dskochilov.cosmos.core.data

import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.common.result.AppResult
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response

class SafeApiCallTest {

    @Test
    fun `wraps the block result on success`() = runTest {
        val result = safeApiCall { "data" }

        assertEquals(AppResult.Success("data"), result)
    }

    @Test
    fun `maps IOException to NoConnection`() = runTest {
        val result = safeApiCall<String> { throw IOException("offline") }

        assertEquals(AppResult.Failure(AppError.NoConnection), result)
    }

    @Test
    fun `maps HTTP 429 to RateLimited`() = runTest {
        val result = safeApiCall<String> { throw httpException(code = 429) }

        assertEquals(AppResult.Failure(AppError.RateLimited), result)
    }

    @Test
    fun `maps other HTTP codes to Http with the code`() = runTest {
        val result = safeApiCall<String> { throw httpException(code = 500) }

        assertEquals(AppResult.Failure(AppError.Http(code = 500)), result)
    }

    @Test
    fun `maps unexpected exceptions to Unknown with cause`() = runTest {
        val cause = IllegalStateException("boom")

        val result = safeApiCall<String> { throw cause }

        val failure = result as AppResult.Failure
        val error = assertInstanceOf(AppError.Unknown::class.java, failure.error)
        assertEquals(cause, error.cause)
    }

    private fun httpException(code: Int): HttpException =
        HttpException(Response.error<String>(code, "{}".toResponseBody()))
}
