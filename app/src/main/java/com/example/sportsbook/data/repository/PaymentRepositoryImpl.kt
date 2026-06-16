package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.ConfirmPaymentRequestDto
import com.example.sportsbook.data.remote.dto.CreatePaymentIntentRequestDto
import com.example.sportsbook.domain.model.Payment
import com.example.sportsbook.domain.model.PaymentIntentResponse
import com.example.sportsbook.domain.repository.PaymentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : PaymentRepository {

    private val paymentsCollection get() = firestore.collection("payments")

    override suspend fun createPaymentIntent(
        bookingId: Long,
    ): Result<PaymentIntentResponse> = runCatching {
        // Create via backend API
        val response = apiService.createPaymentIntent(
            CreatePaymentIntentRequestDto(bookingId = bookingId)
        ).data.toDomain()

        // Also record in Firebase for tracking
        val paymentDoc = paymentsCollection.document(response.paymentId.toString())
        val paymentData = hashMapOf(
            "paymentId" to response.paymentId,
            "bookingId" to bookingId,
            "payerId" to (auth.currentUser?.uid ?: ""),
            "amount" to response.amount,
            "currency" to response.currency,
            "status" to "pending",
            "paymentMethod" to "firebase",
            "createdAt" to com.google.firebase.Timestamp.now(),
        )
        paymentDoc.set(paymentData).await()

        // Return with empty clientSecret (no Stripe)
        response.copy(clientSecret = "")
    }

    override suspend fun confirmPayment(paymentId: Long): Result<Payment> = runCatching {
        // Confirm via backend API
        val payment = apiService.confirmPayment(paymentId, ConfirmPaymentRequestDto()).data.toDomain()

        // Update Firebase record
        paymentsCollection.document(paymentId.toString())
            .update(
                mapOf(
                    "status" to "completed",
                    "paidAt" to com.google.firebase.Timestamp.now(),
                    "updatedAt" to com.google.firebase.Timestamp.now(),
                )
            ).await()

        payment
    }

    override suspend fun failPayment(paymentId: Long): Result<Payment> = runCatching {
        // Fail via backend API
        val payment = apiService.failPayment(paymentId, ConfirmPaymentRequestDto()).data.toDomain()

        // Update Firebase record
        paymentsCollection.document(paymentId.toString())
            .update(
                mapOf(
                    "status" to "failed",
                    "updatedAt" to com.google.firebase.Timestamp.now(),
                )
            ).await()

        payment
    }

    override suspend fun getMyPayments(): Result<List<Payment>> = runCatching {
        apiService.getMyPayments().data.map { it.toDomain() }
    }

    override suspend fun getPaymentById(id: Long): Result<Payment> = runCatching {
        apiService.getPaymentById(id).data.toDomain()
    }
}
