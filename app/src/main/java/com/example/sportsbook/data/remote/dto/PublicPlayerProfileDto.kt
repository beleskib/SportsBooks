package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.PublicMatchSummary
import com.example.sportsbook.domain.model.PublicPlayerProfile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PublicPlayerProfileDto(
    val id: Long = 0,
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("photoUrl") val photoUrl: String? = null,
    val bio: String? = null,
    @SerialName("interestedSports") val interestedSports: List<String> = emptyList(),
    @SerialName("sportExpertise") val sportExpertise: List<SportExpertiseEntryDto> = emptyList(),
    @SerialName("avgPlayerSkillRating") val avgPlayerSkillRating: Double = 0.0,
    @SerialName("avgPlayerSportsmanshipRating") val avgPlayerSportsmanshipRating: Double = 0.0,
    @SerialName("avgPlayerPunctualityRating") val avgPlayerPunctualityRating: Double = 0.0,
    @SerialName("totalPlayerRatings") val totalPlayerRatings: Int = 0,
    @SerialName("totalMatchesPlayed") val totalMatchesPlayed: Int = 0,
    @SerialName("recentMatches") val recentMatches: List<PublicMatchSummaryDto> = emptyList(),
    @SerialName("createdAt") val createdAt: String? = null
) {
    fun toDomain() = PublicPlayerProfile(
        id = id,
        displayName = displayName,
        photoUrl = photoUrl,
        bio = bio,
        interestedSports = interestedSports.mapNotNull { name ->
            try { SportType.valueOf(name.uppercase()) } catch (_: Exception) { null }
        },
        sportExpertise = emptyList(), // Simplified — expertise mapping handled separately
        avgPlayerSkillRating = avgPlayerSkillRating,
        avgPlayerSportsmanshipRating = avgPlayerSportsmanshipRating,
        avgPlayerPunctualityRating = avgPlayerPunctualityRating,
        totalPlayerRatings = totalPlayerRatings,
        totalMatchesPlayed = totalMatchesPlayed,
        recentMatches = recentMatches.map { it.toDomain() },
        createdAt = createdAt
    )
}

@Serializable
data class PublicMatchSummaryDto(
    val id: Long = 0,
    val title: String = "",
    @SerialName("sportType") val sportType: String = "",
    @SerialName("matchDate") val matchDate: String = "",
    val status: String = ""
) {
    fun toDomain() = PublicMatchSummary(
        id = id,
        title = title,
        sportType = try { SportType.valueOf(sportType.uppercase()) } catch (_: Exception) { SportType.BASKETBALL },
        matchDate = matchDate,
        status = status
    )
}
