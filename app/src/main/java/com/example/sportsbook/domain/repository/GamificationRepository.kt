package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.Achievement
import com.example.sportsbook.domain.model.PlayerAchievement
import com.example.sportsbook.domain.model.PlayerLevel
import com.example.sportsbook.domain.model.PlayerStats
import com.example.sportsbook.domain.model.XpTransaction

interface GamificationRepository {
    suspend fun getMyLevel(): Result<PlayerLevel>
    suspend fun getXpHistory(limit: Int? = null): Result<List<XpTransaction>>
    suspend fun getAllAchievements(): Result<List<Achievement>>
    suspend fun getMyAchievements(): Result<List<PlayerAchievement>>
    suspend fun getMyStats(): Result<PlayerStats>
    suspend fun checkAndAwardAchievements(): Result<CheckAchievementsResult>
    suspend fun redeemXp(bookingId: Long, xpAmount: Int): Result<RedeemXpResponse>
}

data class CheckAchievementsResult(
    val newlyEarned: List<PlayerAchievement> = emptyList(),
    val level: PlayerLevel = PlayerLevel()
)

data class RedeemXpResponse(
    val xpSpent: Int = 0,
    val discountAmount: Double = 0.0,
    val remainingXp: Int = 0,
    val newLevel: Int = 1
)
