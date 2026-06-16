package com.example.sportsbook.ui.screens.player.mybookings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.ui.common.EmptyStateView
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.DarkTextTertiary
import com.example.sportsbook.ui.theme.ErrorRed
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.OrangeAccent

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBookingClick: (Long) -> Unit,
    onMatchClick: (Long) -> Unit = {},
    onPayNow: (Long) -> Unit = {},
    viewModel: MyBookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Past", "Cancelled")
    val pullToRefreshState = rememberPullToRefreshState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "My Bookings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .clickable { }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text("☰ Filter", fontSize = 13.sp, color = DarkTextPrimary)
            }
        }

        // ── Tab bar ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface),
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) GreenAccent else Color.Transparent)
                        .clickable { selectedTabIndex = index }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color.White else DarkTextSecondary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Content ──────────────────────────────────────────────────────
        when {
            uiState.isLoading -> {
                LoadingIndicator(modifier = Modifier.fillMaxWidth())
            }

            uiState.error != null -> {
                ErrorView(
                    message = uiState.error!!,
                    onRetry = viewModel::loadBookings,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            else -> {
                val bookingsToShow = when (selectedTabIndex) {
                    0 -> uiState.upcomingBookings
                    1 -> uiState.pastBookings.filter { it.status != BookingStatus.CANCELLED }
                    2 -> (uiState.upcomingBookings + uiState.pastBookings).filter { it.status == BookingStatus.CANCELLED }
                    else -> emptyList()
                }

                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (bookingsToShow.isEmpty()) {
                        val emptyTitle = when (selectedTabIndex) {
                            0 -> "No upcoming bookings"
                            1 -> "No past bookings"
                            else -> "No cancelled bookings"
                        }
                        EmptyStateView(
                            title = emptyTitle,
                            subtitle = if (selectedTabIndex == 0) "Book a venue or coach to get started" else null,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(bookingsToShow, key = { it.id }) { booking ->
                                BookingCard(
                                    booking = booking,
                                    onClick = {
                                        if (booking.isMatchBooking && booking.matchId != null) {
                                            onMatchClick(booking.matchId)
                                        } else {
                                            onBookingClick(booking.id)
                                        }
                                    },
                                    onPayNow = { onPayNow(booking.id) },
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ── Booking Card ─────────────────────────────────────────────────────────────

@Composable
private fun BookingCard(
    booking: Booking,
    onClick: () -> Unit,
    onPayNow: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val displayName = if (booking.isMatchBooking) {
        booking.matchTitle ?: booking.venue?.name ?: "Match Booking"
    } else {
        booking.venue?.name ?: booking.coach?.name ?: "Booking"
    }

    val sportEmoji = when {
        booking.venue?.sportType?.name?.uppercase() == "BASKETBALL" -> "🏀"
        booking.venue?.sportType?.name?.uppercase() == "FOOTBALL" -> "⚽"
        booking.venue?.sportType?.name?.uppercase() == "TENNIS" -> "🎾"
        booking.venue?.sportType?.name?.uppercase() == "VOLLEYBALL" -> "🏐"
        booking.coach != null -> "👋"
        else -> "🏟️"
    }

    val gradientColors = when {
        booking.venue?.sportType?.name?.uppercase() == "BASKETBALL" -> listOf(Color(0xFF1B5E20), GreenAccent)
        booking.venue?.sportType?.name?.uppercase() == "FOOTBALL" -> listOf(Color(0xFFE65100), Color(0xFFFF9800))
        booking.venue?.sportType?.name?.uppercase() == "TENNIS" -> listOf(Color(0xFF0D47A1), Color(0xFF2196F3))
        booking.coach != null -> listOf(Color(0xFF4A148C), Color(0xFF9C27B0))
        else -> listOf(Color(0xFF1B5E20), GreenAccent)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        // Top row: image + info + status
        Row(modifier = Modifier.fillMaxWidth()) {
            // Sport image
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center,
            ) {
                Text(sportEmoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (booking.isMatchBooking && booking.venue != null) {
                    Text(
                        text = booking.venue.name,
                        fontSize = 12.sp,
                        color = DarkTextSecondary,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                } else {
                    val subtext = booking.venue?.sportType?.displayName
                        ?: booking.coach?.sportType?.displayName
                        ?: ""
                    if (subtext.isNotBlank()) {
                        Text(
                            text = subtext,
                            fontSize = 12.sp,
                            color = DarkTextSecondary,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            BookingStatusPill(status = booking.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Meta row
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            booking.timeSlot?.let { slot ->
                Text(
                    text = "📅 ${slot.slotDate.toDisplayDate()}, ${slot.displayTime}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.67f),
                )
            }
            Text(
                text = "💰 ${"%.0f".format(booking.totalPrice)} MKD",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.67f),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Actions row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DarkBorder),
        )
        Spacer(modifier = Modifier.height(10.dp))

        when (booking.status) {
            BookingStatus.APPROVED -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("Pay Now", ActionStyle.PRIMARY, Modifier.weight(1f), onClick = onPayNow)
                    ActionButton("View Details", ActionStyle.SECONDARY, Modifier.weight(1f))
                }
            }

            BookingStatus.PENDING -> {
                ActionButton("Waiting for approval...", ActionStyle.SECONDARY, Modifier.fillMaxWidth())
            }

            BookingStatus.CONFIRMED -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("View Details", ActionStyle.PRIMARY, Modifier.weight(1f), onClick = onClick)
                    ActionButton("Get Directions", ActionStyle.SECONDARY, Modifier.weight(1f))
                    ActionButton("Cancel", ActionStyle.DANGER, Modifier.weight(1f))
                }
            }

            BookingStatus.COMPLETED -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("View Details", ActionStyle.PRIMARY, Modifier.weight(1f), onClick = onClick)
                    ActionButton("Rebook", ActionStyle.SECONDARY, Modifier.weight(1f))
                }
            }

            BookingStatus.CANCELLED, BookingStatus.NO_SHOW -> {
                ActionButton("View Details", ActionStyle.SECONDARY, Modifier.fillMaxWidth(), onClick = onClick)
            }
        }
    }
}

// ── Status Pill ──────────────────────────────────────────────────────────────

@Composable
private fun BookingStatusPill(status: BookingStatus) {
    val (bg, textColor, label) = when (status) {
        BookingStatus.PENDING -> Triple(
            Color(0xFF3A2E1B),
            OrangeAccent,
            "Pending",
        )
        BookingStatus.APPROVED -> Triple(
            Color(0xFF1B3A1E),
            GreenAccent,
            "Approved",
        )
        BookingStatus.CONFIRMED -> Triple(
            Color(0xFF1B3A1E),
            GreenAccent,
            "Confirmed",
        )
        BookingStatus.COMPLETED -> Triple(
            DarkSurface,
            DarkTextSecondary,
            "Completed",
        )
        BookingStatus.CANCELLED -> Triple(
            Color(0xFF3A1B1B),
            ErrorRed,
            "Cancelled",
        )
        BookingStatus.NO_SHOW -> Triple(
            DarkSurface,
            DarkTextSecondary,
            "No Show",
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .let {
                if (status == BookingStatus.COMPLETED) it.border(1.dp, Color(0xFF333333), RoundedCornerShape(6.dp))
                else it
            }
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

// ── Action Buttons ───────────────────────────────────────────────────────────

private enum class ActionStyle { PRIMARY, SECONDARY, DANGER }

@Composable
private fun ActionButton(
    label: String,
    style: ActionStyle,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val bg = when (style) {
        ActionStyle.PRIMARY -> GreenAccent
        ActionStyle.SECONDARY -> DarkBorder
        ActionStyle.DANGER -> Color.Transparent
    }
    val textColor = when (style) {
        ActionStyle.PRIMARY -> Color.White
        ActionStyle.SECONDARY -> Color.White.copy(alpha = 0.67f)
        ActionStyle.DANGER -> ErrorRed
    }
    val borderMod = if (style == ActionStyle.DANGER) {
        Modifier.border(1.dp, ErrorRed, RoundedCornerShape(8.dp))
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(borderMod)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun MyBookingsScreenPreview() {
    val sampleBookings = listOf(
        Booking(
            id = 1L,
            status = BookingStatus.CONFIRMED,
            totalPrice = 800.0,
            venue = com.example.sportsbook.domain.model.Venue(
                id = 1L,
                name = "Arena Sport Center",
                address = "",
                sportType = com.example.sportsbook.domain.enums.SportType.BASKETBALL,
            ),
            timeSlot = com.example.sportsbook.domain.model.TimeSlot(
                id = 1L,
                slotDate = "2026-05-24",
                startTime = "18:00",
                endTime = "19:00",
            ),
        ),
        Booking(
            id = 2L,
            status = BookingStatus.CONFIRMED,
            totalPrice = 600.0,
            venue = com.example.sportsbook.domain.model.Venue(
                id = 2L,
                name = "Tennis Club Vardar",
                address = "",
                sportType = com.example.sportsbook.domain.enums.SportType.TENNIS,
            ),
            timeSlot = com.example.sportsbook.domain.model.TimeSlot(
                id = 2L,
                slotDate = "2026-05-25",
                startTime = "10:00",
                endTime = "11:00",
            ),
        ),
        Booking(
            id = 3L,
            status = BookingStatus.PENDING,
            totalPrice = 1200.0,
            coach = com.example.sportsbook.domain.model.Coach(
                id = 1L,
                name = "Coach Aleksandar P.",
                sportType = com.example.sportsbook.domain.enums.SportType.BASKETBALL,
            ),
            timeSlot = com.example.sportsbook.domain.model.TimeSlot(
                id = 3L,
                slotDate = "2026-05-25",
                startTime = "15:00",
                endTime = "16:00",
            ),
        ),
    )
    Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("My Bookings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.weight(1f))
            }

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface),
            ) {
                listOf("Upcoming", "Past", "Cancelled").forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (index == 0) GreenAccent else Color.Transparent)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (index == 0) Color.White else DarkTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Today - May 24", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextSecondary, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            sampleBookings.forEach { booking ->
                BookingCard(booking = booking, onClick = {}, onPayNow = {})
            }
        }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun BookingStatusPillsPreview() {
    Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BookingStatusPill(BookingStatus.CONFIRMED)
            BookingStatusPill(BookingStatus.PENDING)
            BookingStatusPill(BookingStatus.CANCELLED)
            BookingStatusPill(BookingStatus.COMPLETED)
        }
}
