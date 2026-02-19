package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.model.Review
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    val id: Long,
    val playerId: Long,
    val venueId: Long? = null,
    val coachId: Long? = null,
    val bookingId: Long? = null,
    val rating: Int,
    val comment: String? = null,
    val playerName: String? = null,
    val playerPhotoUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Review = Review(
        id = id, playerId = playerId,
        venueId = venueId, coachId = coachId, bookingId = bookingId,
        rating = rating, comment = comment,
        playerName = playerName, playerPhotoUrl = playerPhotoUrl,
        createdAt = createdAt, updatedAt = updatedAt
    )
}

@Serializable
data class CreateReviewRequestDto(
    val venueId: Long? = null,
    val coachId: Long? = null,
    val bookingId: Long? = null,
    val rating: Int,
    val comment: String? = null
)
