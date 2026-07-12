package com.dskochilov.cosmos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dskochilov.cosmos.core.domain.settings.ObserveUserPreferencesUseCase
import com.dskochilov.cosmos.core.domain.settings.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(observeUserPreferences: ObserveUserPreferencesUseCase) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = observeUserPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UserPreferences())

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
