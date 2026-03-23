package com.example.sportsbook.domain.model

data class PlayerStats(
    val totalBookings: Int = 0,
    val completedBookings: Int = 0,
    val totalMatches: Int = 0,
    val completedMatches: Int = 0,
    val totalReviews: Int = 0,
    val totalFriends: Int = 0,
    val uniqueSportsBooked: Int = 0,
    val hasEarlyBirdBooking: Boolean = false,
    val hasNightOwlBooking: Boolean = false,
    val totalHoursPlayed: Int = 0,
    val memberSinceDays: Int = 0,
    val favoriteSport: String? = null
)
