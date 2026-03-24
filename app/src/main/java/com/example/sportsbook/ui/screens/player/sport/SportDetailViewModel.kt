package com.example.sportsbook.ui.screens.player.sport

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SportDetailUiState(
    val sportType: SportType? = null,
    val sportDisplayName: String = "",
    val venues: List<Venue> = emptyList(),
    val coaches: List<Coach> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SportDetailViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    private val coachRepository: CoachRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SportDetailUiState())
    val uiState: StateFlow<SportDetailUiState> = _uiState.asStateFlow()

    init {
        val sportTypeStr: String? = savedStateHandle["sportType"]
        if (sportTypeStr != null) {
            val parsedType = try { SportType.valueOf(sportTypeStr.uppercase()) } catch (_: Exception) { null }
            if (parsedType != null) {
                _uiState.update { it.copy(sportType = parsedType, sportDisplayName = parsedType.displayName) }
                loadData()
            }
        }
    }

    fun loadData() {
        val sportType = _uiState.value.sportType ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val venuesDeferred = async {
                venueRepository.getVenuesBySport(sportType)
            }
            val coachesDeferred = async {
                coachRepository.getCoachesBySport(sportType)
            }
            val venuesResult = venuesDeferred.await()
            val coachesResult = coachesDeferred.await()
            val error = venuesResult.exceptionOrNull() ?: coachesResult.exceptionOrNull()
            _uiState.update {
                it.copy(
                    venues = venuesResult.getOrElse { emptyList() },
                    coaches = coachesResult.getOrElse { emptyList() },
                    isLoading = false,
                    error = error?.message
                )
            }
        }
    }
}
