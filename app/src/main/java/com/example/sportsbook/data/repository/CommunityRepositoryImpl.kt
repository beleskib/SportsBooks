package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.CreateCommunityRequestDto
import com.example.sportsbook.data.remote.dto.CreateLobbyRequestDto
import com.example.sportsbook.data.remote.dto.InviteToCommunityRequestDto
import com.example.sportsbook.data.remote.dto.RespondToMemberRequestDto
import com.example.sportsbook.data.remote.dto.UpdateCommunityRequestDto
import com.example.sportsbook.domain.model.Community
import com.example.sportsbook.domain.model.CommunityMember
import com.example.sportsbook.domain.model.Lobby
import com.example.sportsbook.domain.repository.CommunityRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : CommunityRepository {

    override suspend fun createCommunity(request: CreateCommunityRequestDto): Result<Community> = runCatching {
        apiService.createCommunity(request).data.toDomain()
    }

    override suspend fun getMyCommunities(): Result<List<Community>> = runCatching {
        apiService.getMyCommunities().data.map { it.toDomain() }
    }

    override suspend fun getPublicCommunities(sportType: String?): Result<List<Community>> = runCatching {
        apiService.getPublicCommunities(sportType).data.map { it.toDomain() }
    }

    override suspend fun getCommunityById(id: Long): Result<Community> = runCatching {
        apiService.getCommunityById(id).data.toDomain()
    }

    override suspend fun updateCommunity(id: Long, request: UpdateCommunityRequestDto): Result<Community> = runCatching {
        apiService.updateCommunity(id, request).data.toDomain()
    }

    override suspend fun inviteToCommunity(communityId: Long, userId: Long): Result<Unit> = runCatching {
        apiService.inviteToCommunity(communityId, InviteToCommunityRequestDto(userId))
        Unit
    }

    override suspend fun joinCommunity(communityId: Long): Result<CommunityMember> = runCatching {
        apiService.joinCommunity(communityId).data.toDomain()
    }

    override suspend fun getCommunityMembers(communityId: Long, status: String?): Result<List<CommunityMember>> = runCatching {
        apiService.getCommunityMembers(communityId, status).data.map { it.toDomain() }
    }

    override suspend fun respondToMember(communityId: Long, userId: Long, approve: Boolean): Result<CommunityMember> = runCatching {
        val status = if (approve) "approved" else "rejected"
        apiService.respondToMember(communityId, userId, RespondToMemberRequestDto(status)).data.toDomain()
    }

    override suspend fun removeCommunityMember(communityId: Long, userId: Long): Result<Unit> = runCatching {
        apiService.removeCommunityMember(communityId, userId)
        Unit
    }

    override suspend fun createLobby(communityId: Long, request: CreateLobbyRequestDto): Result<Lobby> = runCatching {
        apiService.createLobby(communityId, request).data.toDomain()
    }

    override suspend fun getCommunityLobbies(communityId: Long, status: String?): Result<List<Lobby>> = runCatching {
        apiService.getCommunityLobbies(communityId, status).data.map { it.toDomain() }
    }

    override suspend fun getPublicLobbies(sportType: String?): Result<List<Lobby>> = runCatching {
        apiService.getPublicLobbies(sportType).data.map { it.toDomain() }
    }

    override suspend fun getLobbyById(id: Long): Result<Lobby> = runCatching {
        apiService.getLobbyById(id).data.toDomain()
    }

    override suspend fun joinLobby(lobbyId: Long): Result<Lobby> = runCatching {
        apiService.joinLobby(lobbyId).data.toDomain()
    }

    override suspend fun leaveLobby(lobbyId: Long): Result<Lobby> = runCatching {
        apiService.leaveLobby(lobbyId).data.toDomain()
    }

    override suspend fun makeLobbyPublic(lobbyId: Long): Result<Lobby> = runCatching {
        apiService.makeLobbyPublic(lobbyId).data.toDomain()
    }
}
