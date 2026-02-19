package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.enums.PartnerType
import com.example.sportsbook.domain.enums.UserRole
import com.example.sportsbook.domain.model.User

interface UserRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(user: User): Result<User>
    suspend fun setRole(role: UserRole, partnerType: PartnerType? = null): Result<User>
    suspend fun registerUser(firebaseUid: String, email: String, displayName: String?): Result<User>
}
