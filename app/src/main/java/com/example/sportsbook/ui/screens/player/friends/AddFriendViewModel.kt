package com.example.sportsbook.ui.screens.player.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.domain.repository.FriendshipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddFriendUiState(
    val query: String = "",
    val searchResults: List<Friendship> = emptyList(),
    val pendingRequests: List<Friendship> = emptyList(),
    val suggestedPlayers: List<Friendship> = emptyList(),
    val isSearching: Boolean = false,
    val isLoadingPending: Boolean = false,
    val isLoadingSuggested: Boolean = false,
    val sentRequests: Set<Long> = emptySet(),
    val respondedRequests: Set<Long> = emptySet(),
    val error: String? = null
)

@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddFriendUiState())
    val uiState: StateFlow<AddFriendUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init {
        loadPendingRequests()
        loadSuggestedPlayers()
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPending = true) }
            friendshipRepository.getPendingRequests()
                .onSuccess { requests ->
                    _uiState.update { it.copy(pendingRequests = requests, isLoadingPending = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoadingPending = false, error = e.message) }
                }
        }
    }

    private fun loadSuggestedPlayers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSuggested = true) }
            friendshipRepository.searchUsers("")
                .onSuccess { players ->
                    _uiState.update { it.copy(suggestedPlayers = players, isLoadingSuggested = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoadingSuggested = false, error = e.message) }
                }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(isSearching = true) }
            friendshipRepository.searchUsers(query)
                .onSuccess { results ->
                    _uiState.update { it.copy(searchResults = results, isSearching = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isSearching = false) }
                }
        }
    }

    fun sendRequest(userId: Long) {
        viewModelScope.launch {
            friendshipRepository.sendFriendRequest(userId)
                .onSuccess {
                    _uiState.update { it.copy(sentRequests = it.sentRequests + userId) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun acceptRequest(friendshipId: Long) {
        viewModelScope.launch {
            friendshipRepository.respondToFriendRequest(friendshipId, true)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            respondedRequests = state.respondedRequests + friendshipId,
                            pendingRequests = state.pendingRequests.filter { it.id != friendshipId }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun declineRequest(friendshipId: Long) {
        viewModelScope.launch {
            friendshipRepository.respondToFriendRequest(friendshipId, false)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            respondedRequests = state.respondedRequests + friendshipId,
                            pendingRequests = state.pendingRequests.filter { it.id != friendshipId }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }
}
