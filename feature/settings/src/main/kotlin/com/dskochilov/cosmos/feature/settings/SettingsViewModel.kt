package com.dskochilov.cosmos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dskochilov.cosmos.core.domain.cache.ClearCachesUseCase
import com.dskochilov.cosmos.core.domain.notifications.DailyApodNotificationScheduler
import com.dskochilov.cosmos.core.domain.settings.ObserveUserPreferencesUseCase
import com.dskochilov.cosmos.core.domain.settings.SetDailyApodNotificationUseCase
import com.dskochilov.cosmos.core.domain.settings.SetDynamicColorUseCase
import com.dskochilov.cosmos.core.domain.settings.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeUserPreferences: ObserveUserPreferencesUseCase,
    private val setThemeMode: SetThemeModeUseCase,
    private val setDynamicColor: SetDynamicColorUseCase,
    private val setDailyApodNotification: SetDailyApodNotificationUseCase,
    private val notificationScheduler: DailyApodNotificationScheduler,
    private val clearCaches: ClearCachesUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = observeUserPreferences()
        .map { SettingsUiState(preferences = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SettingsUiState())

    private val _effects = Channel<SettingsEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<SettingsEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetThemeMode -> viewModelScope.launch { setThemeMode(intent.mode) }

            is SettingsIntent.SetDynamicColor -> viewModelScope.launch { setDynamicColor(intent.enabled) }

            is SettingsIntent.SetDailyNotification -> viewModelScope.launch {
                setDailyApodNotification(intent.enabled)
                if (intent.enabled) {
                    notificationScheduler.schedule()
                } else {
                    notificationScheduler.cancel()
                }
            }

            SettingsIntent.ClearCache -> viewModelScope.launch {
                clearCaches()
                _effects.send(SettingsEffect.CacheCleared)
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
