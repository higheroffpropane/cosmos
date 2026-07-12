package com.dskochilov.cosmos.core.domain.settings

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setUseDynamicColor(enabled: Boolean)

    suspend fun setDailyApodNotificationEnabled(enabled: Boolean)
}
