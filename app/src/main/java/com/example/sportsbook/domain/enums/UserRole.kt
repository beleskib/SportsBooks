package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    @SerialName("player") PLAYER,
    @SerialName("partner") PARTNER,
    @SerialName("admin") ADMIN
}
