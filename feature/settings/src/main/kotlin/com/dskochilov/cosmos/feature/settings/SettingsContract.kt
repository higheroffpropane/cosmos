package com.dskochilov.cosmos.feature.settings

import com.dskochilov.cosmos.core.domain.settings.ThemeMode
import com.dskochilov.cosmos.core.domain.settings.UserPreferences

/**
 * MVI contract of the settings screen.
 */
data class SettingsUiState(val preferences: UserPreferences = UserPreferences())

sealed interface SettingsIntent {
    data class SetThemeMode(val mode: ThemeMode) : SettingsIntent

    data class SetDynamicColor(val enabled: Boolean) : SettingsIntent

    /** Sent after the permission flow resolved; [enabled] is the final decision. */
    data class SetDailyNotification(val enabled: Boolean) : SettingsIntent

    data object ClearCache : SettingsIntent
}

sealed interface SettingsEffect {
    data object CacheCleared : SettingsEffect
}
