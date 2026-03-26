package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.model.Community
import com.example.sportsbook.domain.model.CommunityMember
import kotlinx.serialization.Serializable

@Serializable
data class CommunityDto(
    val id: Long = 0,
    val ownerId: Long = 0,
    val ownerName: String? = null,
    val name: String = "",
    val description: String? = null,
    val sportType: String? = null,
    val imageUrl: String? = null,
    val maxMembers: Int = 50,
    val isPublic: Boolean = false,
    val invitePolicy: String = "friends_only",
    val memberCount: Int = 0,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Community = Community(
        id = id,
        ownerId = ownerId,
        ownerName = ownerName,
        name = name,
        description = description,
        sportType = sportType,
        imageUrl = imageUrl,
        maxMembers = maxMembers,
        isPublic = isPublic,
        invitePolicy = invitePolicy,
        memberCount = memberCount,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Serializable
data class CommunityMemberDto(
    val id: Long = 0,
    val communityId: Long = 0,
    val userId: Long = 0,
    val displayName: String = "",
    val photoUrl: String? = null,
    val role: String = "member",
    val status: String = "pending",
    val invitedBy: Long? = null,
    val createdAt: String? = null
) {
    fun toDomain(): CommunityMember = CommunityMember(
        id = id,
        communityId = communityId,
        userId = userId,
        displayName = displayName,
        photoUrl = photoUrl,
        role = role,
        status = status,
        invitedBy = invitedBy,
        createdAt = createdAt
    )
}

// ── Request DTOs ──

@Serializable
data class CreateCommunityRequestDto(
    val name: String,
    val description: String? = null,
    val sportType: String? = null,
    val maxMembers: Int = 50,
    val isPublic: Boolean = false,
    val invitePolicy: String = "friends_only"
)

@Serializable
data class UpdateCommunityRequestDto(
    val name: String? = null,
    val description: String? = null,
    val sportType: String? = null,
    val maxMembers: Int? = null,
    val isPublic: Boolean? = null,
    val invitePolicy: String? = null
)

@Serializable
data class InviteToCommunityRequestDto(
    val userId: Long
)

@Serializable
data class RespondToMemberRequestDto(
    val status: String
)
