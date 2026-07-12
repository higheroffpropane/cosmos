package com.dskochilov.cosmos.feature.favorites

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dskochilov.cosmos.core.designsystem.theme.CosmosTheme
import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.domain.favorites.Favorite
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_showsHint() {
        composeTestRule.setContent {
            CosmosTheme {
                FavoritesContent(state = FavoritesUiState.Empty, onIntent = {})
            }
        }

        composeTestRule.onNodeWithText("Nothing saved yet", substring = true).assertIsDisplayed()
    }

    @Test
    fun content_showsFavorite_andRemoveEmitsIntent() {
        var removedId: String? = null
        composeTestRule.setContent {
            CosmosTheme {
                FavoritesContent(
                    state = FavoritesUiState.Content(listOf(apodFavorite())),
                    onIntent = { intent ->
                        if (intent is FavoritesIntent.Remove) removedId = intent.favoriteId
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Messier 24").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Remove from favorites").performClick()

        assertEquals("apod:2026-07-11", removedId)
    }

    private fun apodFavorite() = Favorite.ApodEntry(
        apod = Apod(
            date = LocalDate.of(2026, 7, 11),
            title = "Messier 24",
            explanation = "x",
            copyright = null,
            // No image URL so the UI test never touches the network.
            media = ApodMedia.Unsupported
        ),
        savedAt = 1
    )
}
