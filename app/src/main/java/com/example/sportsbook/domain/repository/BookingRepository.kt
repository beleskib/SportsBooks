package com.example.sportsbook.domain.repository

import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.model.BookingReceipt

interface BookingRepository {
    suspend fun createBooking(timeSlotId: Long, notes: String? = null): Result<Booking>
    suspend fun getMyBookings(status: BookingStatus? = null): Result<List<Booking>>
    suspend fun getBookingById(id: Long): Result<Booking>
    suspend fun cancelBooking(id: Long): Result<Booking>
    suspend fun getPartnerBookings(status: BookingStatus? = null): Result<List<Booking>>
    suspend fun updateBookingStatus(id: Long, status: BookingStatus): Result<Booking>
    suspend fun approveBooking(id: Long): Result<Booking>
    suspend fun declineBooking(id: Long): Result<Booking>
    suspend fun getBookingReceipt(bookingId: Long): Result<BookingReceipt>
}
