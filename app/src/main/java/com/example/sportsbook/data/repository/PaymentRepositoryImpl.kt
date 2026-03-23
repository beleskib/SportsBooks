package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.ConfirmPaymentRequestDto
import com.example.sportsbook.data.remote.dto.CreatePaymentIntentRequestDto
import com.example.sportsbook.domain.model.Payment
import com.example.sportsbook.domain.model.PaymentIntentResponse
import com.example.sportsbook.domain.repository.PaymentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
) : PaymentRepository {

    override suspend fun createPaymentIntent(
        timeSlotId: Long,
        notes: String?,
    ): Result<PaymentIntentResponse> = runCatching {
        apiService.createPaymentIntent(
            CreatePaymentIntentRequestDto(timeSlotId = timeSlotId, notes = notes)
        ).data.toDomain()
    }

    override suspend fun confirmPayment(paymentId: Long): Result<Payment> = runCatching {
        apiService.confirmPayment(paymentId, ConfirmPaymentRequestDto()).data.toDomain()
    }

    override suspend fun failPayment(paymentId: Long): Result<Payment> = runCatching {
        apiService.failPayment(paymentId, ConfirmPaymentRequestDto()).data.toDomain()
    }

    override suspend fun getMyPayments(): Result<List<Payment>> = runCatching {
        apiService.getMyPayments().data.map { it.toDomain() }
    }

    override suspend fun getPaymentById(id: Long): Result<Payment> = runCatching {
        apiService.getPaymentById(id).data.toDomain()
    }
}
