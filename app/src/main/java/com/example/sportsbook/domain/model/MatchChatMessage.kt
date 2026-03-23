package com.example.sportsbook.domain.model

data class MatchChatMessage(
    val id: Long = 0,
    val matchId: Long = 0,
    val senderId: Long = 0,
    val senderName: String? = null,
    val senderPhotoUrl: String? = null,
    val content: String = "",
    val createdAt: String? = null
)
