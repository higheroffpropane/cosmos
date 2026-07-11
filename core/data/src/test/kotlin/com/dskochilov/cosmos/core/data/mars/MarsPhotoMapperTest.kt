package com.dskochilov.cosmos.core.data.mars

import com.dskochilov.cosmos.core.database.entity.MarsPhotoEntity
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import com.dskochilov.cosmos.core.network.mars.MarsCameraDto
import com.dskochilov.cosmos.core.network.mars.MarsPhotoDto
import com.dskochilov.cosmos.core.network.mars.MarsRoverDto
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MarsPhotoMapperTest {

    @Test
    fun `dto maps to entity partitioned by the queried rover`() {
        val dto = MarsPhotoDto(
            id = 424905,
            sol = 1000,
            camera = MarsCameraDto(name = "FHAZ", fullName = "Front Hazard Avoidance Camera"),
            imageUrl = "https://mars.nasa.gov/img.jpg",
            earthDate = "2015-05-30",
            rover = MarsRoverDto(name = "Curiosity")
        )

        val entity = dto.toEntity(queriedRover = MarsRover.CURIOSITY)

        assertEquals(
            MarsPhotoEntity(
                id = 424905,
                rover = "curiosity",
                cameraName = "FHAZ",
                cameraFullName = "Front Hazard Avoidance Camera",
                imageUrl = "https://mars.nasa.gov/img.jpg",
                earthDate = "2015-05-30",
                sol = 1000
            ),
            entity
        )
    }

    @Test
    fun `entity maps to domain with parsed date and rover enum`() {
        val entity = MarsPhotoEntity(
            id = 1,
            rover = "perseverance",
            cameraName = "MCZ_LEFT",
            cameraFullName = "Mastcam-Z Left",
            imageUrl = "https://mars.nasa.gov/p.jpg",
            earthDate = "2026-07-01",
            sol = 1500
        )

        val photo = entity.toDomain()

        assertEquals(MarsRover.PERSEVERANCE, photo.rover)
        assertEquals(LocalDate.of(2026, 7, 1), photo.earthDate)
        assertEquals("MCZ_LEFT", photo.cameraName)
        assertEquals(1500, photo.sol)
    }
}
