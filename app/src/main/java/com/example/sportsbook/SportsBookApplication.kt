package com.example.sportsbook

import android.app.Application
import android.app.NotificationManager
import com.example.sportsbook.data.service.SportsBookFirebaseMessagingService
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SportsBookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SportsBookFirebaseMessagingService.createNotificationChannels(
            getSystemService(NotificationManager::class.java)
        )
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}
