package com.example.sportsbook.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class StripeConnectOnboardingResponseDto(
    val onboardingUrl: String,
    val stripeAccountId: String,
)
@Serializable
data class StripeAccountStatusResponseDto(
    val stripeAccountId: String? = null,
    val onboardingStatus: String,
    val payoutsEnabled: Boolean,
    val dashboardUrl: String? = null,
)

@Serializable
data class StripeDashboardLinkResponseDto(
    val dashboardUrl: String,
)
