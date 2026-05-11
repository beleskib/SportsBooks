package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.model.BookingReceipt
import kotlinx.serialization.Serializable

@Serializable
data class ReceiptDto(
    val receiptNumber: String,
    val bookingId: Long,
    val paymentId: Long,
    val status: String,
    val issuedAt: String,
    val playerName: String,
    val playerEmail: String,
    val providerType: String,
    val providerName: String,
    val providerAddress: String? = null,
    val sportType: String,
    val slotDate: String,
    val startTime: String,
    val endTime: String,
    val subtotal: Double,
    val platformFee: Double,
    val total: Double,
    val currency: String,
    val paymentMethod: String? = null,
    val externalPaymentId: String? = null,
) {
    fun toDomain(): BookingReceipt = BookingReceipt(
        receiptNumber = receiptNumber,
        bookingId = bookingId,
        paymentId = paymentId,
        status = status,
        issuedAt = issuedAt,
        playerName = playerName,
        playerEmail = playerEmail,
        providerType = providerType,
        providerName = providerName,
        providerAddress = providerAddress,
        sportType = sportType,
        slotDate = slotDate,
        startTime = startTime,
        endTime = endTime,
        subtotal = subtotal,
        platformFee = platformFee,
        total = total,
        currency = currency,
        paymentMethod = paymentMethod,
        externalPaymentId = externalPaymentId,
    )
}
