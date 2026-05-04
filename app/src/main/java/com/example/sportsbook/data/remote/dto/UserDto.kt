package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.PartnerType
import com.example.sportsbook.domain.enums.SportType
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
    val bio: String? = null,
    val dateOfBirth: String? = null,
    val onboardingCompleted: Boolean = false,
    val role: UserRole,
    val partnerType: PartnerType? = null,
    val interestedSports: List<SportType> = emptyList(),
    val sportExpertise: List<SportExpertiseDto> = emptyList(),
    val isActive: Boolean,
    val avgPlayerSkillRating: Double = 0.0,
    val avgPlayerSportsmanshipRating: Double = 0.0,
    val avgPlayerPunctualityRating: Double = 0.0,
    val totalPlayerRatings: Int = 0,
    val totalMatchesPlayed: Int = 0,
    val isPlus: Boolean = false,
    val profileVisibility: String = "public",
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
        bio = bio,
        dateOfBirth = dateOfBirth,
        onboardingCompleted = onboardingCompleted,
        role = role,
        partnerType = partnerType,
        interestedSports = interestedSports,
        sportExpertise = sportExpertise.map { it.toDomain() },
        isActive = isActive,
        avgPlayerSkillRating = avgPlayerSkillRating,
        avgPlayerSportsmanshipRating = avgPlayerSportsmanshipRating,
        avgPlayerPunctualityRating = avgPlayerPunctualityRating,
        totalPlayerRatings = totalPlayerRatings,
        totalMatchesPlayed = totalMatchesPlayed,
        isPlus = isPlus,
        profileVisibility = profileVisibility,
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
    val phoneNumber: String? = null,
    val bio: String? = null,
    val dateOfBirth: String? = null
)

@Serializable
data class SetRoleRequestDto(
    val role: UserRole,
    val partnerType: PartnerType? = null
)

@Serializable
data class UpdateInterestedSportsRequestDto(
    val sportTypes: List<SportType>
)

@Serializable
data class InterestedSportsResponseDto(
    val interestedSports: List<SportType>
)
