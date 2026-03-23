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

data class FriendsListUiState(
    val friends: List<Friendship> = emptyList(),
    val pendingCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FriendsListViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsListUiState())
    val uiState: StateFlow<FriendsListUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
        loadPendingCount()
    }

    fun loadFriends() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            friendshipRepository.getMyFriends()
                .onSuccess { friends ->
                    _uiState.update { it.copy(friends = friends, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun loadPendingCount() {
        viewModelScope.launch {
            friendshipRepository.getPendingRequests()
                .onSuccess { requests ->
                    _uiState.update { it.copy(pendingCount = requests.size) }
                }
        }
    }

    fun removeFriend(friendId: Long) {
        viewModelScope.launch {
            friendshipRepository.removeFriend(friendId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(friends = state.friends.filter { it.friendId != friendId })
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }
}
