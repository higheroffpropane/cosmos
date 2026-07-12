package com.dskochilov.cosmos.feature.apod

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.designsystem.theme.CosmosTheme
import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApodContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsProgressIndicator() {
        composeTestRule.setContent {
            CosmosTheme {
                ApodContent(state = ApodUiState.Loading, isFavorite = false, onIntent = {})
            }
        }

        composeTestRule
            .onNode(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo.Indeterminate
                )
            )
            .assertIsDisplayed()
    }

    @Test
    fun errorState_showsMessage_andRetryEmitsIntent() {
        var retried = false
        composeTestRule.setContent {
            CosmosTheme {
                ApodContent(
                    state = ApodUiState.Error(AppError.NoConnection),
                    isFavorite = false,
                    onIntent = { intent -> retried = retried || intent == ApodIntent.Retry }
                )
            }
        }

        composeTestRule.onNodeWithText("No internet connection", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").performClick()

        assertTrue(retried)
    }

    @Test
    fun contentState_showsTitleAndExplanation() {
        composeTestRule.setContent {
            CosmosTheme {
                ApodContent(
                    state = ApodUiState.Content(
                        Apod(
                            date = LocalDate.of(2026, 7, 7),
                            title = "Pillars of Creation",
                            explanation = "A famous nebula.",
                            copyright = "NASA",
                            // Unsupported media keeps the test hermetic: no Coil network fetch.
                            media = ApodMedia.Unsupported
                        )
                    ),
                    isFavorite = false,
                    onIntent = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Pillars of Creation").assertIsDisplayed()
        composeTestRule.onNodeWithText("A famous nebula.").assertIsDisplayed()
    }
}
