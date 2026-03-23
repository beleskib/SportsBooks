package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MatchVisibility {
    @SerialName("public") PUBLIC,
    @SerialName("private") PRIVATE;

    val displayName: String
        get() = when (this) {
            PUBLIC -> "Public"
            PRIVATE -> "Private"
        }
}
