package com.example.sportsbook.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VenueBookingLobbyDto(
    val id: Long,
    val creatorId: Long,
    val creatorName: String? = null,
    val creatorPhotoUrl: String? = null,
    val timeSlotId: Long,
    val venueId: Long,
    val venueName: String? = null,
    val title: String,
    val paymentType: String = "split", // "split", "creator_pays", or "split_to_teams"
    val maxPlayers: Int,
    val currentPlayers: Int,
    val totalPrice: Double,
    val pricePerPlayer: Double,
    val currency: String = "MKD",
    val status: String,
    val bookingId: Long? = null,
    val description: String? = null,
    val slotDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val sportType: String? = null,
    val expiresAt: String? = null,
    val members: List<VenueBookingLobbyMemberDto> = emptyList(),
    val teams: List<VenueBookingLobbyTeamDto> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class VenueBookingLobbyMemberDto(
    val id: Long,
    val lobbyId: Long,
    val userId: Long,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val status: String = "joined",
    val shareAmount: Double? = null,
    val paymentId: Long? = null,
    val teamId: Long? = null,
    val joinedAt: String? = null,
    val paidAt: String? = null
)

@Serializable
data class VenueBookingLobbyTeamDto(
    val id: Long,
    val lobbyId: Long,
    val teamName: String,
    val teamNumber: Int,
    val leaderId: Long? = null,
    val leaderName: String? = null,
    val shareAmount: Double? = null,
    val members: List<VenueBookingLobbyMemberDto> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateVenueBookingLobbyRequestDto(
    val timeSlotId: Long,
    val title: String,
    val paymentType: String = "split",
    val maxPlayers: Int,
    val description: String? = null
)

@Serializable
data class UpdateVenueBookingLobbyRequestDto(
    val title: String? = null,
    val description: String? = null,
    val maxPlayers: Int? = null,
    val paymentType: String? = null
)

@Serializable
data class AddTeamRequestDto(
    val teamName: String
)

@Serializable
data class SplitPaymentIntentResponseDto(
    val clientSecret: String,
    val paymentId: Long,
    val amount: Double,
    val currency: String = "MKD",
    val lobbyId: Long,
    val bookingId: Long
)
