package com.example.sportsbook.ui.screens.player.mybookings

import androidx.lifecycle.SavedStateHandle
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.model.BookingReceipt
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.toDisplayDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkNavBar
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.ErrorRed
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import com.example.sportsbook.ui.theme.OrangeAccent

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class BookingDetailViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class State(
        val booking: Booking? = null,
        val receipt: BookingReceipt? = null,
        val isLoading: Boolean = false,
        val receiptLoading: Boolean = false,
        val error: String? = null,
        val cancelSuccess: Boolean = false
    )

    private val bookingId: Long = checkNotNull(savedStateHandle["bookingId"])

    private val _uiState = MutableStateFlow(State())
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    init {
        loadBooking()
    }

    fun loadBooking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            bookingRepository.getBookingById(bookingId)
                .onSuccess { booking ->
                    _uiState.update { it.copy(booking = booking, isLoading = false) }
                    if (booking.status == BookingStatus.CONFIRMED || booking.status == BookingStatus.COMPLETED) {
                        loadReceipt()
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun loadReceipt() {
        viewModelScope.launch {
            _uiState.update { it.copy(receiptLoading = true) }
            bookingRepository.getBookingReceipt(bookingId)
                .onSuccess { receipt ->
                    _uiState.update { it.copy(receipt = receipt, receiptLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(receiptLoading = false) }
                }
        }
    }

    fun cancelBooking() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            bookingRepository.cancelBooking(bookingId)
                .onSuccess {
                    _uiState.update { it.copy(cancelSuccess = true, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun BookingDetailScreen(
    bookingId: Long,
    onWriteReview: (Long) -> Unit,
    onOpenChat: (Long) -> Unit = {},
    onBack: () -> Unit,
    viewModel: BookingDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.cancelSuccess) {
        if (uiState.cancelSuccess) onBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (uiState.booking != null) 80.dp else 0.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Header ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Booking Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("⋯", fontSize = 18.sp, color = DarkTextPrimary)
                }
            }

            when {
                uiState.isLoading -> LoadingIndicator(modifier = Modifier.fillMaxWidth())
                uiState.error != null -> ErrorView(message = uiState.error!!, onRetry = viewModel::loadBooking, modifier = Modifier.fillMaxWidth())
                uiState.booking != null -> {
                    val booking = uiState.booking!!
                    BookingDetailContent(booking = booking, receipt = uiState.receipt, onOpenChat = onOpenChat, onWriteReview = onWriteReview)
                }
            }
        }
        if (uiState.booking != null) {
            BookingDetailActionBar(
                status = uiState.booking!!.status,
                onCancel = { viewModel.cancelBooking() },
                onReschedule = {},
                onDirections = {},
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun BookingDetailContent(
    booking: Booking,
    receipt: BookingReceipt?,
    onOpenChat: (Long) -> Unit,
    onWriteReview: (Long) -> Unit,
) {
    // ── Status banner ────────────────────────────────────────────────────
    val (statusBg, statusBorder, statusTextColor, statusDot, statusLabel) = when (booking.status) {
        BookingStatus.CONFIRMED -> quintuple(
            GreenAccent.copy(alpha = 0.15f), GreenAccent.copy(alpha = 0.3f), GreenAccent, GreenAccent, "Confirmed"
        )
        BookingStatus.PENDING -> quintuple(
            OrangeAccent.copy(alpha = 0.15f), OrangeAccent.copy(alpha = 0.3f), OrangeAccent, OrangeAccent, "Pending"
        )
        BookingStatus.CANCELLED -> quintuple(
            ErrorRed.copy(alpha = 0.15f), ErrorRed.copy(alpha = 0.3f), ErrorRed, ErrorRed, "Cancelled"
        )
        BookingStatus.COMPLETED -> quintuple(
            DarkSurface, DarkBorder, DarkTextSecondary, DarkTextSecondary, "Completed"
        )
        else -> quintuple(DarkSurface, DarkBorder, DarkTextSecondary, DarkTextSecondary, booking.status.name.lowercase().replaceFirstChar { it.uppercase() })
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(statusBg)
            .border(1.dp, statusBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(statusDot))
        Spacer(modifier = Modifier.width(10.dp))
        Text(statusLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = statusTextColor, modifier = Modifier.weight(1f))
        Text("#BK-${booking.id}", fontSize = 12.sp, color = DarkTextSecondary)
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Venue card ───────────────────────────────────────────────────────
    val venueName = booking.venue?.name ?: booking.coach?.name ?: "Booking"
    val sportEmoji = when (booking.venue?.sportType?.name?.uppercase()) {
        "BASKETBALL" -> "🏀"
        "FOOTBALL" -> "⚽"
        "TENNIS" -> "🎾"
        "VOLLEYBALL" -> "🏐"
        else -> "🏟️"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Brush.linearGradient(listOf(Color(0xFF1a3a1a), Color(0xFF0d2b0d)))),
            contentAlignment = Alignment.Center,
        ) {
            Text(sportEmoji, fontSize = 48.sp)
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(venueName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            Text("📍 ${booking.venue?.address ?: "—"}", fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 4.dp))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Detail grid (2×2) ────────────────────────────────────────────────
    val slot = booking.timeSlot
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DetailGridCard("📅", "Date", slot?.slotDate?.toDisplayDate() ?: "—", Modifier.weight(1f))
        DetailGridCard("⏰", "Time", slot?.displayTime ?: "—", Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DetailGridCard("🏟️", "Venue", booking.venue?.name ?: booking.coach?.name ?: "—", Modifier.weight(1f))
        val duration = if (slot != null) {
            try {
                val start = slot.startTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
                val end = slot.endTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
                val mins = end - start
                if (mins >= 60) "${mins / 60}h${if (mins % 60 > 0) " ${mins % 60}m" else ""}" else "${mins}m"
            } catch (_: Exception) { "—" }
        } else "—"
        DetailGridCard("⏱️", "Duration", duration, Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Booked By ─────────────────────────────────────────────────────
    Text("Booked By", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(modifier = Modifier.height(12.dp))

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val bookerName = booking.playerName ?: "You"
        val bookerStatus = when (booking.status) {
            BookingStatus.CONFIRMED -> "Confirmed • ${booking.totalPrice.toInt()} MKD"
            BookingStatus.PENDING -> "Pending payment"
            BookingStatus.COMPLETED -> "Completed • ${booking.totalPrice.toInt()} MKD"
            BookingStatus.CANCELLED -> "Cancelled"
            else -> booking.status.name.lowercase().replaceFirstChar { it.uppercase() }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkBorder),
                contentAlignment = Alignment.Center,
            ) {
                Text("🧑", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(bookerName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                Text(bookerStatus, fontSize = 12.sp, color = DarkTextSecondary)
            }
            if (booking.isMatchBooking) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GreenAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text("Match", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GreenAccent)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Payment ──────────────────────────────────────────────────────────
    Text("Payment", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(modifier = Modifier.height(12.dp))

    val total = receipt?.total ?: booking.totalPrice
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PayRow("Court rental (1h)", "${"%.0f".format(total)} MKD")
        PayRow("Split (4 ways)", "${"%.0f".format(total / 4)} MKD/person")
        PayRow("Your share", "${"%.0f".format(total / 4)} MKD")
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Status", fontSize = 14.sp, color = DarkTextPrimary, fontWeight = FontWeight.SemiBold)
            Text("Paid ✓", fontSize = 14.sp, color = GreenAccent, fontWeight = FontWeight.SemiBold)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ── Booking chat preview ─────────────────────────────────────────────
    Text("Booking Chat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(modifier = Modifier.height(12.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .clickable { onOpenChat(booking.id) }
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("3 messages", fontSize = 13.sp, color = DarkTextSecondary)
            Box(
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(GreenAccent).padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("2 new", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(DarkBorder),
                contentAlignment = Alignment.Center,
            ) {
                Text("M", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF252525))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text("I'll bring the extra rackets! See you at 6 🎾", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}

// ── Bottom action bar ─────────────────────────────────────────────────────────

@Composable
private fun BookingDetailActionBar(
    status: BookingStatus,
    onCancel: () -> Unit,
    onReschedule: () -> Unit,
    onDirections: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, DarkBg)))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ErrorRed.copy(alpha = 0.1f))
                    .border(1.dp, Color(0xFFC62828), RoundedCornerShape(14.dp))
                    .clickable(onClick = onCancel)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF5350))
            }
        }
        if (status == BookingStatus.CONFIRMED) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onReschedule)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Reschedule", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(GreenAccent, GreenDark)))
                .clickable(onClick = onDirections)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Directions", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

// ── Small helpers ─────────────────────────────────────────────────────────────

@Composable
private fun DetailGridCard(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(16.dp),
    ) {
        Text(icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(label.uppercase(), fontSize = 11.sp, color = DarkTextSecondary, letterSpacing = 0.5.sp)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun PayRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.67f))
        Text(value, fontSize = 14.sp, color = Color.White.copy(alpha = 0.67f))
    }
}

private data class StatusStyle(
    val bg: Color, val border: Color, val text: Color, val dot: Color, val label: String
)

private fun quintuple(bg: Color, border: Color, text: Color, dot: Color, label: String) =
    StatusStyle(bg, border, text, dot, label)

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun BookingDetailScreenPreview() {
    val sample = Booking(
        id = 2847L,
        status = BookingStatus.CONFIRMED,
        totalPrice = 50.0,
        venue = com.example.sportsbook.domain.model.Venue(
            id = 1L, name = "Champions Tennis Club", address = "123 Sport Ave, Downtown",
            sportType = com.example.sportsbook.domain.enums.SportType.TENNIS,
        ),
        timeSlot = com.example.sportsbook.domain.model.TimeSlot(
            id = 1L, slotDate = "2026-06-05", startTime = "18:00", endTime = "19:00"
        ),
    )
    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp).verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Booking Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }
            BookingDetailContent(booking = sample, receipt = null, onOpenChat = {}, onWriteReview = {})
        }
        BookingDetailActionBar(
            status = sample.status,
            onCancel = {}, onReschedule = {}, onDirections = {},
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
