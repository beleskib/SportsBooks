package com.example.sportsbook.ui.screens.player.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.v2.ProfileVisibility
import com.example.sportsbook.domain.model.v2.SubscriptionInfo
import com.example.sportsbook.domain.model.v2.SubscriptionStatus
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.UserRepository
import com.example.sportsbook.domain.repository.V2Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val displayName: String? = null,
    val email: String = "",
    val photoUrl: String? = null,
    val interestedSports: List<SportType> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // SportsBooks+ subscription
    val isPlus: Boolean = false,
    val subscription: SubscriptionInfo? = null,
    val profileVisibility: ProfileVisibility = ProfileVisibility.PUBLIC,
    val subscriptionLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val toast: String? = null,
    val checkoutUrl: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val v2Repository: V2Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadSubscription()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = userRepository.getProfile()
            _uiState.update { current ->
                val user = result.getOrNull()
                current.copy(
                    displayName = user?.displayName,
                    email = user?.email ?: "",
                    photoUrl = user?.photoUrl,
                    interestedSports = user?.interestedSports ?: emptyList(),
                    isPlus = user?.isPlus ?: false,
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    private fun loadSubscription() {
        viewModelScope.launch {
            _uiState.update { it.copy(subscriptionLoading = true) }
            v2Repository.getSubscriptionStatus()
                .onSuccess { state ->
                    _uiState.update { current ->
                        current.copy(
                            isPlus = state.isPlus,
                            subscription = state.subscription,
                            profileVisibility = state.profileVisibility,
                            subscriptionLoading = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(subscriptionLoading = false) }
                }
        }
    }

    fun startCheckout() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            v2Repository.createCheckout()
                .onSuccess { url ->
                    _uiState.update { it.copy(checkoutUrl = url, actionLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Failed to start checkout",
                            actionLoading = false
                        )
                    }
                }
        }
    }

    fun clearCheckoutUrl() {
        _uiState.update { it.copy(checkoutUrl = null) }
    }

    fun cancelSubscription() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            v2Repository.cancelSubscription()
                .onSuccess {
                    _uiState.update { it.copy(toast = "Subscription will cancel at end of period", actionLoading = false) }
                    loadSubscription()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "Failed to cancel", actionLoading = false)
                    }
                }
        }
    }

    fun reactivateSubscription() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            v2Repository.reactivateSubscription()
                .onSuccess {
                    _uiState.update { it.copy(toast = "Subscription reactivated!", actionLoading = false) }
                    loadSubscription()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "Failed to reactivate", actionLoading = false)
                    }
                }
        }
    }

    fun setVisibility(visibility: ProfileVisibility) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            v2Repository.setProfileVisibility(visibility)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            profileVisibility = visibility,
                            toast = "Profile visibility set to ${visibility.displayLabel}",
                            actionLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(error = e.message ?: "Failed to update visibility", actionLoading = false)
                    }
                }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toast = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
