package com.dskochilov.cosmos.feature.settings

import app.cash.turbine.test
import com.dskochilov.cosmos.core.domain.cache.CacheCleaner
import com.dskochilov.cosmos.core.domain.cache.ClearCachesUseCase
import com.dskochilov.cosmos.core.domain.notifications.DailyApodNotificationScheduler
import com.dskochilov.cosmos.core.domain.settings.ObserveUserPreferencesUseCase
import com.dskochilov.cosmos.core.domain.settings.SetDailyApodNotificationUseCase
import com.dskochilov.cosmos.core.domain.settings.SetDynamicColorUseCase
import com.dskochilov.cosmos.core.domain.settings.SetThemeModeUseCase
import com.dskochilov.cosmos.core.domain.settings.ThemeMode
import com.dskochilov.cosmos.core.domain.settings.UserPreferences
import com.dskochilov.cosmos.core.domain.settings.UserPreferencesRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val repository = mockk<UserPreferencesRepository> {
        every { userPreferences } returns flowOf(UserPreferences(themeMode = ThemeMode.DARK))
        coEvery { setThemeMode(any()) } just Runs
        coEvery { setUseDynamicColor(any()) } just Runs
        coEvery { setDailyApodNotificationEnabled(any()) } just Runs
    }
    private val scheduler = mockk<DailyApodNotificationScheduler>(relaxed = true)
    private val cacheCleaner = mockk<CacheCleaner> {
        coEvery { clearCaches() } just Runs
    }
    private val dispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state reflects stored preferences`() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(SettingsUiState(), awaitItem())
            assertEquals(SettingsUiState(UserPreferences(themeMode = ThemeMode.DARK)), awaitItem())
        }
    }

    @Test
    fun `theme and dynamic color intents persist`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.SetThemeMode(ThemeMode.LIGHT))
        viewModel.onIntent(SettingsIntent.SetDynamicColor(false))
        runCurrent()

        coVerify(exactly = 1) { repository.setThemeMode(ThemeMode.LIGHT) }
        coVerify(exactly = 1) { repository.setUseDynamicColor(false) }
    }

    @Test
    fun `enabling daily notification persists and schedules`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.SetDailyNotification(true))
        runCurrent()

        coVerify(exactly = 1) { repository.setDailyApodNotificationEnabled(true) }
        verify(exactly = 1) { scheduler.schedule() }
        verify(exactly = 0) { scheduler.cancel() }
    }

    @Test
    fun `disabling daily notification persists and cancels`() = runTest {
        val viewModel = viewModel()

        viewModel.onIntent(SettingsIntent.SetDailyNotification(false))
        runCurrent()

        coVerify(exactly = 1) { repository.setDailyApodNotificationEnabled(false) }
        verify(exactly = 1) { scheduler.cancel() }
        verify(exactly = 0) { scheduler.schedule() }
    }

    @Test
    fun `clear cache clears and emits the effect`() = runTest {
        val viewModel = viewModel()

        viewModel.effects.test {
            viewModel.onIntent(SettingsIntent.ClearCache)
            assertEquals(SettingsEffect.CacheCleared, awaitItem())
        }
        coVerify(exactly = 1) { cacheCleaner.clearCaches() }
    }

    private fun viewModel() = SettingsViewModel(
        observeUserPreferences = ObserveUserPreferencesUseCase(repository),
        setThemeMode = SetThemeModeUseCase(repository),
        setDynamicColor = SetDynamicColorUseCase(repository),
        setDailyApodNotification = SetDailyApodNotificationUseCase(repository),
        notificationScheduler = scheduler,
        clearCaches = ClearCachesUseCase(cacheCleaner)
    )
}
