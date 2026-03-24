package com.example.sportsbook.domain.model

import com.example.sportsbook.domain.enums.BookingStatus

data class PartnerDashboardStats(
    val totalBookings: Int = 0,
    val confirmedBookings: Int = 0,
    val totalRevenue: Double = 0.0,
    val avgRating: Double = 0.0,
    val totalReviews: Int = 0,
    val upcomingBookings: Int = 0,
    val revenueByMonth: List<MonthlyRevenue> = emptyList(),
    val bookingsByStatus: List<BookingStatusCount> = emptyList()
)

data class MonthlyRevenue(
    val month: String = "",
    val revenue: Double = 0.0,
    val bookingCount: Int = 0
)

data class BookingStatusCount(
    val status: BookingStatus = BookingStatus.PENDING,
    val count: Int = 0
)
