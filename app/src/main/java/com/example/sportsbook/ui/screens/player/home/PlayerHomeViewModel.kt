package com.example.sportsbook.ui.screens.player.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Sport
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.SportRepository
import com.example.sportsbook.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerHomeUiState(
    val sports: List<Sport> = emptyList(),
    val topDealVenues: List<Venue> = emptyList(),
    val topDealCoaches: List<Coach> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PlayerHomeViewModel @Inject constructor(
    private val sportRepository: SportRepository,
    private val venueRepository: VenueRepository,
    private val coachRepository: CoachRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerHomeUiState())
    val uiState: StateFlow<PlayerHomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val sportsDeferred = async { sportRepository.getSports() }
            val venueDealsDeferred = async { venueRepository.getTopDeals() }
            val coachDealsDeferred = async { coachRepository.getTopDeals() }

            val sportsResult = sportsDeferred.await()
            val venueDealsResult = venueDealsDeferred.await()
            val coachDealsResult = coachDealsDeferred.await()

            val error = listOf(sportsResult, venueDealsResult, coachDealsResult)
                .firstOrNull { it.isFailure }
                ?.exceptionOrNull()
                ?.message

            _uiState.update { current ->
                current.copy(
                    sports = sportsResult.getOrElse { current.sports },
                    topDealVenues = venueDealsResult.getOrElse { current.topDealVenues },
                    topDealCoaches = coachDealsResult.getOrElse { current.topDealCoaches },
                    isLoading = false,
                    error = error
                )
            }
        }
    }
}
