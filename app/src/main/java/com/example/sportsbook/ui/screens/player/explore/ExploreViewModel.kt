package com.example.sportsbook.ui.screens.player.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreUiState(
    val venues: List<Venue> = emptyList(),
    val coaches: List<Coach> = emptyList(),
    val topDealVenues: List<Venue> = emptyList(),
    val isLoading: Boolean = false,
    val selectedCategory: Int = 0, // 0=All, 1=Venues, 2=Coaches, 3=Deals, 4=TopRated
    val error: String? = null,
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    private val coachRepository: CoachRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectCategory(index: Int) {
        _uiState.update { it.copy(selectedCategory = index) }
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load venues
            venueRepository.getAllVenues()
                .onSuccess { venues ->
                    _uiState.update { it.copy(venues = venues) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }

            // Load coaches
            coachRepository.getAllCoaches()
                .onSuccess { coaches ->
                    _uiState.update { it.copy(coaches = coaches) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }

            // Load top deal venues
            venueRepository.getTopDeals()
                .onSuccess { deals ->
                    _uiState.update { it.copy(topDealVenues = deals) }
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
