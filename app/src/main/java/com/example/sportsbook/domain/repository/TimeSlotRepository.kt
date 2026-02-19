package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.model.TimeSlot

interface TimeSlotRepository {
    suspend fun getAvailableSlots(
        venueId: Long? = null,
        coachId: Long? = null,
        dateFrom: String,
        dateTo: String
    ): Result<List<TimeSlot>>

    suspend fun getSlotById(id: Long): Result<TimeSlot>
}
