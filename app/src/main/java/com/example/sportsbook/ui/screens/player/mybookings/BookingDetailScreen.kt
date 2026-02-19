package com.example.sportsbook.ui.screens.player.mybookings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import dagger.hilt.android.lifecycle.HiltViewModel
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
        val isLoading: Boolean = false,
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
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
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
                                DetailRow(label = "Date", value = slot.slotDate)
                                Spacer(modifier = Modifier.height(8.dp))
                                DetailRow(label = "Time", value = slot.displayTime)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            DetailRow(label = "Status", value = booking.status.name)

                            Spacer(modifier = Modifier.height(8.dp))

                            DetailRow(
                                label = "Total Price",
                                value = "$${"%.2f".format(booking.totalPrice)}"
                            )

                            if (!booking.notes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                DetailRow(label = "Notes", value = booking.notes)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    when (booking.status) {
                        BookingStatus.PENDING, BookingStatus.CONFIRMED -> {
                            OutlinedButton(
                                onClick = { viewModel.cancelBooking() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel Booking")
                            }
                        }
                        BookingStatus.COMPLETED -> {
                            Button(
                                onClick = { onWriteReview(bookingId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Write Review")
                            }
                        }
                        else -> Unit
                    }
                }
            }
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

@Preview(showBackground = true)
@Composable
private fun BookingDetailScreenPreview() {
    MaterialTheme {
        BookingDetailScreen(
            bookingId = 1L,
            onWriteReview = {},
            onBack = {}
        )
    }
}
