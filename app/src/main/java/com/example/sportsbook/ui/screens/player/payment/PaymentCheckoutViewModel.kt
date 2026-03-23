package com.example.sportsbook.ui.screens.player.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.domain.repository.PaymentRepository
import com.example.sportsbook.domain.repository.TimeSlotRepository
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentCheckoutUiState(
    val timeSlot: TimeSlot? = null,
    val notes: String = "",
    val clientSecret: String? = null,
    val paymentId: Long? = null,
    val bookingId: Long? = null,
    val amount: Double = 0.0,
    val currency: String = "USD",
    val isCreatingIntent: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val paymentSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PaymentCheckoutViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val timeSlotRepository: TimeSlotRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentCheckoutUiState())
    val uiState: StateFlow<PaymentCheckoutUiState> = _uiState.asStateFlow()

    private val timeSlotId: Long = savedStateHandle["timeSlotId"] ?: 0L
    private val notes: String = savedStateHandle["notes"] ?: ""

    init {
        _uiState.update { it.copy(notes = notes) }
        loadSlotDetails()
    }

    private fun loadSlotDetails() {
        viewModelScope.launch {
            timeSlotRepository.getSlotById(timeSlotId)
                .fold(
                    onSuccess = { slot ->
                        _uiState.update { it.copy(timeSlot = slot) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(error = error.message ?: "Failed to load slot details")
                        }
                    },
                )
        }
    }

    fun initiatePayment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingIntent = true, error = null) }

            paymentRepository.createPaymentIntent(
                timeSlotId = timeSlotId,
                notes = _uiState.value.notes.ifBlank { null },
            ).fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isCreatingIntent = false,
                            clientSecret = response.clientSecret,
                            paymentId = response.paymentId,
                            bookingId = response.bookingId,
                            amount = response.amount,
                            currency = response.currency,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isCreatingIntent = false,
                            error = error.message ?: "Failed to initiate payment",
                        )
                    }
                },
            )
        }
    }

    fun onPaymentSheetResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> confirmPayment()
            is PaymentSheetResult.Failed -> failPayment(result.error.localizedMessage)
            is PaymentSheetResult.Canceled -> {
                // User cancelled — fail the payment and re-open the slot
                failPayment("Payment was cancelled")
            }
        }
    }

    private fun confirmPayment() {
        val paymentId = _uiState.value.paymentId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingPayment = true) }

            paymentRepository.confirmPayment(paymentId)
                .fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isProcessingPayment = false,
                                paymentSuccess = true,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isProcessingPayment = false,
                                error = error.message ?: "Failed to confirm payment",
                            )
                        }
                    },
                )
        }
    }

    private fun failPayment(errorMsg: String?) {
        val paymentId = _uiState.value.paymentId ?: return

        viewModelScope.launch {
            paymentRepository.failPayment(paymentId)
            _uiState.update {
                it.copy(
                    isProcessingPayment = false,
                    error = errorMsg ?: "Payment failed",
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
