package com.dskochilov.cosmos.feature.apod.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.dskochilov.cosmos.core.common.result.AppError
import com.dskochilov.cosmos.core.common.result.AppResult
import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.core.domain.apod.ApodMedia
import com.dskochilov.cosmos.core.domain.apod.ApodRepository
import com.dskochilov.cosmos.core.domain.apod.GetApodUseCase
import com.dskochilov.cosmos.feature.apod.notification.ApodNotifier
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyApodWorkerTest {

    private val shownApods = mutableListOf<Apod>()
    private val notifier = ApodNotifier { apod -> shownApods += apod }

    @Test
    fun successfulFetch_postsNotification_andSucceeds() = runTest {
        val apod = testApod()
        val worker = worker(FakeApodRepository(AppResult.Success(apod)))

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(listOf(apod), shownApods)
    }

    @Test
    fun failedFetch_requestsRetry_withoutNotifying() = runTest {
        val worker = worker(FakeApodRepository(AppResult.Failure(AppError.NoConnection)))

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        assertEquals(emptyList<Apod>(), shownApods)
    }

    private fun worker(repository: ApodRepository): DailyApodWorker {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return TestListenableWorkerBuilder<DailyApodWorker>(context)
            .setWorkerFactory(
                object : WorkerFactory() {
                    override fun createWorker(
                        appContext: Context,
                        workerClassName: String,
                        workerParameters: WorkerParameters
                    ): ListenableWorker = DailyApodWorker(
                        appContext = appContext,
                        params = workerParameters,
                        getApod = GetApodUseCase(repository),
                        notifier = notifier
                    )
                }
            )
            .build()
    }

    private class FakeApodRepository(private val result: AppResult<Apod>) : ApodRepository {
        override suspend fun getApod(date: LocalDate?): AppResult<Apod> {
            assertNull(date)
            return result
        }
    }

    private fun testApod() = Apod(
        date = LocalDate.of(2026, 7, 13),
        title = "Today's picture",
        explanation = "x",
        copyright = null,
        media = ApodMedia.Image(url = "https://apod.nasa.gov/today.jpg", hdUrl = null)
    )
}
