package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.NotificationType
import com.example.sportsbook.domain.model.Notification
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: Long = 0,
    @SerialName("userId") val userId: Long = 0,
    val type: String = "general",
    val title: String = "",
    val body: String = "",
    val data: Map<String, String> = emptyMap(),
    @SerialName("isRead") val isRead: Boolean = false,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("updatedAt") val updatedAt: String? = null
) {
    fun toDomain() = Notification(
        id = id,
        userId = userId,
        type = NotificationType.fromValue(type),
        title = title,
        body = body,
        data = data,
        isRead = isRead,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Serializable
data class RegisterDeviceTokenRequestDto(
    @SerialName("fcmToken") val fcmToken: String,
    @SerialName("deviceType") val deviceType: String = "android"
)

@Serializable
data class MarkNotificationsReadRequestDto(
    @SerialName("notificationIds") val notificationIds: List<Long>
)

@Serializable
data class UnreadCountDto(
    val count: Int = 0
)
