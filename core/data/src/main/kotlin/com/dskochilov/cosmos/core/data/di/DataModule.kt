package com.dskochilov.cosmos.core.data.di

import com.dskochilov.cosmos.core.data.apod.ApodRepositoryImpl
import com.dskochilov.cosmos.core.data.mars.MarsPhotoRepositoryImpl
import com.dskochilov.cosmos.core.data.search.MediaSearchRepositoryImpl
import com.dskochilov.cosmos.core.domain.apod.ApodRepository
import com.dskochilov.cosmos.core.domain.mars.MarsPhotoRepository
import com.dskochilov.cosmos.core.domain.search.MediaSearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindsApodRepository(impl: ApodRepositoryImpl): ApodRepository

    @Binds
    @Singleton
    abstract fun bindsMarsPhotoRepository(impl: MarsPhotoRepositoryImpl): MarsPhotoRepository

    @Binds
    @Singleton
    abstract fun bindsMediaSearchRepository(impl: MediaSearchRepositoryImpl): MediaSearchRepository
}
