package com.dskochilov.cosmos.core.domain.mars

import com.dskochilov.cosmos.core.common.result.AppResult
import javax.inject.Inject

/**
 * Loads a single cached Mars photo for the detail screen.
 */
class GetMarsPhotoUseCase @Inject constructor(private val repository: MarsPhotoRepository) {
    suspend operator fun invoke(id: String): AppResult<MarsPhoto> = repository.getPhoto(id)
}
