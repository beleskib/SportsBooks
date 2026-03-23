package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.CreateUserRequestDto
import com.example.sportsbook.data.remote.firebase.FirebaseAuthService
import com.example.sportsbook.domain.model.AuthState
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val apiService: ApiService
) : AuthRepository {

    override val authState: Flow<AuthState> = firebaseAuthService.authStateFlow.map { firebaseUser ->
        if (firebaseUser != null) {
            try {
                val response = apiService.getProfile()
                val user = response.data.toDomain()
                AuthState.Authenticated(user)
            } catch (e: Exception) {
                // User exists in Firebase but not in backend — auto-register
                try {
                    apiService.registerUser(
                        CreateUserRequestDto(
                            firebaseUid = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            displayName = firebaseUser.displayName,
                            photoUrl = firebaseUser.photoUrl?.toString()
                        )
                    )
                    // Newly registered user — needs role selection
                    AuthState.NeedsRoleSelection
                } catch (regError: Exception) {
                    // 409 means user already exists — retry getProfile
                    try {
                        val retryResponse = apiService.getProfile()
                        val user = retryResponse.data.toDomain()
                        AuthState.Authenticated(user)
                    } catch (retryError: Exception) {
                        AuthState.NeedsRoleSelection
                    }
                }
            }
        } else {
            AuthState.Unauthenticated
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val firebaseUser = firebaseAuthService.signInWithEmail(email, password)
            // Try to get existing profile; if not found, register
            val user = try {
                apiService.getProfile().data.toDomain()
            } catch (e: Exception) {
                // User exists in Firebase but not in backend — register them
                try {
                    apiService.registerUser(
                        CreateUserRequestDto(
                            firebaseUid = firebaseUser.uid,
                            email = firebaseUser.email ?: email,
                            displayName = firebaseUser.displayName,
                            photoUrl = firebaseUser.photoUrl?.toString()
                        )
                    ).data.toDomain()
                } catch (regError: Exception) {
                    // 409 means user already exists — retry getProfile
                    apiService.getProfile().data.toDomain()
                }
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<User> {
        return try {
            val firebaseUser = firebaseAuthService.signUpWithEmail(email, password, displayName)
            val response = apiService.registerUser(
                CreateUserRequestDto(
                    firebaseUid = firebaseUser.uid,
                    email = email,
                    displayName = displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
            )
            Result.success(response.data.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val firebaseUser = firebaseAuthService.signInWithGoogle(idToken)
            // Try to get existing profile; if not found, register
            val user = try {
                apiService.getProfile().data.toDomain()
            } catch (e: Exception) {
                apiService.registerUser(
                    CreateUserRequestDto(
                        firebaseUid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = firebaseUser.displayName,
                        photoUrl = firebaseUser.photoUrl?.toString()
                    )
                ).data.toDomain()
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuthService.signOut()
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            apiService.getProfile().data.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override fun isSignedIn(): Boolean = firebaseAuthService.isSignedIn
}
