package com.dskochilov.cosmos

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp

private const val IMAGE_DISK_CACHE_BYTES = 256L * 1024 * 1024

@HiltAndroidApp
class CosmosApplication :
    Application(),
    SingletonImageLoader.Factory {

    /**
     * App-wide Coil configuration: a generous disk cache so favorites (whose
     * images are prefetched on save) remain viewable offline.
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader = ImageLoader.Builder(context)
        .crossfade(true)
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(IMAGE_DISK_CACHE_BYTES)
                .build()
        }
        .build()
}
