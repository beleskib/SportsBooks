package com.example.sportsbook.ui.screens.player.payment

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

data class PaymentHistoryUiState(
    val payments: List<Payment> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentHistoryUiState())
    val uiState: StateFlow<PaymentHistoryUiState> = _uiState.asStateFlow()

    init {
        loadPayments()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            paymentRepository.getMyPayments()
                .fold(
                    onSuccess = { payments ->
                        _uiState.update { it.copy(isRefreshing = false, payments = payments) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isRefreshing = false,
                                error = error.message ?: "Failed to load payments",
                            )
                        }
                    },
                )
        }
    }

    fun loadPayments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            paymentRepository.getMyPayments()
                .fold(
                    onSuccess = { payments ->
                        _uiState.update {
                            it.copy(isLoading = false, payments = payments)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load payments",
                            )
                        }
                    },
                )
        }
    }
}
