package com.example.sportsbook.ui.screens.partner.reservations

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
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
class PendingReservationsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
) : ViewModel() {

    data class UiState(
        val pendingBookings: List<Booking> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val actionSuccess: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadPendingBookings()
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

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun PendingReservationsScreen(
    onBack: () -> Unit,
    onOpenChat: (Long) -> Unit = {},
    viewModel: PendingReservationsViewModel = hiltViewModel(),
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ───────────────────────────────────────────────────
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
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pending Reservations", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                    if (uiState.pendingBookings.isNotEmpty()) {
                        Text("${uiState.pendingBookings.size} awaiting review", fontSize = 13.sp, color = OrangeAccent)
                    }
                }
                // Live badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OrangeAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(OrangeAccent))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Live", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OrangeAccent)
                    }
                }
            }

            // ── Content ──────────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenAccent)
                    }
                }
                uiState.pendingBookings.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
                            Text("✅", fontSize = 56.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("All caught up!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            Text("No pending reservations at the moment", fontSize = 14.sp, color = DarkTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                        items(uiState.pendingBookings, key = { it.id }) { booking ->
                            PendingBookingCard(
                                booking = booking,
                                onApprove = { viewModel.approveBooking(booking.id) },
                                onDecline = { viewModel.declineBooking(booking.id) },
                                onOpenChat = { onOpenChat(booking.id) },
                            )
                        }
                        item { Spacer(modifier = Modifier.height(32.dp)) }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ── Booking card ─────────────────────────────────────────────────────────────

@Composable
private fun PendingBookingCard(
    booking: Booking,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
    onOpenChat: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val venueName = booking.venue?.name ?: booking.coach?.name ?: "Service"
    val playerLabel = booking.playerName?.takeIf { it.isNotBlank() } ?: "Player #${booking.playerId}"
    val sportEmoji = when {
        venueName.contains("tennis", ignoreCase = true) -> "🎾"
        venueName.contains("basket", ignoreCase = true) -> "🏀"
        venueName.contains("football", ignoreCase = true) || venueName.contains("soccer", ignoreCase = true) -> "⚽"
        venueName.contains("volleyball", ignoreCase = true) -> "🏐"
        venueName.contains("swim", ignoreCase = true) -> "🏊"
        else -> "🏟️"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(16.dp),
    ) {
        // Header row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrangeAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(sportEmoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(venueName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                Text(playerLabel, fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangeAccent.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text("Pending", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OrangeAccent)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
        Spacer(modifier = Modifier.height(12.dp))

        // Details
        booking.timeSlot?.let { slot ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailChip("📅", slot.slotDate.toDisplayDate())
                DetailChip("🕐", slot.displayTime)
                DetailChip("💰", "${"%.0f".format(booking.totalPrice)} MKD")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        booking.expiresAt?.let { expires ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A1A0A))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("⏰", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Expires: $expires", fontSize = 12.sp, color = OrangeAccent)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onApprove)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("✓ Approve", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2A1515))
                    .border(1.dp, Color(0xFF5C1C1C), RoundedCornerShape(10.dp))
                    .clickable(onClick = onDecline)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("✕ Decline", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF5350))
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBorder)
                    .clickable(onClick = onOpenChat),
                contentAlignment = Alignment.Center,
            ) {
                Text("💬", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun DetailChip(emoji: String, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(5.dp))
        Text(label, fontSize = 12.sp, color = DarkTextSecondary)
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PendingReservationsScreenPreview() {
    val mockBookings = listOf(
        Booking(id = 1L, playerId = 42L, status = BookingStatus.PENDING, totalPrice = 2100.00, playerName = "Marco Silva", venue = Venue(id = 1L, name = "City Tennis Center", address = ""), timeSlot = TimeSlot(id = 1L, slotDate = "2026-06-05", startTime = "10:00", endTime = "11:00"), expiresAt = "2026-06-04T18:00:00Z"),
        Booking(id = 2L, playerId = 17L, status = BookingStatus.PENDING, totalPrice = 3000.00, playerName = "Elena Rossi", venue = Venue(id = 2L, name = "Downtown Basketball Court", address = ""), timeSlot = TimeSlot(id = 2L, slotDate = "2026-06-06", startTime = "14:00", endTime = "15:00")),
    )
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
            Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Pending Reservations", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(mockBookings) { booking ->
                    PendingBookingCard(booking = booking, onApprove = {}, onDecline = {})
                }
            }
        }
}
