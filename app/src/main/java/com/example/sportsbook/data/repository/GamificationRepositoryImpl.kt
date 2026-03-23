package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.domain.model.Achievement
import com.example.sportsbook.domain.model.PlayerAchievement
import com.example.sportsbook.domain.model.PlayerLevel
import com.example.sportsbook.domain.model.PlayerStats
import com.example.sportsbook.domain.model.XpTransaction
import com.example.sportsbook.domain.repository.CheckAchievementsResult
import com.example.sportsbook.domain.repository.GamificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamificationRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : GamificationRepository {

    override suspend fun getMyLevel(): Result<PlayerLevel> = runCatching {
        apiService.getMyLevel().data.toDomain()
    }

    override suspend fun getXpHistory(limit: Int?): Result<List<XpTransaction>> = runCatching {
        apiService.getXpHistory(limit).data.map { it.toDomain() }
    }

    override suspend fun getAllAchievements(): Result<List<Achievement>> = runCatching {
        apiService.getAllAchievements().data.map { it.toDomain() }
    }

    override suspend fun getMyAchievements(): Result<List<PlayerAchievement>> = runCatching {
        apiService.getMyAchievements().data.map { it.toDomain() }
    }

    override suspend fun getMyStats(): Result<PlayerStats> = runCatching {
        apiService.getMyStats().data.toDomain()
    }

    override suspend fun checkAndAwardAchievements(): Result<CheckAchievementsResult> = runCatching {
        apiService.checkAndAwardAchievements().data.toDomain()
    }
}
