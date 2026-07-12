package com.dskochilov.cosmos.feature.apod.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dskochilov.cosmos.core.common.result.fold
import com.dskochilov.cosmos.core.domain.apod.GetApodUseCase
import com.dskochilov.cosmos.feature.apod.notification.ApodNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Fetches today's APOD and posts a notification. Scheduled daily with a
 * network constraint; failures use WorkManager's exponential backoff.
 */
@HiltWorker
class DailyApodWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val getApod: GetApodUseCase,
    private val notifier: ApodNotifier
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = getApod().fold(
        onSuccess = { apod ->
            notifier.showDailyApod(apod)
            Result.success()
        },
        onFailure = { Result.retry() }
    )
}
