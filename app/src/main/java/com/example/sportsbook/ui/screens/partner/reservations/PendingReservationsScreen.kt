package com.example.sportsbook.ui.screens.partner.reservations

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.ui.common.toDisplayDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendingReservationsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    data class UiState(
        val pendingBookings: List<Booking> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val actionSuccess: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadPendingBookings()
        // Auto-refresh every 30 seconds to catch new incoming bookings
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(30_000L)
                loadPendingBookings()
            }
        }
    }

    fun loadPendingBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            bookingRepository.getPartnerBookings(BookingStatus.PENDING)
                .onSuccess { bookings ->
                    _uiState.update { it.copy(pendingBookings = bookings, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun approveBooking(bookingId: Long) {
        viewModelScope.launch {
            bookingRepository.approveBooking(bookingId)
                .onSuccess {
                    _uiState.update { it.copy(actionSuccess = "Booking approved") }
                    loadPendingBookings()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun declineBooking(bookingId: Long) {
        viewModelScope.launch {
            bookingRepository.declineBooking(bookingId)
                .onSuccess {
                    _uiState.update { it.copy(actionSuccess = "Booking declined") }
                    loadPendingBookings()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun clearActionSuccess() {
        _uiState.update { it.copy(actionSuccess = null) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingReservationsScreen(
    onBack: () -> Unit,
    onOpenChat: (Long) -> Unit = {},
    viewModel: PendingReservationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionSuccess) {
        uiState.actionSuccess?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearActionSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pending Reservations") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.pendingBookings.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No pending reservations",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(uiState.pendingBookings, key = { it.id }) { booking ->
                            PendingBookingCard(
                                booking = booking,
                                onApprove = { viewModel.approveBooking(booking.id) },
                                onDecline = { viewModel.declineBooking(booking.id) },
                                onOpenChat = { onOpenChat(booking.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingBookingCard(
    booking: Booking,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
    onOpenChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val venueName = booking.venue?.name ?: booking.coach?.name ?: "Service"
    val playerLabel = booking.playerName?.takeIf { it.isNotBlank() } ?: "Player #${booking.playerId}"

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = venueName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = playerLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            booking.timeSlot?.let { slot ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${slot.slotDate.toDisplayDate()} · ${slot.displayTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${"%.0f".format(booking.totalPrice)} ден",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            booking.expiresAt?.let { expires ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Expires: $expires",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White
                    )
                ) {
                    Text("Approve")
                }
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Decline")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun PendingReservationsScreenPreview() {
    val mockBookings = listOf(
        Booking(
            id = 1L,
            playerId = 42L,
            status = BookingStatus.PENDING,
            totalPrice = 2100.00,
            venue = Venue(id = 1L, name = "City Tennis Center", address = ""),
            timeSlot = TimeSlot(
                id = 1L,
                slotDate = "2026-03-25",
                startTime = "10:00",
                endTime = "11:00"
            ),
            expiresAt = "2026-03-24T18:00:00Z"
        ),
        Booking(
            id = 2L,
            playerId = 17L,
            status = BookingStatus.PENDING,
            totalPrice = 3000.00,
            venue = Venue(id = 2L, name = "Downtown Basketball Court", address = ""),
            timeSlot = TimeSlot(
                id = 2L,
                slotDate = "2026-03-26",
                startTime = "14:00",
                endTime = "15:00"
            )
        )
    )
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pending Reservations") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(mockBookings, key = { it.id }) { booking ->
                    PendingBookingCard(
                        booking = booking,
                        onApprove = {},
                        onDecline = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
