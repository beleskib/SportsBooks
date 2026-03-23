package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.Notification

interface NotificationRepository {
    suspend fun getNotifications(limit: Int = 20, offset: Int = 0): Result<List<Notification>>
    suspend fun getUnreadCount(): Result<Int>
    suspend fun markAsRead(notificationIds: List<Long>): Result<Unit>
    suspend fun registerDeviceToken(fcmToken: String): Result<Unit>
    suspend fun removeDeviceToken(fcmToken: String): Result<Unit>
}
