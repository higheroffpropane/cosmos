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
            id = "img-42",
            rover = MarsRover.CURIOSITY,
            cameraName = "NAVCAM",
            cameraFullName = "Navigation Camera",
            imageUrl = "https://mars.nasa.gov/42.jpg",
            thumbnailUrl = "https://mars.nasa.gov/42_320.jpg",
            earthDate = LocalDate.of(2026, 1, 1),
            sol = 4000
        )
        coEvery { repository.getPhoto("img-42") } returns AppResult.Success(photo)

        val result = GetMarsPhotoUseCase(repository).invoke("img-42")

        assertEquals(AppResult.Success(photo), result)
        coVerify(exactly = 1) { repository.getPhoto("img-42") }
    }

    @Test
    fun `get photo propagates failure`() = runTest {
        coEvery { repository.getPhoto("x") } returns AppResult.Failure(AppError.Unknown())

        val result = GetMarsPhotoUseCase(repository).invoke("x")

        assertEquals(AppResult.Failure(AppError.Unknown()), result)
    }
}
