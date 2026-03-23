package com.example.sportsbook.domain.model

data class PlayerRating(
    val id: Long = 0,
    val matchId: Long = 0,
    val raterId: Long = 0,
    val raterName: String? = null,
    val ratedId: Long = 0,
    val ratedName: String? = null,
    val skillRating: Int = 0,
    val sportsmanshipRating: Int = 0,
    val punctualityRating: Int = 0,
    val comment: String? = null,
    val createdAt: String? = null
) {
    val averageRating: Double
        get() = (skillRating + sportsmanshipRating + punctualityRating) / 3.0
}
