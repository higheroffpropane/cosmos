package com.dskochilov.cosmos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dskochilov.cosmos.core.database.dao.MarsPhotoDao
import com.dskochilov.cosmos.core.database.dao.MarsRemoteKeyDao
import com.dskochilov.cosmos.core.database.entity.MarsPhotoEntity
import com.dskochilov.cosmos.core.database.entity.MarsRemoteKeyEntity

@Database(
    entities = [
        MarsPhotoEntity::class,
        MarsRemoteKeyEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class CosmosDatabase : RoomDatabase() {
    abstract fun marsPhotoDao(): MarsPhotoDao

    abstract fun marsRemoteKeyDao(): MarsRemoteKeyDao
}
