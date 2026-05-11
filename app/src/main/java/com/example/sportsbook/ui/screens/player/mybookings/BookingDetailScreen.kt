package com.example.sportsbook.ui.screens.player.mybookings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.model.BookingReceipt
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.common.toDisplayDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
                    if (booking.status == BookingStatus.CONFIRMED ||
                        booking.status == BookingStatus.COMPLETED
                    ) {
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

@OptIn(ExperimentalMaterial3Api::class)
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
        if (uiState.cancelSuccess) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator(modifier = Modifier.fillMaxWidth())
                }
                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error!!,
                        onRetry = viewModel::loadBooking,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                uiState.booking != null -> {
                    val booking = uiState.booking!!
                    val displayName = booking.venue?.name ?: booking.coach?.name ?: "Booking"

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            booking.timeSlot?.let { slot ->
                                DetailRow(label = "Date", value = slot.slotDate.toDisplayDate())
                                Spacer(modifier = Modifier.height(8.dp))
                                DetailRow(label = "Time", value = slot.displayTime)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Show COMPLETED for past confirmed bookings
                            val displayStatus = if (
                                booking.status == BookingStatus.CONFIRMED &&
                                booking.timeSlot?.slotDate?.let { dateStr ->
                                    try { LocalDate.parse(dateStr).isBefore(LocalDate.now()) }
                                    catch (_: Exception) { false }
                                } == true
                            ) "COMPLETED" else booking.status.name
                            DetailRow(label = "Status", value = displayStatus)

                            Spacer(modifier = Modifier.height(8.dp))

                            DetailRow(
                                label = "Total Price",
                                value = "${"%.0f".format(booking.totalPrice)} ден"
                            )

                            if (!booking.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                DetailRow(label = "Notes", value = booking.notes)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Determine if the booking date has passed
                    val isPast = try {
                        booking.timeSlot?.slotDate?.let { dateStr ->
                            LocalDate.parse(dateStr).isBefore(LocalDate.now())
                        } ?: false
                    } catch (_: Exception) { false }

                    when {
                        // Past confirmed bookings → treat as completed, show review button
                        isPast && (booking.status == BookingStatus.CONFIRMED ||
                                   booking.status == BookingStatus.COMPLETED) -> {
                            Button(
                                onClick = { onWriteReview(bookingId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Write Review")
                            }
                        }
                        // Future pending/confirmed → allow cancellation
                        !isPast && (booking.status == BookingStatus.PENDING ||
                                    booking.status == BookingStatus.CONFIRMED) -> {
                            OutlinedButton(
                                onClick = { viewModel.cancelBooking() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel Booking")
                            }
                        }
                        // Explicitly completed → review
                        booking.status == BookingStatus.COMPLETED -> {
                            Button(
                                onClick = { onWriteReview(bookingId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Write Review")
                            }
                        }
                        else -> Unit
                    }

                    // Chat button available for approved, confirmed, and completed bookings
                    if (booking.status == BookingStatus.APPROVED ||
                        booking.status == BookingStatus.CONFIRMED ||
                        booking.status == BookingStatus.COMPLETED
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onOpenChat(bookingId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Chat with Partner")
                        }
                    }

                    // Receipt card — visible when loaded for CONFIRMED/COMPLETED bookings
                    uiState.receipt?.let { receipt ->
                        Spacer(modifier = Modifier.height(24.dp))
                        ReceiptCard(receipt = receipt)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ReceiptCard(receipt: BookingReceipt) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Receipt",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = receipt.receiptNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Provider and booking details
            DetailRow(label = "Provider", value = receipt.providerName)
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(
                label = "Sport",
                value = receipt.sportType.lowercase()
                    .replaceFirstChar { it.uppercase() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Date", value = receipt.slotDate.toDisplayDate())
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Time", value = "${receipt.startTime} - ${receipt.endTime}")

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Pricing
            DetailRow(
                label = "Subtotal",
                value = "${"%.0f".format(receipt.subtotal)} ${receipt.currency}"
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(
                label = "Platform fee",
                value = "${"%.0f".format(receipt.platformFee)} ${receipt.currency}"
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Bold total row
            Column {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${"%.0f".format(receipt.total)} ден",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Payment status with colored badge
            Column {
                Text(
                    text = "Payment status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                val isCompleted = receipt.status.equals("completed", ignoreCase = true) ||
                    receipt.status.equals("paid", ignoreCase = true)
                Text(
                    text = receipt.status.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isCompleted) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .background(
                            color = if (isCompleted) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(
                label = "Paid at",
                value = receipt.issuedAt.toDisplayDateTime()
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Receipt #", value = receipt.receiptNumber)
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun BookingDetailScreenPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Booking Details") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "City Tennis Center",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow(label = "Date", value = "Mar 25, 2026")
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(label = "Time", value = "10:00 - 11:00")
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(label = "Status", value = "CONFIRMED")
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(label = "Total Price", value = "1500 ден")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel Booking")
                }
                Spacer(modifier = Modifier.height(24.dp))
                ReceiptCard(
                    receipt = BookingReceipt(
                        receiptNumber = "RCP-2026-0042",
                        bookingId = 1L,
                        paymentId = 1L,
                        status = "completed",
                        issuedAt = "2026-03-25T10:00:00Z",
                        playerName = "Alex Johnson",
                        playerEmail = "alex@example.com",
                        providerType = "venue",
                        providerName = "City Tennis Center",
                        providerAddress = "Skopje, MK",
                        sportType = "tennis",
                        slotDate = "2026-03-25",
                        startTime = "10:00",
                        endTime = "11:00",
                        subtotal = 1400.0,
                        platformFee = 100.0,
                        total = 1500.0,
                        currency = "ден",
                        paymentMethod = "card",
                        externalPaymentId = null,
                    )
                )
            }
        }
    }
}
