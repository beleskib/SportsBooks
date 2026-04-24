package com.example.sportsbook.data.remote.dto.v2

import com.example.sportsbook.domain.model.v2.SplitPaymentShare
import com.example.sportsbook.domain.model.v2.SplitPaymentSummary
import kotlinx.serialization.Serializable

// ============================================================
// v2-practical-ux: DTOs for split payment endpoints
// ============================================================

@Serializable
data class SplitPaymentShareDto(
    val id: Long,
    val bookingId: Long,
    val payerUserId: Long,
    val amount: Double,
    val currency: String,
    val status: String, // "pending" | "awaiting" | "paid" | "refunded" | "expired"
    val payerName: String? = null,
    val payerPhotoUrl: String? = null,
    val expiresAt: String? = null,
    val paidAt: String? = null
) {
    fun toDomain(): SplitPaymentShare = SplitPaymentShare(
        id = id,
        bookingId = bookingId,
        payerUserId = payerUserId,
        amount = amount,
        currency = currency,
        status = status,
        payerName = payerName,
        payerPhotoUrl = payerPhotoUrl,
        expiresAt = expiresAt,
        paidAt = paidAt
    )
}

@Serializable
data class SplitPaymentSummaryDto(
    val bookingId: Long,
    val totalAmount: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val shares: List<SplitPaymentShareDto> = emptyList()
) {
    fun toDomain(): SplitPaymentSummary = SplitPaymentSummary(
        bookingId = bookingId,
        totalAmount = totalAmount,
        paidAmount = paidAmount,
        pendingAmount = pendingAmount,
        shares = shares.map { it.toDomain() }
    )
}

// ---- Request DTOs ----

@Serializable
data class RebookRequestDto(
    val slotDate: String,
    val startTime: String
)

@Serializable
data class CreateSplitRequestDto(
    val payerUserIds: List<Long>,
    val expiresInMinutes: Int? = null
)

@Serializable
data class InviteParticipantsRequestDto(
    val userIds: List<Long>
)

@Serializable
data class RespondToInviteRequestDto(
    val accept: Boolean
)

@Serializable
data class AttendanceEntryDto(
    val userId: Long,
    val attended: Boolean
)

@Serializable
data class MarkAttendanceRequestDto(
    val attendance: List<AttendanceEntryDto>
)

@Serializable
data class PaySplitShareRequestDto(
    val stripePaymentIntentId: String
)
