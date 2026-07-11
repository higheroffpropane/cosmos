package com.dskochilov.cosmos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dskochilov.cosmos.core.database.entity.MarsRemoteKeyEntity

@Dao
interface MarsRemoteKeyDao {

    @Query("SELECT * FROM mars_remote_keys WHERE queryKey = :queryKey")
    suspend fun get(queryKey: String): MarsRemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(key: MarsRemoteKeyEntity)

    @Query("DELETE FROM mars_remote_keys WHERE queryKey = :queryKey")
    suspend fun delete(queryKey: String)
}
