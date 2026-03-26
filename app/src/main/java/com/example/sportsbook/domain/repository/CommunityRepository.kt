package com.example.sportsbook.domain.repository

import com.example.sportsbook.data.remote.dto.CreateCommunityRequestDto
import com.example.sportsbook.data.remote.dto.CreateLobbyRequestDto
import com.example.sportsbook.data.remote.dto.UpdateCommunityRequestDto
import com.example.sportsbook.domain.model.Community
import com.example.sportsbook.domain.model.CommunityMember
import com.example.sportsbook.domain.model.Lobby

interface CommunityRepository {

    // Communities
    suspend fun createCommunity(request: CreateCommunityRequestDto): Result<Community>
    suspend fun getMyCommunities(): Result<List<Community>>
    suspend fun getPublicCommunities(sportType: String? = null): Result<List<Community>>
    suspend fun getCommunityById(id: Long): Result<Community>
    suspend fun updateCommunity(id: Long, request: UpdateCommunityRequestDto): Result<Community>

    // Members
    suspend fun inviteToCommunity(communityId: Long, userId: Long): Result<Unit>
    suspend fun joinCommunity(communityId: Long): Result<CommunityMember>
    suspend fun getCommunityMembers(communityId: Long, status: String? = null): Result<List<CommunityMember>>
    suspend fun respondToMember(communityId: Long, userId: Long, approve: Boolean): Result<CommunityMember>
    suspend fun removeCommunityMember(communityId: Long, userId: Long): Result<Unit>

    // Lobbies
    suspend fun createLobby(communityId: Long, request: CreateLobbyRequestDto): Result<Lobby>
    suspend fun getCommunityLobbies(communityId: Long, status: String? = null): Result<List<Lobby>>
    suspend fun getPublicLobbies(sportType: String? = null): Result<List<Lobby>>
    suspend fun getLobbyById(id: Long): Result<Lobby>
    suspend fun joinLobby(lobbyId: Long): Result<Lobby>
    suspend fun leaveLobby(lobbyId: Long): Result<Lobby>
    suspend fun makeLobbyPublic(lobbyId: Long): Result<Lobby>
}
