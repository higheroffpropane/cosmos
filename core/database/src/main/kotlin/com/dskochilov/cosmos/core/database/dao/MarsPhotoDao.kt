package com.dskochilov.cosmos.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dskochilov.cosmos.core.database.entity.MarsPhotoEntity

@Dao
interface MarsPhotoDao {

    @Query(
        """
        SELECT * FROM mars_photos
        WHERE rover = :rover AND (:camera IS NULL OR cameraName = :camera)
        ORDER BY sol DESC, id ASC
        """
    )
    fun pagingSource(rover: String, camera: String?): PagingSource<Int, MarsPhotoEntity>

    @Query("SELECT * FROM mars_photos WHERE id = :id")
    suspend fun getById(id: Long): MarsPhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<MarsPhotoEntity>)

    @Query("DELETE FROM mars_photos WHERE rover = :rover")
    suspend fun clearForRover(rover: String)
}
