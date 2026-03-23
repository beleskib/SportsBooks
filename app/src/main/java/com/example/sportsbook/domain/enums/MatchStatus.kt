package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MatchStatus {
    @SerialName("draft") DRAFT,
    @SerialName("open") OPEN,
    @SerialName("full") FULL,
    @SerialName("in_progress") IN_PROGRESS,
    @SerialName("completed") COMPLETED,
    @SerialName("cancelled") CANCELLED;

    val displayName: String
        get() = when (this) {
            DRAFT -> "Draft"
            OPEN -> "Open"
            FULL -> "Full"
            IN_PROGRESS -> "In Progress"
            COMPLETED -> "Completed"
            CANCELLED -> "Cancelled"
        }
}
