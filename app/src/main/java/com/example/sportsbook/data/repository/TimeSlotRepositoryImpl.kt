package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.GenerateTimeSlotsRequestDto
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

    override suspend fun generateSlots(
        venueId: Long?,
        coachId: Long?,
        dateFrom: String,
        dateTo: String,
        startHour: Int?,
        endHour: Int?,
        daysOfWeek: List<Int>?
    ): Result<List<TimeSlot>> = runCatching {
        apiService.generateTimeSlots(
            GenerateTimeSlotsRequestDto(
                venueId = venueId,
                coachId = coachId,
                dateFrom = dateFrom,
                dateTo = dateTo,
                startHour = startHour,
                endHour = endHour,
                daysOfWeek = daysOfWeek
            )
        ).data.map { it.toDomain() }
    }

    override suspend fun deleteSlot(id: Long): Result<Unit> = runCatching {
        apiService.deleteTimeSlot(id)
        Unit
    }
}
