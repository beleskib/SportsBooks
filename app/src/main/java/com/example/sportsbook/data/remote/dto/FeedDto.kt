package com.example.sportsbook.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FollowCountsDto(
    val followers: Int = 0,
    val following: Int = 0
)

@Serializable
data class FeedPostDto(
    val id: Long = 0,
    val userId: Long = 0,
    val authorName: String = "",
    val authorPhotoUrl: String? = null,
    val postType: String = "text",
    val content: String? = null,
    val imageUrl: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val createdAt: String = ""
)

@Serializable
data class FeedCommentDto(
    val id: Long = 0,
    val postId: Long = 0,
    val userId: Long = 0,
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val content: String = "",
    val createdAt: String = ""
)

@Serializable
data class CreateFeedPostRequestDto(
    val postType: String = "text",
    val content: String? = null,
    val imageUrl: String? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
data class AddCommentRequestDto(
    val content: String
)
