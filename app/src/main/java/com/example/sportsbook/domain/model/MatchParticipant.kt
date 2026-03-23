package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.ParticipantRole
import com.example.sportsbook.domain.enums.ParticipantStatus

data class MatchParticipant(
    val id: Long = 0,
    val matchId: Long = 0,
    val userId: Long = 0,
    val userName: String? = null,
    val userPhotoUrl: String? = null,
    val status: ParticipantStatus = ParticipantStatus.PENDING,
    val role: ParticipantRole = ParticipantRole.PLAYER,
    val joinedAt: String? = null,
    val createdAt: String? = null
)
