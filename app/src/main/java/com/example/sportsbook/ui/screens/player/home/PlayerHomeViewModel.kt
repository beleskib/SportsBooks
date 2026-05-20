package com.example.sportsbook.ui.screens.player.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.MatchStatus
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.Party
import com.example.sportsbook.domain.model.Sport
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.MatchRepository
import com.example.sportsbook.domain.repository.NotificationRepository
import com.example.sportsbook.domain.repository.PartyRepository
import com.example.sportsbook.domain.repository.SportRepository
import com.example.sportsbook.domain.repository.UserRepository
import com.example.sportsbook.domain.repository.VenueRepository
import com.example.sportsbook.domain.service.LocationService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.firstOrNull
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
    val myMatches: List<Match> = emptyList(),
    val topDealVenues: List<Venue> = emptyList(),
    val topDealCoaches: List<Coach> = emptyList(),
    val allVenues: List<Venue> = emptyList(),
    val allCoaches: List<Coach> = emptyList(),
    val selectedSportType: SportType? = null,
    val searchQuery: String = "",
    val activeParties: List<Party> = emptyList(),
    val notificationCount: Int = 0,
    val cityName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val matchStatusMessage: String? = null
) {
    /** Active matches (open, full, or in progress) — shown prominently at top */
    val activeMatches: List<Match>
        get() = myMatches.filter {
            it.status in listOf(MatchStatus.OPEN, MatchStatus.FULL, MatchStatus.IN_PROGRESS)
        }

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
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository,
    private val matchRepository: MatchRepository,
    private val partyRepository: PartyRepository,
    private val locationService: LocationService,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerHomeUiState())
    val uiState: StateFlow<PlayerHomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private val knownMatchStatuses = java.util.concurrent.ConcurrentHashMap<Long, String>()
    private val matchListenerRegistrations = mutableListOf<ListenerRegistration>()

    init {
        loadData()
        startNotificationCountPolling()
        resolveUserCity()
    }

    private fun resolveUserCity() {
        viewModelScope.launch {
            try {
                // Try current location first, fall back to last known
                val location = locationService.getCurrentLocation()
                    ?: locationService.getLastKnownLocation().firstOrNull()
                if (location != null) {
                    val city = locationService.getCityName(location.latitude, location.longitude)
                    if (city != null) {
                        _uiState.update { it.copy(cityName = city) }
                    }
                }
            } catch (_: Exception) {
                // Location unavailable — keep null, UI will show "Discover"
            }
        }
    }

    private fun startNotificationCountPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    kotlinx.coroutines.withTimeout(10_000L) {
                        notificationRepository.getUnreadCount()
                            .onSuccess { count ->
                                _uiState.update { it.copy(notificationCount = count) }
                            }
                    }
                } catch (_: Exception) {
                    // Timeout or network error — skip this cycle
                }
                delay(30_000L)
            }
        }
    }

    fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val userDeferred = async { userRepository.getProfile() }
            val sportsDeferred = async { sportRepository.getSports() }
            val venueDealsDeferred = async { venueRepository.getTopDeals() }
            val coachDealsDeferred = async { coachRepository.getTopDeals() }
            val allVenuesDeferred = async { venueRepository.getAllVenues() }
            val allCoachesDeferred = async { coachRepository.getAllCoaches() }
            val myMatchesDeferred = async { matchRepository.getMyMatches() }
            val partiesDeferred = async { partyRepository.getActiveParties() }

            val userResult = userDeferred.await()
            val sportsResult = sportsDeferred.await()
            val venueDealsResult = venueDealsDeferred.await()
            val coachDealsResult = coachDealsDeferred.await()
            val allVenuesResult = allVenuesDeferred.await()
            val allCoachesResult = allCoachesDeferred.await()
            val myMatchesResult = myMatchesDeferred.await()
            val partiesResult = partiesDeferred.await()

            val error = listOf(userResult, sportsResult, venueDealsResult, coachDealsResult, allVenuesResult, allCoachesResult, myMatchesResult, partiesResult)
                .firstOrNull { it.isFailure }
                ?.exceptionOrNull()
                ?.message

            _uiState.update { current ->
                current.copy(
                    user = userResult.getOrNull() ?: current.user,
                    sports = sportsResult.getOrElse { current.sports },
                    myMatches = myMatchesResult.getOrElse { current.myMatches },
                    activeParties = partiesResult.getOrElse { current.activeParties },
                    topDealVenues = venueDealsResult.getOrElse { current.topDealVenues },
                    topDealCoaches = coachDealsResult.getOrElse { current.topDealCoaches },
                    allVenues = allVenuesResult.getOrElse { current.allVenues },
                    allCoaches = allCoachesResult.getOrElse { current.allCoaches },
                    isLoading = false,
                    error = error
                )
            }

            startMatchStatusListeners()
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

    fun clearMatchStatusMessage() {
        _uiState.update { it.copy(matchStatusMessage = null) }
    }

    private fun startMatchStatusListeners() {
        matchListenerRegistrations.forEach { it.remove() }
        matchListenerRegistrations.clear()

        val activeMatchIds = _uiState.value.activeMatches.map { it.id }

        for (matchId in activeMatchIds) {
            val registration = firestore
                .collection("matches")
                .document(matchId.toString())
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                    val newStatus = snapshot.getString("status") ?: return@addSnapshotListener
                    val title = snapshot.getString("title") ?: "Match"
                    val oldStatus = knownMatchStatuses[matchId]
                    knownMatchStatuses[matchId] = newStatus

                    // Skip the initial load — only react to real changes
                    if (oldStatus == null || oldStatus == newStatus) return@addSnapshotListener

                    val message = when (newStatus) {
                        "full" -> "Match \"$title\" is full!"
                        "in_progress" -> "Match \"$title\" is starting now!"
                        "completed" -> "Match \"$title\" has been completed."
                        "cancelled" -> "Match \"$title\" has been cancelled."
                        else -> null
                    }
                    if (message != null) {
                        _uiState.update { it.copy(matchStatusMessage = message) }
                    }
                }
            matchListenerRegistrations.add(registration)
        }
    }

    override fun onCleared() {
        super.onCleared()
        matchListenerRegistrations.forEach { it.remove() }
    }
}
