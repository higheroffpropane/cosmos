package com.dskochilov.cosmos.core.data.apod

import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.common.result.AppResult
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.network.apod.ApodDto
import com.dskochilov.cosmos.core.network.apod.NasaApodApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ApodRepositoryImplTest {

    private val api = mockk<NasaApodApi>()

    @Test
    fun `fetches today and maps dto to domain on success`() = runTest {
        val repository = repository()
        coEvery { api.getApod(date = null, thumbs = true) } returns testDto()

        val result = repository.getApod()

        val apod = (result as AppResult.Success).data
        assertEquals(LocalDate.of(2026, 7, 7), apod.date)
        assertEquals(ApodMedia.Image(url = "https://apod.nasa.gov/i.jpg", hdUrl = null), apod.media)
    }

    @Test
    fun `passes the date as iso string`() = runTest {
        val repository = repository()
        coEvery { api.getApod(date = "2001-01-01", thumbs = true) } returns testDto(date = "2001-01-01")

        repository.getApod(LocalDate.of(2001, 1, 1))

        coVerify(exactly = 1) { api.getApod(date = "2001-01-01", thumbs = true) }
    }

    @Test
    fun `maps network failure to typed error`() = runTest {
        val repository = repository()
        coEvery { api.getApod(date = null, thumbs = true) } throws IOException("offline")

        val result = repository.getApod()

        assertEquals(AppResult.Failure(AppError.NoConnection), result)
    }

    private fun TestScope.repository() = ApodRepositoryImpl(
        api = api,
        ioDispatcher = StandardTestDispatcher(testScheduler)
    )

    private fun testDto(date: String = "2026-07-07") = ApodDto(
        date = date,
        title = "Title",
        explanation = "Explanation",
        mediaType = "image",
        url = "https://apod.nasa.gov/i.jpg"
    )
}
