package com.dskochilov.cosmos.core.domain.settings

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveUserPreferencesUseCase @Inject constructor(private val repository: UserPreferencesRepository) {
    operator fun invoke(): Flow<UserPreferences> = repository.userPreferences
}

class SetThemeModeUseCase @Inject constructor(private val repository: UserPreferencesRepository) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}

class SetDynamicColorUseCase @Inject constructor(private val repository: UserPreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setUseDynamicColor(enabled)
}

class SetDailyApodNotificationUseCase @Inject constructor(private val repository: UserPreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setDailyApodNotificationEnabled(enabled)
}
