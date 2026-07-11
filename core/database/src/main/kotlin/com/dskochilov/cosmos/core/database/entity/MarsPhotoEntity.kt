package com.dskochilov.cosmos.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached Mars Rover photo. `rover` is the query the photo was fetched for;
 * camera filtering happens locally on `cameraName` (instrument id).
 * The primary key is the feed's string `imageid`.
 */
@Entity(
    tableName = "mars_photos",
    indices = [Index(value = ["rover", "cameraName"])]
)
data class MarsPhotoEntity(
    @PrimaryKey val id: String,
    val rover: String,
    val cameraName: String,
    val cameraFullName: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val earthDate: String,
    val sol: Int
)
