package com.dskochilov.cosmos.feature.marsrover

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.common.result.AppErrorException
import com.dskochilov.cosmos.core.designsystem.theme.CosmosTheme
import com.dskochilov.cosmos.core.domain.mars.MarsFeedFilter
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import java.time.LocalDate
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarsRoverContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val loadedStates = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = false),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true)
    )

    @Test
    fun errorState_showsSharedErrorMessage_andRetryIsClickable() {
        val errorStates = LoadStates(
            refresh = LoadState.Error(AppErrorException(AppError.NoConnection)),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true)
        )
        composeTestRule.setContent {
            CosmosTheme {
                MarsRoverContent(
                    filter = MarsFeedFilter(),
                    photos = flowOf(
                        PagingData.from(emptyList<MarsPhoto>(), sourceLoadStates = errorStates)
                    ).collectAsLazyPagingItems(),
                    onIntent = {},
                    onPhotoClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No internet connection", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun contentState_showsRoverAndCameraFilters() {
        composeTestRule.setContent {
            CosmosTheme {
                MarsRoverContent(
                    filter = MarsFeedFilter(rover = MarsRover.CURIOSITY),
                    photos = flowOf(
                        PagingData.from(listOf(testPhoto()), sourceLoadStates = loadedStates)
                    ).collectAsLazyPagingItems(),
                    onIntent = {},
                    onPhotoClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Curiosity").assertIsDisplayed()
        composeTestRule.onNodeWithText("Perseverance").assertIsDisplayed()
        composeTestRule.onNodeWithText("All cameras").assertIsDisplayed()
    }

    @Test
    fun selectingRover_emitsSelectRoverIntent() {
        var selected: MarsRover? = null
        composeTestRule.setContent {
            CosmosTheme {
                MarsRoverContent(
                    filter = MarsFeedFilter(rover = MarsRover.CURIOSITY),
                    photos = flowOf(
                        PagingData.from(listOf(testPhoto()), sourceLoadStates = loadedStates)
                    ).collectAsLazyPagingItems(),
                    onIntent = { intent ->
                        if (intent is MarsRoverIntent.SelectRover) selected = intent.rover
                    },
                    onPhotoClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Perseverance").performClick()

        assertTrue(selected == MarsRover.PERSEVERANCE)
    }

    private fun testPhoto() = MarsPhoto(
        id = 1,
        rover = MarsRover.CURIOSITY,
        cameraName = "NAVCAM",
        cameraFullName = "Navigation Camera",
        imageUrl = "https://mars.nasa.gov/1.jpg",
        earthDate = LocalDate.of(2026, 1, 1),
        sol = 4000
    )
}
