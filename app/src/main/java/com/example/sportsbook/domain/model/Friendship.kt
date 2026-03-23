package com.example.sportsbook.domain.model

data class Friendship(
    val id: Long = 0,
    val friendId: Long = 0,
    val friendName: String? = null,
    val friendPhotoUrl: String? = null,
    val status: String = "accepted",
    val createdAt: String? = null
)
