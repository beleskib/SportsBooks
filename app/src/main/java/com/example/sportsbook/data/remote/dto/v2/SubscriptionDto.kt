package com.example.sportsbook.data.remote.dto.v2

import kotlinx.serialization.Serializable

// ============================================================
// v2-practical-ux: SportsBooks+ subscription DTOs.
// Maps to src/shared/types/subscription.ts contracts.
// ============================================================

@Serializable
data class SubscriptionInfoDto(
    val id: Long,
    val status: String,
    val currentPeriodEnd: String,
    val cancelAtPeriodEnd: Boolean,
    val trialEnd: String? = null
)

@Serializable
data class SubscriptionStatusDto(
    val isPlus: Boolean,
    val subscription: SubscriptionInfoDto? = null,
    val profileVisibility: String = "public"
)

@Serializable
data class CheckoutResponseDto(
    val checkoutUrl: String,
    val sessionId: String
)

@Serializable
data class SetVisibilityRequestDto(
    val visibility: String
)
