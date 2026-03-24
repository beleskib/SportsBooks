package com.example.sportsbook.ui.screens.player.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.repository.GamificationRepository
import com.example.sportsbook.domain.repository.PaymentRepository
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentCheckoutUiState(
    val clientSecret: String? = null,
    val paymentId: Long? = null,
    val bookingId: Long? = null,
    val amount: Double = 0.0,
    val currency: String = "MKD",
    val isCreatingIntent: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val paymentSuccess: Boolean = false,
    val error: String? = null,
    // Booking details
    val isLoadingBooking: Boolean = true,
    val venueName: String? = null,
    val coachName: String? = null,
    val slotDate: String? = null,
    val slotTime: String? = null,
    val sportType: String? = null,
    // XP redemption state
    val availableXp: Int = 0,
    val xpToRedeem: Int = 0,
    val xpDiscount: Double = 0.0,
    val xpRedeemed: Boolean = false,
    val isRedeemingXp: Boolean = false,
    val currentLevel: Int = 1,
)

@HiltViewModel
class PaymentCheckoutViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val gamificationRepository: GamificationRepository,
    private val bookingRepository: BookingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentCheckoutUiState())
    val uiState: StateFlow<PaymentCheckoutUiState> = _uiState.asStateFlow()

    private val bookingId: Long = savedStateHandle["bookingId"] ?: 0L

    init {
        _uiState.update { it.copy(bookingId = bookingId) }
        loadBookingDetails()
        loadXpBalance()
    }

    private fun loadBookingDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBooking = true) }
            bookingRepository.getBookingById(bookingId)
                .onSuccess { booking ->
                    _uiState.update {
                        it.copy(
                            isLoadingBooking = false,
                            amount = booking.totalPrice,
                            venueName = booking.venue?.name,
                            coachName = booking.coach?.name,
                            slotDate = booking.timeSlot?.slotDate,
                            slotTime = booking.timeSlot?.displayTime,
                            sportType = (booking.venue?.sportType ?: booking.coach?.sportType)?.displayName,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingBooking = false,
                            error = error.message ?: "Failed to load booking details"
                        )
                    }
                }
        }
    }

    private fun loadXpBalance() {
        viewModelScope.launch {
            gamificationRepository.getMyLevel()
                .onSuccess { level ->
                    _uiState.update {
                        it.copy(
                            availableXp = level.totalXp,
                            currentLevel = level.currentLevel
                        )
                    }
                }
        }
    }

    fun onXpSliderChange(xp: Int) {
        val discount = xp / 100.0
        _uiState.update { it.copy(xpToRedeem = xp, xpDiscount = discount) }
    }

    fun setQuickXp(xp: Int) {
        val state = _uiState.value
        val maxRedeemable = minOf(state.availableXp, (state.amount * 100).toInt())
        val clamped = xp.coerceIn(0, maxRedeemable)
        val discount = clamped / 100.0
        _uiState.update { it.copy(xpToRedeem = clamped, xpDiscount = discount) }
    }

    fun redeemXp() {
        val state = _uiState.value
        if (state.xpRedeemed || state.xpToRedeem <= 0) return
        val currentBookingId = state.bookingId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isRedeemingXp = true, error = null) }
            gamificationRepository.redeemXp(
                bookingId = currentBookingId,
                xpAmount = state.xpToRedeem
            ).fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isRedeemingXp = false,
                            xpRedeemed = true,
                            xpDiscount = response.discountAmount,
                            xpToRedeem = response.xpSpent,
                            availableXp = response.remainingXp,
                            currentLevel = response.newLevel,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isRedeemingXp = false,
                            error = error.message ?: "Failed to redeem XP",
                        )
                    }
                },
            )
        }
    }

    fun initiatePayment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingIntent = true, error = null) }

            paymentRepository.createPaymentIntent(
                bookingId = bookingId,
            ).fold(
                onSuccess = { response ->
                    if (response.clientSecret.isNullOrEmpty()) {
                        // XP fully covered the payment — no Stripe needed
                        _uiState.update {
                            it.copy(
                                isCreatingIntent = false,
                                paymentSuccess = true,
                            )
                        }
                    } else {
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
