package com.example.sportsbook.ui.screens.player.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.MatchStatus
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchListUiState(
    val matches: List<Match> = emptyList(),
    val myMatches: List<Match> = emptyList(),
    val selectedSport: SportType? = null,
    val showMyMatchesOnly: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val displayedMatches: List<Match>
        get() {
            val source = if (showMyMatchesOnly) myMatches else matches
            return if (selectedSport != null) {
                source.filter { it.sportType == selectedSport }
            } else {
                source
            }
        }
}

@HiltViewModel
class MatchListViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchListUiState())
    val uiState: StateFlow<MatchListUiState> = _uiState.asStateFlow()

    init { loadMatches() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            val openResult = matchRepository.listMatches(status = "open")
            val myResult = matchRepository.getMyMatches()

            openResult.onSuccess { matches ->
                _uiState.update { it.copy(matches = matches) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }

            myResult.onSuccess { my ->
                _uiState.update { it.copy(myMatches = my) }
            }

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun loadMatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val openResult = matchRepository.listMatches(status = "open")
            val myResult = matchRepository.getMyMatches()

            openResult.onSuccess { matches ->
                _uiState.update { it.copy(matches = matches) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }

            myResult.onSuccess { my ->
                _uiState.update { it.copy(myMatches = my) }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun selectSport(sport: SportType?) {
        _uiState.update { it.copy(selectedSport = sport) }
    }

    fun toggleMyMatches() {
        _uiState.update { it.copy(showMyMatchesOnly = !it.showMyMatchesOnly) }
    }
}
