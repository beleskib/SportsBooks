package com.example.sportsbook.ui.screens.partner.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerAnalyticsScreen(
    onBack: () -> Unit,
    viewModel: PartnerAnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PartnerAnalyticsContent(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::loadAnalytics
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PartnerAnalyticsContent(
    uiState: PartnerAnalyticsViewModel.UiState,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partner Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null && uiState.stats == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = uiState.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Button(onClick = onRetry) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.stats == null && uiState.recentBookings.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No analytics data available yet.\nStart accepting bookings to see your stats.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
                ) {
                    // Stats grid
                    uiState.stats?.let { stats ->
                        item {
                            StatsGrid(stats = stats)
                        }

                        // Revenue trend
                        item {
                            RevenueTrendSection(revenueByMonth = stats.revenueByMonth)
                        }

                        // Booking status breakdown
                        item {
                            BookingStatusSection(bookingsByStatus = stats.bookingsByStatus)
                        }
                    }

                    // Recent bookings
                    item {
                        Text(
                            text = "Recent Bookings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (uiState.recentBookings.isEmpty()) {
                        item {
                            Text(
                                text = "No recent bookings",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(uiState.recentBookings) { booking ->
                            BookingListItem(booking = booking)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(stats: PartnerDashboardStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsStatCard(
                icon = Icons.Default.AttachMoney,
                label = "Total Revenue",
                value = formatCurrency(stats.totalRevenue),
                modifier = Modifier.weight(1f)
            )
            AnalyticsStatCard(
                icon = Icons.Default.BookOnline,
                label = "Total Bookings",
                value = stats.totalBookings.toString(),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnalyticsStatCard(
                icon = Icons.Default.Star,
                label = "Avg Rating",
                value = String.format(Locale.US, "%.1f / 5.0", stats.avgRating),
                modifier = Modifier.weight(1f)
            )
            AnalyticsStatCard(
                icon = Icons.Default.CalendarMonth,
                label = "Upcoming",
                value = stats.upcomingBookings.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AnalyticsStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RevenueTrendSection(revenueByMonth: List<MonthlyRevenue>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Revenue (Last 6 Months)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (revenueByMonth.isEmpty()) {
            Text(
                text = "No revenue data yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            val maxRevenue = revenueByMonth.maxOfOrNull { it.revenue } ?: 1.0
            revenueByMonth.forEach { monthlyRevenue ->
                RevenueBar(
                    monthlyRevenue = monthlyRevenue,
                    maxRevenue = maxRevenue
                )
            }
        }
    }
}

@Composable
private fun RevenueBar(monthlyRevenue: MonthlyRevenue, maxRevenue: Double) {
    val fraction = if (maxRevenue > 0) (monthlyRevenue.revenue / maxRevenue).toFloat() else 0f
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val primary = MaterialTheme.colorScheme.primary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = formatMonthLabel(monthlyRevenue.month),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(56.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
        ) {
            // Background bar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(surfaceVariant)
            )
            // Filled bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(primary)
            )
        }
        Text(
            text = formatCurrencyShort(monthlyRevenue.revenue),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(72.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun BookingStatusSection(bookingsByStatus: List<BookingStatusCount>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Bookings by Status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        bookingsByStatus.forEach { statusCount ->
            BookingStatusRow(statusCount = statusCount)
        }
    }
}

@Composable
private fun BookingStatusRow(statusCount: BookingStatusCount) {
    val color = bookingStatusColor(statusCount.status)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = statusCount.status.name.lowercase()
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = statusCount.count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BookingListItem(booking: Booking) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.playerName ?: "Player #${booking.playerId}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    val venueName = booking.venue?.name ?: booking.coach?.name ?: ""
                    if (venueName.isNotEmpty()) {
                        Text(
                            text = venueName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val dateLabel = booking.timeSlot?.slotDate ?: booking.createdAt?.take(10) ?: ""
                    if (dateLabel.isNotEmpty()) {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(booking.totalPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BookingStatusChip(status = booking.status)
                }
            }
        }
    }
}

@Composable
private fun BookingStatusChip(status: BookingStatus) {
    val color = bookingStatusColor(status)
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
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
    MaterialTheme {
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
}
