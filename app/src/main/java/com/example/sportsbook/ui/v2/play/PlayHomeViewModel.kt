package com.example.sportsbook.ui.v2.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.v2.HomeFeedSnapshot
import com.example.sportsbook.domain.model.v2.PlaySearchResult
import com.example.sportsbook.domain.repository.V2Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// ============================================================
// v2-practical-ux: ViewModel for PlayHomeScreen
// ============================================================

data class PlayHomeUiState(
    val feedLoading: Boolean = true,
    val feedError: String? = null,
    val feed: HomeFeedSnapshot? = null,

    // Search form state
    val fromDate: String = "",
    val toDate: String = "",
    val sportType: String = "", // "" means "any"
    val skillLevelMin: Int? = null,
    val skillLevelMax: Int? = null,

    // Search results
    val searching: Boolean = false,
    val searchError: String? = null,
    val searchResult: PlaySearchResult? = null,
    val hasSearched: Boolean = false,

    // Rebook state (per bookingId)
    val rebookingIds: Set<Long> = emptySet(),
    val rebookError: String? = null
)

@HiltViewModel
class PlayHomeViewModel @Inject constructor(
    private val v2Repository: V2Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayHomeUiState())
    val uiState: StateFlow<PlayHomeUiState> = _uiState.asStateFlow()

    private val isoFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        // Default date range: tonight 19:00 → tomorrow 23:00
        val now = LocalDateTime.now()
        val tonight = now.toLocalDate().atTime(19, 0)
            .let { if (it.isBefore(now)) it.plusDays(1) else it }
        val tomorrowEnd = tonight.toLocalDate().plusDays(1).atTime(23, 0)

        _uiState.update { state ->
            state.copy(
                fromDate = tonight.format(isoFmt),
                toDate = tomorrowEnd.format(isoFmt)
            )
        }
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(feedLoading = true, feedError = null) }
            v2Repository.getHomeFeed()
                .onSuccess { feed ->
                    _uiState.update { it.copy(feedLoading = false, feed = feed) }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(feedLoading = false, feedError = err.message ?: "Failed to load feed")
                    }
                }
        }
    }

    fun onFromDateChanged(from: String) = _uiState.update { it.copy(fromDate = from) }
    fun onToDateChanged(to: String) = _uiState.update { it.copy(toDate = to) }
    fun onSportTypeChanged(sport: String) = _uiState.update { it.copy(sportType = sport) }
    fun onSkillMinChanged(min: Int?) = _uiState.update { it.copy(skillLevelMin = min) }
    fun onSkillMaxChanged(max: Int?) = _uiState.update { it.copy(skillLevelMax = max) }

    fun search() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(searching = true, searchError = null, hasSearched = true) }
            v2Repository.searchPlay(
                from = state.fromDate,
                to = state.toDate,
                sportType = state.sportType.takeIf { it.isNotBlank() },
                skillLevelMin = state.skillLevelMin,
                skillLevelMax = state.skillLevelMax
            )
                .onSuccess { result ->
                    _uiState.update { it.copy(searching = false, searchResult = result) }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(searching = false, searchError = err.message ?: "Search failed")
                    }
                }
        }
    }

    fun rebook(bookingId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(rebookingIds = it.rebookingIds + bookingId, rebookError = null) }
            // Default: rebook for 19:00 tomorrow
            val tomorrow = LocalDate.now().plusDays(1).format(dateFmt)
            v2Repository.rebook(bookingId, slotDate = tomorrow, startTime = "19:00")
                .onSuccess {
                    _uiState.update { it.copy(rebookingIds = it.rebookingIds - bookingId) }
                    loadFeed() // optimistic refresh
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            rebookingIds = it.rebookingIds - bookingId,
                            rebookError = err.message ?: "Rebook failed"
                        )
                    }
                }
        }
    }

    fun clearRebookError() = _uiState.update { it.copy(rebookError = null) }
    fun clearSearchError() = _uiState.update { it.copy(searchError = null) }
}
