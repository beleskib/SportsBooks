package com.example.sportsbook.ui.screens.player.match

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.MatchParticipant
import com.example.sportsbook.domain.model.Party
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.MatchRepository
import com.example.sportsbook.domain.repository.PartyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchDetailUiState(
    val match: Match? = null,
    val currentUserId: Long? = null,
    val activeParty: Party? = null,
    val isLoading: Boolean = false,
    val isJoining: Boolean = false,
    val error: String? = null,
    val joinSuccess: Boolean = false,
    val leaveSuccess: Boolean = false
) {
    val isHost: Boolean get() = match?.hostId == currentUserId
    val isParticipant: Boolean
        get() = match?.participants?.any { it.userId == currentUserId } == true
    val canJoin: Boolean
        get() = !isParticipant && match?.status?.name == "OPEN"
    val pendingRequests: List<MatchParticipant>
        get() = match?.participants?.filter { it.status.name == "PENDING" } ?: emptyList()
    val canJoinWithParty: Boolean
        get() = canJoin && activeParty != null && activeParty.status == "ready"
}

@HiltViewModel
class MatchDetailViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val authRepository: AuthRepository,
    private val partyRepository: PartyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: Long = checkNotNull(savedStateHandle["matchId"])
    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadMatch()
        loadActiveParty()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()?.id?.let { uid ->
                _uiState.update { it.copy(currentUserId = uid) }
            }
        }
    }

    fun loadMatch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            matchRepository.getMatchById(matchId)
                .onSuccess { match ->
                    _uiState.update { it.copy(match = match, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun loadActiveParty() {
        viewModelScope.launch {
            partyRepository.getActiveParty()
                .onSuccess { party ->
                    _uiState.update { it.copy(activeParty = party) }
                }
        }
    }

    fun joinWithParty() {
        val partyId = _uiState.value.activeParty?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true) }
            partyRepository.joinMatchWithParty(matchId, partyId)
                .onSuccess {
                    _uiState.update { it.copy(isJoining = false, joinSuccess = true) }
                    loadMatch()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isJoining = false, error = e.message) }
                }
        }
    }

    fun joinMatch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true) }
            matchRepository.joinMatch(matchId)
                .onSuccess {
                    _uiState.update { it.copy(isJoining = false, joinSuccess = true) }
                    loadMatch()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isJoining = false, error = e.message) }
                }
        }
    }

    fun leaveMatch() {
        viewModelScope.launch {
            matchRepository.leaveMatch(matchId)
                .onSuccess {
                    _uiState.update { it.copy(leaveSuccess = true) }
                    loadMatch()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun respondToJoinRequest(participantId: Long, approve: Boolean) {
        viewModelScope.launch {
            matchRepository.respondToJoinRequest(matchId, participantId, approve)
                .onSuccess { loadMatch() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun cancelMatch() {
        viewModelScope.launch {
            matchRepository.cancelMatch(matchId)
                .onSuccess { loadMatch() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }
}
