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
    @Serializable data class WriteReview(val bookingId: Long) : Route
    @Serializable data class PaymentCheckout(val timeSlotId: Long, val notes: String = "") : Route
    @Serializable data object PaymentHistory : Route
    @Serializable data class PaymentDetail(val paymentId: Long) : Route
    @Serializable data object Notifications : Route
    @Serializable data object Settings : Route

    // Matchmaking
    @Serializable data object MatchList : Route
    @Serializable data class MatchDetail(val matchId: Long) : Route
    @Serializable data object CreateMatch : Route
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

    // Search
    @Serializable data object Search : Route

    // Favorites
    @Serializable data object Favorites : Route

    // Friends
    @Serializable data object FriendsList : Route
    @Serializable data object FriendRequests : Route
    @Serializable data object AddFriend : Route

    // Party
    @Serializable data object CreateParty : Route
    @Serializable data class PartyDetail(val partyId: Long) : Route
    @Serializable data class PartyInviteMembers(val partyId: Long) : Route

    // Gamification
    @Serializable data object PlayerXpLevel : Route
    @Serializable data object PlayerAchievements : Route
    @Serializable data object PlayerStatsScreen : Route

    // Partner flow
    @Serializable data object VenueSetup : Route
    @Serializable data object CoachSetup : Route
    @Serializable data object PartnerDashboard : Route
    @Serializable data class ManageImages(val entityType: String, val entityId: Long) : Route
    @Serializable data object StripeConnect : Route
    @Serializable data object TimeSlotManagement : Route
}
