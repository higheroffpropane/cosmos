package com.dskochilov.cosmos.core.domain.mars

import androidx.paging.PagingData
import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.common.result.AppResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class MarsUseCasesTest {

    private val repository = mockk<MarsPhotoRepository>()

    @Test
    fun `observe delegates to repository with the same filter`() {
        val filter = MarsFeedFilter(rover = MarsRover.PERSEVERANCE, cameraApiName = "MCZ_LEFT")
        val flow: Flow<PagingData<MarsPhoto>> = flowOf(PagingData.empty())
        every { repository.observeFeed(filter) } returns flow

        val result = ObserveMarsPhotosUseCase(repository).invoke(filter)

        assertSame(flow, result)
        verify(exactly = 1) { repository.observeFeed(filter) }
    }

    @Test
    fun `get photo returns repository success`() = runTest {
        val photo = MarsPhoto(
            id = 42,
            rover = MarsRover.CURIOSITY,
            cameraName = "NAVCAM",
            cameraFullName = "Navigation Camera",
            imageUrl = "https://mars.nasa.gov/42.jpg",
            earthDate = LocalDate.of(2026, 1, 1),
            sol = 4000
        )
        coEvery { repository.getPhoto(42) } returns AppResult.Success(photo)

        val result = GetMarsPhotoUseCase(repository).invoke(42)

        assertEquals(AppResult.Success(photo), result)
        coVerify(exactly = 1) { repository.getPhoto(42) }
    }

    @Test
    fun `get photo propagates failure`() = runTest {
        coEvery { repository.getPhoto(1) } returns AppResult.Failure(AppError.Unknown())

        val result = GetMarsPhotoUseCase(repository).invoke(1)

        assertEquals(AppResult.Failure(AppError.Unknown()), result)
    }
}
