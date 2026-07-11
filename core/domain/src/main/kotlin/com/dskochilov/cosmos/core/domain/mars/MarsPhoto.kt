package com.dskochilov.cosmos.core.domain.mars

import java.time.LocalDate

/**
 * A single photo taken by a Mars rover.
 */
data class MarsPhoto(
    val id: Long,
    val rover: MarsRover,
    val cameraName: String,
    val cameraFullName: String,
    val imageUrl: String,
    val earthDate: LocalDate,
    val sol: Int
)

enum class MarsRover(val apiName: String, val displayName: String) {
    CURIOSITY("curiosity", "Curiosity"),
    PERSEVERANCE("perseverance", "Perseverance");

    /** Cameras available for filtering; a curated subset of what each rover carries. */
    val cameras: List<MarsCamera>
        get() = when (this) {
            CURIOSITY -> listOf(
                MarsCamera("FHAZ", "Front Hazard Camera"),
                MarsCamera("RHAZ", "Rear Hazard Camera"),
                MarsCamera("MAST", "Mast Camera"),
                MarsCamera("CHEMCAM", "Chemistry Camera"),
                MarsCamera("MAHLI", "Hand Lens Imager"),
                MarsCamera("NAVCAM", "Navigation Camera")
            )

            PERSEVERANCE -> listOf(
                MarsCamera("NAVCAM_LEFT", "Navigation Camera – Left"),
                MarsCamera("NAVCAM_RIGHT", "Navigation Camera – Right"),
                MarsCamera("MCZ_LEFT", "Mastcam-Z – Left"),
                MarsCamera("MCZ_RIGHT", "Mastcam-Z – Right"),
                MarsCamera("FRONT_HAZCAM_LEFT_A", "Front Hazard Camera – Left"),
                MarsCamera("REAR_HAZCAM_LEFT", "Rear Hazard Camera – Left")
            )
        }

    companion object {
        fun fromApiName(apiName: String): MarsRover? =
            entries.firstOrNull { it.apiName.equals(apiName, ignoreCase = true) }
    }
}

data class MarsCamera(val apiName: String, val displayName: String)

/**
 * Query for the Mars photo feed: a rover and an optional camera restriction.
 */
data class MarsFeedFilter(val rover: MarsRover = MarsRover.CURIOSITY, val cameraApiName: String? = null)
