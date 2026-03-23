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
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        state = pullToRefreshState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(bookingsToShow, key = { it.id }) { booking ->
                                BookingCard(
                                    booking = booking,
                                    onClick = { onBookingClick(booking.id) },
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
    modifier: Modifier = Modifier
) {
    val displayName = booking.venue?.name ?: booking.coach?.name ?: "Booking"

    Card(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                text = "$${"%.2f".format(booking.totalPrice)}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BookingStatusBadge(status: BookingStatus) {
    val (containerColor, contentColor) = when (status) {
        BookingStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        BookingStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        BookingStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        BookingStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        BookingStatus.NO_SHOW -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MyBookingsScreenPreview() {
    MaterialTheme {
        MyBookingsScreen(onBookingClick = {})
    }
}
