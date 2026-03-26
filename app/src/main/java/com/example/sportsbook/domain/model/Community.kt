package com.example.sportsbook.domain.model

data class Community(
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
    val spotsLeft: Int get() = maxMembers - memberCount
    val isFull: Boolean get() = memberCount >= maxMembers
}

data class CommunityMember(
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
    val isAdmin: Boolean get() = role == "admin" || role == "owner"
    val isOwner: Boolean get() = role == "owner"
    val isPending: Boolean get() = status == "pending"
    val isApproved: Boolean get() = status == "approved" || status == "active"
}
