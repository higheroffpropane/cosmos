package com.dskochilov.cosmos.core.domain.settings

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * User-facing app settings, persisted in DataStore.
 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val dailyApodNotificationEnabled: Boolean = false
)
