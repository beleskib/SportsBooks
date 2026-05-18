package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.BookingStatus

data class Booking(
    val id: Long = 0,
    val playerId: Long = 0,
    val timeSlotId: Long = 0,
    val venueId: Long? = null,
    val coachId: Long? = null,
    val status: BookingStatus = BookingStatus.PENDING,
    val totalPrice: Double = 0.0,
    val notes: String? = null,
    val timeSlot: TimeSlot? = null,
    val venue: Venue? = null,
    val coach: Coach? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val expiresAt: String? = null,
    val playerName: String? = null,
    val playerEmail: String? = null,
    val isParticipant: Boolean = false,
    val matchId: Long? = null,
    val matchTitle: String? = null,
    val matchStatus: String? = null
) {
    /** True when this booking is linked to a match (lobby booking) */
    val isMatchBooking: Boolean get() = matchId != null
}
