package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.data.remote.dto.v2.SplitPaymentShareDto
import kotlinx.serialization.Serializable

@Serializable
data class MatchPaymentStatusDto(
    val matchId: Long,
    val bookingId: Long? = null,
    val paymentType: String? = null,
    val totalPrice: Double = 0.0,
    val pricePerPlayer: Double = 0.0,
    val currency: String = "MKD",
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val shares: List<SplitPaymentShareDto> = emptyList()
)

@Serializable
data class MatchPaymentIntentDto(
    val shareId: Long? = null,
    val amount: Double = 0.0,
    val currency: String = "MKD",
    val clientSecret: String? = null,
    val bookingId: Long? = null,
    val matchId: Long? = null,
    val alreadyPaid: Boolean = false
)
