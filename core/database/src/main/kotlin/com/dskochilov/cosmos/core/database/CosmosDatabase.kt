package com.dskochilov.cosmos.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.dskochilov.cosmos.core.database.dao.FavoriteDao
import com.dskochilov.cosmos.core.database.dao.MarsPhotoDao
import com.dskochilov.cosmos.core.database.dao.MarsRemoteKeyDao
import com.dskochilov.cosmos.core.database.dao.SearchHistoryDao
import com.dskochilov.cosmos.core.database.entity.FavoriteEntity
import com.dskochilov.cosmos.core.database.entity.MarsPhotoEntity
import com.dskochilov.cosmos.core.database.entity.MarsRemoteKeyEntity
import com.dskochilov.cosmos.core.database.entity.SearchHistoryEntity

@Database(
    entities = [
        MarsPhotoEntity::class,
        MarsRemoteKeyEntity::class,
        SearchHistoryEntity::class,
        FavoriteEntity::class
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ],
    exportSchema = true
)
abstract class CosmosDatabase : RoomDatabase() {
    abstract fun marsPhotoDao(): MarsPhotoDao

    abstract fun marsRemoteKeyDao(): MarsRemoteKeyDao

    abstract fun searchHistoryDao(): SearchHistoryDao

    abstract fun favoriteDao(): FavoriteDao
}
