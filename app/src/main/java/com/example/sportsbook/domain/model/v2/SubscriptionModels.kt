package com.example.sportsbook.domain.model.v2

// ============================================================
// v2-practical-ux: SportsBooks+ subscription domain models.
// ============================================================

enum class SubscriptionStatus {
    TRIALING, ACTIVE, PAST_DUE, CANCELED, UNPAID;

    companion object {
        fun fromString(value: String): SubscriptionStatus = when (value) {
            "trialing" -> TRIALING
            "active" -> ACTIVE
            "past_due" -> PAST_DUE
            "canceled" -> CANCELED
            "unpaid" -> UNPAID
            else -> CANCELED
        }
    }
}

enum class ProfileVisibility {
    PUBLIC, FRIENDS_ONLY, PRIVATE;

    val apiValue: String
        get() = when (this) {
            PUBLIC -> "public"
            FRIENDS_ONLY -> "friends_only"
            PRIVATE -> "private"
        }

    val displayLabel: String
        get() = when (this) {
            PUBLIC -> "Public"
            FRIENDS_ONLY -> "Friends Only"
            PRIVATE -> "Private"
        }

    val description: String
        get() = when (this) {
            PUBLIC -> "Anyone can view your profile"
            FRIENDS_ONLY -> "Only accepted friends can see your full profile"
            PRIVATE -> "Your profile is hidden from everyone"
        }

    companion object {
        fun fromString(value: String): ProfileVisibility = when (value) {
            "public" -> PUBLIC
            "friends_only" -> FRIENDS_ONLY
            "private" -> PRIVATE
            else -> PUBLIC
        }
    }
}

data class SubscriptionInfo(
    val id: Long,
    val status: SubscriptionStatus,
    val currentPeriodEnd: String,
    val cancelAtPeriodEnd: Boolean,
    val trialEnd: String? = null
)

data class SubscriptionState(
    val isPlus: Boolean,
    val subscription: SubscriptionInfo? = null,
    val profileVisibility: ProfileVisibility = ProfileVisibility.PUBLIC
)
