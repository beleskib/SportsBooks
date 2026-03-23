package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.MarkNotificationsReadRequestDto
import com.example.sportsbook.data.remote.dto.RegisterDeviceTokenRequestDto
import com.example.sportsbook.domain.model.Notification
import com.example.sportsbook.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : NotificationRepository {

    override suspend fun getNotifications(limit: Int, offset: Int): Result<List<Notification>> = runCatching {
        apiService.getNotifications(limit, offset).data.map { it.toDomain() }
    }

    override suspend fun getUnreadCount(): Result<Int> = runCatching {
        apiService.getNotificationUnreadCount().data.count
    }

    override suspend fun markAsRead(notificationIds: List<Long>): Result<Unit> = runCatching {
        apiService.markNotificationsRead(MarkNotificationsReadRequestDto(notificationIds))
        Unit
    }

    override suspend fun registerDeviceToken(fcmToken: String): Result<Unit> = runCatching {
        apiService.registerDeviceToken(RegisterDeviceTokenRequestDto(fcmToken))
        Unit
    }

    override suspend fun removeDeviceToken(fcmToken: String): Result<Unit> = runCatching {
        apiService.removeDeviceToken(RegisterDeviceTokenRequestDto(fcmToken))
        Unit
    }
}
