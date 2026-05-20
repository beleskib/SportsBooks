package com.example.sportsbook.ui.screens.player.mybookings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.ui.common.EmptyStateView
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.toDisplayDate

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
    val tabs = listOf("Upcoming", "Past")
    val pullToRefreshState = rememberPullToRefreshState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "My Bookings",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            uiState.isLoading -> {
                LoadingIndicator(modifier = Modifier.fillMaxWidth())
            }
            uiState.error != null -> {
                ErrorView(
                    message = uiState.error!!,
                    onRetry = viewModel::loadBookings,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {
                val bookingsToShow = if (selectedTabIndex == 0) {
                    uiState.upcomingBookings
                } else {
                    uiState.pastBookings
                }

                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (bookingsToShow.isEmpty()) {
                        val emptyTitle = if (selectedTabIndex == 0) {
                            "No upcoming bookings"
                        } else {
                            "No past bookings"
                        }
                        EmptyStateView(
                            title = emptyTitle,
                            subtitle = if (selectedTabIndex == 0) "Book a venue or coach to get started" else null,
                            modifier = Modifier.fillMaxWidth()
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    onClick: () -> Unit,
    onPayNow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val displayName = if (booking.isMatchBooking) {
        booking.matchTitle ?: booking.venue?.name ?: "Match Booking"
    } else {
        booking.venue?.name ?: booking.coach?.name ?: "Booking"
    }

    Card(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Match badge row (for match-linked bookings)
            if (booking.isMatchBooking) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (booking.isParticipant) "Match (Joined)" else "Match (Host)",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (booking.matchStatus != null) {
                        val matchStatusColor = when (booking.matchStatus.lowercase()) {
                            "full" -> androidx.compose.ui.graphics.Color(0xFF16A34A)
                            "open" -> MaterialTheme.colorScheme.primary
                            "in_progress" -> androidx.compose.ui.graphics.Color(0xFF2563EB)
                            "completed" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            text = booking.matchStatus.replace("_", " ").replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = matchStatusColor,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                BookingStatusBadge(status = booking.status)
            }

            // Venue name (shown separately when match title is used as display name)
            if (booking.isMatchBooking && booking.venue != null) {
                Text(
                    text = booking.venue.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            booking.timeSlot?.let { slot ->
                Text(
                    text = slot.slotDate.toDisplayDate(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = slot.displayTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${"%.0f".format(booking.totalPrice)} ден",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            if (booking.status == BookingStatus.APPROVED) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onPayNow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pay Now")
                }
            }
        }
    }
}

@Composable
private fun BookingStatusBadge(status: BookingStatus) {
    val (containerColor, contentColor, label) = when (status) {
        BookingStatus.PENDING -> Triple(
            androidx.compose.ui.graphics.Color(0xFFFFF8E1),
            androidx.compose.ui.graphics.Color(0xFFF57F17),
            "Awaiting Approval"
        )
        BookingStatus.APPROVED -> Triple(
            androidx.compose.ui.graphics.Color(0xFFE8F5E9),
            androidx.compose.ui.graphics.Color(0xFF2E7D32),
            "Approved - Pay Now"
        )
        BookingStatus.CONFIRMED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Confirmed"
        )
        BookingStatus.COMPLETED -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Completed"
        )
        BookingStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Cancelled"
        )
        BookingStatus.NO_SHOW -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "No Show"
        )
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyBookingsScreenPreview() {
    val sampleBookings = listOf(
        Booking(
            id = 1L,
            status = BookingStatus.CONFIRMED,
            totalPrice = 25.00,
            venue = com.example.sportsbook.domain.model.Venue(id = 1L, name = "City Tennis Center", address = ""),
            timeSlot = com.example.sportsbook.domain.model.TimeSlot(
                id = 1L,
                slotDate = "2026-03-25",
                startTime = "10:00",
                endTime = "11:00"
            )
        ),
        Booking(
            id = 2L,
            status = BookingStatus.PENDING,
            totalPrice = 40.00,
            venue = com.example.sportsbook.domain.model.Venue(id = 2L, name = "Downtown Basketball Court", address = ""),
            timeSlot = com.example.sportsbook.domain.model.TimeSlot(
                id = 2L,
                slotDate = "2026-03-26",
                startTime = "14:00",
                endTime = "15:00"
            )
        )
    )
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "My Bookings", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            TabRow(selectedTabIndex = 0) {
                listOf("Upcoming", "Past").forEachIndexed { index, title ->
                    Tab(
                        selected = index == 0,
                        onClick = {},
                        text = { Text(title) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            sampleBookings.forEach { booking ->
                BookingCard(
                    booking = booking,
                    onClick = {},
                    onPayNow = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}
