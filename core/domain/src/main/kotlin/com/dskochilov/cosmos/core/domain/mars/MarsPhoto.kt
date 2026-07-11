package com.dskochilov.cosmos.core.domain.mars

import java.time.LocalDate

/**
 * A single photo taken by a Mars rover. [id] is the feed's string image id.
 */
data class MarsPhoto(
    val id: String,
    val rover: MarsRover,
    val cameraName: String,
    val cameraFullName: String,
    val imageUrl: String,
    val thumbnailUrl: String,
    val earthDate: LocalDate,
    val sol: Int
)

enum class MarsRover(val apiName: String, val displayName: String) {
    CURIOSITY("curiosity", "Curiosity"),
    PERSEVERANCE("perseverance", "Perseverance");

    /**
     * Cameras available for filtering; a curated subset of the instrument ids
     * actually served by each rover's raw-image feed.
     */
    val cameras: List<MarsCamera>
        get() = when (this) {
            CURIOSITY -> listOf(
                MarsCamera("MAST_LEFT", "Mastcam Left"),
                MarsCamera("MAST_RIGHT", "Mastcam Right"),
                MarsCamera("NAV_LEFT_B", "Navcam Left"),
                MarsCamera("NAV_RIGHT_B", "Navcam Right"),
                MarsCamera("CHEMCAM_RMI", "ChemCam RMI"),
                MarsCamera("MAHLI", "Hand Lens Imager")
            )

            PERSEVERANCE -> listOf(
                MarsCamera("NAVCAM_LEFT", "Navcam Left"),
                MarsCamera("NAVCAM_RIGHT", "Navcam Right"),
                MarsCamera("MCZ_LEFT", "Mastcam-Z Left"),
                MarsCamera("MCZ_RIGHT", "Mastcam-Z Right"),
                MarsCamera("FRONT_HAZCAM_LEFT_A", "Front Hazcam Left"),
                MarsCamera("SHERLOC_WATSON", "SHERLOC Watson")
            )
        }

    /** Human-readable name for an instrument id, falling back to the raw id. */
    fun cameraDisplayName(instrument: String): String =
        cameras.firstOrNull { it.apiName == instrument }?.displayName ?: instrument

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
