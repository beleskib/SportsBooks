package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.Friendship

interface FriendshipRepository {
    suspend fun getMyFriends(): Result<List<Friendship>>
    suspend fun getPendingRequests(): Result<List<Friendship>>
    suspend fun sendFriendRequest(userId: Long): Result<Friendship>
    suspend fun respondToFriendRequest(friendshipId: Long, accept: Boolean): Result<Friendship>
    suspend fun removeFriend(friendId: Long): Result<Unit>
    suspend fun searchUsers(query: String): Result<List<Friendship>>
}
