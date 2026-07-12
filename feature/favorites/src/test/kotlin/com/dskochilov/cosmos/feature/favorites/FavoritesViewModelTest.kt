package com.dskochilov.cosmos.feature.favorites

import app.cash.turbine.test
import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.domain.favorites.Favorite
import com.dskochilov.cosmos.core.domain.favorites.FavoritesRepository
import com.dskochilov.cosmos.core.domain.favorites.ObserveFavoritesUseCase
import com.dskochilov.cosmos.core.domain.favorites.RemoveFavoriteUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val repository = mockk<FavoritesRepository>()
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
    fun `emits Loading then Content when favorites exist`() = runTest {
        val favorite = apodFavorite()
        every { repository.observeFavorites() } returns flowOf(listOf(favorite))
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(FavoritesUiState.Loading, awaitItem())
            assertEquals(FavoritesUiState.Content(listOf(favorite)), awaitItem())
        }
    }

    @Test
    fun `emits Empty when there are no favorites`() = runTest {
        every { repository.observeFavorites() } returns flowOf(emptyList())
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(FavoritesUiState.Loading, awaitItem())
            assertEquals(FavoritesUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `Remove intent delegates to the use case`() = runTest {
        every { repository.observeFavorites() } returns flowOf(emptyList())
        coEvery { repository.remove("apod:2026-07-11") } just Runs
        val viewModel = viewModel()

        viewModel.onIntent(FavoritesIntent.Remove("apod:2026-07-11"))
        runCurrent()

        coVerify(exactly = 1) { repository.remove("apod:2026-07-11") }
    }

    private fun viewModel() = FavoritesViewModel(
        observeFavorites = ObserveFavoritesUseCase(repository),
        removeFavorite = RemoveFavoriteUseCase(repository)
    )

    private fun apodFavorite() = Favorite.ApodEntry(
        apod = Apod(
            date = LocalDate.of(2026, 7, 11),
            title = "Messier 24",
            explanation = "x",
            copyright = null,
            media = ApodMedia.Image(url = "https://apod.nasa.gov/m24.jpg", hdUrl = null)
        ),
        savedAt = 1
    )
}
