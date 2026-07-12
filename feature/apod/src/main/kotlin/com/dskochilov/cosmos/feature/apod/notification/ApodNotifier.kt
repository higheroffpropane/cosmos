package com.dskochilov.cosmos.feature.apod.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dskochilov.cosmos.core.domain.apod.Apod
import com.dskochilov.cosmos.feature.apod.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Posts the daily APOD notification. Abstracted so the worker can be tested
 * with a fake instead of the Android notification stack.
 */
fun interface ApodNotifier {
    fun showDailyApod(apod: Apod)
}

internal class DefaultApodNotifier @Inject constructor(@param:ApplicationContext private val context: Context) :
    ApodNotifier {

    override fun showDailyApod(apod: Apod) {
        // Inline so lint can verify the POST_NOTIFICATIONS guard.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        ensureChannel()

        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { launchIntent ->
            val contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_apod_notification)
                .setContentTitle(context.getString(R.string.feature_apod_notification_title))
                .setContentText(apod.title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(apod.title))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.feature_apod_notification_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "daily_apod"
        const val NOTIFICATION_ID = 1001
        const val REQUEST_CODE = 0
    }
}
