package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.model.Lobby
import com.example.sportsbook.domain.model.LobbyParticipant
import kotlinx.serialization.Serializable

@Serializable
data class LobbyDto(
    val id: Long = 0,
    val communityId: Long = 0,
    val communityName: String? = null,
    val createdBy: Long = 0,
    val creatorName: String? = null,
    val title: String = "",
    val sportType: String = "",
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val durationMinutes: Int = 60,
    val maxPlayers: Int = 10,
    val currentPlayers: Int = 0,
    val venueId: Long? = null,
    val venueName: String? = null,
    val skillLevelMin: Int = 1,
    val skillLevelMax: Int = 5,
    val status: String = "open",
    val isPublic: Boolean = false,
    val madePublicAt: String? = null,
    val description: String? = null,
    val participants: List<LobbyParticipantDto> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Lobby = Lobby(
        id = id,
        communityId = communityId,
        communityName = communityName,
        createdBy = createdBy,
        creatorName = creatorName,
        title = title,
        sportType = sportType,
        scheduledDate = scheduledDate,
        scheduledTime = scheduledTime,
        durationMinutes = durationMinutes,
        maxPlayers = maxPlayers,
        currentPlayers = currentPlayers,
        venueId = venueId,
        venueName = venueName,
        skillLevelMin = skillLevelMin,
        skillLevelMax = skillLevelMax,
        status = status,
        isPublic = isPublic,
        madePublicAt = madePublicAt,
        description = description,
        participants = participants.map { it.toDomain() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Serializable
data class LobbyParticipantDto(
    val id: Long = 0,
    val lobbyId: Long = 0,
    val userId: Long = 0,
    val displayName: String = "",
    val photoUrl: String? = null,
    val status: String = "joined",
    val joinedAt: String? = null
) {
    fun toDomain(): LobbyParticipant = LobbyParticipant(
        id = id,
        lobbyId = lobbyId,
        userId = userId,
        displayName = displayName,
        photoUrl = photoUrl,
        status = status,
        joinedAt = joinedAt
    )
}

// ── Request DTOs ──

@Serializable
data class CreateLobbyRequestDto(
    val title: String,
    val sportType: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val durationMinutes: Int = 60,
    val maxPlayers: Int = 10,
    val venueId: Long? = null,
    val skillLevelMin: Int = 1,
    val skillLevelMax: Int = 5,
    val isPublic: Boolean = false,
    val description: String? = null
)
