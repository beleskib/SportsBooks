package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PartnerType {
    @SerialName("coach") COACH,
    @SerialName("venue_owner") VENUE_OWNER
}
