package com.example.sportsbook.domain.model

data class GlobalSearchResults(
    val venues: List<Venue> = emptyList(),
    val coaches: List<Coach> = emptyList(),
    val matches: List<Match> = emptyList()
)
