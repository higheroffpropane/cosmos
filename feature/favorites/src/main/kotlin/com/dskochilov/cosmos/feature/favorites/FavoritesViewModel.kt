package com.dskochilov.cosmos.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dskochilov.cosmos.core.domain.favorites.ObserveFavoritesUseCase
import com.dskochilov.cosmos.core.domain.favorites.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeFavorites: ObserveFavoritesUseCase,
    private val removeFavorite: RemoveFavoriteUseCase
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = observeFavorites()
        .map { favorites ->
            if (favorites.isEmpty()) FavoritesUiState.Empty else FavoritesUiState.Content(favorites)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FavoritesUiState.Loading)

    fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.Remove -> viewModelScope.launch { removeFavorite(intent.favoriteId) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
