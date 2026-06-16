package com.example.sportsbook.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    // Auth flow
    @Serializable data object Splash : Route
    @Serializable data object Login : Route
    @Serializable data object Register : Route

    // Onboarding
    @Serializable data object RoleSelection : Route
    @Serializable data object PartnerTypeSelection : Route
    @Serializable data object PlayerOnboarding : Route

    // Player flow
    @Serializable data object PlayerHome : Route
    @Serializable data class SportDetail(val sportType: String) : Route
    @Serializable data class VenueList(val sportType: String) : Route
    @Serializable data class VenueDetail(val venueId: Long) : Route
    @Serializable data class CoachList(val sportType: String) : Route
    @Serializable data class CoachDetail(val coachId: Long) : Route
    @Serializable data class BookingCalendar(
        val venueId: Long? = null,
        val coachId: Long? = null
    ) : Route
    @Serializable data class BookingConfirmation(val timeSlotId: Long) : Route
    @Serializable data object MyBookings : Route
    @Serializable data object PlayerProfile : Route
    @Serializable data class BookingDetail(val bookingId: Long) : Route
    @Serializable data class BookingChat(val bookingId: Long) : Route
    @Serializable data class WriteReview(val bookingId: Long) : Route
    @Serializable data class PaymentCheckout(val bookingId: Long) : Route
    @Serializable data object PaymentHistory : Route
    @Serializable data class PaymentDetail(val paymentId: Long) : Route
    @Serializable data object PaymentMethods : Route
    @Serializable data object Notifications : Route
    @Serializable data object Settings : Route

    // Matchmaking
    @Serializable data object MatchList : Route
    @Serializable data class MatchDetail(val matchId: Long) : Route
    @Serializable data class CreateMatch(
        val preselectedVenueId: Long? = null,
        val preselectedTimeSlotId: Long? = null,
        val preselectedPaymentType: String? = null
    ) : Route
    @Serializable data class MatchChat(val matchId: Long) : Route
    @Serializable data class RatePlayers(val matchId: Long) : Route
    @Serializable data class AvailablePlayers(
        val matchId: Long? = null,
        val sportType: String? = null,
        val minSkillLevel: Int? = null,
        val maxSkillLevel: Int? = null
    ) : Route

    // Maps
    @Serializable data object VenueMap : Route
    @Serializable data object MatchMap : Route

    // Public Profile
    @Serializable data class PlayerPublicProfile(val userId: Long) : Route

    // All Sports
    @Serializable data object AllSports : Route

    // News Feed
    @Serializable data object NewsFeed : Route

    // Search
    @Serializable data object Search : Route

    // Favorites
    @Serializable data object Favorites : Route

    // Friends
    @Serializable data object FriendsList : Route
    @Serializable data object FriendRequests : Route
    @Serializable data object AddFriend : Route
    @Serializable data class FriendChat(
        val friendUserId: Long,
        val friendName: String,
        val friendPhotoUrl: String? = null,
    ) : Route

    // Party
    @Serializable data object CreateParty : Route
    @Serializable data class PartyDetail(val partyId: Long) : Route
    @Serializable data class PartyInviteMembers(val partyId: Long) : Route

    // Gamification
    @Serializable data object PlayerXpLevel : Route
    @Serializable data object PlayerAchievements : Route
    @Serializable data object PlayerStatsScreen : Route

    // Community & Lobby
    @Serializable data object CommunityList : Route
    @Serializable data class CommunityDetail(val communityId: Long) : Route
    @Serializable data object CreateCommunity : Route
    @Serializable data class LobbyDetail(val lobbyId: Long) : Route
    @Serializable data class InviteFriends(val communityId: Long) : Route
    @Serializable data class CreateLobby(val communityId: Long) : Route

    // Booking Success
    @Serializable data class BookingSuccess(val bookingId: Long) : Route

    // Venue Booking Lobbies
    @Serializable data class BrowseVenueLobbies(val venueId: Long? = null) : Route
    @Serializable data class CreateVenueBookingLobby(val timeSlotId: Long = -1, val venueId: Long = -1) : Route
    @Serializable data class VenueBookingLobbyDetail(val lobbyId: Long) : Route

    @Serializable data object PendingReservations : Route

    // Partner flow
    @Serializable data object VenueSetup : Route
    @Serializable data object CoachSetup : Route
    @Serializable data object PartnerDashboard : Route
    @Serializable data class ManageImages(val entityType: String, val entityId: Long) : Route
    @Serializable data object TimeSlotManagement : Route
    @Serializable data class EditVenue(val venueId: Long) : Route
    @Serializable data class EditCoach(val coachId: Long) : Route
    @Serializable data object PartnerAnalytics : Route

    // v3 bottom nav tabs
    @Serializable data object Explore : Route        // Browse sports, venues, coaches
    @Serializable data object SocialHub : Route      // Friends, parties, find players
    @Serializable data object MoreMenu : Route       // Profile, settings, payments, etc.

    // v2-practical-ux (legacy, kept for compat)
    @Serializable data object V2PlayHome : Route
    @Serializable data object V2WeeklyCalendar : Route
    @Serializable data object SportsIFollow : Route

    // Chats hub
    @Serializable data object ChatsList : Route
}
