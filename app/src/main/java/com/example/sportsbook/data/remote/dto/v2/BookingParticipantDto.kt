package com.example.sportsbook.data.remote.dto.v2

import com.example.sportsbook.domain.model.v2.BookingParticipant
import kotlinx.serialization.Serializable

// ============================================================
// v2-practical-ux: DTO for GET /api/bookings/:id/participants
// ============================================================

@Serializable
data class BookingParticipantDto(
    val userId: Long,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val role: String? = null,      // "organizer" | "invitee"
    val status: String? = null,    // "pending" | "accepted" | "declined"
    val attended: Boolean? = null
) {
    fun toDomain(): BookingParticipant = BookingParticipant(
        userId = userId,
        displayName = displayName,
        photoUrl = photoUrl,
        role = role,
        status = status,
        attended = attended
    )
}
