package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.AvailablePlayer

interface AvailablePlayerRepository {
    suspend fun register(sportType: String, skillLevel: Int?, note: String?): Result<AvailablePlayer>
    suspend fun unregister(sportType: String): Result<Unit>
    suspend fun getMyAvailability(): Result<List<AvailablePlayer>>
    suspend fun listBySport(sportType: String, skillMin: Int? = null, skillMax: Int? = null): Result<List<AvailablePlayer>>
    suspend fun inviteToMatch(matchId: Long, userId: Long): Result<Unit>
}
