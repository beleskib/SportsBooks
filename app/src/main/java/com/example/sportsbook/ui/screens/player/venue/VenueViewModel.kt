package com.example.sportsbook.ui.screens.player.venue

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.Review
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.repository.MatchRepository
import com.example.sportsbook.domain.repository.ReviewRepository
import com.example.sportsbook.domain.repository.TimeSlotRepository
import com.example.sportsbook.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class VenueUiState(
    val venues: List<Venue> = emptyList(),
    val selectedVenue: Venue? = null,
    val reviews: List<Review> = emptyList(),
    val timeSlots: List<TimeSlot> = emptyList(),
    val venueMatches: List<Match> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VenueViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    private val reviewRepository: ReviewRepository,
    private val timeSlotRepository: TimeSlotRepository,
    private val matchRepository: MatchRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(VenueUiState())
    val uiState: StateFlow<VenueUiState> = _uiState.asStateFlow()

    private var currentSportType: String? = null

    fun loadVenuesBySport(sportType: String) {
        currentSportType = sportType

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = if (sportType.equals("all", ignoreCase = true)) {
                venueRepository.getAllVenues()
            } else {
                val parsedType = runCatching { SportType.valueOf(sportType.uppercase()) }.getOrNull()
                    ?: run {
                        _uiState.update { it.copy(error = "Unknown sport type: $sportType", isLoading = false) }
                        return@launch
                    }
                venueRepository.getVenuesBySport(parsedType)
            }

            result
                .onSuccess { venues ->
                    _uiState.update { it.copy(venues = venues, isLoading = false, error = null) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Failed to load venues"
                        )
                    }
                }
        }
    }

    fun loadVenueDetail(venueId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val venueDeferred = async { venueRepository.getVenueById(venueId) }
            val reviewsDeferred = async { reviewRepository.getReviewsForVenue(venueId) }

            val venueResult = venueDeferred.await()
            val reviewsResult = reviewsDeferred.await()

            val venue = venueResult.getOrNull()
            val reviews = reviewsResult.getOrNull() ?: emptyList()

            if (venue != null) {
                _uiState.update {
                    it.copy(
                        selectedVenue = venue,
                        reviews = reviews,
                        isLoading = false,
                        error = null
                    )
                }
                loadTimeSlotsForDate(venueId, _uiState.value.selectedDate)
                loadVenueMatches(venueId)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = venueResult.exceptionOrNull()?.message ?: "Failed to load venue"
                    )
                }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        val venueId = _uiState.value.selectedVenue?.id ?: return
        loadTimeSlotsForDate(venueId, date)
    }

    private fun loadTimeSlotsForDate(venueId: Long, date: LocalDate) {
        viewModelScope.launch {
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            timeSlotRepository.getAvailableSlots(
                venueId = venueId,
                dateFrom = dateStr,
                dateTo = dateStr
            ).onSuccess { slots ->
                _uiState.update { it.copy(timeSlots = slots) }
            }
        }
    }

    private fun loadVenueMatches(venueId: Long) {
        viewModelScope.launch {
            matchRepository.listMatches(status = "open")
                .onSuccess { matches ->
                    val venueMatches = matches.filter { it.venueId == venueId }
                    _uiState.update { it.copy(venueMatches = venueMatches) }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            currentSportType?.let { loadVenuesBySport(it) }
            return
        }
        viewModelScope.launch {
            venueRepository.searchVenues(query)
                .onSuccess { venues ->
                    _uiState.update { it.copy(venues = venues, error = null) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(error = throwable.message ?: "Search failed")
                    }
                }
        }
    }

    fun retry() {
        val currentState = _uiState.value
        if (currentState.selectedVenue != null) {
            loadVenueDetail(currentState.selectedVenue.id)
        } else {
            currentSportType?.let { loadVenuesBySport(it) }
        }
    }
}
