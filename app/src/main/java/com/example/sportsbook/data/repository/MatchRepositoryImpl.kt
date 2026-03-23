package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.CreateMatchRequestDto
import com.example.sportsbook.data.remote.dto.CreatePlayerRatingRequestDto
import com.example.sportsbook.data.remote.dto.JoinMatchRequestDto
import com.example.sportsbook.data.remote.dto.RespondToJoinRequestDto
import com.example.sportsbook.data.remote.dto.SendChatMessageRequestDto
import com.example.sportsbook.data.remote.dto.UpdateMatchRequestDto
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.MatchChatMessage
import com.example.sportsbook.domain.model.MatchParticipant
import com.example.sportsbook.domain.model.PlayerRating
import com.example.sportsbook.domain.repository.MatchRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : MatchRepository {

    override suspend fun createMatch(request: CreateMatchRequestDto): Result<Match> = runCatching {
        apiService.createMatch(request).data.toDomain()
    }

    override suspend fun listMatches(
        sportType: String?,
        status: String?,
        matchDate: String?,
        matchType: String?,
        minSkillLevel: Int?,
        maxSkillLevel: Int?,
        hostId: Long?
    ): Result<List<Match>> = runCatching {
        apiService.listMatches(sportType, status, matchDate, matchType, minSkillLevel, maxSkillLevel, hostId)
            .data.map { it.toDomain() }
    }

    override suspend fun getMyMatches(): Result<List<Match>> = runCatching {
        apiService.getMyMatches().data.map { it.toDomain() }
    }

    override suspend fun getNearbyMatches(lat: Double, lng: Double, radius: Int): Result<List<Match>> = runCatching {
        apiService.getNearbyMatches(lat, lng, radius).data.map { it.toDomain() }
    }

    override suspend fun getMatchById(id: Long): Result<Match> = runCatching {
        apiService.getMatchById(id).data.toDomain()
    }

    override suspend fun updateMatch(id: Long, request: UpdateMatchRequestDto): Result<Match> = runCatching {
        apiService.updateMatch(id, request).data.toDomain()
    }

    override suspend fun cancelMatch(id: Long): Result<Match> = runCatching {
        apiService.cancelMatch(id).data.toDomain()
    }

    override suspend fun joinMatch(matchId: Long): Result<MatchParticipant> = runCatching {
        apiService.joinMatch(matchId, JoinMatchRequestDto()).data.toDomain()
    }

    override suspend fun leaveMatch(matchId: Long): Result<Unit> = runCatching {
        apiService.leaveMatch(matchId)
        Unit
    }

    override suspend fun getParticipants(matchId: Long): Result<List<MatchParticipant>> = runCatching {
        apiService.getMatchParticipants(matchId).data.map { it.toDomain() }
    }

    override suspend fun respondToJoinRequest(
        matchId: Long,
        participantId: Long,
        approve: Boolean
    ): Result<MatchParticipant> = runCatching {
        val status = if (approve) "approved" else "declined"
        apiService.respondToJoinRequest(matchId, participantId, RespondToJoinRequestDto(status)).data.toDomain()
    }

    override suspend fun getChatMessages(matchId: Long, since: String?, limit: Int?): Result<List<MatchChatMessage>> = runCatching {
        apiService.getMatchChat(matchId, since, limit).data.map { it.toDomain() }
    }

    override suspend fun sendChatMessage(matchId: Long, content: String): Result<MatchChatMessage> = runCatching {
        apiService.sendMatchChatMessage(matchId, SendChatMessageRequestDto(content)).data.toDomain()
    }

    override suspend fun ratePlayer(matchId: Long, request: CreatePlayerRatingRequestDto): Result<PlayerRating> = runCatching {
        apiService.ratePlayer(matchId, request).data.toDomain()
    }

    override suspend fun getMatchRatings(matchId: Long): Result<List<PlayerRating>> = runCatching {
        apiService.getMatchRatings(matchId).data.map { it.toDomain() }
    }

    override suspend fun getPlayerRatings(userId: Long): Result<List<PlayerRating>> = runCatching {
        apiService.getPlayerRatings(userId).data.map { it.toDomain() }
    }
}
