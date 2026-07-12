package com.dskochilov.cosmos.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import com.dskochilov.cosmos.core.domain.settings.ThemeMode
import com.dskochilov.cosmos.core.domain.settings.UserPreferences
import java.io.File
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserPreferencesRepositoryImplTest {

    @Test
    fun `defaults are system theme, dynamic color on, notifications off`() = runTest {
        withRepository { repository, _ ->
            assertEquals(UserPreferences(), repository.userPreferences.first())
        }
    }

    @Test
    fun `set values round-trip through the store`() = runTest {
        withRepository { repository, _ ->
            repository.setThemeMode(ThemeMode.DARK)
            repository.setUseDynamicColor(false)
            repository.setDailyApodNotificationEnabled(true)

            assertEquals(
                UserPreferences(
                    themeMode = ThemeMode.DARK,
                    useDynamicColor = false,
                    dailyApodNotificationEnabled = true
                ),
                repository.userPreferences.first()
            )
        }
    }

    @Test
    fun `corrupted theme value falls back to system`() = runTest {
        withRepository { repository, dataStore ->
            dataStore.edit { it[UserPreferencesRepositoryImpl.Keys.THEME_MODE] = "NOT_A_MODE" }

            assertEquals(ThemeMode.SYSTEM, repository.userPreferences.first().themeMode)
        }
    }

    private suspend fun TestScope.withRepository(
        block: suspend (
            UserPreferencesRepositoryImpl,
            androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
        ) -> Unit
    ) {
        val storeScope = CoroutineScope(StandardTestDispatcher(testScheduler) + Job())
        val dataStore = PreferenceDataStoreFactory.create(scope = storeScope) {
            File(System.getProperty("java.io.tmpdir"), "prefs-test-${Random.nextLong()}.preferences_pb")
        }
        try {
            block(UserPreferencesRepositoryImpl(dataStore), dataStore)
        } finally {
            storeScope.cancel()
        }
    }
}
