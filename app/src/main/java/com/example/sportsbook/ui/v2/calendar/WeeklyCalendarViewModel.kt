package com.example.sportsbook.ui.v2.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.TimeSlot
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.TimeSlotRepository
import com.example.sportsbook.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// ============================================================
// v2-practical-ux: ViewModel for WeeklyCalendarScreen
// ============================================================

enum class V2EntityType { VENUE, COACH }

data class V2EntityOption(val id: Long, val label: String)

data class WeeklyCalendarUiState(
    val loading: Boolean = true,
    val error: String? = null,

    // Entity selector
    val venues: List<Venue> = emptyList(),
    val myCoach: Coach? = null,
    val entityType: V2EntityType = V2EntityType.VENUE,
    val selectedEntityId: Long? = null,

    // Week
    val weekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY),

    // Slots
    val slots: List<TimeSlot> = emptyList(),
    val busy: Boolean = false // saving indicator
)

@HiltViewModel
class WeeklyCalendarViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    private val coachRepository: CoachRepository,
    private val timeSlotRepository: TimeSlotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyCalendarUiState())
    val uiState: StateFlow<WeeklyCalendarUiState> = _uiState.asStateFlow()

    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        loadEntities()
    }

    private fun loadEntities() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            val venuesResult = venueRepository.getMyVenues()
            val coachResult = coachRepository.getMyCoachProfile()

            val venues = venuesResult.getOrElse { emptyList() }
            val coach = coachResult.getOrElse { null }

            // Determine default selection
            val defaultEntityType = if (venues.isNotEmpty()) V2EntityType.VENUE else V2EntityType.COACH
            val defaultEntityId = when {
                venues.isNotEmpty() -> venues.first().id
                coach != null -> coach.id
                else -> null
            }

            _uiState.update {
                it.copy(
                    loading = false,
                    venues = venues,
                    myCoach = coach,
                    entityType = defaultEntityType,
                    selectedEntityId = defaultEntityId
                )
            }

            if (defaultEntityId != null) {
                loadSlots()
            }
        }
    }

    fun loadSlots() {
        val state = _uiState.value
        val entityId = state.selectedEntityId ?: return
        viewModelScope.launch {
            val from = state.weekStart.format(dateFmt)
            val to = state.weekStart.plusDays(6).format(dateFmt)
            val result = timeSlotRepository.getAvailableSlots(
                venueId = if (state.entityType == V2EntityType.VENUE) entityId else null,
                coachId = if (state.entityType == V2EntityType.COACH) entityId else null,
                dateFrom = from,
                dateTo = to
            )
            _uiState.update { it.copy(slots = result.getOrElse { emptyList() }) }
        }
    }

    fun selectEntityType(type: V2EntityType) {
        val state = _uiState.value
        val id = when (type) {
            V2EntityType.VENUE -> state.venues.firstOrNull()?.id
            V2EntityType.COACH -> state.myCoach?.id
        }
        _uiState.update { it.copy(entityType = type, selectedEntityId = id) }
        if (id != null) loadSlots()
    }

    fun selectEntity(id: Long) {
        _uiState.update { it.copy(selectedEntityId = id) }
        loadSlots()
    }

    fun prevWeek() {
        _uiState.update { it.copy(weekStart = it.weekStart.minusDays(7)) }
        loadSlots()
    }

    fun nextWeek() {
        _uiState.update { it.copy(weekStart = it.weekStart.plusDays(7)) }
        loadSlots()
    }

    fun goToToday() {
        _uiState.update { it.copy(weekStart = LocalDate.now().with(DayOfWeek.MONDAY)) }
        loadSlots()
    }

    /** Tap an empty cell → generate a single 1-hour slot. */
    fun addSlot(date: String, hour: Int) {
        val state = _uiState.value
        val entityId = state.selectedEntityId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, error = null) }
            timeSlotRepository.generateSlots(
                venueId = if (state.entityType == V2EntityType.VENUE) entityId else null,
                coachId = if (state.entityType == V2EntityType.COACH) entityId else null,
                dateFrom = date,
                dateTo = date,
                startHour = hour,
                endHour = hour + 1
            ).onFailure { err ->
                _uiState.update { it.copy(error = err.message ?: "Failed to add slot") }
            }
            _uiState.update { it.copy(busy = false) }
            loadSlots()
        }
    }

    /** Tap an available cell → delete that slot. */
    fun removeSlot(slotId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, error = null) }
            timeSlotRepository.deleteSlot(slotId)
                .onFailure { err ->
                    _uiState.update { it.copy(error = err.message ?: "Failed to remove slot") }
                }
            _uiState.update { it.copy(busy = false) }
            loadSlots()
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
