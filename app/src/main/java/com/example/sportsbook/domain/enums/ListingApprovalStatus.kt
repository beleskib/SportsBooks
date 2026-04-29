package com.example.sportsbook.domain.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Listing-level approval state for venues and coaches.
 *
 * New listings start as [PENDING] and stay invisible to the public until an
 * admin moves them to [APPROVED]. [REJECTED] listings are visible only to the
 * owner and the admin, with a [Venue.approvalRejectionReason] /
 * [Coach.approvalRejectionReason] explaining what to fix.
 *
 * Mirrors the backend `listing_approval_status` enum (migration 0054).
 */
@Serializable
enum class ListingApprovalStatus {
    @SerialName("pending") PENDING,
    @SerialName("approved") APPROVED,
    @SerialName("rejected") REJECTED
}
