package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.PaymentStatus
import com.example.sportsbook.domain.model.Payment
import com.example.sportsbook.domain.model.PaymentIntentResponse
import kotlinx.serialization.Serializable

@Serializable
data class PaymentDto(
    val id: Long,
    val bookingId: Long,
    val payerId: Long,
    val amount: Double,
    val currency: String,
    val status: PaymentStatus,
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
) {
    fun toDomain(): Payment = Payment(
        id = id, bookingId = bookingId, payerId = payerId,
        amount = amount, currency = currency, status = status,
        paymentMethod = paymentMethod, externalPaymentId = externalPaymentId,
        paidAt = paidAt, venueName = venueName, coachName = coachName,
        venueId = venueId, coachId = coachId, slotDate = slotDate,
        startTime = startTime, endTime = endTime,
        createdAt = createdAt, updatedAt = updatedAt,
    )
}

@Serializable
data class CreatePaymentIntentRequestDto(
    val bookingId: Long,
)

@Serializable
data class PaymentIntentResponseDto(
    val clientSecret: String,
    val bookingId: Long,
    val paymentId: Long,
    val amount: Double,
    val currency: String,
) {
    fun toDomain(): PaymentIntentResponse = PaymentIntentResponse(
        clientSecret = clientSecret,
        bookingId = bookingId,
        paymentId = paymentId,
        amount = amount,
        currency = currency,
    )
}

@Serializable
data class ConfirmPaymentRequestDto(
    val paymentIntentId: String? = null,
)
