package com.dskochilov.cosmos.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.dskochilov.cosmos.core.datastore.UserPreferencesRepositoryImpl
import com.dskochilov.cosmos.core.domain.settings.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.userPreferencesStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreProviderModule {

    @Provides
    @Singleton
    fun providesUserPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.userPreferencesStore
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataStoreBindingModule {

    @Binds
    @Singleton
    abstract fun bindsUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
