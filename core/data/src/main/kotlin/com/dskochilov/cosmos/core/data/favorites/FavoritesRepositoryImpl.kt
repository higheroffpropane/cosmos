package com.dskochilov.cosmos.core.data.favorites

import com.dskochilov.cosmos.core.common.coroutines.CosmosDispatchers
import com.dskochilov.cosmos.core.common.coroutines.Dispatcher
import com.dskochilov.cosmos.core.database.CosmosDatabase
import com.dskochilov.cosmos.core.database.entity.FavoriteEntity
import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.favorites.Favorite
import com.dskochilov.cosmos.core.domain.favorites.FavoritesRepository
import com.dskochilov.cosmos.core.domain.favorites.ImagePrefetcher
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class FavoritesRepositoryImpl @Inject constructor(
    private val database: CosmosDatabase,
    private val imagePrefetcher: ImagePrefetcher,
    @param:Dispatcher(CosmosDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : FavoritesRepository {

    private val dao get() = database.favoriteDao()

    override fun observeFavorites(): Flow<List<Favorite>> =
        dao.observeAll().map { entities -> entities.mapNotNull { it.toDomain() } }

    override fun observeIsFavorite(favoriteId: String): Flow<Boolean> = dao.observeExists(favoriteId)

    override suspend fun toggleApod(apod: Apod) = toggle(apod.toFavoriteEntity(savedAt = System.currentTimeMillis()))

    override suspend fun toggleMarsPhoto(photo: MarsPhoto) =
        toggle(photo.toFavoriteEntity(savedAt = System.currentTimeMillis()))

    override suspend fun remove(favoriteId: String) = withContext(ioDispatcher) {
        dao.deleteById(favoriteId)
    }

    private suspend fun toggle(entity: FavoriteEntity) = withContext(ioDispatcher) {
        if (dao.exists(entity.id)) {
            dao.deleteById(entity.id)
        } else {
            dao.insert(entity)
            // Warm the disk cache so the favorite stays viewable offline.
            setOf(entity.imageUrl, entity.thumbnailUrl)
                .filter { it.isNotEmpty() }
                .forEach(imagePrefetcher::prefetch)
        }
    }
}
