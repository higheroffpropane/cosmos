package com.dskochilov.cosmos.feature.marsrover

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.common.result.AppResult
import com.dskochilov.cosmos.core.domain.mars.GetMarsPhotoUseCase
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import com.dskochilov.cosmos.feature.marsrover.detail.MarsPhotoDetailViewModel
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MarsPhotoDetailViewModelTest {

    private val getMarsPhoto = mockk<GetMarsPhotoUseCase>()
    private val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads the photo for the id from navigation args`() = runTest {
        val photo = testPhoto()
        coEvery { getMarsPhoto.invoke("img-42") } returns AppResult.Success(photo)

        val viewModel = viewModel(photoId = "img-42")

        viewModel.uiState.test {
            assertEquals(MarsPhotoDetailUiState.Loading, awaitItem())
            assertEquals(MarsPhotoDetailUiState.Content(photo), awaitItem())
        }
    }

    @Test
    fun `emits Error then recovers on Retry`() = runTest {
        coEvery { getMarsPhoto.invoke("img-42") } returns AppResult.Failure(AppError.Unknown())
        val viewModel = viewModel(photoId = "img-42")

        viewModel.uiState.test {
            assertEquals(MarsPhotoDetailUiState.Loading, awaitItem())
            assertEquals(MarsPhotoDetailUiState.Error(AppError.Unknown()), awaitItem())

            val photo = testPhoto()
            coEvery { getMarsPhoto.invoke("img-42") } returns AppResult.Success(photo)
            viewModel.onIntent(MarsPhotoDetailIntent.Retry)

            assertEquals(MarsPhotoDetailUiState.Loading, awaitItem())
            assertEquals(MarsPhotoDetailUiState.Content(photo), awaitItem())
        }
    }

    private fun viewModel(photoId: String) = MarsPhotoDetailViewModel(
        getMarsPhoto = getMarsPhoto,
        savedStateHandle = SavedStateHandle(mapOf(MarsPhotoDetailViewModel.ARG_PHOTO_ID to photoId))
    )

    private fun testPhoto() = MarsPhoto(
        id = "img-42",
        rover = MarsRover.CURIOSITY,
        cameraName = "NAVCAM",
        cameraFullName = "Navigation Camera",
        imageUrl = "https://mars.nasa.gov/42.jpg",
        thumbnailUrl = "https://mars.nasa.gov/42_320.jpg",
        earthDate = LocalDate.of(2026, 1, 1),
        sol = 4000
    )
}
