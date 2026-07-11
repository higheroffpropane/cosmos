package com.dskochilov.cosmos.feature.search

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
import com.dskochilov.cosmos.core.domain.search.MediaItem
import java.time.LocalDate
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val loadedStates = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = false),
        prepend = LoadState.NotLoading(endOfPaginationReached = true),
        append = LoadState.NotLoading(endOfPaginationReached = true)
    )

    @Test
    fun blankQuery_showsHistory_andSelectingAnEntryEmitsIntent() {
        var selected: String? = null
        composeTestRule.setContent {
            CosmosTheme {
                SearchContent(
                    query = "",
                    history = listOf("apollo 11", "nebula"),
                    results = flowOf(PagingData.empty<MediaItem>()).collectAsLazyPagingItems(),
                    onIntent = { intent ->
                        if (intent is SearchIntent.HistorySelected) selected = intent.query
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Recent searches").assertIsDisplayed()
        composeTestRule.onNodeWithText("apollo 11").performClick()

        assertEquals("apollo 11", selected)
    }

    @Test
    fun activeQuery_withResults_showsTheGrid() {
        composeTestRule.setContent {
            CosmosTheme {
                SearchContent(
                    query = "apollo",
                    history = emptyList(),
                    results = flowOf(
                        PagingData.from(listOf(testItem()), sourceLoadStates = loadedStates)
                    ).collectAsLazyPagingItems(),
                    onIntent = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Aldrin on the Moon").assertIsDisplayed()
    }

    @Test
    fun activeQuery_withError_showsRetryableErrorState() {
        val errorStates = LoadStates(
            refresh = LoadState.Error(AppErrorException(AppError.RateLimited)),
            prepend = LoadState.NotLoading(endOfPaginationReached = true),
            append = LoadState.NotLoading(endOfPaginationReached = true)
        )
        composeTestRule.setContent {
            CosmosTheme {
                SearchContent(
                    query = "apollo",
                    history = emptyList(),
                    results = flowOf(
                        PagingData.from(emptyList<MediaItem>(), sourceLoadStates = errorStates)
                    ).collectAsLazyPagingItems(),
                    onIntent = {}
                )
            }
        }

        composeTestRule.onNodeWithText("rate limit", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    private fun testItem() = MediaItem(
        nasaId = "as11-40-5874",
        title = "Aldrin on the Moon",
        description = "Buzz Aldrin walks on the lunar surface.",
        center = "JSC",
        dateCreated = LocalDate.of(1969, 7, 20),
        thumbnailUrl = "https://assets.nasa.gov/thumb.jpg"
    )
}
