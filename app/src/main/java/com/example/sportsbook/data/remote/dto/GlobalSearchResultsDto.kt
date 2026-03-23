package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.model.GlobalSearchResults
import kotlinx.serialization.Serializable

@Serializable
data class GlobalSearchResultsDto(
    val venues: List<VenueDto> = emptyList(),
    val coaches: List<CoachDto> = emptyList(),
    val matches: List<MatchDto> = emptyList()
) {
    fun toDomain() = GlobalSearchResults(
        venues = venues.map { it.toDomain() },
        coaches = coaches.map { it.toDomain() },
        matches = matches.map { it.toDomain() }
    )
}
