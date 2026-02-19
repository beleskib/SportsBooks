package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.PartnerType
import com.example.sportsbook.domain.enums.UserRole
import com.example.sportsbook.domain.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val firebaseUid: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val role: UserRole,
    val partnerType: PartnerType? = null,
    val isActive: Boolean,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): User = User(
        id = id,
        firebaseUid = firebaseUid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl,
        phoneNumber = phoneNumber,
        role = role,
        partnerType = partnerType,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Serializable
data class CreateUserRequestDto(
    val firebaseUid: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null
)

@Serializable
data class UpdateUserRequestDto(
    val displayName: String? = null,
    val photoUrl: String? = null,
    val phoneNumber: String? = null
)

@Serializable
data class SetRoleRequestDto(
    val role: UserRole,
    val partnerType: PartnerType? = null
)
