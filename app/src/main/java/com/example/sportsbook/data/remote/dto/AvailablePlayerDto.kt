package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.AvailablePlayer
import kotlinx.serialization.Serializable

@Serializable
data class AvailablePlayerDto(
    val id: Long,
    val userId: Long,
    val sportType: String,
    val skillLevel: Int? = null,
    val note: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val availableUntil: String? = null,
    val createdAt: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
)

fun AvailablePlayerDto.toDomain() = AvailablePlayer(
    id = id,
    userId = userId,
    sportType = SportType.entries.find { it.name.equals(sportType, ignoreCase = true) } ?: SportType.BASKETBALL,
    skillLevel = skillLevel,
    note = note,
    latitude = latitude,
    longitude = longitude,
    availableUntil = availableUntil,
    createdAt = createdAt,
    displayName = displayName,
    photoUrl = photoUrl,
)

@Serializable
data class RegisterAvailableRequestDto(
    val sportType: String,
    val skillLevel: Int? = null,
    val note: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val availableUntil: String? = null,
)

@Serializable
data class InviteToMatchRequestDto(
    val userId: Long,
)
