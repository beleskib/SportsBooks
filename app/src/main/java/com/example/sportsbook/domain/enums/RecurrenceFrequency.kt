package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RecurrenceFrequency {
    @SerialName("weekly") WEEKLY,
    @SerialName("biweekly") BIWEEKLY,
    @SerialName("monthly") MONTHLY;

    val displayName: String
        get() = when (this) {
            WEEKLY -> "Weekly"
            BIWEEKLY -> "Every 2 Weeks"
            MONTHLY -> "Monthly"
        }
}
