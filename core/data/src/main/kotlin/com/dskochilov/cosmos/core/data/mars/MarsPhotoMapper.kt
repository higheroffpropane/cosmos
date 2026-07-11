package com.dskochilov.cosmos.core.data.mars

import com.dskochilov.cosmos.core.database.entity.MarsPhotoEntity
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import com.dskochilov.cosmos.core.network.mars.MarsPhotoDto
import java.time.LocalDate

/**
 * [queriedRover] is the rover the feed was queried for; the entity keeps it as the
 * cache partition key regardless of how the API spells the rover name in the payload.
 */
internal fun MarsPhotoDto.toEntity(queriedRover: MarsRover): MarsPhotoEntity = MarsPhotoEntity(
    id = id,
    rover = queriedRover.apiName,
    cameraName = camera.name,
    cameraFullName = camera.fullName,
    imageUrl = imageUrl,
    earthDate = earthDate,
    sol = sol
)

internal fun MarsPhotoEntity.toDomain(): MarsPhoto = MarsPhoto(
    id = id,
    rover = MarsRover.fromApiName(rover) ?: MarsRover.CURIOSITY,
    cameraName = cameraName,
    cameraFullName = cameraFullName,
    imageUrl = imageUrl,
    earthDate = LocalDate.parse(earthDate),
    sol = sol
)
