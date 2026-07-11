package com.dskochilov.cosmos.core.data.search

import com.dskochilov.cosmos.core.network.images.NasaImagesItemDataDto
import com.dskochilov.cosmos.core.network.images.NasaImagesItemDto
import com.dskochilov.cosmos.core.network.images.NasaImagesLinkDto
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MediaItemMapperTest {

    @Test
    fun `maps a complete search hit`() {
        val dto = NasaImagesItemDto(
            data = listOf(
                NasaImagesItemDataDto(
                    nasaId = "as11-40-5874",
                    title = "Aldrin on the Moon",
                    description = "  Buzz Aldrin walks on the lunar surface.  ",
                    dateCreated = "1969-07-20T00:00:00Z",
                    center = "JSC"
                )
            ),
            links = listOf(
                NasaImagesLinkDto(href = "https://assets.nasa.gov/a thumb.jpg", rel = "preview", render = "image")
            )
        )

        val item = requireNotNull(dto.toDomain())

        assertEquals("as11-40-5874", item.nasaId)
        assertEquals("Aldrin on the Moon", item.title)
        assertEquals("Buzz Aldrin walks on the lunar surface.", item.description)
        assertEquals("JSC", item.center)
        assertEquals(LocalDate.of(1969, 7, 20), item.dateCreated)
        assertEquals("https://assets.nasa.gov/a%20thumb.jpg", item.thumbnailUrl)
    }

    @Test
    fun `falls back to any image link when preview is missing`() {
        val dto = NasaImagesItemDto(
            data = listOf(NasaImagesItemDataDto(nasaId = "x")),
            links = listOf(
                NasaImagesLinkDto(href = "https://assets.nasa.gov/alt.jpg", rel = "alternate", render = "image")
            )
        )

        assertEquals("https://assets.nasa.gov/alt.jpg", requireNotNull(dto.toDomain()).thumbnailUrl)
    }

    @Test
    fun `drops hits without metadata or without any image link`() {
        val noData = NasaImagesItemDto(
            data = emptyList(),
            links = listOf(NasaImagesLinkDto(href = "u", rel = "preview", render = "image"))
        )
        val noImage = NasaImagesItemDto(
            data = listOf(NasaImagesItemDataDto(nasaId = "x")),
            links = emptyList()
        )

        assertNull(noData.toDomain())
        assertNull(noImage.toDomain())
    }

    @Test
    fun `blank title falls back to nasa id and bad date becomes null`() {
        val dto = NasaImagesItemDto(
            data = listOf(
                NasaImagesItemDataDto(nasaId = "pia-1", title = " ", dateCreated = "not-a-date")
            ),
            links = listOf(NasaImagesLinkDto(href = "u.jpg", rel = "preview", render = "image"))
        )

        val item = requireNotNull(dto.toDomain())

        assertEquals("pia-1", item.title)
        assertNull(item.dateCreated)
    }
}
