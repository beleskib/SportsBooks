package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.Payment
import com.example.sportsbook.domain.model.PaymentIntentResponse

interface PaymentRepository {
    suspend fun createPaymentIntent(bookingId: Long): Result<PaymentIntentResponse>
    suspend fun confirmPayment(paymentId: Long): Result<Payment>
    suspend fun failPayment(paymentId: Long): Result<Payment>
    suspend fun getMyPayments(): Result<List<Payment>>
    suspend fun getPaymentById(id: Long): Result<Payment>
}
