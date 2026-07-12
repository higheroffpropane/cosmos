package com.dskochilov.cosmos.di

import android.content.Context
import coil3.SingletonImageLoader
import coil3.decode.BlackholeDecoder
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.dskochilov.cosmos.core.domain.favorites.ImagePrefetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
}
