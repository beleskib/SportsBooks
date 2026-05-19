package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.MatchStatus
import com.example.sportsbook.domain.enums.MatchType
import com.example.sportsbook.domain.enums.MatchVisibility
import com.example.sportsbook.domain.enums.ParticipantRole
import com.example.sportsbook.domain.enums.ParticipantStatus
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.MatchChatMessage
import com.example.sportsbook.domain.model.MatchParticipant
import com.example.sportsbook.domain.model.PlayerRating
import kotlinx.serialization.Serializable

@Serializable
data class MatchDto(
    val id: Long,
    val hostId: Long,
    val hostName: String? = null,
    val hostPhotoUrl: String? = null,
    val bookingId: Long? = null,
    val venueId: Long? = null,
    val venueName: String? = null,
    val sportType: SportType,
    val matchType: MatchType,
    val status: MatchStatus,
    val visibility: MatchVisibility,
    val title: String,
    val description: String? = null,
    val matchDate: String,
    val startTime: String,
    val endTime: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val currentPlayers: Int,
    val minSkillLevel: Int? = null,
    val maxSkillLevel: Int? = null,
    val locationName: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isFree: Boolean = true,
    val costPerPlayer: Double = 0.0,
    val paymentType: String? = null,
    val timeSlotId: Long? = null,
    val totalPrice: Double? = null,
    val pricePerPlayer: Double? = null,
    val currency: String? = null,
    val recurrenceRuleId: Long? = null,
    val parentMatchId: Long? = null,
    val participants: List<MatchParticipantDto> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Match = Match(
        id = id, hostId = hostId, hostName = hostName, hostPhotoUrl = hostPhotoUrl,
        bookingId = bookingId, venueId = venueId, venueName = venueName,
        sportType = sportType, matchType = matchType, status = status,
        visibility = visibility, title = title, description = description,
        matchDate = matchDate, startTime = startTime, endTime = endTime,
        minPlayers = minPlayers, maxPlayers = maxPlayers, currentPlayers = currentPlayers,
        minSkillLevel = minSkillLevel, maxSkillLevel = maxSkillLevel,
        locationName = locationName, address = address,
        latitude = latitude, longitude = longitude,
        isFree = isFree, costPerPlayer = costPerPlayer,
        paymentType = paymentType,
        timeSlotId = timeSlotId,
        totalPrice = totalPrice ?: 0.0,
        pricePerPlayer = pricePerPlayer ?: 0.0,
        currency = currency ?: "MKD",
        recurrenceRuleId = recurrenceRuleId, parentMatchId = parentMatchId,
        participants = participants.map { it.toDomain() },
        createdAt = createdAt, updatedAt = updatedAt
    )
}

@Serializable
data class MatchParticipantDto(
    val id: Long,
    val matchId: Long,
    val userId: Long,
    val userName: String? = null,
    val userPhotoUrl: String? = null,
    val status: ParticipantStatus,
    val role: ParticipantRole,
    val joinedAt: String? = null,
    val createdAt: String? = null
) {
    fun toDomain(): MatchParticipant = MatchParticipant(
        id = id, matchId = matchId, userId = userId,
        userName = userName, userPhotoUrl = userPhotoUrl,
        status = status, role = role,
        joinedAt = joinedAt, createdAt = createdAt
    )
}

@Serializable
data class MatchChatMessageDto(
    val id: Long,
    val matchId: Long,
    val senderId: Long,
    val senderName: String? = null,
    val senderPhotoUrl: String? = null,
    val content: String,
    val createdAt: String? = null
) {
    fun toDomain(): MatchChatMessage = MatchChatMessage(
        id = id, matchId = matchId, senderId = senderId,
        senderName = senderName, senderPhotoUrl = senderPhotoUrl,
        content = content, createdAt = createdAt
    )
}

@Serializable
data class PlayerRatingDto(
    val id: Long,
    val matchId: Long,
    val raterId: Long,
    val raterName: String? = null,
    val ratedId: Long,
    val ratedName: String? = null,
    val skillRating: Int,
    val sportsmanshipRating: Int,
    val punctualityRating: Int,
    val comment: String? = null,
    val createdAt: String? = null
) {
    fun toDomain(): PlayerRating = PlayerRating(
        id = id, matchId = matchId, raterId = raterId, raterName = raterName,
        ratedId = ratedId, ratedName = ratedName,
        skillRating = skillRating, sportsmanshipRating = sportsmanshipRating,
        punctualityRating = punctualityRating, comment = comment,
        createdAt = createdAt
    )
}

// Request DTOs

@Serializable
data class CreateMatchRequestDto(
    val bookingId: Long? = null,
    val venueId: Long? = null,
    val timeSlotId: Long? = null,
    val sportType: SportType,
    val matchType: MatchType,
    val visibility: MatchVisibility = MatchVisibility.PUBLIC,
    val title: String,
    val description: String? = null,
    val matchDate: String,
    val startTime: String,
    val endTime: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val minSkillLevel: Int? = null,
    val maxSkillLevel: Int? = null,
    val locationName: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isFree: Boolean = true,
    val costPerPlayer: Double = 0.0,
    val paymentType: String? = null,
    val partyId: Long? = null
)

@Serializable
data class UpdateMatchRequestDto(
    val title: String? = null,
    val description: String? = null,
    val matchDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val minPlayers: Int? = null,
    val maxPlayers: Int? = null,
    val minSkillLevel: Int? = null,
    val maxSkillLevel: Int? = null,
    val locationName: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isFree: Boolean? = null,
    val costPerPlayer: Double? = null,
    val visibility: MatchVisibility? = null,
    val status: MatchStatus? = null
)

@Serializable
data class JoinMatchRequestDto(
    val message: String? = null
)

@Serializable
data class RespondToJoinRequestDto(
    val status: String
)

@Serializable
data class SendChatMessageRequestDto(
    val content: String
)

@Serializable
data class CreatePlayerRatingRequestDto(
    val ratedId: Long,
    val skillRating: Int,
    val sportsmanshipRating: Int,
    val punctualityRating: Int,
    val comment: String? = null
)
