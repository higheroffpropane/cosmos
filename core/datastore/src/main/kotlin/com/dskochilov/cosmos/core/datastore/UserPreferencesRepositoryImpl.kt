package com.dskochilov.cosmos.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dskochilov.cosmos.core.domain.settings.ThemeMode
import com.dskochilov.cosmos.core.domain.settings.UserPreferences
import com.dskochilov.cosmos.core.domain.settings.UserPreferencesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class UserPreferencesRepositoryImpl @Inject constructor(private val dataStore: DataStore<Preferences>) :
    UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            themeMode = preferences[Keys.THEME_MODE]
                ?.let { stored -> runCatching { ThemeMode.valueOf(stored) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            useDynamicColor = preferences[Keys.DYNAMIC_COLOR] ?: true,
            dailyApodNotificationEnabled = preferences[Keys.DAILY_APOD_NOTIFICATION] ?: false
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setUseDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    override suspend fun setDailyApodNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DAILY_APOD_NOTIFICATION] = enabled }
    }

    internal object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DAILY_APOD_NOTIFICATION = booleanPreferencesKey("daily_apod_notification")
    }
}
