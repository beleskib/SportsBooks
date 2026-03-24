package com.example.sportsbook.data.repository

import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.CreateBookingRequestDto
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.repository.BookingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : BookingRepository {

    override suspend fun createBooking(timeSlotId: Long, notes: String?): Result<Booking> = runCatching {
        apiService.createBooking(CreateBookingRequestDto(timeSlotId = timeSlotId, notes = notes)).data.toDomain()
    }

    override suspend fun getMyBookings(status: BookingStatus?): Result<List<Booking>> = runCatching {
        apiService.getMyBookings(status?.name?.lowercase()).data.map { it.toDomain() }
    }

    override suspend fun getBookingById(id: Long): Result<Booking> = runCatching {
        apiService.getBookingById(id).data.toDomain()
    }

    override suspend fun cancelBooking(id: Long): Result<Booking> = runCatching {
        apiService.updateBookingStatus(id, mapOf("status" to "cancelled")).data.toDomain()
    }

    override suspend fun getPartnerBookings(status: BookingStatus?): Result<List<Booking>> = runCatching {
        apiService.getPartnerBookings(status?.name?.lowercase()).data.map { it.toDomain() }
    }

    override suspend fun updateBookingStatus(id: Long, status: BookingStatus): Result<Booking> = runCatching {
        apiService.updateBookingStatus(id, mapOf("status" to status.name.lowercase())).data.toDomain()
    }

    override suspend fun approveBooking(id: Long): Result<Booking> = runCatching {
        apiService.approveBooking(id).data.toDomain()
    }

    override suspend fun declineBooking(id: Long): Result<Booking> = runCatching {
        apiService.declineBooking(id).data.toDomain()
    }
}
