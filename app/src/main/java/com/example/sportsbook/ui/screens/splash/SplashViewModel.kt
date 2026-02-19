package com.example.sportsbook.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.UserRole
import com.example.sportsbook.domain.model.AuthState
import com.example.sportsbook.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data object NavigateToLogin : SplashUiState
    data object NavigateToRoleSelection : SplashUiState
    data object NavigateToPlayerHome : SplashUiState
    data object NavigateToPartnerDashboard : SplashUiState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<SplashUiState> = MutableStateFlow(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        viewModelScope.launch {
            authRepository.authState.collect { authState ->
                _uiState.update {
                    when (authState) {
                        is AuthState.Loading -> SplashUiState.Loading
                        is AuthState.Unauthenticated -> SplashUiState.NavigateToLogin
                        is AuthState.NeedsRoleSelection -> SplashUiState.NavigateToRoleSelection
                        is AuthState.NeedsProfileSetup -> SplashUiState.NavigateToRoleSelection
                        is AuthState.Authenticated -> when (authState.user.role) {
                            UserRole.PLAYER -> SplashUiState.NavigateToPlayerHome
                            UserRole.PARTNER -> SplashUiState.NavigateToPartnerDashboard
                            UserRole.ADMIN -> SplashUiState.NavigateToPlayerHome
                        }
                    }
                }
            }
        }
    }
}
