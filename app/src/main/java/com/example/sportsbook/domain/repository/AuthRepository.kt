package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.AuthState
import com.example.sportsbook.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
    suspend fun getCurrentUser(): User?
    fun isSignedIn(): Boolean
}
