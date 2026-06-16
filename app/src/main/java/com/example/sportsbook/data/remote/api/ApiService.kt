package com.example.sportsbook.data.remote.api

import com.example.sportsbook.data.remote.dto.AddImageRequestDto
import com.example.sportsbook.data.remote.dto.ApiResponseDto
import com.example.sportsbook.data.remote.dto.BookingDto
import com.example.sportsbook.data.remote.dto.CoachDto
import com.example.sportsbook.data.remote.dto.CoachImageDto
import com.example.sportsbook.data.remote.dto.CreateBookingRequestDto
import com.example.sportsbook.data.remote.dto.CreateCoachRequestDto
import com.example.sportsbook.data.remote.dto.CreateReviewRequestDto
import com.example.sportsbook.data.remote.dto.CreateUserRequestDto
import com.example.sportsbook.data.remote.dto.CreateVenueRequestDto
import com.example.sportsbook.data.remote.dto.GenerateTimeSlotsRequestDto
import com.example.sportsbook.data.remote.dto.ImageActionResponseDto
import com.example.sportsbook.data.remote.dto.ConfirmPaymentRequestDto
import com.example.sportsbook.data.remote.dto.CreatePaymentIntentRequestDto
import com.example.sportsbook.data.remote.dto.PaginatedResponseDto
import com.example.sportsbook.data.remote.dto.PaymentDto
import com.example.sportsbook.data.remote.dto.PaymentIntentResponseDto
import com.example.sportsbook.data.remote.dto.ReviewDto
import com.example.sportsbook.data.remote.dto.SetRoleRequestDto
import com.example.sportsbook.data.remote.dto.SportCategoryDto
import com.example.sportsbook.data.remote.dto.TimeSlotDto
import com.example.sportsbook.data.remote.dto.CompleteOnboardingRequestDto
import com.example.sportsbook.data.remote.dto.InterestedSportsResponseDto
import com.example.sportsbook.data.remote.dto.SportExpertiseEntryDto
import com.example.sportsbook.data.remote.dto.SportExpertiseResponseDto
import com.example.sportsbook.data.remote.dto.UpdateInterestedSportsRequestDto
import com.example.sportsbook.data.remote.dto.UpdateUserRequestDto
import com.example.sportsbook.data.remote.dto.UserDto
import com.example.sportsbook.data.remote.dto.SavedCardDto
import com.example.sportsbook.data.remote.dto.SetupIntentResultDto
import com.example.sportsbook.data.remote.dto.FollowCountsDto
import com.example.sportsbook.data.remote.dto.FeedPostDto
import com.example.sportsbook.data.remote.dto.FeedCommentDto
import com.example.sportsbook.data.remote.dto.CreateFeedPostRequestDto
import com.example.sportsbook.data.remote.dto.AddCommentRequestDto
import com.example.sportsbook.data.remote.dto.VenueDto
import com.example.sportsbook.data.remote.dto.VenueImageDto
import com.example.sportsbook.data.remote.dto.MatchDto
import com.example.sportsbook.data.remote.dto.MatchParticipantDto
import com.example.sportsbook.data.remote.dto.MatchChatMessageDto
import com.example.sportsbook.data.remote.dto.PlayerRatingDto
import com.example.sportsbook.data.remote.dto.CreateMatchRequestDto
import com.example.sportsbook.data.remote.dto.UpdateMatchRequestDto
import com.example.sportsbook.data.remote.dto.JoinMatchRequestDto
import com.example.sportsbook.data.remote.dto.RespondToJoinRequestDto
import com.example.sportsbook.data.remote.dto.SendChatMessageRequestDto
import com.example.sportsbook.data.remote.dto.CreatePlayerRatingRequestDto
import com.example.sportsbook.data.remote.dto.MarkNotificationsReadRequestDto
import com.example.sportsbook.data.remote.dto.NotificationDto
import com.example.sportsbook.data.remote.dto.CheckFavoritesRequestDto
import com.example.sportsbook.data.remote.dto.CheckFavoritesResponseDto
import com.example.sportsbook.data.remote.dto.FavoriteDto
import com.example.sportsbook.data.remote.dto.FriendshipDto
import com.example.sportsbook.data.remote.dto.GlobalSearchResultsDto
import com.example.sportsbook.data.remote.dto.PublicPlayerProfileDto
import com.example.sportsbook.data.remote.dto.RegisterDeviceTokenRequestDto
import com.example.sportsbook.data.remote.dto.RespondToFriendRequestDto
import com.example.sportsbook.data.remote.dto.SendFriendRequestDto
import com.example.sportsbook.data.remote.dto.ToggleFavoriteRequestDto
import com.example.sportsbook.data.remote.dto.ToggleFavoriteResponseDto
import com.example.sportsbook.data.remote.dto.UnreadCountDto
import com.example.sportsbook.data.remote.dto.UserSearchResultDto
import com.example.sportsbook.data.remote.dto.PartyDto
import com.example.sportsbook.data.remote.dto.CreatePartyRequestDto
import com.example.sportsbook.data.remote.dto.InviteToPartyRequestDto
import com.example.sportsbook.data.remote.dto.RespondToPartyInviteDto
import com.example.sportsbook.data.remote.dto.JoinMatchWithPartyDto
import com.example.sportsbook.data.remote.dto.AvailablePlayerDto
import com.example.sportsbook.data.remote.dto.RegisterAvailableRequestDto
import com.example.sportsbook.data.remote.dto.InviteToMatchRequestDto
import com.example.sportsbook.data.remote.dto.PlayerLevelDto
import com.example.sportsbook.data.remote.dto.XpTransactionDto
import com.example.sportsbook.data.remote.dto.AchievementDto
import com.example.sportsbook.data.remote.dto.PlayerAchievementDto
import com.example.sportsbook.data.remote.dto.PlayerStatsDto
import com.example.sportsbook.data.remote.dto.CheckAchievementsResponseDto
import com.example.sportsbook.data.remote.dto.RedeemXpRequestDto
import com.example.sportsbook.data.remote.dto.RedeemXpResponseDto
import com.example.sportsbook.data.remote.dto.BookingMessageDto
import com.example.sportsbook.data.remote.dto.SendMessageRequestDto
import com.example.sportsbook.data.remote.dto.BookingContactDto
import com.example.sportsbook.data.remote.dto.PartnerDashboardStatsDto
import com.example.sportsbook.data.remote.dto.CommunityDto
import com.example.sportsbook.data.remote.dto.CommunityMemberDto
import com.example.sportsbook.data.remote.dto.LobbyDto
import com.example.sportsbook.data.remote.dto.CreateCommunityRequestDto
import com.example.sportsbook.data.remote.dto.UpdateCommunityRequestDto
import com.example.sportsbook.data.remote.dto.InviteToCommunityRequestDto
import com.example.sportsbook.data.remote.dto.RespondToMemberRequestDto
import com.example.sportsbook.data.remote.dto.CreateLobbyRequestDto
import com.example.sportsbook.data.remote.dto.VenueBookingLobbyDto
import com.example.sportsbook.data.remote.dto.VenueBookingLobbyTeamDto
import com.example.sportsbook.data.remote.dto.CreateVenueBookingLobbyRequestDto
import com.example.sportsbook.data.remote.dto.UpdateVenueBookingLobbyRequestDto
import com.example.sportsbook.data.remote.dto.AddTeamRequestDto
import com.example.sportsbook.data.remote.dto.SplitPaymentIntentResponseDto
import com.example.sportsbook.data.remote.dto.ReceiptDto
import com.example.sportsbook.data.remote.dto.MatchPaymentStatusDto
import com.example.sportsbook.data.remote.dto.MatchPaymentIntentDto
import com.example.sportsbook.data.remote.dto.v2.CreateSplitRequestDto
import com.example.sportsbook.data.remote.dto.v2.SplitPaymentSummaryDto
import com.example.sportsbook.data.remote.dto.v2.SplitPaymentShareDto
import com.example.sportsbook.data.remote.dto.v2.PaySplitShareRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Auth
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: CreateUserRequestDto): ApiResponseDto<UserDto>

    // User
    @GET("api/users/me")
    suspend fun getProfile(): ApiResponseDto<UserDto>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: UpdateUserRequestDto): ApiResponseDto<UserDto>

    @PUT("api/users/me/role")
    suspend fun setRole(@Body request: SetRoleRequestDto): ApiResponseDto<UserDto>

    @PUT("api/users/me/interested-sports")
    suspend fun updateInterestedSports(@Body request: UpdateInterestedSportsRequestDto): ApiResponseDto<InterestedSportsResponseDto>

    @POST("api/users/me/complete-onboarding")
    suspend fun completeOnboarding(@Body request: CompleteOnboardingRequestDto): ApiResponseDto<UserDto>

    @GET("api/users/me/follow-counts")
    suspend fun getFollowCounts(): ApiResponseDto<FollowCountsDto>

    @GET("api/users/me/sport-expertise")
    suspend fun getSportExpertise(): ApiResponseDto<SportExpertiseResponseDto>

    @PUT("api/users/me/sport-expertise")
    suspend fun setSportExpertise(@Body request: Map<String, List<SportExpertiseEntryDto>>): ApiResponseDto<SportExpertiseResponseDto>

    // Sports
    @GET("api/sports")
    suspend fun getSports(): ApiResponseDto<List<SportCategoryDto>>

    @GET("api/sports/{id}")
    suspend fun getSportById(@Path("id") id: Long): ApiResponseDto<SportCategoryDto>

    // Venues
    @GET("api/venues")
    suspend fun getAllVenues(): ApiResponseDto<List<VenueDto>>

    @GET("api/venues/by-sport/{sportType}")
    suspend fun getVenuesBySport(@Path("sportType") sportType: String): ApiResponseDto<List<VenueDto>>

    @GET("api/venues/{id}")
    suspend fun getVenueById(@Path("id") id: Long): ApiResponseDto<VenueDto>

    @GET("api/venues/top-deals")
    suspend fun getTopDealVenues(): ApiResponseDto<List<VenueDto>>

    @GET("api/venues/search")
    suspend fun searchVenues(@Query("q") query: String): ApiResponseDto<List<VenueDto>>

    @GET("api/venues/mine")
    suspend fun getMyVenues(): ApiResponseDto<List<VenueDto>>

    @POST("api/venues")
    suspend fun createVenue(@Body request: CreateVenueRequestDto): ApiResponseDto<VenueDto>

    @PUT("api/venues/{id}")
    suspend fun updateVenue(@Path("id") id: Long, @Body request: CreateVenueRequestDto): ApiResponseDto<VenueDto>

    // Venue Images
    @POST("api/venues/{venueId}/images")
    suspend fun addVenueImage(
        @Path("venueId") venueId: Long,
        @Body request: AddImageRequestDto
    ): ApiResponseDto<VenueImageDto>

    @DELETE("api/venues/{venueId}/images/{imageId}")
    suspend fun deleteVenueImage(
        @Path("venueId") venueId: Long,
        @Path("imageId") imageId: Long
    ): ApiResponseDto<ImageActionResponseDto>

    @PUT("api/venues/{venueId}/images/{imageId}/primary")
    suspend fun setVenuePrimaryImage(
        @Path("venueId") venueId: Long,
        @Path("imageId") imageId: Long
    ): ApiResponseDto<ImageActionResponseDto>

    // Coaches
    @GET("api/coaches")
    suspend fun getAllCoaches(): ApiResponseDto<List<CoachDto>>

    @GET("api/coaches/by-sport/{sportType}")
    suspend fun getCoachesBySport(@Path("sportType") sportType: String): ApiResponseDto<List<CoachDto>>

    @GET("api/coaches/{id}")
    suspend fun getCoachById(@Path("id") id: Long): ApiResponseDto<CoachDto>

    @GET("api/coaches/top-deals")
    suspend fun getTopDealCoaches(): ApiResponseDto<List<CoachDto>>

    @GET("api/coaches/search")
    suspend fun searchCoaches(@Query("q") query: String): ApiResponseDto<List<CoachDto>>

    @GET("api/coaches/mine")
    suspend fun getMyCoachProfile(): ApiResponseDto<CoachDto?>

    @POST("api/coaches")
    suspend fun createCoach(@Body request: CreateCoachRequestDto): ApiResponseDto<CoachDto>

    @PUT("api/coaches/{id}")
    suspend fun updateCoach(@Path("id") id: Long, @Body request: CreateCoachRequestDto): ApiResponseDto<CoachDto>

    // Coach Images
    @POST("api/coaches/{coachId}/images")
    suspend fun addCoachImage(
        @Path("coachId") coachId: Long,
        @Body request: AddImageRequestDto
    ): ApiResponseDto<CoachImageDto>

    @DELETE("api/coaches/{coachId}/images/{imageId}")
    suspend fun deleteCoachImage(
        @Path("coachId") coachId: Long,
        @Path("imageId") imageId: Long
    ): ApiResponseDto<ImageActionResponseDto>

    @PUT("api/coaches/{coachId}/images/{imageId}/primary")
    suspend fun setCoachPrimaryImage(
        @Path("coachId") coachId: Long,
        @Path("imageId") imageId: Long
    ): ApiResponseDto<ImageActionResponseDto>

    // Time Slots
    @GET("api/venues/{venueId}/time-slots")
    suspend fun getVenueTimeSlots(
        @Path("venueId") venueId: Long,
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String
    ): ApiResponseDto<List<TimeSlotDto>>

    @GET("api/coaches/{coachId}/time-slots")
    suspend fun getCoachTimeSlots(
        @Path("coachId") coachId: Long,
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String
    ): ApiResponseDto<List<TimeSlotDto>>

    @GET("api/time-slots/{id}")
    suspend fun getTimeSlotById(@Path("id") id: Long): ApiResponseDto<TimeSlotDto>

    @POST("api/time-slots/generate")
    suspend fun generateTimeSlots(@Body request: GenerateTimeSlotsRequestDto): ApiResponseDto<List<TimeSlotDto>>

    @DELETE("api/time-slots/{id}")
    suspend fun deleteTimeSlot(@Path("id") id: Long): ApiResponseDto<Map<String, String>>

    // Bookings
    @POST("api/bookings")
    suspend fun createBooking(@Body request: CreateBookingRequestDto): ApiResponseDto<BookingDto>

    @GET("api/bookings/mine")
    suspend fun getMyBookings(@Query("status") status: String? = null): ApiResponseDto<List<BookingDto>>

    @GET("api/bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Long): ApiResponseDto<BookingDto>

    @PUT("api/bookings/{id}/status")
    suspend fun updateBookingStatus(
        @Path("id") id: Long,
        @Body request: Map<String, String>
    ): ApiResponseDto<BookingDto>

    @GET("api/bookings/partner")
    suspend fun getPartnerBookings(@Query("status") status: String? = null): ApiResponseDto<List<BookingDto>>

    @PUT("api/bookings/{id}/approve")
    suspend fun approveBooking(@Path("id") id: Long): ApiResponseDto<BookingDto>

    @PUT("api/bookings/{id}/decline")
    suspend fun declineBooking(@Path("id") id: Long): ApiResponseDto<BookingDto>

    // Booking Receipt
    @GET("api/bookings/{id}/receipt")
    suspend fun getBookingReceipt(@Path("id") bookingId: Long): ApiResponseDto<ReceiptDto>

    // Reviews
    @GET("api/reviews/venue/{venueId}")
    suspend fun getVenueReviews(@Path("venueId") venueId: Long): ApiResponseDto<List<ReviewDto>>

    @GET("api/reviews/coach/{coachId}")
    suspend fun getCoachReviews(@Path("coachId") coachId: Long): ApiResponseDto<List<ReviewDto>>

    @POST("api/reviews")
    suspend fun createReview(@Body request: CreateReviewRequestDto): ApiResponseDto<ReviewDto>

    @GET("api/reviews/mine")
    suspend fun getMyReviews(): ApiResponseDto<List<ReviewDto>>

    // Payments
    @POST("api/payments/create-intent")
    suspend fun createPaymentIntent(
        @Body request: CreatePaymentIntentRequestDto
    ): ApiResponseDto<PaymentIntentResponseDto>

    @POST("api/payments/{id}/confirm")
    suspend fun confirmPayment(
        @Path("id") paymentId: Long,
        @Body request: ConfirmPaymentRequestDto
    ): ApiResponseDto<PaymentDto>

    @POST("api/payments/{id}/fail")
    suspend fun failPayment(
        @Path("id") paymentId: Long,
        @Body request: ConfirmPaymentRequestDto
    ): ApiResponseDto<PaymentDto>

    @GET("api/payments/mine")
    suspend fun getMyPayments(): ApiResponseDto<List<PaymentDto>>

    @GET("api/payments/{id}")
    suspend fun getPaymentById(@Path("id") id: Long): ApiResponseDto<PaymentDto>

    // Match venue time slots
    @GET("api/matches/venue-slots/{venueId}")
    suspend fun getVenueTimeSlotsForMatch(
        @Path("venueId") venueId: Long,
        @Query("date") date: String,
    ): ApiResponseDto<List<TimeSlotDto>>

    // Matches
    @POST("api/matches")
    suspend fun createMatch(@Body request: CreateMatchRequestDto): ApiResponseDto<MatchDto>

    @GET("api/matches")
    suspend fun listMatches(
        @Query("sportType") sportType: String? = null,
        @Query("status") status: String? = null,
        @Query("matchDate") matchDate: String? = null,
        @Query("matchType") matchType: String? = null,
        @Query("minSkillLevel") minSkillLevel: Int? = null,
        @Query("maxSkillLevel") maxSkillLevel: Int? = null,
        @Query("hostId") hostId: Long? = null
    ): ApiResponseDto<List<MatchDto>>

    @GET("api/matches/mine")
    suspend fun getMyMatches(): ApiResponseDto<List<MatchDto>>

    @GET("api/matches/nearby")
    suspend fun getNearbyMatches(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int = 25
    ): ApiResponseDto<List<MatchDto>>

    @GET("api/matches/{id}")
    suspend fun getMatchById(@Path("id") id: Long): ApiResponseDto<MatchDto>

    @PUT("api/matches/{id}")
    suspend fun updateMatch(
        @Path("id") id: Long,
        @Body request: UpdateMatchRequestDto
    ): ApiResponseDto<MatchDto>

    @PUT("api/matches/{id}/cancel")
    suspend fun cancelMatch(@Path("id") id: Long): ApiResponseDto<MatchDto>

    // Match Participants
    @POST("api/matches/{id}/join")
    suspend fun joinMatch(
        @Path("id") id: Long,
        @Body request: JoinMatchRequestDto = JoinMatchRequestDto()
    ): ApiResponseDto<MatchParticipantDto>

    @POST("api/matches/{id}/leave")
    suspend fun leaveMatch(@Path("id") id: Long): ApiResponseDto<Map<String, String>>

    @GET("api/matches/{id}/participants")
    suspend fun getMatchParticipants(@Path("id") id: Long): ApiResponseDto<List<MatchParticipantDto>>

    @PUT("api/matches/{matchId}/participants/{pid}")
    suspend fun respondToJoinRequest(
        @Path("matchId") matchId: Long,
        @Path("pid") participantId: Long,
        @Body request: RespondToJoinRequestDto
    ): ApiResponseDto<MatchParticipantDto>

    // Match Chat
    @GET("api/matches/{id}/chat")
    suspend fun getMatchChat(
        @Path("id") matchId: Long,
        @Query("since") since: String? = null,
        @Query("limit") limit: Int? = null
    ): ApiResponseDto<List<MatchChatMessageDto>>

    @POST("api/matches/{id}/chat")
    suspend fun sendMatchChatMessage(
        @Path("id") matchId: Long,
        @Body request: SendChatMessageRequestDto
    ): ApiResponseDto<MatchChatMessageDto>

    // Player Ratings
    @POST("api/matches/{matchId}/ratings")
    suspend fun ratePlayer(
        @Path("matchId") matchId: Long,
        @Body request: CreatePlayerRatingRequestDto
    ): ApiResponseDto<PlayerRatingDto>

    @GET("api/matches/{matchId}/ratings")
    suspend fun getMatchRatings(@Path("matchId") matchId: Long): ApiResponseDto<List<PlayerRatingDto>>

    @GET("api/matches/players/{userId}/ratings")
    suspend fun getPlayerRatings(@Path("userId") userId: Long): ApiResponseDto<List<PlayerRatingDto>>

    // Match Split Payments
    @GET("api/matches/{id}/payment-status")
    suspend fun getMatchPaymentStatus(@Path("id") id: Long): ApiResponseDto<MatchPaymentStatusDto>

    @POST("api/matches/{id}/pay")
    suspend fun createMatchPaymentIntent(@Path("id") id: Long): ApiResponseDto<MatchPaymentIntentDto>

    @POST("api/matches/{id}/confirm-payment")
    suspend fun confirmMatchPayment(@Path("id") id: Long): ApiResponseDto<SplitPaymentShareDto>

    // Notifications
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ApiResponseDto<List<NotificationDto>>

    @GET("api/notifications/unread-count")
    suspend fun getNotificationUnreadCount(): ApiResponseDto<UnreadCountDto>

    @POST("api/notifications/mark-read")
    suspend fun markNotificationsRead(@Body request: MarkNotificationsReadRequestDto): ApiResponseDto<Map<String, String>>

    @POST("api/notifications/device-token")
    suspend fun registerDeviceToken(@Body request: RegisterDeviceTokenRequestDto): ApiResponseDto<Map<String, String>>

    @HTTP(method = "DELETE", path = "api/notifications/device-token", hasBody = true)
    suspend fun removeDeviceToken(@Body request: RegisterDeviceTokenRequestDto): ApiResponseDto<Map<String, String>>

    // Public Profile
    @GET("api/users/{id}/profile")
    suspend fun getPublicProfile(@Path("id") userId: Long): ApiResponseDto<PublicPlayerProfileDto>

    // Search
    @GET("api/search")
    suspend fun globalSearch(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5
    ): ApiResponseDto<GlobalSearchResultsDto>

    // Friends
    @GET("api/friends")
    suspend fun getMyFriends(): ApiResponseDto<List<FriendshipDto>>

    @GET("api/friends/requests")
    suspend fun getPendingRequests(): ApiResponseDto<List<FriendshipDto>>

    @POST("api/friends/request")
    suspend fun sendFriendRequest(@Body request: SendFriendRequestDto): ApiResponseDto<FriendshipDto>

    @PUT("api/friends/request/{id}/respond")
    suspend fun respondToFriendRequest(
        @Path("id") id: Long,
        @Body request: RespondToFriendRequestDto
    ): ApiResponseDto<FriendshipDto>

    @DELETE("api/friends/{friendId}")
    suspend fun removeFriend(@Path("friendId") friendId: Long): ApiResponseDto<Map<String, String>>

    @GET("api/friends/search-users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20
    ): ApiResponseDto<List<UserSearchResultDto>>

    // Parties
    @POST("api/parties")
    suspend fun createParty(@Body request: CreatePartyRequestDto): ApiResponseDto<PartyDto>

    @GET("api/parties/active")
    suspend fun getActiveParties(): ApiResponseDto<List<PartyDto>>

    @GET("api/parties/{id}")
    suspend fun getPartyById(@Path("id") id: Long): ApiResponseDto<PartyDto>

    @POST("api/parties/{id}/invite")
    suspend fun inviteToParty(@Path("id") id: Long, @Body request: InviteToPartyRequestDto): ApiResponseDto<PartyDto>

    @POST("api/parties/{id}/respond")
    suspend fun respondToPartyInvite(@Path("id") id: Long, @Body request: RespondToPartyInviteDto): ApiResponseDto<PartyDto>

    @POST("api/parties/{id}/disband")
    suspend fun disbandParty(@Path("id") id: Long): ApiResponseDto<Map<String, String>>

    @POST("api/matches/{id}/join-with-party")
    suspend fun joinMatchWithParty(@Path("id") matchId: Long, @Body request: JoinMatchWithPartyDto): ApiResponseDto<Map<String, String>>

    // Favorites
    @GET("api/favorites")
    suspend fun getMyFavorites(@Query("entityType") entityType: String? = null): ApiResponseDto<List<FavoriteDto>>

    @POST("api/favorites/toggle")
    suspend fun toggleFavorite(@Body request: ToggleFavoriteRequestDto): ApiResponseDto<ToggleFavoriteResponseDto>

    @POST("api/favorites/check")
    suspend fun checkFavorites(@Body request: CheckFavoritesRequestDto): ApiResponseDto<CheckFavoritesResponseDto>

    // Available Players
    @POST("api/available-players")
    suspend fun registerAvailable(@Body request: RegisterAvailableRequestDto): ApiResponseDto<AvailablePlayerDto>

    @GET("api/available-players/me")
    suspend fun getMyAvailability(): ApiResponseDto<List<AvailablePlayerDto>>

    @GET("api/available-players")
    suspend fun listAvailablePlayers(
        @Query("sportType") sportType: String,
        @Query("skillMin") skillMin: Int? = null,
        @Query("skillMax") skillMax: Int? = null,
    ): ApiResponseDto<List<AvailablePlayerDto>>

    @DELETE("api/available-players/{sportType}")
    suspend fun unregisterAvailable(@Path("sportType") sportType: String): ApiResponseDto<Unit>

    @POST("api/matches/{id}/invite")
    suspend fun inviteToMatch(
        @Path("id") matchId: Long,
        @Body request: InviteToMatchRequestDto
    ): ApiResponseDto<Unit>

    // ── Gamification ──────────────────────────────────────────────────────
    @GET("api/gamification/me/level")
    suspend fun getMyLevel(): ApiResponseDto<PlayerLevelDto>

    @GET("api/gamification/me/xp-history")
    suspend fun getXpHistory(@Query("limit") limit: Int? = null): ApiResponseDto<List<XpTransactionDto>>

    @GET("api/gamification/achievements")
    suspend fun getAllAchievements(): ApiResponseDto<List<AchievementDto>>

    @GET("api/gamification/me/achievements")
    suspend fun getMyAchievements(): ApiResponseDto<List<PlayerAchievementDto>>

    @GET("api/gamification/me/stats")
    suspend fun getMyStats(): ApiResponseDto<PlayerStatsDto>

    @POST("api/gamification/me/check-achievements")
    suspend fun checkAndAwardAchievements(): ApiResponseDto<CheckAchievementsResponseDto>

    @POST("api/gamification/me/redeem-xp")
    suspend fun redeemXp(@Body request: RedeemXpRequestDto): ApiResponseDto<RedeemXpResponseDto>

    // ── Booking Chat ───────────────────────────────────────────────────────
    @GET("api/bookings/{id}/messages")
    suspend fun getBookingMessages(@Path("id") bookingId: Long): ApiResponseDto<List<BookingMessageDto>>

    @POST("api/bookings/{id}/messages")
    suspend fun sendBookingMessage(
        @Path("id") bookingId: Long,
        @Body request: SendMessageRequestDto
    ): ApiResponseDto<BookingMessageDto>

    @GET("api/bookings/{id}/contact")
    suspend fun getBookingContact(@Path("id") bookingId: Long): ApiResponseDto<BookingContactDto>

    // ── Partner Analytics ─────────────────────────────────────────────────
    @GET("api/dashboard/partner/stats")
    suspend fun getPartnerDashboardStats(): ApiResponseDto<PartnerDashboardStatsDto>

    // Communities
    @POST("api/communities")
    suspend fun createCommunity(@Body request: CreateCommunityRequestDto): ApiResponseDto<CommunityDto>

    @GET("api/communities")
    suspend fun getMyCommunities(): ApiResponseDto<List<CommunityDto>>

    @GET("api/communities/public")
    suspend fun getPublicCommunities(@Query("sportType") sportType: String? = null): ApiResponseDto<List<CommunityDto>>

    @GET("api/communities/{id}")
    suspend fun getCommunityById(@Path("id") id: Long): ApiResponseDto<CommunityDto>

    @PUT("api/communities/{id}")
    suspend fun updateCommunity(@Path("id") id: Long, @Body request: UpdateCommunityRequestDto): ApiResponseDto<CommunityDto>

    @POST("api/communities/{id}/invite")
    suspend fun inviteToCommunity(@Path("id") id: Long, @Body request: InviteToCommunityRequestDto): ApiResponseDto<Map<String, String>>

    @POST("api/communities/{id}/join")
    suspend fun joinCommunity(@Path("id") id: Long): ApiResponseDto<CommunityMemberDto>

    @GET("api/communities/{id}/members")
    suspend fun getCommunityMembers(@Path("id") id: Long, @Query("status") status: String? = null): ApiResponseDto<List<CommunityMemberDto>>

    @PUT("api/communities/{id}/members/{userId}/respond")
    suspend fun respondToMember(@Path("id") id: Long, @Path("userId") userId: Long, @Body request: RespondToMemberRequestDto): ApiResponseDto<CommunityMemberDto>

    @DELETE("api/communities/{id}/members/{userId}")
    suspend fun removeCommunityMember(@Path("id") id: Long, @Path("userId") userId: Long): ApiResponseDto<Map<String, String>>

    // Lobbies
    @POST("api/communities/{communityId}/lobbies")
    suspend fun createLobby(@Path("communityId") communityId: Long, @Body request: CreateLobbyRequestDto): ApiResponseDto<LobbyDto>

    @GET("api/communities/{communityId}/lobbies")
    suspend fun getCommunityLobbies(@Path("communityId") communityId: Long, @Query("status") status: String? = null): ApiResponseDto<List<LobbyDto>>

    @GET("api/lobbies/public")
    suspend fun getPublicLobbies(@Query("sportType") sportType: String? = null): ApiResponseDto<List<LobbyDto>>

    @GET("api/lobbies/{id}")
    suspend fun getLobbyById(@Path("id") id: Long): ApiResponseDto<LobbyDto>

    @POST("api/lobbies/{id}/join")
    suspend fun joinLobby(@Path("id") id: Long): ApiResponseDto<LobbyDto>

    @POST("api/lobbies/{id}/leave")
    suspend fun leaveLobby(@Path("id") id: Long): ApiResponseDto<LobbyDto>

    @POST("api/lobbies/{id}/make-public")
    suspend fun makeLobbyPublic(@Path("id") id: Long): ApiResponseDto<LobbyDto>

    // Feed
    @GET("api/feed")
    suspend fun getFeed(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ApiResponseDto<List<FeedPostDto>>

    @POST("api/feed")
    suspend fun createPost(@Body request: CreateFeedPostRequestDto): ApiResponseDto<FeedPostDto>

    @POST("api/feed/{id}/like")
    suspend fun toggleLike(@Path("id") postId: Long): ApiResponseDto<FeedPostDto>

    @GET("api/feed/{id}/comments")
    suspend fun getPostComments(@Path("id") postId: Long): ApiResponseDto<List<FeedCommentDto>>

    @POST("api/feed/{id}/comments")
    suspend fun addComment(@Path("id") postId: Long, @Body request: AddCommentRequestDto): ApiResponseDto<FeedCommentDto>

    // Venue Booking Lobbies
    @POST("api/venue-booking-lobbies")
    suspend fun createVenueBookingLobby(@Body request: CreateVenueBookingLobbyRequestDto): ApiResponseDto<VenueBookingLobbyDto>

    @GET("api/venue-booking-lobbies")
    suspend fun getOpenVenueBookingLobbies(
        @Query("venueId") venueId: Long? = null,
        @Query("sportType") sportType: String? = null
    ): ApiResponseDto<List<VenueBookingLobbyDto>>

    @GET("api/venue-booking-lobbies/mine")
    suspend fun getMyVenueBookingLobbies(): ApiResponseDto<List<VenueBookingLobbyDto>>

    @GET("api/venue-booking-lobbies/{id}")
    suspend fun getVenueBookingLobbyById(@Path("id") id: Long): ApiResponseDto<VenueBookingLobbyDto>

    @PUT("api/venue-booking-lobbies/{id}")
    suspend fun updateVenueBookingLobby(@Path("id") id: Long, @Body request: UpdateVenueBookingLobbyRequestDto): ApiResponseDto<VenueBookingLobbyDto>

    @POST("api/venue-booking-lobbies/{id}/join")
    suspend fun joinVenueBookingLobby(@Path("id") id: Long): ApiResponseDto<VenueBookingLobbyDto>

    @POST("api/venue-booking-lobbies/{id}/leave")
    suspend fun leaveVenueBookingLobby(@Path("id") id: Long): ApiResponseDto<VenueBookingLobbyDto>

    @POST("api/venue-booking-lobbies/{id}/create-booking")
    suspend fun createBookingFromLobby(@Path("id") id: Long): ApiResponseDto<VenueBookingLobbyDto>

    @POST("api/venue-booking-lobbies/{id}/cancel")
    suspend fun cancelVenueBookingLobby(@Path("id") id: Long): ApiResponseDto<VenueBookingLobbyDto>

    @POST("api/venue-booking-lobbies/{id}/pay")
    suspend fun createLobbyPaymentIntent(@Path("id") id: Long): ApiResponseDto<SplitPaymentIntentResponseDto>

    @POST("api/venue-booking-lobbies/{id}/confirm-payment")
    suspend fun confirmLobbyPayment(@Path("id") id: Long): ApiResponseDto<VenueBookingLobbyDto>

    // ── Split Payments ──────────────────────────────────────────────────
    @POST("api/bookings/{id}/split")
    suspend fun createSplit(
        @Path("id") bookingId: Long,
        @Body request: CreateSplitRequestDto
    ): ApiResponseDto<SplitPaymentSummaryDto>

    @GET("api/bookings/{id}/split")
    suspend fun getSplitSummary(@Path("id") bookingId: Long): ApiResponseDto<SplitPaymentSummaryDto>

    @POST("api/split-payments/{shareId}/pay")
    suspend fun paySplitShare(
        @Path("shareId") shareId: Long,
        @Body request: PaySplitShareRequestDto
    ): ApiResponseDto<SplitPaymentShareDto>

    // Lobby Teams
    @GET("api/venue-booking-lobbies/{id}/teams")
    suspend fun getLobbyTeams(@Path("id") lobbyId: Long): ApiResponseDto<List<VenueBookingLobbyTeamDto>>

    @POST("api/venue-booking-lobbies/{id}/teams")
    suspend fun addLobbyTeam(@Path("id") lobbyId: Long, @Body request: AddTeamRequestDto): ApiResponseDto<VenueBookingLobbyDto>

    @POST("api/venue-booking-lobbies/{id}/teams/{teamId}/join")
    suspend fun joinLobbyTeam(@Path("id") lobbyId: Long, @Path("teamId") teamId: Long): ApiResponseDto<VenueBookingLobbyDto>

    @POST("api/venue-booking-lobbies/{id}/teams/leave")
    suspend fun leaveLobbyTeam(@Path("id") lobbyId: Long): ApiResponseDto<VenueBookingLobbyDto>

    // ── Card Management ──────────────────────────────────────────────────
    @GET("api/cards")
    suspend fun getSavedCards(): ApiResponseDto<List<SavedCardDto>>

    @POST("api/cards/setup-intent")
    suspend fun createCardSetupIntent(): ApiResponseDto<SetupIntentResultDto>

    @DELETE("api/cards/{paymentMethodId}")
    suspend fun deleteCard(@Path("paymentMethodId") paymentMethodId: String): ApiResponseDto<Unit?>

    @PUT("api/cards/{paymentMethodId}/default")
    suspend fun setDefaultCard(@Path("paymentMethodId") paymentMethodId: String): ApiResponseDto<Unit?>
}
