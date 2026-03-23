package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MatchType {
    @SerialName("venue_linked") VENUE_LINKED,
    @SerialName("standalone") STANDALONE;

    val displayName: String
        get() = when (this) {
            VENUE_LINKED -> "At Venue"
            STANDALONE -> "Pickup Game"
        }
}
