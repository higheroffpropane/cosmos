package com.dskochilov.cosmos.feature.marsrover

import androidx.lifecycle.SavedStateHandle
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.dskochilov.cosmos.core.domain.mars.MarsFeedFilter
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import com.dskochilov.cosmos.core.domain.mars.ObserveMarsPhotosUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MarsRoverViewModelTest {

    private val observeMarsPhotos = mockk<ObserveMarsPhotosUseCase>()

    // Unconfined so that cachedIn/stateIn collectors run eagerly; required for asSnapshot().
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        // Explicit load states: asSnapshot() waits until the refresh state settles.
        val loadedStates = LoadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true)
        )
        every { observeMarsPhotos.invoke(any()) } returns
            flowOf(PagingData.from(listOf(testPhoto()), sourceLoadStates = loadedStates))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default filter is Curiosity with all cameras`() = runTest {
        val viewModel = MarsRoverViewModel(observeMarsPhotos, SavedStateHandle())
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(MarsFeedFilter(rover = MarsRover.CURIOSITY, cameraApiName = null), viewModel.filter.value)
    }

    @Test
    fun `photos stream queries the use case with the current filter`() = runTest {
        val viewModel = MarsRoverViewModel(observeMarsPhotos, SavedStateHandle())

        val items = viewModel.photos.asSnapshot()

        assertEquals(listOf(testPhoto()), items)
        verify { observeMarsPhotos.invoke(MarsFeedFilter(rover = MarsRover.CURIOSITY, cameraApiName = null)) }
    }

    @Test
    fun `selecting a rover resets the camera and persists both`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = MarsRoverViewModel(observeMarsPhotos, savedStateHandle)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(MarsRoverIntent.SelectCamera("NAVCAM"))
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("NAVCAM", viewModel.filter.value.cameraApiName)

        viewModel.onIntent(MarsRoverIntent.SelectRover(MarsRover.PERSEVERANCE))
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(MarsRover.PERSEVERANCE, viewModel.filter.value.rover)
        assertNull(viewModel.filter.value.cameraApiName)
        assertEquals(MarsRover.PERSEVERANCE.name, savedStateHandle.get<String>(MarsRoverViewModel.KEY_ROVER))
    }

    @Test
    fun `filter is restored from SavedStateHandle after process death`() = runTest {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                MarsRoverViewModel.KEY_ROVER to MarsRover.PERSEVERANCE.name,
                MarsRoverViewModel.KEY_CAMERA to "MCZ_LEFT"
            )
        )

        val viewModel = MarsRoverViewModel(observeMarsPhotos, savedStateHandle)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            MarsFeedFilter(rover = MarsRover.PERSEVERANCE, cameraApiName = "MCZ_LEFT"),
            viewModel.filter.value
        )
    }

    private fun testPhoto() = MarsPhoto(
        id = "img-1",
        rover = MarsRover.CURIOSITY,
        cameraName = "NAVCAM",
        cameraFullName = "Navigation Camera",
        imageUrl = "https://mars.nasa.gov/1.jpg",
        thumbnailUrl = "https://mars.nasa.gov/1_320.jpg",
        earthDate = LocalDate.of(2026, 1, 1),
        sol = 4000
    )
}
