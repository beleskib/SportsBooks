package com.example.sportsbook.domain.model

data class Review(
    val id: Long = 0,
    val playerId: Long = 0,
    val venueId: Long? = null,
    val coachId: Long? = null,
    val bookingId: Long? = null,
    val rating: Int = 0,
    val comment: String? = null,
    val playerName: String? = null,
    val playerPhotoUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
