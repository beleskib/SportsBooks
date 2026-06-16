package com.example.sportsbook.ui.screens.partner.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.VenueRepository
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.model.PartnerDashboardStats
import com.example.sportsbook.domain.repository.DashboardRepository
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import com.example.sportsbook.ui.theme.OrangeAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class PartnerDashboardViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val venueRepository: VenueRepository,
    private val coachRepository: CoachRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {

    data class State(
        val totalBookings: Int = 0,
        val pendingBookings: Int = 0,
        val totalRevenue: Double = 0.0,
        val avgRating: Double = 0.0,
        val revenueByMonth: List<com.example.sportsbook.domain.model.MonthlyRevenue> = emptyList(),
        val todayBookings: List<Booking> = emptyList(),
        val venues: List<Venue> = emptyList(),
        val coachProfile: Coach? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(State())
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            bookingRepository.getPartnerBookings()
                .onSuccess { bookings ->
                    val total = bookings.size
                    val pending = bookings.count { it.status == BookingStatus.PENDING }
                    val today = java.time.LocalDate.now().toString()
                    val todayBookings = bookings.filter { b ->
                        b.timeSlot?.slotDate == today ||
                        b.createdAt?.startsWith(today) == true
                    }.sortedBy { it.timeSlot?.startTime }
                    _uiState.update { it.copy(totalBookings = total, pendingBookings = pending, todayBookings = todayBookings) }
                }

            dashboardRepository.getPartnerStats()
                .onSuccess { stats ->
                    _uiState.update { it.copy(
                        totalRevenue = stats.totalRevenue,
                        avgRating = stats.avgRating,
                        revenueByMonth = stats.revenueByMonth,
                    ) }
                }

            venueRepository.getMyVenues()
                .onSuccess { venues ->
                    _uiState.update { it.copy(venues = venues) }
                }

            coachRepository.getMyCoachProfile()
                .onSuccess { coach ->
                    _uiState.update { it.copy(coachProfile = coach) }
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun PartnerDashboardScreen(
    onSignOut: () -> Unit,
    onManageImages: (entityType: String, entityId: Long) -> Unit = { _, _ -> },
    onOpenWebDashboard: () -> Unit = {},
    onBrowseAsPlayer: () -> Unit = {},
    onPaymentSetup: () -> Unit = {},
    onManageTimeSlots: () -> Unit = {},
    onEditVenue: (Long) -> Unit = {},
    onEditCoach: (Long) -> Unit = {},
    onAddVenue: () -> Unit = {},
    onAddCoachProfile: () -> Unit = {},
    onViewPendingReservations: () -> Unit = {},
    onViewAnalytics: () -> Unit = {},
    viewModel: PartnerDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState()),
    ) {
        // ── Gradient header ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(GreenDark, GreenAccent, Color(0xFF81C784)))
                )
                .padding(start = 20.dp, end = 20.dp, top = 56.dp, bottom = 24.dp),
        ) {
            Column {
                Text("Good morning 👋", fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
                val partnerName = uiState.venues.firstOrNull()?.name
                    ?: uiState.coachProfile?.name
                    ?: "Partner Dashboard"
                Text(partnerName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 4.dp))
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open • ${uiState.venues.size} venue${if (uiState.venues.size != 1) "s" else ""}", fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }
            uiState.error != null -> {
                ErrorView(message = uiState.error!!, onRetry = viewModel::loadDashboard, modifier = Modifier.padding(16.dp))
            }
            else -> {
                // ── Quick stats ──────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DashStatCard("📅", uiState.totalBookings.toString(), "Bookings", GreenAccent, null, Modifier.weight(1f))
                    DashStatCard("💰", "${uiState.totalRevenue.toInt()} MKD", "Revenue", Color(0xFF42A5F5), null, Modifier.weight(1f))
                    DashStatCard("⏳", uiState.pendingBookings.toString(), "Pending", OrangeAccent, null, Modifier.weight(1f).clickable { onViewPendingReservations() })
                    val ratingText = if (uiState.avgRating > 0) "%.1f".format(uiState.avgRating) else "—"
                    DashStatCard("⭐", ratingText, "Rating", Color(0xFFFFC107), null, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Revenue card ─────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .padding(16.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Revenue", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text("Last ${uiState.revenueByMonth.size} months", fontSize = 12.sp, color = DarkTextSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${uiState.totalRevenue.toInt()} MKD", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = DarkTextPrimary)
                    if (uiState.totalRevenue == 0.0) {
                        Text("No revenue yet — bookings will show here", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                    }

                    if (uiState.revenueByMonth.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        val maxRevenue = uiState.revenueByMonth.maxOfOrNull { it.revenue } ?: 1.0
                        val months = uiState.revenueByMonth.takeLast(6)
                        Row(
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            months.forEachIndexed { i, m ->
                                val isLast = i == months.lastIndex
                                val frac = if (maxRevenue > 0) (m.revenue / maxRevenue).toFloat().coerceIn(0.05f, 1f) else 0.05f
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .height((60 * frac).dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(if (isLast) GreenAccent else GreenAccent.copy(alpha = 0.3f)),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            months.forEachIndexed { i, m ->
                                val isLast = i == months.lastIndex
                                val label = m.month.takeLast(2).let { mm ->
                                    when (mm) { "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"; "05" -> "May"; "06" -> "Jun"; "07" -> "Jul"; "08" -> "Aug"; "09" -> "Sep"; "10" -> "Oct"; "11" -> "Nov"; "12" -> "Dec"; else -> mm }
                                }
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isLast) GreenAccent else DarkTextSecondary,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Today's schedule ─────────────────────────────────────
                SectionHeader("Today's Schedule", "View All →", onViewPendingReservations)

                if (uiState.todayBookings.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("📅", fontSize = 28.sp)
                        Text("No bookings today", fontSize = 14.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 6.dp))
                    }
                } else {
                    uiState.todayBookings.forEach { booking ->
                        val time = booking.timeSlot?.startTime?.take(5) ?: "—"
                        val name = booking.playerName ?: "Player #${booking.playerId}"
                        val status = when (booking.status) {
                            BookingStatus.CONFIRMED -> "Confirmed"
                            BookingStatus.PENDING -> "Pending"
                            BookingStatus.COMPLETED -> "Completed"
                            BookingStatus.CANCELLED -> "Cancelled"
                            else -> booking.status.name
                        }
                        ScheduleRow(time = time, name = name, status = status)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── My Venues ────────────────────────────────────────────
                SectionHeader("My Venues", "+ Add", onAddVenue)

                if (uiState.venues.isEmpty()) {
                    EmptyAddCard("🏟️", "No venues yet", "Add your first venue to start accepting bookings", onAddVenue)
                } else {
                    uiState.venues.forEach { venue ->
                        ListingCard(
                            title = venue.name,
                            subtitle = "Venue",
                            emoji = "🏟️",
                            imageCount = venue.images.size,
                            onManageImages = { onManageImages("venue", venue.id) },
                            onEdit = { onEditVenue(venue.id) },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Coach Profile ────────────────────────────────────────
                SectionHeader("Coach Profile", if (uiState.coachProfile == null) "+ Create" else null, onAddCoachProfile)

                if (uiState.coachProfile == null) {
                    EmptyAddCard("🏅", "No coach profile", "Create a coach profile to offer training sessions", onAddCoachProfile)
                } else {
                    val coach = uiState.coachProfile!!
                    ListingCard(
                        title = coach.name,
                        subtitle = "Coach",
                        emoji = "🏅",
                        imageCount = coach.images.size,
                        onManageImages = { onManageImages("coach", coach.id) },
                        onEdit = { onEditCoach(coach.id) },
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Quick actions grid ───────────────────────────────────
                Text("Quick Actions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                Spacer(modifier = Modifier.height(12.dp))

                val actions = listOf(
                    Triple("📋", "Manage Slots", onManageTimeSlots),
                    Triple("⏳", "Pending (${uiState.pendingBookings})", onViewPendingReservations),
                    Triple("📊", "Analytics", onViewAnalytics),
                    Triple("💳", "Payments", onPaymentSetup),
                    Triple("🌐", "Web Dashboard", onOpenWebDashboard),
                    Triple("🏃", "Browse as Player", onBrowseAsPlayer),
                )

                val rows = actions.chunked(3)
                rows.forEach { rowActions ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowActions.forEach { (emoji, label, action) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkSurface)
                                    .clickable(onClick = action)
                                    .padding(vertical = 16.dp, horizontal = 8.dp),
                            ) {
                                Text(emoji, fontSize = 24.sp)
                                Text(label, fontSize = 11.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 6.dp), maxLines = 2, lineHeight = 14.sp)
                            }
                        }
                        // Fill empty cells if row has fewer than 3
                        repeat(3 - rowActions.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Sign out ─────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF2A1515))
                        .border(1.dp, Color(0xFF5C1C1C), RoundedCornerShape(14.dp))
                        .clickable(onClick = onSignOut)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Sign Out", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF5350))
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun DashStatCard(
    emoji: String,
    value: String,
    label: String,
    valueColor: Color,
    change: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = valueColor, modifier = Modifier.padding(top = 4.dp))
        Text(label, fontSize = 10.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
        if (change != null) {
            Text(change, fontSize = 9.sp, color = GreenAccent, modifier = Modifier.padding(top = 2.dp), lineHeight = 12.sp)
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String?, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        if (actionLabel != null) {
            Text(actionLabel, fontSize = 13.sp, color = GreenAccent, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onAction))
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun ScheduleRow(time: String, name: String, status: String) {
    val statusColor = when {
        status.contains("Live", ignoreCase = true) -> Color(0xFF4CAF50)
        status.contains("Confirmed", ignoreCase = true) -> Color(0xFF42A5F5)
        else -> OrangeAccent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
            Text(time.substringBefore(":"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            Text(if (time < "12:00") "AM" else "PM", fontSize = 10.sp, color = DarkTextSecondary)
        }
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(statusColor)
                .padding(horizontal = 8.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(statusColor.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
        }
    }
}

@Composable
private fun ListingCard(
    title: String,
    subtitle: String,
    emoji: String,
    imageCount: Int,
    onManageImages: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GreenDark.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            Text("$subtitle · $imageCount image${if (imageCount != 1) "s" else ""}", fontSize = 12.sp, color = DarkTextSecondary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onEdit)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .clickable(onClick = onManageImages)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text("📷", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EmptyAddCard(emoji: String, title: String, desc: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, fontSize = 32.sp)
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(top = 8.dp))
        Text(desc, fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(GreenAccent)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text("+ Add Now", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PartnerDashboardScreenPreview() {
    Column(modifier = Modifier.fillMaxSize().background(DarkBg).verticalScroll(rememberScrollState())) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(GreenDark, GreenAccent)))
                    .padding(start = 20.dp, end = 20.dp, top = 56.dp, bottom = 24.dp),
            ) {
                Column {
                    Text("Good morning 👋", fontSize = 14.sp, color = Color.White.copy(0.85f))
                    Text("Champions Tennis Club", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("🟢 Open • 6 courts available", fontSize = 13.sp, color = Color.White.copy(0.85f), modifier = Modifier.padding(top = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DashStatCard("📅", "12", "Bookings", GreenAccent, "↑ 20% vs last week", Modifier.weight(1f))
                DashStatCard("💰", "580 MKD", "Revenue", Color(0xFF42A5F5), "↑ +45 today", Modifier.weight(1f))
                DashStatCard("⏳", "3", "Pending", OrangeAccent, null, Modifier.weight(1f))
                DashStatCard("⭐", "4.8", "Rating", Color(0xFFFFC107), "+0.1 this month", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader("Today's Schedule", "View All →") {}
            ScheduleRow("10:00", "Marco Silva", "● Live")
            Spacer(modifier = Modifier.height(8.dp))
            ScheduleRow("11:00", "Elena Rossi", "Confirmed")
            Spacer(modifier = Modifier.height(32.dp))
        }
}
