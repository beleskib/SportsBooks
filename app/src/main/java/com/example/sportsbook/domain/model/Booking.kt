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
    val updatedAt: String? = null
)
