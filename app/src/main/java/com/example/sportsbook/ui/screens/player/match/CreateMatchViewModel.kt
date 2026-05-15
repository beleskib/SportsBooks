package com.example.sportsbook.ui.screens.player.match

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.CreateMatchRequestDto
import com.example.sportsbook.domain.enums.MatchPaymentType
import com.example.sportsbook.domain.enums.MatchType
import com.example.sportsbook.domain.enums.MatchVisibility
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.MatchRepository
import com.example.sportsbook.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimeSlotInfo(
    val id: Long,
    val startTime: String,
    val endTime: String,
    val price: Double,
    val isAvailable: Boolean = true,
)

data class CreateMatchUiState(
    val title: String = "",
    val description: String = "",
    val sportType: SportType = SportType.BASKETBALL,
    val matchType: MatchType = MatchType.STANDALONE,
    val visibility: MatchVisibility = MatchVisibility.PUBLIC,
    val matchDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val minPlayers: Int = 2,
    val maxPlayers: Int = 10,
    val minSkillLevel: Int = 1,
    val maxSkillLevel: Int = 5,
    val locationName: String = "",
    val address: String = "",
    val isFree: Boolean = true,
    val costPerPlayer: Double = 0.0,
    val isCreating: Boolean = false,
    val createdMatch: Match? = null,
    val error: String? = null,
    // Venue suggestion state
    val venueSuggestions: List<Venue> = emptyList(),
    val showSuggestions: Boolean = false,
    val selectedVenueId: Long? = null,
    val isSearchingVenues: Boolean = false,
    // Venue time slot state
    val availableTimeSlots: List<TimeSlotInfo> = emptyList(),
    val selectedTimeSlotId: Long? = null,
    val isLoadingTimeSlots: Boolean = false,
    val paymentType: MatchPaymentType = MatchPaymentType.HOST_PAYS
) {
    val isValid: Boolean
        get() = title.isNotBlank() && matchDate.isNotBlank() &&
                startTime.isNotBlank() && endTime.isNotBlank() &&
                maxPlayers >= minPlayers
}

@HiltViewModel
class CreateMatchViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val venueRepository: VenueRepository,
    private val apiService: ApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateMatchUiState())
    val uiState: StateFlow<CreateMatchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var timeSlotsJob: Job? = null
    private var cachedVenuesBySport: Map<SportType, List<Venue>> = emptyMap()

    private val preselectedVenueId: Long? = savedStateHandle["preselectedVenueId"]
    private val preselectedTimeSlotId: Long? = savedStateHandle["preselectedTimeSlotId"]
    private val preselectedPaymentType: String? = savedStateHandle["preselectedPaymentType"]

    init {
        preloadVenuesForSport(_uiState.value.sportType)
        applyPreselection()
    }

    private fun applyPreselection() {
        val venueId = preselectedVenueId ?: return
        viewModelScope.launch {
            venueRepository.getVenueById(venueId).onSuccess { venue ->
                _uiState.update {
                    it.copy(
                        locationName = venue.name,
                        address = venue.address,
                        selectedVenueId = venue.id,
                        sportType = venue.sportType,
                    )
                }
                preselectedPaymentType?.let { pt ->
                    val matchPaymentType = MatchPaymentType.entries.find { it.apiValue == pt }
                    if (matchPaymentType != null) {
                        updatePaymentType(matchPaymentType)
                    }
                }
                if (_uiState.value.matchDate.isNotBlank()) {
                    loadVenueTimeSlots()
                }
            }
        }
    }

    fun updateTitle(value: String) { _uiState.update { it.copy(title = value) } }
    fun updateDescription(value: String) { _uiState.update { it.copy(description = value) } }
    fun updateSportType(value: SportType) {
        _uiState.update {
            it.copy(
                sportType = value,
                venueSuggestions = emptyList(),
                showSuggestions = false,
                selectedVenueId = null,
                availableTimeSlots = emptyList(),
                selectedTimeSlotId = null
            )
        }
        preloadVenuesForSport(value)
    }
    fun updateMatchType(value: MatchType) { _uiState.update { it.copy(matchType = value) } }
    fun updateVisibility(value: MatchVisibility) { _uiState.update { it.copy(visibility = value) } }
    fun updateMatchDate(value: String) {
        _uiState.update { it.copy(matchDate = value) }
        loadVenueTimeSlots()
    }
    fun updateStartTime(value: String) { _uiState.update { it.copy(startTime = value) } }
    fun updateEndTime(value: String) { _uiState.update { it.copy(endTime = value) } }
    fun updateMinPlayers(value: Int) { _uiState.update { it.copy(minPlayers = value) } }
    fun updateMaxPlayers(value: Int) { _uiState.update { it.copy(maxPlayers = value) } }
    fun updateMinSkillLevel(value: Int) { _uiState.update { it.copy(minSkillLevel = value) } }
    fun updateMaxSkillLevel(value: Int) { _uiState.update { it.copy(maxSkillLevel = value) } }
    fun updateAddress(value: String) { _uiState.update { it.copy(address = value) } }
    fun updateIsFree(value: Boolean) { _uiState.update { it.copy(isFree = value) } }
    fun updateCostPerPlayer(value: Double) { _uiState.update { it.copy(costPerPlayer = value) } }

    fun updatePaymentType(value: MatchPaymentType) {
        _uiState.update {
            it.copy(
                paymentType = value,
                isFree = value == MatchPaymentType.CASH_AT_VENUE || value == MatchPaymentType.HOST_PAYS
            )
        }
    }

    fun updateLocationName(value: String) {
        _uiState.update {
            it.copy(
                locationName = value,
                selectedVenueId = null,
                availableTimeSlots = emptyList(),
                selectedTimeSlotId = null
            )
        }
        searchVenues(value)
    }

    fun selectVenue(venue: Venue) {
        _uiState.update {
            it.copy(
                locationName = venue.name,
                address = venue.address ?: "",
                selectedVenueId = venue.id,
                showSuggestions = false,
                venueSuggestions = emptyList(),
                selectedTimeSlotId = null,
                availableTimeSlots = emptyList()
            )
        }
        loadVenueTimeSlots()
    }

    fun selectTimeSlot(slot: TimeSlotInfo) {
        _uiState.update {
            it.copy(
                selectedTimeSlotId = slot.id,
                startTime = slot.startTime,
                endTime = slot.endTime
            )
        }
    }

    fun dismissSuggestions() {
        _uiState.update { it.copy(showSuggestions = false) }
    }

    private fun preloadVenuesForSport(sportType: SportType) {
        if (cachedVenuesBySport.containsKey(sportType)) return
        viewModelScope.launch {
            venueRepository.getVenuesBySport(sportType).onSuccess { venues ->
                cachedVenuesBySport = cachedVenuesBySport + (sportType to venues)
            }
        }
    }

    private fun searchVenues(query: String) {
        searchJob?.cancel()

        if (query.length < 2) {
            _uiState.update { it.copy(showSuggestions = false, venueSuggestions = emptyList()) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // debounce typing
            _uiState.update { it.copy(isSearchingVenues = true) }

            val sportType = _uiState.value.sportType
            val queryLower = query.lowercase()

            // Try from cache first
            val cached = cachedVenuesBySport[sportType]
            if (cached != null) {
                val filtered = cached.filter { it.name.lowercase().contains(queryLower) }
                _uiState.update {
                    it.copy(
                        venueSuggestions = filtered.take(5),
                        showSuggestions = filtered.isNotEmpty(),
                        isSearchingVenues = false
                    )
                }
                return@launch
            }

            // Fetch from API then filter
            venueRepository.getVenuesBySport(sportType)
                .onSuccess { venues ->
                    cachedVenuesBySport = cachedVenuesBySport + (sportType to venues)
                    val filtered = venues.filter { it.name.lowercase().contains(queryLower) }
                    _uiState.update {
                        it.copy(
                            venueSuggestions = filtered.take(5),
                            showSuggestions = filtered.isNotEmpty(),
                            isSearchingVenues = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isSearchingVenues = false) }
                }
        }
    }

    private fun loadVenueTimeSlots() {
        timeSlotsJob?.cancel()
        val venueId = _uiState.value.selectedVenueId
        val date = _uiState.value.matchDate

        if (venueId == null || date.isBlank()) {
            _uiState.update { it.copy(availableTimeSlots = emptyList(), isLoadingTimeSlots = false) }
            return
        }

        timeSlotsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTimeSlots = true) }
            try {
                val response = apiService.getVenueTimeSlotsForMatch(venueId, date)
                val slots = response.data.map { dto ->
                    TimeSlotInfo(
                        id = dto.id,
                        startTime = dto.startTime,
                        endTime = dto.endTime,
                        price = dto.priceOverride ?: 0.0,
                        isAvailable = dto.isAvailable
                    )
                }
                _uiState.update {
                    it.copy(availableTimeSlots = slots, isLoadingTimeSlots = false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(availableTimeSlots = emptyList(), isLoadingTimeSlots = false) }
            }
        }
    }

    fun createMatch() {
        val s = _uiState.value
        if (!s.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, error = null) }
            val request = CreateMatchRequestDto(
                venueId = s.selectedVenueId,
                timeSlotId = s.selectedTimeSlotId,
                paymentType = s.paymentType.apiValue,
                sportType = s.sportType,
                matchType = s.matchType,
                visibility = s.visibility,
                title = s.title,
                description = s.description.ifBlank { null },
                matchDate = s.matchDate,
                startTime = s.startTime,
                endTime = s.endTime,
                minPlayers = s.minPlayers,
                maxPlayers = s.maxPlayers,
                minSkillLevel = s.minSkillLevel,
                maxSkillLevel = s.maxSkillLevel,
                locationName = s.locationName.ifBlank { null },
                address = s.address.ifBlank { null },
                isFree = s.isFree,
                costPerPlayer = if (s.isFree) 0.0 else s.costPerPlayer
            )
            matchRepository.createMatch(request)
                .onSuccess { match ->
                    _uiState.update { it.copy(isCreating = false, createdMatch = match) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isCreating = false, error = e.message) }
                }
        }
    }
}
