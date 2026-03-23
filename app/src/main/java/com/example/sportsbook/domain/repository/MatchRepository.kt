package com.example.sportsbook.domain.repository

import com.example.sportsbook.data.remote.dto.CreateMatchRequestDto
import com.example.sportsbook.data.remote.dto.CreatePlayerRatingRequestDto
import com.example.sportsbook.data.remote.dto.UpdateMatchRequestDto
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.MatchChatMessage
import com.example.sportsbook.domain.model.MatchParticipant
import com.example.sportsbook.domain.model.PlayerRating

interface MatchRepository {
    // Matches
    suspend fun createMatch(request: CreateMatchRequestDto): Result<Match>
    suspend fun listMatches(
        sportType: String? = null,
        status: String? = null,
        matchDate: String? = null,
        matchType: String? = null,
        minSkillLevel: Int? = null,
        maxSkillLevel: Int? = null,
        hostId: Long? = null
    ): Result<List<Match>>
    suspend fun getMyMatches(): Result<List<Match>>
    suspend fun getNearbyMatches(lat: Double, lng: Double, radius: Int = 25): Result<List<Match>>
    suspend fun getMatchById(id: Long): Result<Match>
    suspend fun updateMatch(id: Long, request: UpdateMatchRequestDto): Result<Match>
    suspend fun cancelMatch(id: Long): Result<Match>

    // Participants
    suspend fun joinMatch(matchId: Long): Result<MatchParticipant>
    suspend fun leaveMatch(matchId: Long): Result<Unit>
    suspend fun getParticipants(matchId: Long): Result<List<MatchParticipant>>
    suspend fun respondToJoinRequest(matchId: Long, participantId: Long, approve: Boolean): Result<MatchParticipant>

    // Chat
    suspend fun getChatMessages(matchId: Long, since: String? = null, limit: Int? = null): Result<List<MatchChatMessage>>
    suspend fun sendChatMessage(matchId: Long, content: String): Result<MatchChatMessage>

    // Ratings
    suspend fun ratePlayer(matchId: Long, request: CreatePlayerRatingRequestDto): Result<PlayerRating>
    suspend fun getMatchRatings(matchId: Long): Result<List<PlayerRating>>
    suspend fun getPlayerRatings(userId: Long): Result<List<PlayerRating>>
}
