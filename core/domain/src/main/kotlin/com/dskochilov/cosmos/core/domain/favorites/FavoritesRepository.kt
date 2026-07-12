package com.dskochilov.cosmos.core.domain.favorites

import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    /** All favorites, newest first. Works fully offline. */
    fun observeFavorites(): Flow<List<Favorite>>

    fun observeIsFavorite(favoriteId: String): Flow<Boolean>

    /** Adds or removes the entry; adding also prefetches its image for offline viewing. */
    suspend fun toggleApod(apod: Apod)

    suspend fun toggleMarsPhoto(photo: MarsPhoto)

    suspend fun remove(favoriteId: String)
}
