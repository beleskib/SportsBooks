package com.example.sportsbook.ui.screens.player.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Payment
import com.example.sportsbook.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentDetailUiState(
    val payment: Payment? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PaymentDetailViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val paymentId: Long = checkNotNull(savedStateHandle["paymentId"])

    private val _uiState = MutableStateFlow(PaymentDetailUiState())
    val uiState: StateFlow<PaymentDetailUiState> = _uiState.asStateFlow()

    init {
        loadPayment()
    }

    fun loadPayment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            paymentRepository.getPaymentById(paymentId)
                .onSuccess { payment ->
                    _uiState.update { it.copy(payment = payment, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load payment details",
                        )
                    }
                }
        }
    }
}
