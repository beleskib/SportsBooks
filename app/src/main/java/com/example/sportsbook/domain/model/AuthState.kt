package com.example.sportsbook.domain.model

sealed interface AuthState {
    data object Loading : AuthState
    data class Authenticated(val user: User) : AuthState
    data object Unauthenticated : AuthState
    data object NeedsRoleSelection : AuthState
    data object NeedsProfileSetup : AuthState
}
