package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.NotificationType

data class Notification(
    val id: Long = 0,
    val userId: Long = 0,
    val type: NotificationType = NotificationType.GENERAL,
    val title: String = "",
    val body: String = "",
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
