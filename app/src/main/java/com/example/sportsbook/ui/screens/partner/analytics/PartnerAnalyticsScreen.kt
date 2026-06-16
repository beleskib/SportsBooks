package com.example.sportsbook.ui.screens.partner.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.model.BookingStatusCount
import com.example.sportsbook.domain.model.MonthlyRevenue
import com.example.sportsbook.domain.model.PartnerDashboardStats
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.repository.DashboardRepository
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import com.example.sportsbook.ui.theme.OrangeAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PartnerAnalyticsViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    data class UiState(
        val stats: PartnerDashboardStats? = null,
        val recentBookings: List<Booking> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { loadAnalytics() }

    fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val statsDeferred = async { dashboardRepository.getPartnerStats() }
            val bookingsDeferred = async { bookingRepository.getPartnerBookings() }

            statsDeferred.await()
                .onSuccess { stats -> _uiState.update { it.copy(stats = stats) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }

            bookingsDeferred.await()
                .onSuccess { bookings ->
                    _uiState.update { it.copy(recentBookings = bookings.take(10)) }
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

@Composable
fun PartnerAnalyticsScreen(
    onBack: () -> Unit,
    viewModel: PartnerAnalyticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PartnerAnalyticsContent(uiState = uiState, onBack = onBack, onRetry = viewModel::loadAnalytics)
}

@Composable
private fun PartnerAnalyticsContent(
    uiState: PartnerAnalyticsViewModel.UiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Analytics", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }

        // ── Content ──────────────────────────────────────────────────────
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }
            uiState.error != null && uiState.stats == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("⚠️", fontSize = 40.sp)
                        Text(uiState.error, fontSize = 14.sp, color = DarkTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(GreenAccent).clickable(onClick = onRetry).padding(horizontal = 24.dp, vertical = 12.dp),
                        ) {
                            Text("Retry", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
            }
            uiState.stats == null && uiState.recentBookings.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 48.sp)
                        Text("No data yet", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary, modifier = Modifier.padding(top = 12.dp))
                        Text("Start accepting bookings to see your stats", fontSize = 13.sp, color = DarkTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp, start = 40.dp, end = 40.dp))
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    uiState.stats?.let { stats ->
                        item { StatsGrid(stats = stats) }
                        item { RevenueTrendSection(revenueByMonth = stats.revenueByMonth) }
                        item { BookingStatusSection(bookingsByStatus = stats.bookingsByStatus) }
                    }
                    item {
                        Text("Recent Bookings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                    if (uiState.recentBookings.isEmpty()) {
                        item {
                            Text("No recent bookings", fontSize = 14.sp, color = DarkTextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    } else {
                        items(uiState.recentBookings) { booking ->
                            BookingListItem(booking = booking)
                        }
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(stats: PartnerDashboardStats) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnalyticsStatCard("💰", "Total Revenue", formatCurrency(stats.totalRevenue), GreenAccent, Modifier.weight(1f))
            AnalyticsStatCard("📅", "Total Bookings", stats.totalBookings.toString(), Color(0xFF42A5F5), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AnalyticsStatCard("⭐", "Avg Rating", String.format(Locale.US, "%.1f / 5.0", stats.avgRating), Color(0xFFFFC107), Modifier.weight(1f))
            AnalyticsStatCard("📋", "Upcoming", stats.upcomingBookings.toString(), OrangeAccent, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AnalyticsStatCard(
    emoji: String,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(16.dp),
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 11.sp, color = DarkTextSecondary)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun RevenueTrendSection(revenueByMonth: List<MonthlyRevenue>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Revenue (Last 6 Months)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (revenueByMonth.isEmpty()) {
                Text("No revenue data yet", fontSize = 13.sp, color = DarkTextSecondary)
            } else {
                val maxRevenue = revenueByMonth.maxOfOrNull { it.revenue } ?: 1.0
                revenueByMonth.forEach { monthlyRevenue ->
                    RevenueBar(monthlyRevenue = monthlyRevenue, maxRevenue = maxRevenue)
                }
            }
        }
    }
}

@Composable
private fun RevenueBar(monthlyRevenue: MonthlyRevenue, maxRevenue: Double) {
    val fraction = if (maxRevenue > 0) (monthlyRevenue.revenue / maxRevenue).toFloat() else 0f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = formatMonthLabel(monthlyRevenue.month),
            fontSize = 12.sp,
            color = DarkTextSecondary,
            modifier = Modifier.width(52.dp),
        )
        Box(modifier = Modifier.weight(1f).height(24.dp)) {
            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)).background(DarkBorder))
            Box(modifier = Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(24.dp).clip(RoundedCornerShape(4.dp)).background(GreenAccent))
        }
        Text(
            text = formatCurrencyShort(monthlyRevenue.revenue),
            fontSize = 12.sp,
            color = DarkTextPrimary,
            modifier = Modifier.width(64.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun BookingStatusSection(bookingsByStatus: List<BookingStatusCount>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Bookings by Status", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            bookingsByStatus.forEach { statusCount ->
                BookingStatusRow(statusCount = statusCount)
            }
        }
    }
}

@Composable
private fun BookingStatusRow(statusCount: BookingStatusCount) {
    val color = bookingStatusColor(statusCount.status)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(
            text = statusCount.status.name.lowercase().replaceFirstChar { it.uppercase() },
            fontSize = 14.sp,
            color = DarkTextPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = statusCount.count.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DarkTextPrimary,
        )
    }
}

@Composable
private fun BookingListItem(booking: Booking) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = booking.playerName ?: "Player #${booking.playerId}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary,
            )
            val venueName = booking.venue?.name ?: booking.coach?.name ?: ""
            if (venueName.isNotEmpty()) {
                Text(venueName, fontSize = 12.sp, color = DarkTextSecondary)
            }
            val dateLabel = booking.timeSlot?.slotDate ?: booking.createdAt?.take(10) ?: ""
            if (dateLabel.isNotEmpty()) {
                Text(dateLabel, fontSize = 12.sp, color = DarkTextSecondary)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(formatCurrency(booking.totalPrice), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            val statusColor = bookingStatusColor(booking.status)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = booking.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
                )
            }
        }
    }
}

private fun bookingStatusColor(status: BookingStatus): Color = when (status) {
    BookingStatus.PENDING -> Color(0xFFFFA726)
    BookingStatus.APPROVED -> Color(0xFF42A5F5)
    BookingStatus.CONFIRMED -> Color(0xFF66BB6A)
    BookingStatus.COMPLETED -> Color(0xFF9E9E9E)
    BookingStatus.CANCELLED -> Color(0xFFEF5350)
    BookingStatus.NO_SHOW -> Color(0xFFEF5350)
}

private fun formatCurrency(amount: Double): String =
    String.format(Locale.US, "%,.0f ден", amount)

private fun formatCurrencyShort(amount: Double): String =
    if (amount >= 1000) String.format(Locale.US, "%.0fK ден", amount / 1000)
    else String.format(Locale.US, "%.0f ден", amount)

private fun formatMonthLabel(month: String): String {
    // Input: "2026-03", output: "Mar '26"
    return try {
        val parts = month.split("-")
        if (parts.size < 2) return month
        val year = parts[0].takeLast(2)
        val monthNum = parts[1].toInt()
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        "${monthNames.getOrElse(monthNum - 1) { month }} '$year"
    } catch (e: Exception) {
        month
    }
}

@Preview(showBackground = true)
@Composable
private fun PartnerAnalyticsScreenPreview() {
    val sampleStats = PartnerDashboardStats(
            totalBookings = 47,
            confirmedBookings = 32,
            totalRevenue = 3_750.0,
            avgRating = 4.7,
            totalReviews = 23,
            upcomingBookings = 8,
            revenueByMonth = listOf(
                MonthlyRevenue(month = "2025-10", revenue = 450.0, bookingCount = 6),
                MonthlyRevenue(month = "2025-11", revenue = 620.0, bookingCount = 8),
                MonthlyRevenue(month = "2025-12", revenue = 380.0, bookingCount = 5),
                MonthlyRevenue(month = "2026-01", revenue = 700.0, bookingCount = 9),
                MonthlyRevenue(month = "2026-02", revenue = 850.0, bookingCount = 11),
                MonthlyRevenue(month = "2026-03", revenue = 750.0, bookingCount = 8)
            ),
            bookingsByStatus = listOf(
                BookingStatusCount(status = BookingStatus.CONFIRMED, count = 32),
                BookingStatusCount(status = BookingStatus.PENDING, count = 5),
                BookingStatusCount(status = BookingStatus.COMPLETED, count = 8),
                BookingStatusCount(status = BookingStatus.CANCELLED, count = 2)
            )
        )
        val sampleBookings = listOf(
            Booking(
                id = 1, playerId = 101, timeSlotId = 1,
                status = BookingStatus.CONFIRMED,
                totalPrice = 75.0,
                playerName = "Alex Johnson",
                createdAt = "2026-03-22"
            ),
            Booking(
                id = 2, playerId = 102, timeSlotId = 2,
                status = BookingStatus.PENDING,
                totalPrice = 50.0,
                playerName = "Maria Garcia",
                createdAt = "2026-03-21"
            ),
            Booking(
                id = 3, playerId = 103, timeSlotId = 3,
                status = BookingStatus.COMPLETED,
                totalPrice = 90.0,
                playerName = "Sam Lee",
                createdAt = "2026-03-20"
            )
        )

        PartnerAnalyticsContent(
            uiState = PartnerAnalyticsViewModel.UiState(
                stats = sampleStats,
                recentBookings = sampleBookings,
                isLoading = false,
                error = null
            ),
            onBack = {},
            onRetry = {}
        )
}
