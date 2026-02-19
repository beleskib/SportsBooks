package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.CreateUserRequestDto
import com.example.sportsbook.data.remote.dto.SetRoleRequestDto
import com.example.sportsbook.data.remote.dto.UpdateUserRequestDto
import com.example.sportsbook.domain.enums.PartnerType
import com.example.sportsbook.domain.enums.UserRole
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getProfile(): Result<User> = runCatching {
        apiService.getProfile().data.toDomain()
    }

    override suspend fun updateProfile(user: User): Result<User> = runCatching {
        apiService.updateProfile(
            UpdateUserRequestDto(
                displayName = user.displayName,
                photoUrl = user.photoUrl,
                phoneNumber = user.phoneNumber
            )
        ).data.toDomain()
    }

    override suspend fun setRole(role: UserRole, partnerType: PartnerType?): Result<User> = runCatching {
        apiService.setRole(SetRoleRequestDto(role = role, partnerType = partnerType)).data.toDomain()
    }

    override suspend fun registerUser(firebaseUid: String, email: String, displayName: String?): Result<User> = runCatching {
        apiService.registerUser(
            CreateUserRequestDto(
                firebaseUid = firebaseUid,
                email = email,
                displayName = displayName
            )
        ).data.toDomain()
    }
}
