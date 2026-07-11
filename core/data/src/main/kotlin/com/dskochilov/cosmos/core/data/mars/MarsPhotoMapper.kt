package com.dskochilov.cosmos.core.data.mars

import com.dskochilov.cosmos.core.database.entity.MarsPhotoEntity
import com.dskochilov.cosmos.core.domain.mars.MarsPhoto
import com.dskochilov.cosmos.core.domain.mars.MarsRover
import com.dskochilov.cosmos.core.network.mars.CuriosityImageDto
import com.dskochilov.cosmos.core.network.mars.PerseveranceImageDto
import java.time.LocalDate

private const val ISO_DATE_LENGTH = 10
private val EPOCH_DATE: LocalDate = LocalDate.of(1970, 1, 1)

internal fun PerseveranceImageDto.toEntity(): MarsPhotoEntity = MarsPhotoEntity(
    id = imageId,
    rover = MarsRover.PERSEVERANCE.apiName,
    cameraName = camera.instrument,
    cameraFullName = MarsRover.PERSEVERANCE.cameraDisplayName(camera.instrument),
    imageUrl = imageFiles.large ?: imageFiles.fullRes ?: imageFiles.medium ?: imageFiles.small.orEmpty(),
    thumbnailUrl = imageFiles.medium ?: imageFiles.small ?: imageFiles.large.orEmpty(),
    earthDate = dateTakenUtc.orEmpty().take(ISO_DATE_LENGTH),
    sol = sol
)

internal fun CuriosityImageDto.toEntity(): MarsPhotoEntity = MarsPhotoEntity(
    id = imageId,
    rover = MarsRover.CURIOSITY.apiName,
    cameraName = instrument,
    cameraFullName = MarsRover.CURIOSITY.cameraDisplayName(instrument),
    imageUrl = httpsUrl ?: url,
    thumbnailUrl = httpsUrl ?: url,
    earthDate = dateTaken.orEmpty().take(ISO_DATE_LENGTH),
    sol = sol
)

internal fun MarsPhotoEntity.toDomain(): MarsPhoto = MarsPhoto(
    id = id,
    rover = MarsRover.fromApiName(rover) ?: MarsRover.CURIOSITY,
    cameraName = cameraName,
    cameraFullName = cameraFullName,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    earthDate = runCatching { LocalDate.parse(earthDate) }.getOrDefault(EPOCH_DATE),
    sol = sol
)
