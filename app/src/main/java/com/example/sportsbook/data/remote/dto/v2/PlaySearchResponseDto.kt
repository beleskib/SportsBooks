package com.example.sportsbook.data.remote.dto.v2

import com.example.sportsbook.domain.model.v2.PlaySearchResult
import kotlinx.serialization.Serializable

// ============================================================
// v2-practical-ux: DTOs for GET /api/play/search
// ============================================================

@Serializable
data class PlaySearchCountsDto(
    val lobbies: Int = 0,
    val openSlots: Int = 0,
    val availablePlayers: Int = 0
)

@Serializable
data class PlaySearchResponseDto(
    val results: List<PlaySuggestionDto> = emptyList(),
    val counts: PlaySearchCountsDto = PlaySearchCountsDto()
) {
    fun toDomain(): PlaySearchResult = PlaySearchResult(
        results = results.map { it.toDomain() },
        lobbies = counts.lobbies,
        openSlots = counts.openSlots,
        availablePlayers = counts.availablePlayers
    )
}
