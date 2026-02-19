package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.PartnerType
import com.example.sportsbook.domain.enums.UserRole

data class User(
    val id: Long = 0,
    val firebaseUid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val role: UserRole = UserRole.PLAYER,
    val partnerType: PartnerType? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
