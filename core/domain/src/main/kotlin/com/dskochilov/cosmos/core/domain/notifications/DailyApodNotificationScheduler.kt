package com.dskochilov.cosmos.core.domain.notifications

/**
 * Schedules/cancels the daily APOD background check. Implemented over
 * WorkManager in the APOD feature; exposed here so the settings feature
 * can drive it without a feature-to-feature dependency.
 */
interface DailyApodNotificationScheduler {
    fun schedule()

    fun cancel()
}
