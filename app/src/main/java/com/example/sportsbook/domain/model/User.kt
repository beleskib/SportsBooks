package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.PartnerType
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.enums.UserRole

data class User(
    val id: Long = 0,
    val firebaseUid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val bio: String? = null,
    val dateOfBirth: String? = null,
    val onboardingCompleted: Boolean = false,
    val role: UserRole = UserRole.PLAYER,
    val partnerType: PartnerType? = null,
    val interestedSports: List<SportType> = emptyList(),
    val sportExpertise: List<UserSportExpertise> = emptyList(),
    val isActive: Boolean = true,
    val avgPlayerSkillRating: Double = 0.0,
    val avgPlayerSportsmanshipRating: Double = 0.0,
    val avgPlayerPunctualityRating: Double = 0.0,
    val totalPlayerRatings: Int = 0,
    val totalMatchesPlayed: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
