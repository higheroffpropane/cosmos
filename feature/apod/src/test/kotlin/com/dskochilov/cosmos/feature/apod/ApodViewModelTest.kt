package com.dskochilov.cosmos.feature.apod

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.common.result.AppResult
import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.domain.apod.GetApodUseCase
import com.dskochilov.cosmos.core.domain.favorites.FavoritesRepository
import com.dskochilov.cosmos.core.domain.favorites.ObserveIsFavoriteUseCase
import com.dskochilov.cosmos.core.domain.favorites.ToggleApodFavoriteUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ApodViewModelTest {

    private val getApod = mockk<GetApodUseCase>()
    private val favoritesRepository = mockk<FavoritesRepository> {
        every { observeIsFavorite(any()) } returns flowOf(false)
        coEvery { toggleApod(any()) } just Runs
    }
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
    fun `emits Loading then Content when initial load succeeds`() = runTest {
        val apod = imageApod()
        coEvery { getApod.invoke(null) } returns AppResult.Success(apod)

        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ApodUiState.Loading, awaitItem())
            assertEquals(ApodUiState.Content(apod), awaitItem())
        }
    }

    @Test
    fun `emits Error when initial load fails, then Content after successful Retry`() = runTest {
        coEvery { getApod.invoke(null) } returns AppResult.Failure(AppError.NoConnection)
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(ApodUiState.Loading, awaitItem())
            assertEquals(ApodUiState.Error(AppError.NoConnection), awaitItem())

            val apod = imageApod()
            coEvery { getApod.invoke(null) } returns AppResult.Success(apod)
            viewModel.onIntent(ApodIntent.Retry)

            assertEquals(ApodUiState.Loading, awaitItem())
            assertEquals(ApodUiState.Content(apod), awaitItem())
        }
    }

    @Test
    fun `SelectDate stores the date in SavedStateHandle and reloads`() = runTest {
        val date = LocalDate.of(2001, 1, 1)
        val savedStateHandle = SavedStateHandle()
        coEvery { getApod.invoke(null) } returns AppResult.Success(imageApod())
        coEvery { getApod.invoke(date) } returns AppResult.Success(imageApod(date))

        val viewModel = viewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(ApodUiState.Loading, awaitItem())
            assertEquals(ApodUiState.Content(imageApod()), awaitItem())

            viewModel.onIntent(ApodIntent.SelectDate(date))

            assertEquals(ApodUiState.Loading, awaitItem())
            assertEquals(ApodUiState.Content(imageApod(date)), awaitItem())
        }
        assertEquals("2001-01-01", savedStateHandle.get<String>(ApodViewModel.KEY_SELECTED_DATE))
        coVerify(exactly = 1) { getApod.invoke(date) }
    }

    @Test
    fun `restores the selected date from SavedStateHandle after process death`() = runTest {
        val date = LocalDate.of(2015, 5, 5)
        coEvery { getApod.invoke(date) } returns AppResult.Success(imageApod(date))
        val savedStateHandle = SavedStateHandle(mapOf(ApodViewModel.KEY_SELECTED_DATE to "2015-05-05"))

        val viewModel = viewModel(savedStateHandle)

        viewModel.uiState.test {
            assertEquals(ApodUiState.Loading, awaitItem())
            assertEquals(ApodUiState.Content(imageApod(date)), awaitItem())
        }
        coVerify(exactly = 1) { getApod.invoke(date) }
    }

    @Test
    fun `OpenVideo emits OpenUrl effect for video entries`() = runTest {
        val video = videoApod()
        coEvery { getApod.invoke(null) } returns AppResult.Success(video)
        val viewModel = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(ApodIntent.OpenVideo)
            assertEquals(ApodEffect.OpenUrl("https://youtube.com/watch?v=x"), awaitItem())
        }
    }

    @Test
    fun `OpenVideo does nothing for image entries`() = runTest {
        coEvery { getApod.invoke(null) } returns AppResult.Success(imageApod())
        val viewModel = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            viewModel.onIntent(ApodIntent.OpenVideo)
            expectNoEvents()
        }
    }

    @Test
    fun `ToggleFavorite delegates the shown entry to the favorites use case`() = runTest {
        val apod = imageApod()
        coEvery { getApod.invoke(null) } returns AppResult.Success(apod)
        val viewModel = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(ApodIntent.ToggleFavorite)
        dispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { favoritesRepository.toggleApod(apod) }
    }

    @Test
    fun `late response for a previous date cannot replace the selected entry`() = runTest {
        val pending = CompletableDeferred<AppResult<Apod>>()
        val selected = LocalDate.of(2012, 6, 5)
        coEvery { getApod.invoke(null) } coAnswers { pending.await() }
        coEvery { getApod.invoke(selected) } returns AppResult.Success(imageApod(selected))
        val viewModel = viewModel()
        dispatcher.scheduler.runCurrent()

        viewModel.onIntent(ApodIntent.SelectDate(selected))
        dispatcher.scheduler.runCurrent()
        pending.complete(AppResult.Success(imageApod()))
        dispatcher.scheduler.runCurrent()

        assertEquals(ApodUiState.Content(imageApod(selected)), viewModel.uiState.value)
    }

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = ApodViewModel(
        getApod = getApod,
        observeIsFavorite = ObserveIsFavoriteUseCase(favoritesRepository),
        toggleApodFavorite = ToggleApodFavoriteUseCase(favoritesRepository),
        savedStateHandle = savedStateHandle
    )

    private fun imageApod(date: LocalDate = LocalDate.of(2026, 7, 7)) = Apod(
        date = date,
        title = "Title",
        explanation = "Explanation",
        copyright = null,
        media = ApodMedia.Image(url = "https://apod.nasa.gov/i.jpg", hdUrl = null)
    )

    private fun videoApod() = Apod(
        date = LocalDate.of(2026, 7, 7),
        title = "Video title",
        explanation = "Explanation",
        copyright = null,
        media = ApodMedia.Video(videoUrl = "https://youtube.com/watch?v=x", thumbnailUrl = null)
    )
}
