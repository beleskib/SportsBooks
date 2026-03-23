package com.example.sportsbook.ui.screens.player.party

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Party
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.PartyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PartyDetailUiState(
    val party: Party? = null,
    val currentUserId: Long? = null,
    val isLoading: Boolean = false,
    val isActioning: Boolean = false,
    val error: String? = null,
    val disbanded: Boolean = false
) {
    val isLeader: Boolean get() = party?.leaderId == currentUserId
    val isInvitedMember: Boolean
        get() {
            val userId = currentUserId ?: return false
            return party?.members?.any { it.userId == userId && it.status == "invited" } == true
        }
}

@HiltViewModel
class PartyDetailViewModel @Inject constructor(
    private val partyRepository: PartyRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val partyId: Long = checkNotNull(savedStateHandle["partyId"])
    private val _uiState = MutableStateFlow(PartyDetailUiState())
    val uiState: StateFlow<PartyDetailUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadParty()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()?.id?.let { uid ->
                _uiState.update { it.copy(currentUserId = uid) }
            }
        }
    }

    fun loadParty() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            partyRepository.getPartyById(partyId)
                .onSuccess { party ->
                    _uiState.update { it.copy(party = party, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun respondToInvite(accept: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true, error = null) }
            partyRepository.respondToInvite(partyId, accept)
                .onSuccess { party ->
                    _uiState.update { it.copy(party = party, isActioning = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isActioning = false) }
                }
        }
    }

    fun disbandParty() {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true, error = null) }
            partyRepository.disbandParty(partyId)
                .onSuccess {
                    _uiState.update { it.copy(isActioning = false, disbanded = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isActioning = false) }
                }
        }
    }
}
