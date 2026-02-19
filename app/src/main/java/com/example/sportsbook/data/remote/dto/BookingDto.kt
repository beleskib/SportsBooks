package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.model.Booking
import kotlinx.serialization.Serializable

@Serializable
data class BookingDto(
    val id: Long,
    val playerId: Long,
    val timeSlotId: Long,
    val venueId: Long? = null,
    val coachId: Long? = null,
    val status: BookingStatus,
    val totalPrice: Double,
    val notes: String? = null,
    val timeSlot: TimeSlotDto? = null,
    val venue: VenueDto? = null,
    val coach: CoachDto? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Booking = Booking(
        id = id, playerId = playerId, timeSlotId = timeSlotId,
        venueId = venueId, coachId = coachId,
        status = status, totalPrice = totalPrice, notes = notes,
        timeSlot = timeSlot?.toDomain(),
        venue = venue?.toDomain(),
        coach = coach?.toDomain(),
        createdAt = createdAt, updatedAt = updatedAt
    )
}

@Serializable
data class CreateBookingRequestDto(
    val timeSlotId: Long,
    val notes: String? = null
)
