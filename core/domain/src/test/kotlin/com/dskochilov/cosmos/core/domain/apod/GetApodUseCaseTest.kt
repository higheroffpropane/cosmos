package com.dskochilov.cosmos.core.domain.apod

import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.common.result.AppResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetApodUseCaseTest {

    private val repository = mockk<ApodRepository>()
    private val useCase = GetApodUseCase(repository)

    @Test
    fun `returns repository success for today when no date given`() = runTest {
        val apod = testApod(LocalDate.of(2026, 7, 7))
        coEvery { repository.getApod(null) } returns AppResult.Success(apod)

        val result = useCase()

        assertEquals(AppResult.Success(apod), result)
        coVerify(exactly = 1) { repository.getApod(null) }
    }

    @Test
    fun `passes requested date through to the repository`() = runTest {
        val date = LocalDate.of(2001, 1, 1)
        val apod = testApod(date)
        coEvery { repository.getApod(date) } returns AppResult.Success(apod)

        val result = useCase(date)

        assertEquals(AppResult.Success(apod), result)
        coVerify(exactly = 1) { repository.getApod(date) }
    }

    @Test
    fun `propagates repository failure untouched`() = runTest {
        coEvery { repository.getApod(null) } returns AppResult.Failure(AppError.NoConnection)

        val result = useCase()

        assertEquals(AppResult.Failure(AppError.NoConnection), result)
    }

    private fun testApod(date: LocalDate) = Apod(
        date = date,
        title = "Title",
        explanation = "Explanation",
        copyright = null,
        media = ApodMedia.Image(url = "https://example.com/i.jpg", hdUrl = null)
    )
}
