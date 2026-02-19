package com.example.sportsbook.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onDisplayNameChange(name: String) {
        _uiState.update { it.copy(displayName = name) }
    }

    fun signIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = authRepository.signInWithEmail(
                    email = _uiState.value.email,
                    password = _uiState.value.password
                )
                result.fold(
                    onSuccess = { _uiState.update { it.copy(isSuccess = true) } },
                    onFailure = { throwable ->
                        _uiState.update { it.copy(error = throwable.message ?: "Sign in failed") }
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = authRepository.signUpWithEmail(
                    email = _uiState.value.email,
                    password = _uiState.value.password,
                    displayName = _uiState.value.displayName
                )
                result.fold(
                    onSuccess = { _uiState.update { it.copy(isSuccess = true) } },
                    onFailure = { throwable ->
                        _uiState.update { it.copy(error = throwable.message ?: "Sign up failed") }
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = authRepository.signInWithGoogle(idToken = idToken)
                result.fold(
                    onSuccess = { _uiState.update { it.copy(isSuccess = true) } },
                    onFailure = { throwable ->
                        _uiState.update { it.copy(error = throwable.message ?: "Google sign in failed") }
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
