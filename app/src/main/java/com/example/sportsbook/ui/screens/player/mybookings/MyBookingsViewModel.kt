package com.example.sportsbook.ui.screens.player.mybookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.enums.BookingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyBookingsUiState(
    val upcomingBookings: List<Booking> = emptyList(),
    val pastBookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MyBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBookingsUiState())
    val uiState: StateFlow<MyBookingsUiState> = _uiState.asStateFlow()

    init {
        loadBookings()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            bookingRepository.getMyBookings()
                .onSuccess { bookings ->
                    val upcoming = bookings.filter {
                        it.status in listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)
                    }
                    val past = bookings.filter {
                        it.status in listOf(
                            BookingStatus.COMPLETED,
                            BookingStatus.CANCELLED,
                            BookingStatus.NO_SHOW
                        )
                    }
                    _uiState.update {
                        it.copy(
                            upcomingBookings = upcoming,
                            pastBookings = past,
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isRefreshing = false) }
                }
        }
    }

    fun loadBookings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            bookingRepository.getMyBookings()
                .onSuccess { bookings ->
                    val upcoming = bookings.filter {
                        it.status in listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED)
                    }
                    val past = bookings.filter {
                        it.status in listOf(
                            BookingStatus.COMPLETED,
                            BookingStatus.CANCELLED,
                            BookingStatus.NO_SHOW
                        )
                    }
                    _uiState.update {
                        it.copy(
                            upcomingBookings = upcoming,
                            pastBookings = past,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}
