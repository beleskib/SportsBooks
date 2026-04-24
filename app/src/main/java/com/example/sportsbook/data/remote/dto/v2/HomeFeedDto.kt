package com.example.sportsbook.data.remote.dto.v2

import com.example.sportsbook.domain.model.v2.FriendAvailability
import com.example.sportsbook.domain.model.v2.HomeFeedSnapshot
import com.example.sportsbook.domain.model.v2.PlaySuggestion
import com.example.sportsbook.domain.model.v2.RebookSuggestion
import com.example.sportsbook.domain.model.v2.UpcomingBooking
import kotlinx.serialization.Serializable

// ============================================================
// v2-practical-ux: DTOs for GET /api/home/feed
// ============================================================

@Serializable
data class GreetingDto(
    val displayName: String? = null,
    val reliabilityScore: Double = 0.0,
    val totalAttended: Int = 0
)

@Serializable
data class RebookSuggestionDto(
    val bookingId: Long,
    val venueId: Long? = null,
    val venueName: String? = null,
    val coachId: Long? = null,
    val coachName: String? = null,
    val sportType: String,
    val lastPlayedAt: String,
    val lastSlotStart: String,
    val price: Double,
    val timesBooked: Int
) {
    fun toDomain(): RebookSuggestion = RebookSuggestion(
        bookingId = bookingId,
        venueId = venueId,
        venueName = venueName,
        coachId = coachId,
        coachName = coachName,
        sportType = sportType,
        lastPlayedAt = lastPlayedAt,
        lastSlotStart = lastSlotStart,
        price = price,
        timesBooked = timesBooked
    )
}

@Serializable
data class PlaySuggestionDto(
    val type: String, // "lobby" | "match" | "open_slot"
    val id: Long,
    val title: String,
    val sportType: String,
    val startAt: String,
    val venueName: String? = null,
    val distanceKm: Double? = null,
    val currentPlayers: Int,
    val maxPlayers: Int,
    val skillLevelMin: Int? = null,
    val skillLevelMax: Int? = null,
    val price: Double? = null
) {
    fun toDomain(): PlaySuggestion = PlaySuggestion(
        type = type,
        id = id,
        title = title,
        sportType = sportType,
        startAt = startAt,
        venueName = venueName,
        distanceKm = distanceKm,
        currentPlayers = currentPlayers,
        maxPlayers = maxPlayers,
        skillLevelMin = skillLevelMin,
        skillLevelMax = skillLevelMax,
        price = price
    )
}

@Serializable
data class FriendAvailabilityDto(
    val userId: Long,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val sportType: String,
    val skillLevel: Int? = null,
    val availableUntil: String? = null,
    val distanceKm: Double? = null
) {
    fun toDomain(): FriendAvailability = FriendAvailability(
        userId = userId,
        displayName = displayName,
        photoUrl = photoUrl,
        sportType = sportType,
        skillLevel = skillLevel,
        availableUntil = availableUntil,
        distanceKm = distanceKm
    )
}

@Serializable
data class UpcomingBookingDto(
    val id: Long,
    val venueName: String? = null,
    val coachName: String? = null,
    val status: String,
    val totalPrice: Double,
    val slotDate: String,
    val startTime: String,
    val endTime: String
) {
    fun toDomain(): UpcomingBooking = UpcomingBooking(
        id = id,
        venueName = venueName,
        coachName = coachName,
        status = status,
        totalPrice = totalPrice,
        slotDate = slotDate,
        startTime = startTime,
        endTime = endTime
    )
}

@Serializable
data class HomeFeedDto(
    val greeting: GreetingDto,
    val recentBookings: List<RebookSuggestionDto> = emptyList(),
    val suggestedPlay: List<PlaySuggestionDto> = emptyList(),
    val friendsAvailable: List<FriendAvailabilityDto> = emptyList(),
    val upcoming: List<UpcomingBookingDto> = emptyList()
) {
    fun toDomain(): HomeFeedSnapshot = HomeFeedSnapshot(
        displayName = greeting.displayName,
        reliabilityScore = greeting.reliabilityScore,
        totalAttended = greeting.totalAttended,
        recentBookings = recentBookings.map { it.toDomain() },
        suggestedPlay = suggestedPlay.map { it.toDomain() },
        friendsAvailable = friendsAvailable.map { it.toDomain() },
        upcoming = upcoming.map { it.toDomain() }
    )
}
