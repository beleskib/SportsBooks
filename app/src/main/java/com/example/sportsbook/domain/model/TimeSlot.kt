package com.example.sportsbook.domain.model

data class TimeSlot(
    val id: Long = 0,
    val venueId: Long? = null,
    val coachId: Long? = null,
    val slotDate: String = "",  // "2024-01-15"
    val startTime: String = "", // "09:00"
    val endTime: String = "",   // "10:00"
    val isAvailable: Boolean = true,
    val priceOverride: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val displayTime: String
        get() = "$startTime - $endTime"
}
