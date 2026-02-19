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
    @Serializable data class BookingDetail(val bookingId: Long) : Route
    @Serializable data class WriteReview(val bookingId: Long) : Route

    // Partner flow
    @Serializable data object VenueSetup : Route
    @Serializable data object CoachSetup : Route
    @Serializable data object PartnerDashboard : Route
}
