package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.domain.repository.TimeSlotRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeSlotRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : TimeSlotRepository {

    override suspend fun getAvailableSlots(
        venueId: Long?,
        coachId: Long?,
        dateFrom: String,
        dateTo: String
    ): Result<List<TimeSlot>> = runCatching {
        when {
            venueId != null -> apiService.getVenueTimeSlots(venueId, dateFrom, dateTo).data.map { it.toDomain() }
            coachId != null -> apiService.getCoachTimeSlots(coachId, dateFrom, dateTo).data.map { it.toDomain() }
            else -> throw IllegalArgumentException("Either venueId or coachId must be provided")
        }
    }

    override suspend fun getSlotById(id: Long): Result<TimeSlot> = runCatching {
        apiService.getTimeSlotById(id).data.toDomain()
    }
}
