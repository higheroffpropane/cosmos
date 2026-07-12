package com.dskochilov.cosmos.core.data.favorites

import com.dskochilov.cosmos.core.database.entity.FavoriteEntity
import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.domain.favorites.Favorite
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FavoriteMapperTest {

    @Test
    fun `image apod round-trips through the entity`() {
        val apod = Apod(
            date = LocalDate.of(2026, 7, 11),
            title = "Messier 24",
            explanation = "A star cloud.",
            copyright = "Chuck Ayoub",
            media = ApodMedia.Image(url = "https://apod.nasa.gov/m24.jpg", hdUrl = "https://apod.nasa.gov/hd.jpg")
        )

        val entity = apod.toFavoriteEntity(savedAt = 42)
        val restored = entity.toDomain() as Favorite.ApodEntry

        assertEquals("apod:2026-07-11", entity.id)
        assertEquals(42, restored.savedAt)
        assertEquals(apod.date, restored.apod.date)
        assertEquals(apod.title, restored.apod.title)
        assertEquals(apod.explanation, restored.apod.explanation)
        assertEquals(apod.copyright, restored.apod.copyright)
        // The HD variant is intentionally not persisted; the standard url must survive.
        assertEquals(ApodMedia.Image(url = "https://apod.nasa.gov/m24.jpg", hdUrl = null), restored.apod.media)
    }

    @Test
    fun `video apod keeps its video url and thumbnail`() {
        val apod = Apod(
            date = LocalDate.of(2026, 7, 1),
            title = "Video day",
            explanation = "x",
            copyright = null,
            media = ApodMedia.Video(videoUrl = "https://youtube.com/w?v=1", thumbnailUrl = "https://img.y/1.jpg")
        )

        val restored = apod.toFavoriteEntity(savedAt = 1).toDomain() as Favorite.ApodEntry

        assertEquals(
            ApodMedia.Video(videoUrl = "https://youtube.com/w?v=1", thumbnailUrl = "https://img.y/1.jpg"),
            restored.apod.media
        )
    }

    @Test
    fun `mars photo round-trips through the entity`() {
        val photo = MarsPhoto(
            id = "NLG_1915",
            rover = MarsRover.PERSEVERANCE,
            cameraName = "NAVCAM_LEFT",
            cameraFullName = "Navcam Left",
            imageUrl = "https://mars.nasa.gov/l.jpg",
            thumbnailUrl = "https://mars.nasa.gov/m.jpg",
            earthDate = LocalDate.of(2026, 7, 10),
            sol = 1915
        )

        val entity = photo.toFavoriteEntity(savedAt = 7)
        val restored = entity.toDomain() as Favorite.MarsPhotoEntry

        assertEquals("mars:NLG_1915", entity.id)
        assertEquals(photo, restored.photo)
    }

    @Test
    fun `unknown row type maps to null instead of crashing`() {
        val entity = FavoriteEntity(
            id = "x",
            type = "SOMETHING_NEW",
            title = "t",
            imageUrl = "",
            thumbnailUrl = "",
            savedAt = 1
        )

        assertNull(entity.toDomain())
    }
}
