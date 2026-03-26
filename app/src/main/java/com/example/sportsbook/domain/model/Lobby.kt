package com.example.sportsbook.domain.model

data class Lobby(
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
    val participants: List<LobbyParticipant> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val spotsLeft: Int get() = maxPlayers - currentPlayers
    val isFull: Boolean get() = currentPlayers >= maxPlayers
    val isOpen: Boolean get() = status == "open"
    val displayVenue: String get() = venueName ?: "Venue TBD"
    val skillRange: String get() = "Skill $skillLevelMin–$skillLevelMax"
}

data class LobbyParticipant(
    val id: Long = 0,
    val lobbyId: Long = 0,
    val userId: Long = 0,
    val displayName: String = "",
    val photoUrl: String? = null,
    val status: String = "joined",
    val joinedAt: String? = null
)
