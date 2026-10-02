package com.example.sportsbook.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTokenProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        return try {
            firebaseAuth.currentUser
                ?.getIdToken(forceRefresh)
                ?.await()
                ?.token
        } catch (e: Exception) {
            null
        }
    }
}
