package com.example.sportsbook.ui.screens.player.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.domain.model.Party
import com.example.sportsbook.domain.repository.FriendshipRepository
import com.example.sportsbook.domain.repository.PartyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialHubUiState(
    val friends: List<Friendship> = emptyList(),
    val pendingRequests: List<Friendship> = emptyList(),
    val parties: List<Party> = emptyList(),
    val nearbyPlayers: List<Friendship> = emptyList(),
    val isLoadingFriends: Boolean = false,
    val isLoadingParties: Boolean = false,
    val isLoadingPlayers: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SocialHubViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository,
    private val partyRepository: PartyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialHubUiState())
    val uiState: StateFlow<SocialHubUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
        loadPendingRequests()
        loadParties()
        loadNearbyPlayers()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFriends = true) }
            friendshipRepository.getMyFriends()
                .onSuccess { friends ->
                    _uiState.update { it.copy(friends = friends, isLoadingFriends = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoadingFriends = false) }
                }
        }
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            friendshipRepository.getPendingRequests()
                .onSuccess { requests ->
                    _uiState.update { it.copy(pendingRequests = requests) }
                }
        }
    }

    fun loadParties() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingParties = true) }
            partyRepository.getActiveParties()
                .onSuccess { parties ->
                    _uiState.update { it.copy(parties = parties, isLoadingParties = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoadingParties = false) }
                }
        }
    }

    fun loadNearbyPlayers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPlayers = true) }
            friendshipRepository.searchUsers("")
                .onSuccess { players ->
                    _uiState.update { it.copy(nearbyPlayers = players, isLoadingPlayers = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoadingPlayers = false) }
                }
        }
    }

    fun acceptFriendRequest(friendshipId: Long) {
        viewModelScope.launch {
            friendshipRepository.respondToFriendRequest(friendshipId, accept = true)
                .onSuccess {
                    loadFriends()
                    loadPendingRequests()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun declineFriendRequest(friendshipId: Long) {
        viewModelScope.launch {
            friendshipRepository.respondToFriendRequest(friendshipId, accept = false)
                .onSuccess {
                    loadPendingRequests()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun sendFriendRequest(userId: Long) {
        viewModelScope.launch {
            friendshipRepository.sendFriendRequest(userId)
                .onSuccess {
                    loadNearbyPlayers()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        loadFriends()
        loadPendingRequests()
        loadParties()
        loadNearbyPlayers()
    }
}
