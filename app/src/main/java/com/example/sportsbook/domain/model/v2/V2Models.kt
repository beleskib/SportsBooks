package com.example.sportsbook.domain.model.v2

// ============================================================
// v2-practical-ux: Plain domain models — no Android deps
// ============================================================

data class RebookSuggestion(
    val bookingId: Long,
    val venueId: Long?,
    val venueName: String?,
    val coachId: Long?,
    val coachName: String?,
    val sportType: String,
    val lastPlayedAt: String,
    val lastSlotStart: String,
    val price: Double,
    val timesBooked: Int
)

data class PlaySuggestion(
    val type: String, // "lobby" | "match" | "open_slot"
    val id: Long,
    val title: String,
    val sportType: String,
    val startAt: String,
    val venueName: String?,
    val venueId: Long? = null,
    val distanceKm: Double?,
    val currentPlayers: Int,
    val maxPlayers: Int,
    val skillLevelMin: Int?,
    val skillLevelMax: Int?,
    val price: Double?
)

data class FriendAvailability(
    val userId: Long,
    val displayName: String?,
    val photoUrl: String?,
    val sportType: String,
    val skillLevel: Int?,
    val availableUntil: String?,
    val distanceKm: Double?
)

data class UpcomingBooking(
    val id: Long,
    val venueName: String?,
    val coachName: String?,
    val status: String,
    val totalPrice: Double,
    val slotDate: String,
    val startTime: String,
    val endTime: String
)

data class HomeFeedSnapshot(
    val displayName: String?,
    val reliabilityScore: Double,
    val totalAttended: Int,
    val recentBookings: List<RebookSuggestion>,
    val suggestedPlay: List<PlaySuggestion>,
    val friendsAvailable: List<FriendAvailability>,
    val upcoming: List<UpcomingBooking>
)

data class PlaySearchResult(
    val results: List<PlaySuggestion>,
    val lobbies: Int,
    val openSlots: Int,
    val availablePlayers: Int
) {
    /** Lobbies/matches are shown as individual cards */
    val lobbyResults: List<PlaySuggestion>
        get() = results.filter { it.type != "open_slot" }

    /** Open slots grouped by venue — each venue appears once with a slot count */
    val venueGroups: List<VenueSearchGroup>
        get() = results
            .filter { it.type == "open_slot" && it.venueId != null }
            .groupBy { it.venueId!! }
            .map { (venueId, slots) ->
                val first = slots.first()
                VenueSearchGroup(
                    venueId = venueId,
                    venueName = first.venueName ?: "Venue",
                    sportType = first.sportType,
                    distanceKm = first.distanceKm,
                    availableSlots = slots.size,
                    priceFrom = slots.mapNotNull { it.price }.minOrNull(),
                    firstSlotTime = slots.minByOrNull { it.startAt }?.startAt
                )
            }
            .sortedBy { it.firstSlotTime }
}

/** A venue with available slots, shown as a single card in search results */
data class VenueSearchGroup(
    val venueId: Long,
    val venueName: String,
    val sportType: String,
    val distanceKm: Double?,
    val availableSlots: Int,
    val priceFrom: Double?,
    val firstSlotTime: String?
)

data class SplitPaymentShare(
    val id: Long,
    val bookingId: Long,
    val payerUserId: Long,
    val amount: Double,
    val currency: String,
    val status: String,
    val payerName: String?,
    val payerPhotoUrl: String?,
    val expiresAt: String?,
    val paidAt: String?
)

data class SplitPaymentSummary(
    val bookingId: Long,
    val totalAmount: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val shares: List<SplitPaymentShare>
)

data class BookingParticipant(
    val userId: Long,
    val displayName: String?,
    val photoUrl: String?,
    val role: String?,
    val status: String?,
    val attended: Boolean?
)
