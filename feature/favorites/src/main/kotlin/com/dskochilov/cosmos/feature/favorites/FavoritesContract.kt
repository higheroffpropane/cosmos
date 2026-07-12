package com.dskochilov.cosmos.feature.favorites

import com.dskochilov.cosmos.core.domain.favorites.Favorite

/**
 * MVI contract of the favorites screen.
 */
sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState

    data object Empty : FavoritesUiState

    data class Content(val favorites: List<Favorite>) : FavoritesUiState
}

sealed interface FavoritesIntent {
    data class Remove(val favoriteId: String) : FavoritesIntent
}
