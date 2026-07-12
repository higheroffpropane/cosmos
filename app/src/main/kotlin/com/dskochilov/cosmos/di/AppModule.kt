package com.dskochilov.cosmos.di

import android.content.Context
import coil3.SingletonImageLoader
import coil3.decode.BlackholeDecoder
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.dskochilov.cosmos.core.database.CosmosDatabase
import com.dskochilov.cosmos.core.domain.cache.CacheCleaner
import com.dskochilov.cosmos.core.domain.favorites.ImagePrefetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {

    /**
     * Downloads straight to Coil's disk cache: no memory cache entry, no decode.
     */
    @Provides
    @Singleton
    fun providesImagePrefetcher(@ApplicationContext context: Context): ImagePrefetcher = ImagePrefetcher { url ->
        val request = ImageRequest.Builder(context)
            .data(url)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .decoderFactory(BlackholeDecoder.Factory())
            .build()
        SingletonImageLoader.get(context).enqueue(request)
    }

    /**
     * Clears Coil's caches and the Mars photo cache. Favorited images may be
     * evicted too but are re-fetchable; favorites/search/settings data stay.
     */
    @Provides
    @Singleton
    fun providesCacheCleaner(@ApplicationContext context: Context, database: CosmosDatabase): CacheCleaner =
        object : CacheCleaner {
            override suspend fun clearCaches() {
                withContext(Dispatchers.IO) {
                    database.marsPhotoDao().clearAll()
                    database.marsRemoteKeyDao().clearAll()
                    val imageLoader = SingletonImageLoader.get(context)
                    imageLoader.memoryCache?.clear()
                    imageLoader.diskCache?.clear()
                }
            }
        }
}
