package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.model.Friendship
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FriendshipDto(
    val id: Long = 0,
    @SerialName("requesterId") val requesterId: Long = 0,
    @SerialName("addresseeId") val addresseeId: Long = 0,
    @SerialName("status") val status: String = "pending",
    @SerialName("user") val user: FriendUserDto? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("updatedAt") val updatedAt: String? = null
) {
    fun toDomain(): Friendship {
        // Backend now always provides the "other user" in the `user` field,
        // so we don't need currentUserId to determine the friend.
        return Friendship(
            id = id,
            friendId = user?.id ?: 0,
            friendName = user?.displayName,
            friendPhotoUrl = user?.photoUrl,
            status = status,
            createdAt = createdAt
        )
    }
}

@Serializable
data class FriendUserDto(
    val id: Long = 0,
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("photoUrl") val photoUrl: String? = null
)

@Serializable
data class SendFriendRequestDto(
    @SerialName("userId") val userId: Long
)

@Serializable
data class RespondToFriendRequestDto(
    @SerialName("accept") val accept: Boolean
)

@Serializable
data class UserSearchResultDto(
    val id: Long = 0,
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("photoUrl") val photoUrl: String? = null
) {
    fun toDomain() = Friendship(
        id = 0,
        friendId = id,
        friendName = displayName,
        friendPhotoUrl = photoUrl,
        status = "none",
        createdAt = null
    )
}
