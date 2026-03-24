package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.PaymentStatus

data class Payment(
    val id: Long = 0,
    val bookingId: Long = 0,
    val payerId: Long = 0,
    val amount: Double = 0.0,
    val currency: String = "MKD",
    val status: PaymentStatus = PaymentStatus.PENDING,
    val paymentMethod: String? = null,
    val externalPaymentId: String? = null,
    val paidAt: String? = null,
    val venueName: String? = null,
    val coachName: String? = null,
    val venueId: Long? = null,
    val coachId: Long? = null,
    val slotDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)
