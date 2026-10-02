package com.example.sportsbook.ui.screens.player.coach

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Review
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.ReviewRepository
import com.example.sportsbook.domain.repository.TimeSlotRepository
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

data class CoachUiState(
    val coaches: List<Coach> = emptyList(),
    val selectedCoach: Coach? = null,
    val reviews: List<Review> = emptyList(),
    val timeSlots: List<TimeSlot> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CoachViewModel @Inject constructor(
    private val coachRepository: CoachRepository,
    private val reviewRepository: ReviewRepository,
    private val timeSlotRepository: TimeSlotRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachUiState())
    val uiState: StateFlow<CoachUiState> = _uiState.asStateFlow()

    fun loadCoachesBySport(sportType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = if (sportType.equals("all", ignoreCase = true)) {
                coachRepository.getAllCoaches()
            } else {
                val parsedSportType = runCatching {
                    SportType.valueOf(sportType.uppercase())
                }.getOrElse {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Unknown sport type: $sportType"
                        )
                    }
                    return@launch
                }
                coachRepository.getCoachesBySport(parsedSportType)
            }

            result
                .onSuccess { coaches ->
                    _uiState.update {
                        it.copy(coaches = coaches, isLoading = false, error = null)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Failed to load coaches"
                        )
                    }
                }
        }
    }

    fun loadCoachDetail(coachId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val coachDeferred = async {
                coachRepository.getCoachById(coachId)
            }
            val reviewsDeferred = async {
                reviewRepository.getReviewsForCoach(coachId)
            }
            val coachResult = coachDeferred.await()
            val reviewsResult = reviewsDeferred.await()

            val coachError = coachResult.exceptionOrNull()
            val reviewsError = reviewsResult.exceptionOrNull()

            if (coachError != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = coachError.message ?: "Failed to load coach details"
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    selectedCoach = coachResult.getOrNull(),
                    reviews = reviewsResult.getOrElse { emptyList() },
                    isLoading = false,
                    error = if (reviewsError != null) {
                        "Reviews could not be loaded"
                    } else {
                        null
                    }
                )
            }
            loadTimeSlotsForDate(coachId, _uiState.value.selectedDate)
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        val coachId = _uiState.value.selectedCoach?.id ?: return
        loadTimeSlotsForDate(coachId, date)
    }

    private fun loadTimeSlotsForDate(coachId: Long, date: LocalDate) {
        viewModelScope.launch {
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            timeSlotRepository.getAvailableSlots(
                coachId = coachId,
                dateFrom = dateStr,
                dateTo = dateStr
            ).onSuccess { slots ->
                _uiState.update { it.copy(timeSlots = slots) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun retry() {
        val currentState = _uiState.value
        val selectedCoach = currentState.selectedCoach
        if (selectedCoach != null) {
            loadCoachDetail(selectedCoach.id)
        } else {
            savedStateHandle.get<String>("sportType")?.let { sportType ->
                loadCoachesBySport(sportType)
            }
        }
    }
}
