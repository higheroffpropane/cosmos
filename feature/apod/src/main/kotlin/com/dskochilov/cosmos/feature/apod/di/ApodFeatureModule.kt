package com.dskochilov.cosmos.feature.apod.di

import com.dskochilov.cosmos.core.domain.notifications.DailyApodNotificationScheduler
import com.dskochilov.cosmos.feature.apod.notification.ApodNotifier
import com.dskochilov.cosmos.feature.apod.notification.DefaultApodNotifier
import com.dskochilov.cosmos.feature.apod.work.WorkManagerApodNotificationScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ApodFeatureModule {

    @Binds
    @Singleton
    abstract fun bindsApodNotifier(impl: DefaultApodNotifier): ApodNotifier

    @Binds
    @Singleton
    abstract fun bindsDailyApodNotificationScheduler(
        impl: WorkManagerApodNotificationScheduler
    ): DailyApodNotificationScheduler
}
