package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ParticipantRole {
    @SerialName("host") HOST,
    @SerialName("player") PLAYER;

    val displayName: String
        get() = when (this) {
            HOST -> "Host"
            PLAYER -> "Player"
        }
}
