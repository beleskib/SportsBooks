package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class BookingStatus {
    @SerialName("pending") PENDING,
    @SerialName("confirmed") CONFIRMED,
    @SerialName("cancelled") CANCELLED,
    @SerialName("completed") COMPLETED,
    @SerialName("no_show") NO_SHOW
}
