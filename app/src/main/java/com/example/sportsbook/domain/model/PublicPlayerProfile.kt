package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.SportType

data class PublicPlayerProfile(
    val id: Long = 0,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val bio: String? = null,
    val interestedSports: List<SportType> = emptyList(),
    val sportExpertise: List<UserSportExpertise> = emptyList(),
    val avgPlayerSkillRating: Double = 0.0,
    val avgPlayerSportsmanshipRating: Double = 0.0,
    val avgPlayerPunctualityRating: Double = 0.0,
    val totalPlayerRatings: Int = 0,
    val totalMatchesPlayed: Int = 0,
    val recentMatches: List<PublicMatchSummary> = emptyList(),
    val createdAt: String? = null,
    val friendshipStatus: String? = null,
    val friendshipId: Long? = null
)

data class PublicMatchSummary(
    val id: Long = 0,
    val title: String = "",
    val sportType: SportType = SportType.BASKETBALL,
    val matchDate: String = "",
    val status: String = ""
)
