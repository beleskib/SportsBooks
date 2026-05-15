package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MatchPaymentType(val apiValue: String, val displayName: String) {
    @SerialName("host_pays")
    HOST_PAYS("host_pays", "Host Pays"),

    @SerialName("split")
    SPLIT("split", "Split Equally"),

    @SerialName("split_to_teams")
    SPLIT_TO_TEAMS("split_to_teams", "Split to Teams"),

    @SerialName("cash_at_venue")
    CASH_AT_VENUE("cash_at_venue", "Cash at Venue");
}
