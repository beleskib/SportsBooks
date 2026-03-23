package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.ExperienceDuration
import com.example.sportsbook.domain.enums.SkillLevel
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.UserSportExpertise
import kotlinx.serialization.Serializable

@Serializable
data class SportExpertiseDto(
    val id: Long = 0,
    val userId: Long = 0,
    val sportType: SportType,
    val skillLevel: SkillLevel,
    val experienceDuration: ExperienceDuration,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): UserSportExpertise = UserSportExpertise(
        id = id,
        userId = userId,
        sportType = sportType,
        skillLevel = skillLevel,
        experienceDuration = experienceDuration,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Serializable
data class SportExpertiseEntryDto(
    val sportType: SportType,
    val skillLevel: SkillLevel,
    val experienceDuration: ExperienceDuration
)

@Serializable
data class CompleteOnboardingRequestDto(
    val displayName: String? = null,
    val photoUrl: String? = null,
    val dateOfBirth: String? = null,
    val bio: String? = null,
    val interestedSports: List<SportType> = emptyList(),
    val expertise: List<SportExpertiseEntryDto> = emptyList()
)

@Serializable
data class SportExpertiseResponseDto(
    val sportExpertise: List<SportExpertiseDto>
)
