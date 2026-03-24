package com.example.sportsbook.ui.screens.player.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.repository.TimeSlotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BookingUiState(
    val selectedDate: String = "",
    val availableSlots: List<TimeSlot> = emptyList(),
    val selectedSlotId: Long? = null,
    val selectedSlot: TimeSlot? = null,
    val notes: String = "",
    val isLoading: Boolean = false,
    val isBookingLoading: Boolean = false,
    val error: String? = null,
    val bookingSuccess: Boolean = false,
    val createdBookingId: Long? = null,
    val venueId: Long? = null,
    val coachId: Long? = null,
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val timeSlotRepository: TimeSlotRepository,
    private val bookingRepository: BookingRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        val venueId: Long? = savedStateHandle["venueId"]
        val coachId: Long? = savedStateHandle["coachId"]
        val today = LocalDate.now().toString()

        _uiState.update { current ->
            current.copy(
                venueId = venueId,
                coachId = coachId,
                selectedDate = today,
            )
        }

        loadSlots()
    }

    fun onDateChange(date: String) {
        _uiState.update { current ->
            current.copy(
                selectedDate = date,
                selectedSlotId = null,
                selectedSlot = null,
            )
        }
        loadSlots()
    }

    fun onSlotSelected(slotId: Long) {
        val slot = _uiState.value.availableSlots.find { it.id == slotId }
        _uiState.update { current ->
            current.copy(
                selectedSlotId = slotId,
                selectedSlot = slot,
            )
        }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { current -> current.copy(notes = notes) }
    }

    fun loadSlots() {
        val state = _uiState.value
        val date = state.selectedDate

        if (date.isBlank()) return

        viewModelScope.launch {
            _uiState.update { current -> current.copy(isLoading = true, error = null) }

            timeSlotRepository
                .getAvailableSlots(
                    venueId = state.venueId,
                    coachId = state.coachId,
                    dateFrom = date,
                    dateTo = date,
                )
                .fold(
                    onSuccess = { slots ->
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                availableSlots = slots,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load available slots",
                            )
                        }
                    },
                )
        }
    }

    fun confirmBooking() {
        val state = _uiState.value
        val slotId = state.selectedSlotId ?: return

        viewModelScope.launch {
            _uiState.update { current -> current.copy(isBookingLoading = true, error = null) }

            bookingRepository
                .createBooking(
                    timeSlotId = slotId,
                    notes = state.notes.ifBlank { null },
                )
                .fold(
                    onSuccess = { booking ->
                        _uiState.update { current ->
                            current.copy(
                                isBookingLoading = false,
                                bookingSuccess = true,
                                createdBookingId = booking.id,
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { current ->
                            current.copy(
                                isBookingLoading = false,
                                error = error.message ?: "Failed to create booking",
                            )
                        }
                    },
                )
        }
    }

    fun loadSlotById(slotId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            timeSlotRepository.getSlotById(slotId)
                .onSuccess { slot ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            selectedSlotId = slot.id,
                            selectedSlot = slot,
                            venueId = slot.venueId,
                            coachId = slot.coachId,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load slot details",
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { current -> current.copy(error = null) }
    }
}
