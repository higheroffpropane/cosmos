package com.dskochilov.cosmos.core.database.di

import android.content.Context
import androidx.room.Room
import com.dskochilov.cosmos.core.database.CosmosDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun providesCosmosDatabase(@ApplicationContext context: Context): CosmosDatabase =
        Room.databaseBuilder(context, CosmosDatabase::class.java, "cosmos.db").build()
}
