package com.example.sportsbook.ui.screens.player.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.model.PlayerLevel
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.repository.GamificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookingSuccessUiState(
    val booking: Booking? = null,
    val level: PlayerLevel? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class BookingSuccessViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val gamificationRepository: GamificationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val bookingId: Long = checkNotNull(savedStateHandle["bookingId"])

    private val _uiState = MutableStateFlow(BookingSuccessUiState())
    val uiState: StateFlow<BookingSuccessUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            bookingRepository.getBookingById(bookingId)
                .onSuccess { booking ->
                    _uiState.update { it.copy(booking = booking, isLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
        viewModelScope.launch {
            gamificationRepository.getMyLevel()
                .onSuccess { level ->
                    _uiState.update { it.copy(level = level) }
                }
        }
    }
}
