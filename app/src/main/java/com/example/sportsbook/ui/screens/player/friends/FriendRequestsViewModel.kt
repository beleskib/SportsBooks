package com.example.sportsbook.ui.screens.player.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.domain.repository.FriendshipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendRequestsUiState(
    val requests: List<Friendship> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FriendRequestsViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendRequestsUiState())
    val uiState: StateFlow<FriendRequestsUiState> = _uiState.asStateFlow()

    init {
        loadRequests()
    }

    fun loadRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            friendshipRepository.getPendingRequests()
                .onSuccess { requests ->
                    _uiState.update { it.copy(requests = requests, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun respondToRequest(friendshipId: Long, accept: Boolean) {
        viewModelScope.launch {
            friendshipRepository.respondToFriendRequest(friendshipId, accept)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(requests = state.requests.filter { it.id != friendshipId })
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }
}
