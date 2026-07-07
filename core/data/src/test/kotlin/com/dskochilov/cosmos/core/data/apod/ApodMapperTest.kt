package com.dskochilov.cosmos.core.data.apod

import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.network.apod.ApodDto
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ApodMapperTest {

    @Test
    fun `maps image dto to Image media with hd url`() {
        val dto = testDto(
            mediaType = "image",
            url = "https://apod.nasa.gov/image.jpg",
            hdUrl = "https://apod.nasa.gov/image_hd.jpg"
        )

        val apod = dto.toDomain()

        assertEquals(LocalDate.of(2026, 7, 7), apod.date)
        assertEquals("Title", apod.title)
        assertEquals("Explanation", apod.explanation)
        assertEquals(
            ApodMedia.Image(url = "https://apod.nasa.gov/image.jpg", hdUrl = "https://apod.nasa.gov/image_hd.jpg"),
            apod.media
        )
    }

    @Test
    fun `maps video dto to Video media with thumbnail`() {
        val dto = testDto(
            mediaType = "video",
            url = "https://youtube.com/watch?v=x",
            thumbnailUrl = "https://img.youtube.com/x.jpg"
        )

        val apod = dto.toDomain()

        assertEquals(
            ApodMedia.Video(videoUrl = "https://youtube.com/watch?v=x", thumbnailUrl = "https://img.youtube.com/x.jpg"),
            apod.media
        )
    }

    @Test
    fun `maps unknown media type to Unsupported`() {
        val dto = testDto(mediaType = "other", url = null)

        assertEquals(ApodMedia.Unsupported, dto.toDomain().media)
    }

    @Test
    fun `maps image without url to Unsupported instead of crashing`() {
        val dto = testDto(mediaType = "image", url = null)

        assertEquals(ApodMedia.Unsupported, dto.toDomain().media)
    }

    @Test
    fun `trims copyright and keeps null when absent`() {
        val withCopyright = testDto(mediaType = "image", url = "u", copyright = "\nJane Doe\n")
        val withoutCopyright = testDto(mediaType = "image", url = "u", copyright = null)

        assertEquals("Jane Doe", withCopyright.toDomain().copyright)
        assertNull(withoutCopyright.toDomain().copyright)
    }

    private fun testDto(
        mediaType: String,
        url: String?,
        hdUrl: String? = null,
        thumbnailUrl: String? = null,
        copyright: String? = null
    ) = ApodDto(
        date = "2026-07-07",
        title = "Title",
        explanation = "Explanation",
        mediaType = mediaType,
        url = url,
        hdUrl = hdUrl,
        thumbnailUrl = thumbnailUrl,
        copyright = copyright
    )
}
