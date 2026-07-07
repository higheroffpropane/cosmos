package com.dskochilov.cosmos.core.domain.apod

import com.dskochilov.cosmos.core.common.result.AppResult
import java.time.LocalDate

interface ApodRepository {
    /**
     * Fetches the APOD entry for [date], or today's entry when [date] is null.
     */
    suspend fun getApod(date: LocalDate? = null): AppResult<Apod>
}
