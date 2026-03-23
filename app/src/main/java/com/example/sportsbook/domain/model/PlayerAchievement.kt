package com.example.sportsbook.domain.model

data class PlayerAchievement(
    val id: Long = 0,
    val userId: Long = 0,
    val achievementId: Long = 0,
    val earnedAt: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val category: String = "",
    val xpReward: Int = 0
)
