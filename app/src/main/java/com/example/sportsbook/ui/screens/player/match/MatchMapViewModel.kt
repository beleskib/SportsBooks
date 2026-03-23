package com.example.sportsbook.ui.screens.player.match

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.repository.MatchRepository
import com.example.sportsbook.domain.service.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchMapUiState(
    val matches: List<Match> = emptyList(),
    val userLocation: Location? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MatchMapViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchMapUiState())
    val uiState: StateFlow<MatchMapUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val location = locationService.getCurrentLocation()
            _uiState.update { it.copy(userLocation = location) }

            matchRepository.listMatches(status = "open")
                .onSuccess { matches ->
                    _uiState.update { it.copy(matches = matches, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}
