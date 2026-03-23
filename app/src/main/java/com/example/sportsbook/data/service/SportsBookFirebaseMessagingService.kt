package com.example.sportsbook.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sportsbook.MainActivity
import com.example.sportsbook.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SportsBookFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID_GENERAL = "sportsbook_general"
        const val CHANNEL_ID_MATCHES = "sportsbook_matches"
        const val CHANNEL_ID_BOOKINGS = "sportsbook_bookings"
        const val CHANNEL_ID_CHAT = "sportsbook_chat"

        fun createNotificationChannels(manager: NotificationManager) {
            val channels = listOf(
                NotificationChannel(CHANNEL_ID_GENERAL, "General", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "General notifications"
                },
                NotificationChannel(CHANNEL_ID_MATCHES, "Matches", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Match updates and join requests"
                },
                NotificationChannel(CHANNEL_ID_BOOKINGS, "Bookings", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Booking confirmations and reminders"
                },
                NotificationChannel(CHANNEL_ID_CHAT, "Chat", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "New chat messages"
                }
            )
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token will be registered when user logs in and the app starts
        // Store locally for later registration
        getSharedPreferences("fcm_prefs", MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "SportsBook"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"] ?: "general"

        val channelId = when {
            type.startsWith("match_chat") -> CHANNEL_ID_CHAT
            type.startsWith("match_") -> CHANNEL_ID_MATCHES
            type.startsWith("booking_") -> CHANNEL_ID_BOOKINGS
            else -> CHANNEL_ID_GENERAL
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            message.data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
