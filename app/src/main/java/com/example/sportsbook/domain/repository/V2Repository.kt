package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.v2.BookingParticipant
import com.example.sportsbook.domain.model.v2.HomeFeedSnapshot
import com.example.sportsbook.domain.model.v2.PlaySearchResult
import com.example.sportsbook.domain.model.v2.SplitPaymentSummary

// ============================================================
// v2-practical-ux: Single repository interface for all 11 v2 endpoints.
// Lives in domain/ — no Android or Retrofit dependencies.
// ============================================================

interface V2Repository {

    // ---- Home feed ----
    suspend fun getHomeFeed(): Result<HomeFeedSnapshot>

    // ---- Unified Play search ----
    suspend fun searchPlay(
        from: String,
        to: String,
        sportType: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        radiusKm: Double? = null,
        skillLevelMin: Int? = null,
        skillLevelMax: Int? = null,
        onlyEligible: Boolean? = null
    ): Result<PlaySearchResult>

    // ---- One-tap rebook ----
    suspend fun rebook(
        bookingId: Long,
        slotDate: String,
        startTime: String
    ): Result<Long> // returns new booking id

    // ---- Split payments ----
    suspend fun createSplit(
        bookingId: Long,
        payerUserIds: List<Long>,
        expiresInMinutes: Int? = null
    ): Result<SplitPaymentSummary>

    suspend fun getSplitSummary(bookingId: Long): Result<SplitPaymentSummary>

    suspend fun paySplitShare(
        shareId: Long,
        stripePaymentIntentId: String
    ): Result<Unit>

    // ---- Booking participants ----
    suspend fun inviteParticipants(
        bookingId: Long,
        userIds: List<Long>
    ): Result<Unit>

    suspend fun respondToInvite(
        bookingId: Long,
        accept: Boolean
    ): Result<Unit>

    suspend fun markAttendance(
        bookingId: Long,
        attendance: List<Pair<Long, Boolean>>
    ): Result<Unit>

    suspend fun listParticipants(bookingId: Long): Result<List<BookingParticipant>>
}
