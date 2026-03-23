package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.InviteToMatchRequestDto
import com.example.sportsbook.data.remote.dto.RegisterAvailableRequestDto
import com.example.sportsbook.data.remote.dto.toDomain
import com.example.sportsbook.domain.model.AvailablePlayer
import com.example.sportsbook.domain.repository.AvailablePlayerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvailablePlayerRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AvailablePlayerRepository {

    override suspend fun register(
        sportType: String,
        skillLevel: Int?,
        note: String?
    ): Result<AvailablePlayer> = runCatching {
        withContext(Dispatchers.IO) {
            apiService.registerAvailable(
                RegisterAvailableRequestDto(
                    sportType = sportType,
                    skillLevel = skillLevel,
                    note = note
                )
            ).data.toDomain()
        }
    }

    override suspend fun unregister(sportType: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            apiService.unregisterAvailable(sportType)
            Unit
        }
    }

    override suspend fun getMyAvailability(): Result<List<AvailablePlayer>> = runCatching {
        withContext(Dispatchers.IO) {
            apiService.getMyAvailability().data.map { it.toDomain() }
        }
    }

    override suspend fun listBySport(
        sportType: String,
        skillMin: Int?,
        skillMax: Int?
    ): Result<List<AvailablePlayer>> = runCatching {
        withContext(Dispatchers.IO) {
            apiService.listAvailablePlayers(sportType, skillMin, skillMax).data.map { it.toDomain() }
        }
    }

    override suspend fun inviteToMatch(matchId: Long, userId: Long): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            apiService.inviteToMatch(matchId, InviteToMatchRequestDto(userId))
            Unit
        }
    }
}
