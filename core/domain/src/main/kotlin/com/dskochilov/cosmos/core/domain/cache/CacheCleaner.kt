package com.dskochilov.cosmos.core.domain.cache

import javax.inject.Inject

/**
 * Clears transient caches (image disk/memory cache, Mars photo cache).
 * Favorites, search history and settings are intentionally untouched.
 */
interface CacheCleaner {
    suspend fun clearCaches()
}

class ClearCachesUseCase @Inject constructor(private val cacheCleaner: CacheCleaner) {
    suspend operator fun invoke() = cacheCleaner.clearCaches()
}
