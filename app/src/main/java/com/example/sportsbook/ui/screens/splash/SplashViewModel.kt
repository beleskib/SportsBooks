package com.example.sportsbook.ui.screens.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.UserRole
import com.example.sportsbook.domain.model.AuthState
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data object NavigateToLogin : SplashUiState
    data object NavigateToRoleSelection : SplashUiState
    data object NavigateToPlayerOnboarding : SplashUiState
    data object NavigateToPlayerHome : SplashUiState
    data object NavigateToPartnerDashboard : SplashUiState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    private val firebaseMessaging: FirebaseMessaging,
    @ApplicationContext private val appContext: Context,
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
                        is AuthState.Authenticated -> {
                            // Register FCM token with backend on every app launch
                            registerFcmToken()

                            when (authState.user.role) {
                                UserRole.PLAYER -> {
                                    if (authState.user.onboardingCompleted) {
                                        SplashUiState.NavigateToPlayerHome
                                    } else {
                                        // New users get role='player' by default but haven't
                                        // actually chosen a role yet — send them to role selection
                                        // so they can pick Player or Partner before onboarding.
                                        SplashUiState.NavigateToRoleSelection
                                    }
                                }
                                UserRole.PARTNER -> SplashUiState.NavigateToPartnerDashboard
                                UserRole.ADMIN -> SplashUiState.NavigateToPlayerHome
                            }
                        }
                    }
                }
            }
        }
    }

    private fun registerFcmToken() {
        viewModelScope.launch {
            try {
                val token = firebaseMessaging.token.await()
                // Save locally for later use
                appContext.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("fcm_token", token)
                    .apply()
                // Register with backend so it can send push notifications
                notificationRepository.registerDeviceToken(token)
            } catch (e: Exception) {
                // FCM registration failure should never block the app
                e.printStackTrace()
            }
        }
    }
}
