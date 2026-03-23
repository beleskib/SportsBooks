package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SkillLevel {
    @SerialName("newbie") NEWBIE,
    @SerialName("beginner") BEGINNER,
    @SerialName("intermediate") INTERMEDIATE,
    @SerialName("semi_pro") SEMI_PRO,
    @SerialName("pro") PRO;

    val displayName: String
        get() = when (this) {
            NEWBIE -> "Newbie"
            BEGINNER -> "Beginner"
            INTERMEDIATE -> "Intermediate"
            SEMI_PRO -> "Semi Pro"
            PRO -> "Pro"
        }
}
