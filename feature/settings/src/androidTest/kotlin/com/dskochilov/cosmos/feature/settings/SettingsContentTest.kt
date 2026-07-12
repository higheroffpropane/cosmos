package com.dskochilov.cosmos.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dskochilov.cosmos.core.designsystem.theme.CosmosTheme
import com.dskochilov.cosmos.core.domain.settings.ThemeMode
import com.dskochilov.cosmos.core.domain.settings.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersAllSections() {
        setContent()

        composeTestRule.onNodeWithText("Theme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dynamic color").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daily astronomy picture").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear cache").assertIsDisplayed()
    }

    @Test
    fun selectingDarkTheme_emitsIntent() {
        var selected: ThemeMode? = null
        setContent(onIntent = { intent ->
            if (intent is SettingsIntent.SetThemeMode) selected = intent.mode
        })

        composeTestRule.onNodeWithText("Dark").performClick()

        assertEquals(ThemeMode.DARK, selected)
    }

    @Test
    fun togglingDailyNotification_callsToggleCallbackWithTrue() {
        var toggled: Boolean? = null
        setContent(onDailyNotificationToggle = { toggled = it })

        // Two switches on screen: index 0 is dynamic color, index 1 is the daily notification.
        composeTestRule.onAllNodes(isToggleable())[1].performClick()

        assertEquals(true, toggled)
    }

    private fun setContent(onIntent: (SettingsIntent) -> Unit = {}, onDailyNotificationToggle: (Boolean) -> Unit = {}) {
        composeTestRule.setContent {
            CosmosTheme {
                SettingsContent(
                    state = SettingsUiState(UserPreferences()),
                    onIntent = onIntent,
                    onDailyNotificationToggle = onDailyNotificationToggle
                )
            }
        }
    }
}
