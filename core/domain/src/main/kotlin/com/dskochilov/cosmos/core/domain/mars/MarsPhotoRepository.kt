package com.dskochilov.cosmos.core.domain.mars

import androidx.paging.PagingData
import com.dskochilov.cosmos.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow

interface MarsPhotoRepository {
    /**
     * Infinite feed of photos matching [filter], newest sols first,
     * backed by the local cache (single source of truth).
     */
    fun observeFeed(filter: MarsFeedFilter): Flow<PagingData<MarsPhoto>>

    /** Reads one photo from the local cache (it was cached when the feed showed it). */
    suspend fun getPhoto(id: String): AppResult<MarsPhoto>
}
