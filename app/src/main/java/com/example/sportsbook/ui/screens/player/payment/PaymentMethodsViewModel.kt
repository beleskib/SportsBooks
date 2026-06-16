package com.example.sportsbook.ui.screens.player.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.SavedCard
import com.example.sportsbook.domain.model.SetupIntentResult
import com.example.sportsbook.domain.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentMethodsUiState(
    val cards: List<SavedCard> = emptyList(),
    val isLoading: Boolean = false,
    val isAddingCard: Boolean = false,
    val setupIntent: SetupIntentResult? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    init {
        loadCards()
    }

    fun loadCards() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            cardRepository.getSavedCards()
                .onSuccess { cards ->
                    _uiState.update { it.copy(cards = cards, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun initiateAddCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingCard = true, error = null) }
            cardRepository.createSetupIntent()
                .onSuccess { result ->
                    _uiState.update { it.copy(setupIntent = result, isAddingCard = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isAddingCard = false) }
                }
        }
    }

    fun clearSetupIntent() {
        _uiState.update { it.copy(setupIntent = null) }
    }

    fun deleteCard(paymentMethodId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            cardRepository.deleteCard(paymentMethodId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            cards = state.cards.filter { it.id != paymentMethodId },
                            successMessage = "Card removed"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun setDefaultCard(paymentMethodId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            cardRepository.setDefaultCard(paymentMethodId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            cards = state.cards.map { card ->
                                card.copy(isDefault = card.id == paymentMethodId)
                            },
                            successMessage = "Default card updated"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
