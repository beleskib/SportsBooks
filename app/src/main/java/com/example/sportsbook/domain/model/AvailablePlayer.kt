package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.SportType

data class AvailablePlayer(
    val id: Long,
    val userId: Long,
    val sportType: SportType,
    val skillLevel: Int?,
    val note: String?,
    val latitude: Double?,
    val longitude: Double?,
    val availableUntil: String?,
    val createdAt: String,
    val displayName: String?,
    val photoUrl: String?,
)
