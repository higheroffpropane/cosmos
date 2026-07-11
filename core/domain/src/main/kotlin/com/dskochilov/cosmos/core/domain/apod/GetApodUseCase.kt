package com.dskochilov.cosmos.core.domain.apod

import com.dskochilov.cosmos.core.common.result.AppResult
import java.time.LocalDate
import javax.inject.Inject

/**
 * Loads the APOD entry for the given date (today when null).
 */
class GetApodUseCase @Inject constructor(private val repository: ApodRepository) {
    suspend operator fun invoke(date: LocalDate? = null): AppResult<Apod> = repository.getApod(date)
}
