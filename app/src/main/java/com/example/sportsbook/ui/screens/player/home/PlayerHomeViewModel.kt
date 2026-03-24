package com.example.sportsbook.ui.screens.player.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Sport
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.SportRepository
import com.example.sportsbook.domain.repository.UserRepository
import com.example.sportsbook.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerHomeUiState(
    val user: User? = null,
    val sports: List<Sport> = emptyList(),
    val topDealVenues: List<Venue> = emptyList(),
    val topDealCoaches: List<Coach> = emptyList(),
    val allVenues: List<Venue> = emptyList(),
    val allCoaches: List<Coach> = emptyList(),
    val selectedSportType: SportType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    /** Sports the user picked during onboarding */
    val mySports: List<Sport>
        get() {
            val interested = user?.interestedSports ?: emptyList()
            return if (interested.isEmpty()) sports // fallback: show all
            else sports.filter { it.sportType in interested }
        }

    /** Remaining sports not in the user's selection */
    val otherSports: List<Sport>
        get() {
            val interested = user?.interestedSports ?: emptyList()
            return if (interested.isEmpty()) emptyList()
            else sports.filter { it.sportType !in interested }
        }

    val filteredVenues: List<Venue>
        get() {
            var list = allVenues
            if (selectedSportType != null) {
                list = list.filter { it.sportType == selectedSportType }
            }
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.lowercase()
                list = list.filter {
                    it.name.lowercase().contains(q) ||
                            it.address.lowercase().contains(q) ||
                            it.city?.lowercase()?.contains(q) == true
                }
            }
            return list
        }

    val filteredCoaches: List<Coach>
        get() {
            var list = allCoaches
            if (selectedSportType != null) {
                list = list.filter { it.sportType == selectedSportType }
            }
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.lowercase()
                list = list.filter {
                    it.name.lowercase().contains(q) ||
                            it.specialization?.lowercase()?.contains(q) == true ||
                            it.city?.lowercase()?.contains(q) == true
                }
            }
            return list
        }
}

sealed interface HomeGridItem {
    val id: Long
    val name: String
    val imageUrl: String?
    val price: Double
    val discountedPrice: Double?

    data class VenueItem(val venue: Venue) : HomeGridItem {
        override val id = venue.id
        override val name = venue.name
        override val imageUrl = venue.primaryImageUrl
        override val price = venue.pricePerHour
        override val discountedPrice = venue.discountedPrice
    }

    data class CoachItem(val coach: Coach) : HomeGridItem {
        override val id = coach.id
        override val name = coach.name
        override val imageUrl = coach.primaryImageUrl
        override val price = coach.pricePerHour
        override val discountedPrice = coach.discountedPrice
    }
}

@HiltViewModel
class PlayerHomeViewModel @Inject constructor(
    private val sportRepository: SportRepository,
    private val venueRepository: VenueRepository,
    private val coachRepository: CoachRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerHomeUiState())
    val uiState: StateFlow<PlayerHomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val userDeferred = async { userRepository.getProfile() }
            val sportsDeferred = async { sportRepository.getSports() }
            val venueDealsDeferred = async { venueRepository.getTopDeals() }
            val coachDealsDeferred = async { coachRepository.getTopDeals() }
            val allVenuesDeferred = async { venueRepository.getAllVenues() }
            val allCoachesDeferred = async { coachRepository.getAllCoaches() }

            val userResult = userDeferred.await()
            val sportsResult = sportsDeferred.await()
            val venueDealsResult = venueDealsDeferred.await()
            val coachDealsResult = coachDealsDeferred.await()
            val allVenuesResult = allVenuesDeferred.await()
            val allCoachesResult = allCoachesDeferred.await()

            val error = listOf(sportsResult, venueDealsResult, coachDealsResult, allVenuesResult, allCoachesResult)
                .firstOrNull { it.isFailure }
                ?.exceptionOrNull()
                ?.message

            _uiState.update { current ->
                current.copy(
                    user = userResult.getOrNull() ?: current.user,
                    sports = sportsResult.getOrElse { current.sports },
                    topDealVenues = venueDealsResult.getOrElse { current.topDealVenues },
                    topDealCoaches = coachDealsResult.getOrElse { current.topDealCoaches },
                    allVenues = allVenuesResult.getOrElse { current.allVenues },
                    allCoaches = allCoachesResult.getOrElse { current.allCoaches },
                    isLoading = false,
                    error = error
                )
            }
        }
    }

    fun selectSport(sportType: SportType?) {
        _uiState.update { it.copy(selectedSportType = sportType) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
