package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ParticipantStatus {
    @SerialName("pending") PENDING,
    @SerialName("approved") APPROVED,
    @SerialName("declined") DECLINED,
    @SerialName("left") LEFT;

    val displayName: String
        get() = when (this) {
            PENDING -> "Pending"
            APPROVED -> "Approved"
            DECLINED -> "Declined"
            LEFT -> "Left"
        }
}
