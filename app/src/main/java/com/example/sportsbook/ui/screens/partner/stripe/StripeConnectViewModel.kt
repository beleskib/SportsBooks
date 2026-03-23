package com.example.sportsbook.ui.screens.partner.stripe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.OnboardingStatus
import com.example.sportsbook.domain.model.StripeConnectStatus
import com.example.sportsbook.domain.repository.StripeConnectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StripeConnectUiState(
    val status: StripeConnectStatus? = null,
    val isLoading: Boolean = false,
    val isOnboarding: Boolean = false,
    val onboardingUrl: String? = null,
    val dashboardUrl: String? = null,
    val error: String? = null,
)

@HiltViewModel
class StripeConnectViewModel @Inject constructor(
    private val stripeConnectRepository: StripeConnectRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StripeConnectUiState())
    val uiState: StateFlow<StripeConnectUiState> = _uiState.asStateFlow()

    init {
        loadStatus()
    }

    fun loadStatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            stripeConnectRepository.getStatus()
                .fold(
                    onSuccess = { status ->
                        _uiState.update {
                            it.copy(isLoading = false, status = status)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load Stripe status",
                            )
                        }
                    },
                )
        }
    }

    fun startOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isOnboarding = true, error = null) }

            stripeConnectRepository.onboard()
                .fold(
                    onSuccess = { url ->
                        _uiState.update {
                            it.copy(isOnboarding = false, onboardingUrl = url)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isOnboarding = false,
                                error = error.message ?: "Failed to start onboarding",
                            )
                        }
                    },
                )
        }
    }

    fun openDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }

            stripeConnectRepository.getDashboardLink()
                .fold(
                    onSuccess = { url ->
                        _uiState.update { it.copy(dashboardUrl = url) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(error = error.message ?: "Failed to get dashboard link")
                        }
                    },
                )
        }
    }

    fun clearOnboardingUrl() {
        _uiState.update { it.copy(onboardingUrl = null) }
    }

    fun clearDashboardUrl() {
        _uiState.update { it.copy(dashboardUrl = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
