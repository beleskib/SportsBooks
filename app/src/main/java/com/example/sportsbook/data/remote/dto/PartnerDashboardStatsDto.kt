package com.example.sportsbook.data.remote.dto

import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.model.BookingStatusCount
import com.example.sportsbook.domain.model.MonthlyRevenue
import com.example.sportsbook.domain.model.PartnerDashboardStats
import kotlinx.serialization.Serializable

@Serializable
data class PartnerDashboardStatsDto(
    val totalBookings: Int = 0,
    val confirmedBookings: Int = 0,
    val totalRevenue: Double = 0.0,
    val avgRating: Double = 0.0,
    val totalReviews: Int = 0,
    val upcomingBookings: Int = 0,
    val revenueByMonth: List<MonthlyRevenueDto> = emptyList(),
    val bookingsByStatus: List<BookingStatusCountDto> = emptyList()
) {
    fun toDomain(): PartnerDashboardStats = PartnerDashboardStats(
        totalBookings = totalBookings,
        confirmedBookings = confirmedBookings,
        totalRevenue = totalRevenue,
        avgRating = avgRating,
        totalReviews = totalReviews,
        upcomingBookings = upcomingBookings,
        revenueByMonth = revenueByMonth.map { it.toDomain() },
        bookingsByStatus = bookingsByStatus.map { it.toDomain() }
    )
}

@Serializable
data class MonthlyRevenueDto(
    val month: String = "",
    val revenue: Double = 0.0,
    val bookingCount: Int = 0
) {
    fun toDomain(): MonthlyRevenue = MonthlyRevenue(
        month = month,
        revenue = revenue,
        bookingCount = bookingCount
    )
}

@Serializable
data class BookingStatusCountDto(
    val status: BookingStatus = BookingStatus.PENDING,
    val count: Int = 0
) {
    fun toDomain(): BookingStatusCount = BookingStatusCount(
        status = status,
        count = count
    )
}
