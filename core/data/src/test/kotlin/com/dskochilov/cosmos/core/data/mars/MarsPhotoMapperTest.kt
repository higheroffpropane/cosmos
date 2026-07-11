package com.dskochilov.cosmos.core.data.mars

import com.dskochilov.cosmos.core.database.entity.MarsPhotoEntity
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import com.dskochilov.cosmos.core.network.mars.CuriosityImageDto
import com.dskochilov.cosmos.core.network.mars.PerseveranceCameraDto
import com.dskochilov.cosmos.core.network.mars.PerseveranceImageDto
import com.dskochilov.cosmos.core.network.mars.PerseveranceImageFilesDto
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MarsPhotoMapperTest {

    @Test
    fun `perseverance dto maps sizes, display name and truncated date`() {
        val dto = PerseveranceImageDto(
            imageId = "NLG_1915_0836950452",
            sol = 1915,
            camera = PerseveranceCameraDto(instrument = "NAVCAM_LEFT"),
            imageFiles = PerseveranceImageFilesDto(
                small = "https://m.nasa.gov/s.jpg",
                medium = "https://m.nasa.gov/m.jpg",
                large = "https://m.nasa.gov/l.jpg",
                fullRes = "https://m.nasa.gov/f.png"
            ),
            dateTakenUtc = "2026-07-10T10:38:12.273"
        )

        val entity = dto.toEntity()

        assertEquals("NLG_1915_0836950452", entity.id)
        assertEquals("perseverance", entity.rover)
        assertEquals("NAVCAM_LEFT", entity.cameraName)
        assertEquals("Navcam Left", entity.cameraFullName)
        assertEquals("https://m.nasa.gov/l.jpg", entity.imageUrl)
        assertEquals("https://m.nasa.gov/m.jpg", entity.thumbnailUrl)
        assertEquals("2026-07-10", entity.earthDate)
        assertEquals(1915, entity.sol)
    }

    @Test
    fun `perseverance dto falls back through image sizes`() {
        val dto = PerseveranceImageDto(
            imageId = "X",
            sol = 1,
            camera = PerseveranceCameraDto(instrument = "MCZ_LEFT"),
            imageFiles = PerseveranceImageFilesDto(small = "https://m.nasa.gov/s.jpg"),
            dateTakenUtc = null
        )

        val entity = dto.toEntity()

        assertEquals("https://m.nasa.gov/s.jpg", entity.imageUrl)
        assertEquals("https://m.nasa.gov/s.jpg", entity.thumbnailUrl)
        assertEquals("", entity.earthDate)
    }

    @Test
    fun `curiosity dto maps https url and instrument display name`() {
        val dto = CuriosityImageDto(
            imageId = "CR0_837010829EDR",
            sol = 4951,
            instrument = "CHEMCAM_RMI",
            url = "http://m.nasa.gov/c.jpg",
            httpsUrl = "https://m.nasa.gov/c.jpg",
            dateTaken = "2026-07-11T04:11:48.000Z"
        )

        val entity = dto.toEntity()

        assertEquals("CR0_837010829EDR", entity.id)
        assertEquals("curiosity", entity.rover)
        assertEquals("ChemCam RMI", entity.cameraFullName)
        assertEquals("https://m.nasa.gov/c.jpg", entity.imageUrl)
        assertEquals("2026-07-11", entity.earthDate)
    }

    @Test
    fun `unknown instrument keeps its raw id as display name`() {
        val dto = CuriosityImageDto(
            imageId = "Y",
            sol = 1,
            instrument = "MARDI",
            url = "https://m.nasa.gov/y.jpg"
        )

        assertEquals("MARDI", dto.toEntity().cameraFullName)
    }

    @Test
    fun `entity maps to domain with parsed date and rover enum`() {
        val entity = MarsPhotoEntity(
            id = "Z1",
            rover = "perseverance",
            cameraName = "MCZ_LEFT",
            cameraFullName = "Mastcam-Z Left",
            imageUrl = "https://m.nasa.gov/p.jpg",
            thumbnailUrl = "https://m.nasa.gov/p_320.jpg",
            earthDate = "2026-07-01",
            sol = 1500
        )

        val photo = entity.toDomain()

        assertEquals(MarsRover.PERSEVERANCE, photo.rover)
        assertEquals(LocalDate.of(2026, 7, 1), photo.earthDate)
        assertEquals("https://m.nasa.gov/p_320.jpg", photo.thumbnailUrl)
    }

    @Test
    fun `unparseable date falls back to epoch instead of crashing`() {
        val entity = MarsPhotoEntity(
            id = "Z2",
            rover = "curiosity",
            cameraName = "MAHLI",
            cameraFullName = "Hand Lens Imager",
            imageUrl = "u",
            thumbnailUrl = "u",
            earthDate = "",
            sol = 1
        )

        assertEquals(LocalDate.of(1970, 1, 1), entity.toDomain().earthDate)
    }
}
