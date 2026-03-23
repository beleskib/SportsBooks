package com.example.sportsbook.domain.model

data class Party(
    val id: Long = 0,
    val leaderId: Long = 0,
    val leaderName: String? = null,
    val name: String? = null,
    val sportType: String? = null,
    val status: String = "forming",
    val matchId: Long? = null,
    val members: List<PartyMember> = emptyList(),
    val createdAt: String? = null
)

data class PartyMember(
    val id: Long = 0,
    val userId: Long = 0,
    val userName: String? = null,
    val userPhotoUrl: String? = null,
    val status: String = "invited",
    val respondedAt: String? = null
)
