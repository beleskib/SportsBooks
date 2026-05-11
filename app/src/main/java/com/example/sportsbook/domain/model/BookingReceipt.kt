package com.example.sportsbook.domain.model

data class BookingReceipt(
    val receiptNumber: String,
    val bookingId: Long,
    val paymentId: Long,
    val status: String,
    val issuedAt: String,
    val playerName: String,
    val playerEmail: String,
    val providerType: String,
    val providerName: String,
    val providerAddress: String?,
    val sportType: String,
    val slotDate: String,
    val startTime: String,
    val endTime: String,
    val subtotal: Double,
    val platformFee: Double,
    val total: Double,
    val currency: String,
    val paymentMethod: String?,
    val externalPaymentId: String?,
)
