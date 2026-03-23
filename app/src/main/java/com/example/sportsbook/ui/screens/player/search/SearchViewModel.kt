package com.example.sportsbook.ui.screens.player.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.Venue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val venues: List<Venue> = emptyList(),
    val coaches: List<Coach> = emptyList(),
    val matches: List<Match> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.length < 2) {
            _uiState.update {
                it.copy(
                    venues = emptyList(),
                    coaches = emptyList(),
                    matches = emptyList(),
                    hasSearched = false
                )
            }
            return
        }
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            _uiState.update { it.copy(isSearching = true) }
            try {
                val results = apiService.globalSearch(query).data
                _uiState.update {
                    it.copy(
                        venues = results.venues.map { v -> v.toDomain() },
                        coaches = results.coaches.map { c -> c.toDomain() },
                        matches = results.matches.map { m -> m.toDomain() },
                        isSearching = false,
                        hasSearched = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isSearching = false) }
            }
        }
    }
}
