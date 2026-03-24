package com.example.sportsbook.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookingMessageDto(
    val id: Long = 0,
    @SerialName("bookingId") val bookingId: Long = 0,
    @SerialName("senderId") val senderId: Long = 0,
    @SerialName("senderName") val senderName: String = "",
    val message: String = "",
    @SerialName("createdAt") val createdAt: String = ""
)

@Serializable
data class SendMessageRequestDto(
    val message: String
)

@Serializable
data class BookingContactDto(
    val name: String = "",
    val email: String? = null,
    @SerialName("phoneNumber") val phoneNumber: String? = null
)
