package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.MatchStatus
import com.example.sportsbook.domain.enums.MatchType
import com.example.sportsbook.domain.enums.MatchVisibility
import com.example.sportsbook.domain.enums.SportType

data class Match(
    val id: Long = 0,
    val hostId: Long = 0,
    val hostName: String? = null,
    val hostPhotoUrl: String? = null,
    val bookingId: Long? = null,
    val venueId: Long? = null,
    val venueName: String? = null,
    val sportType: SportType = SportType.BASKETBALL,
    val matchType: MatchType = MatchType.STANDALONE,
    val status: MatchStatus = MatchStatus.DRAFT,
    val visibility: MatchVisibility = MatchVisibility.PUBLIC,
    val title: String = "",
    val description: String? = null,
    val matchDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val minPlayers: Int = 2,
    val maxPlayers: Int = 10,
    val currentPlayers: Int = 1,
    val minSkillLevel: Int? = null,
    val maxSkillLevel: Int? = null,
    val locationName: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isFree: Boolean = true,
    val costPerPlayer: Double = 0.0,
    val recurrenceRuleId: Long? = null,
    val parentMatchId: Long? = null,
    val participants: List<MatchParticipant> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val spotsLeft: Int get() = maxPlayers - currentPlayers
    val isFull: Boolean get() = currentPlayers >= maxPlayers
    val displayLocation: String get() = locationName ?: venueName ?: address ?: "Location TBD"
    val displayTime: String get() = "$startTime - $endTime"
}
