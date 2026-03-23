package com.example.sportsbook.ui.screens.player.venue

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.VenueRepository
import com.example.sportsbook.domain.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VenueMapUiState(
    val venues: List<Venue> = emptyList(),
    val userLocation: Location? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VenueMapViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(VenueMapUiState())
    val uiState: StateFlow<VenueMapUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val location = locationService.getCurrentLocation()
            _uiState.update { it.copy(userLocation = location) }

            venueRepository.getAllVenues()
                .onSuccess { venues ->
                    _uiState.update { it.copy(venues = venues, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}
