package com.example.sportsbook.data.remote.api

import com.example.sportsbook.data.remote.dto.ApiResponseDto
import com.example.sportsbook.data.remote.dto.v2.BookingParticipantDto
import com.example.sportsbook.data.remote.dto.v2.CreateSplitRequestDto
import com.example.sportsbook.data.remote.dto.v2.HomeFeedDto
import com.example.sportsbook.data.remote.dto.v2.InviteParticipantsRequestDto
import com.example.sportsbook.data.remote.dto.v2.MarkAttendanceRequestDto
import com.example.sportsbook.data.remote.dto.v2.PaySplitShareRequestDto
import com.example.sportsbook.data.remote.dto.v2.PlaySearchResponseDto
import com.example.sportsbook.data.remote.dto.v2.RebookRequestDto
import com.example.sportsbook.data.remote.dto.v2.RespondToInviteRequestDto
import com.example.sportsbook.data.remote.dto.v2.SetVisibilityRequestDto
import com.example.sportsbook.data.remote.dto.v2.SplitPaymentSummaryDto
import com.example.sportsbook.data.remote.dto.v2.SubscriptionStatusDto
import com.example.sportsbook.data.remote.dto.v2.CheckoutResponseDto
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// ============================================================
// v2-practical-ux: Isolated Retrofit interface for the 11 v2 endpoints.
// AuthInterceptor is already wired at the OkHttp level — no manual headers.
// ============================================================

@Serializable
data class RebookResponseDto(
    val id: Long,
    val rebookedFrom: Long
)

interface V2ApiService {

    // ---- Home feed ----

    @GET("api/home/feed")
    suspend fun getHomeFeed(): ApiResponseDto<HomeFeedDto>

    // ---- Unified Play search ----

    @GET("api/play/search")
    suspend fun searchPlay(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("sportType") sportType: String? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("radiusKm") radiusKm: Double? = null,
        @Query("skillLevelMin") skillLevelMin: Int? = null,
        @Query("skillLevelMax") skillLevelMax: Int? = null,
        @Query("onlyEligible") onlyEligible: Boolean? = null
    ): ApiResponseDto<PlaySearchResponseDto>

    // ---- One-tap rebook ----

    @POST("api/bookings/{id}/rebook")
    suspend fun rebook(
        @Path("id") bookingId: Long,
        @Body request: RebookRequestDto
    ): ApiResponseDto<RebookResponseDto>

    // ---- Split payments ----

    @POST("api/bookings/{id}/split")
    suspend fun createSplit(
        @Path("id") bookingId: Long,
        @Body request: CreateSplitRequestDto
    ): ApiResponseDto<SplitPaymentSummaryDto>

    @GET("api/bookings/{id}/split")
    suspend fun getSplitSummary(
        @Path("id") bookingId: Long
    ): ApiResponseDto<SplitPaymentSummaryDto>

    @POST("api/split-payments/{id}/pay")
    suspend fun paySplitShare(
        @Path("id") shareId: Long,
        @Body request: PaySplitShareRequestDto
    ): ApiResponseDto<Unit>

    // ---- Booking participants ----

    @POST("api/bookings/{id}/invite")
    suspend fun inviteParticipants(
        @Path("id") bookingId: Long,
        @Body request: InviteParticipantsRequestDto
    ): ApiResponseDto<Unit>

    @POST("api/bookings/{id}/respond")
    suspend fun respondToInvite(
        @Path("id") bookingId: Long,
        @Body request: RespondToInviteRequestDto
    ): ApiResponseDto<Unit>

    @POST("api/bookings/{id}/attendance")
    suspend fun markAttendance(
        @Path("id") bookingId: Long,
        @Body request: MarkAttendanceRequestDto
    ): ApiResponseDto<Unit>

    @GET("api/bookings/{id}/participants")
    suspend fun listParticipants(
        @Path("id") bookingId: Long
    ): ApiResponseDto<List<BookingParticipantDto>>

    // ---- SportsBooks+ Subscription ----

    @GET("api/subscription")
    suspend fun getSubscriptionStatus(): ApiResponseDto<SubscriptionStatusDto>

    @POST("api/subscription/checkout")
    suspend fun createSubscriptionCheckout(): ApiResponseDto<CheckoutResponseDto>

    @POST("api/subscription/cancel")
    suspend fun cancelSubscription(): ApiResponseDto<Unit>

    @POST("api/subscription/reactivate")
    suspend fun reactivateSubscription(): ApiResponseDto<Unit>

    @PUT("api/subscription/visibility")
    suspend fun setProfileVisibility(
        @Body request: SetVisibilityRequestDto
    ): ApiResponseDto<Unit>
}
