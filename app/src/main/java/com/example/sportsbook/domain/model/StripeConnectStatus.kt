package com.example.sportsbook.domain.model

data class StripeConnectStatus(
    val stripeAccountId: String?,
    val onboardingStatus: OnboardingStatus,
    val payoutsEnabled: Boolean,
    val dashboardUrl: String?,
)

enum class OnboardingStatus {
    NOT_STARTED,
    PENDING,
    COMPLETE;

    companion object {
        fun fromString(value: String): OnboardingStatus = when (value) {
            "complete" -> COMPLETE
            "pending" -> PENDING
            else -> NOT_STARTED
        }
    }
}
