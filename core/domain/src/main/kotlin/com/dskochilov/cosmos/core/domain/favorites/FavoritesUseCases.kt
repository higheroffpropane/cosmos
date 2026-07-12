package com.dskochilov.cosmos.core.domain.favorites

import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveFavoritesUseCase @Inject constructor(private val repository: FavoritesRepository) {
    operator fun invoke(): Flow<List<Favorite>> = repository.observeFavorites()
}

class ObserveIsFavoriteUseCase @Inject constructor(private val repository: FavoritesRepository) {
    operator fun invoke(favoriteId: String): Flow<Boolean> = repository.observeIsFavorite(favoriteId)
}

class ToggleApodFavoriteUseCase @Inject constructor(private val repository: FavoritesRepository) {
    suspend operator fun invoke(apod: Apod) = repository.toggleApod(apod)
}

class ToggleMarsPhotoFavoriteUseCase @Inject constructor(private val repository: FavoritesRepository) {
    suspend operator fun invoke(photo: MarsPhoto) = repository.toggleMarsPhoto(photo)
}

class RemoveFavoriteUseCase @Inject constructor(private val repository: FavoritesRepository) {
    suspend operator fun invoke(favoriteId: String) = repository.remove(favoriteId)
}
