package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.V2ApiService
import com.example.sportsbook.data.remote.dto.v2.AttendanceEntryDto
import com.example.sportsbook.data.remote.dto.v2.CreateSplitRequestDto
import com.example.sportsbook.data.remote.dto.v2.InviteParticipantsRequestDto
import com.example.sportsbook.data.remote.dto.v2.MarkAttendanceRequestDto
import com.example.sportsbook.data.remote.dto.v2.PaySplitShareRequestDto
import com.example.sportsbook.data.remote.dto.v2.RebookRequestDto
import com.example.sportsbook.data.remote.dto.v2.RespondToInviteRequestDto
import com.example.sportsbook.data.remote.dto.v2.SetVisibilityRequestDto
import com.example.sportsbook.domain.model.v2.BookingParticipant
import com.example.sportsbook.domain.model.v2.HomeFeedSnapshot
import com.example.sportsbook.domain.model.v2.PlaySearchResult
import com.example.sportsbook.domain.model.v2.ProfileVisibility
import com.example.sportsbook.domain.model.v2.SplitPaymentSummary
import com.example.sportsbook.domain.model.v2.SubscriptionInfo
import com.example.sportsbook.domain.model.v2.SubscriptionState
import com.example.sportsbook.domain.model.v2.SubscriptionStatus
import com.example.sportsbook.domain.repository.V2Repository
import javax.inject.Inject
import javax.inject.Singleton

// ============================================================
// v2-practical-ux: Repository implementation — wraps V2ApiService.
// Follows the runCatching convention used by BookingRepositoryImpl.
// ============================================================

@Singleton
class V2RepositoryImpl @Inject constructor(
    private val v2ApiService: V2ApiService
) : V2Repository {

    override suspend fun getHomeFeed(): Result<HomeFeedSnapshot> = runCatching {
        v2ApiService.getHomeFeed().data.toDomain()
    }

    override suspend fun searchPlay(
        from: String,
        to: String,
        sportType: String?,
        latitude: Double?,
        longitude: Double?,
        radiusKm: Double?,
        skillLevelMin: Int?,
        skillLevelMax: Int?,
        onlyEligible: Boolean?
    ): Result<PlaySearchResult> = runCatching {
        v2ApiService.searchPlay(
            from = from,
            to = to,
            sportType = sportType,
            latitude = latitude,
            longitude = longitude,
            radiusKm = radiusKm,
            skillLevelMin = skillLevelMin,
            skillLevelMax = skillLevelMax,
            onlyEligible = onlyEligible
        ).data.toDomain()
    }

    override suspend fun rebook(
        bookingId: Long,
        slotDate: String,
        startTime: String
    ): Result<Long> = runCatching {
        v2ApiService.rebook(bookingId, RebookRequestDto(slotDate = slotDate, startTime = startTime)).data.id
    }

    override suspend fun createSplit(
        bookingId: Long,
        payerUserIds: List<Long>,
        expiresInMinutes: Int?
    ): Result<SplitPaymentSummary> = runCatching {
        v2ApiService.createSplit(
            bookingId,
            CreateSplitRequestDto(payerUserIds = payerUserIds, expiresInMinutes = expiresInMinutes)
        ).data.toDomain()
    }

    override suspend fun getSplitSummary(bookingId: Long): Result<SplitPaymentSummary> = runCatching {
        v2ApiService.getSplitSummary(bookingId).data.toDomain()
    }

    override suspend fun paySplitShare(
        shareId: Long,
        stripePaymentIntentId: String
    ): Result<Unit> = runCatching {
        v2ApiService.paySplitShare(shareId, PaySplitShareRequestDto(stripePaymentIntentId))
        Unit
    }

    override suspend fun inviteParticipants(
        bookingId: Long,
        userIds: List<Long>
    ): Result<Unit> = runCatching {
        v2ApiService.inviteParticipants(bookingId, InviteParticipantsRequestDto(userIds))
        Unit
    }

    override suspend fun respondToInvite(
        bookingId: Long,
        accept: Boolean
    ): Result<Unit> = runCatching {
        v2ApiService.respondToInvite(bookingId, RespondToInviteRequestDto(accept))
        Unit
    }

    override suspend fun markAttendance(
        bookingId: Long,
        attendance: List<Pair<Long, Boolean>>
    ): Result<Unit> = runCatching {
        v2ApiService.markAttendance(
            bookingId,
            MarkAttendanceRequestDto(attendance.map { (userId, attended) ->
                AttendanceEntryDto(userId = userId, attended = attended)
            })
        )
        Unit
    }

    override suspend fun listParticipants(bookingId: Long): Result<List<BookingParticipant>> = runCatching {
        v2ApiService.listParticipants(bookingId).data.map { it.toDomain() }
    }

    // ---- SportsBooks+ Subscription ----

    override suspend fun getSubscriptionStatus(): Result<SubscriptionState> = runCatching {
        val dto = v2ApiService.getSubscriptionStatus().data
        SubscriptionState(
            isPlus = dto.isPlus,
            subscription = dto.subscription?.let { sub ->
                SubscriptionInfo(
                    id = sub.id,
                    status = SubscriptionStatus.fromString(sub.status),
                    currentPeriodEnd = sub.currentPeriodEnd,
                    cancelAtPeriodEnd = sub.cancelAtPeriodEnd,
                    trialEnd = sub.trialEnd
                )
            },
            profileVisibility = ProfileVisibility.fromString(dto.profileVisibility)
        )
    }

    override suspend fun createCheckout(): Result<String> = runCatching {
        v2ApiService.createSubscriptionCheckout().data.checkoutUrl
    }

    override suspend fun cancelSubscription(): Result<Unit> = runCatching {
        v2ApiService.cancelSubscription()
        Unit
    }

    override suspend fun reactivateSubscription(): Result<Unit> = runCatching {
        v2ApiService.reactivateSubscription()
        Unit
    }

    override suspend fun setProfileVisibility(visibility: ProfileVisibility): Result<Unit> = runCatching {
        v2ApiService.setProfileVisibility(SetVisibilityRequestDto(visibility = visibility.apiValue))
        Unit
    }
}
