package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.model.TimeSlot
import kotlinx.serialization.Serializable

@Serializable
data class TimeSlotDto(
    val id: Long,
    val venueId: Long? = null,
    val coachId: Long? = null,
    val slotDate: String,
    val startTime: String,
    val endTime: String,
    val isAvailable: Boolean = true,
    val priceOverride: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): TimeSlot = TimeSlot(
        id = id, venueId = venueId, coachId = coachId,
        slotDate = slotDate, startTime = startTime, endTime = endTime,
        isAvailable = isAvailable, priceOverride = priceOverride,
        createdAt = createdAt, updatedAt = updatedAt
    )
}
