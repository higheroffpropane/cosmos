package com.dskochilov.cosmos.core.data.di

import com.dskochilov.cosmos.core.data.apod.ApodRepositoryImpl
import com.dskochilov.cosmos.core.domain.apod.ApodRepository
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
}
