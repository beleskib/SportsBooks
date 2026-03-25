package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.RespondToFriendRequestDto
import com.example.sportsbook.data.remote.dto.SendFriendRequestDto
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.domain.repository.FriendshipRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendshipRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : FriendshipRepository {

    override suspend fun getMyFriends(): Result<List<Friendship>> = runCatching {
        apiService.getMyFriends().data.map { it.toDomain() }
    }

    override suspend fun getPendingRequests(): Result<List<Friendship>> = runCatching {
        apiService.getPendingRequests().data.map { it.toDomain() }
    }

    override suspend fun sendFriendRequest(userId: Long): Result<Friendship> = runCatching {
        apiService.sendFriendRequest(SendFriendRequestDto(userId)).data.toDomain()
    }

    override suspend fun respondToFriendRequest(friendshipId: Long, accept: Boolean): Result<Friendship> = runCatching {
        apiService.respondToFriendRequest(friendshipId, RespondToFriendRequestDto(accept)).data.toDomain()
    }

    override suspend fun removeFriend(friendId: Long): Result<Unit> = runCatching {
        apiService.removeFriend(friendId)
        Unit
    }

    override suspend fun searchUsers(query: String): Result<List<Friendship>> = runCatching {
        apiService.searchUsers(query).data.map { it.toDomain() }
    }
}
