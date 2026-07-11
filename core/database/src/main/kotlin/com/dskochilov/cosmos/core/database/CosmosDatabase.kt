package com.dskochilov.cosmos.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.dskochilov.cosmos.core.database.dao.MarsPhotoDao
import com.dskochilov.cosmos.core.database.dao.MarsRemoteKeyDao
import com.dskochilov.cosmos.core.database.dao.SearchHistoryDao
import com.dskochilov.cosmos.core.database.entity.MarsPhotoEntity
import com.dskochilov.cosmos.core.database.entity.MarsRemoteKeyEntity
import com.dskochilov.cosmos.core.database.entity.SearchHistoryEntity

@Database(
    entities = [
        MarsPhotoEntity::class,
        MarsRemoteKeyEntity::class,
        SearchHistoryEntity::class
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
abstract class CosmosDatabase : RoomDatabase() {
    abstract fun marsPhotoDao(): MarsPhotoDao

    abstract fun marsRemoteKeyDao(): MarsRemoteKeyDao

    abstract fun searchHistoryDao(): SearchHistoryDao
}
