package com.example.sportsbook.domain.model

data class PaymentIntentResponse(
    val clientSecret: String,
    val bookingId: Long,
    val paymentId: Long,
    val amount: Double,
    val currency: String,
)
