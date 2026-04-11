package com.example.sportsbook.ui.screens.player.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.PublicPlayerProfile
import com.example.sportsbook.domain.repository.FriendshipRepository
import com.example.sportsbook.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PublicPlayerProfileUiState(
    val profile: PublicPlayerProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val friendActionLoading: Boolean = false,
    val friendActionSuccess: String? = null
)

@HiltViewModel
class PublicPlayerProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val friendshipRepository: FriendshipRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Long = checkNotNull(savedStateHandle["userId"])
    private val _uiState = MutableStateFlow(PublicPlayerProfileUiState())
    val uiState: StateFlow<PublicPlayerProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.getPublicProfile(userId)
                .onSuccess { profile ->
                    _uiState.update { it.copy(profile = profile, isLoading = false, error = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun sendFriendRequest() {
        viewModelScope.launch {
            _uiState.update { it.copy(friendActionLoading = true) }
            friendshipRepository.sendFriendRequest(userId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            profile = state.profile?.copy(friendshipStatus = "pending"),
                            friendActionLoading = false,
                            friendActionSuccess = "Friend request sent!"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(friendActionLoading = false, error = e.message) }
                }
        }
    }

    fun removeFriend() {
        viewModelScope.launch {
            _uiState.update { it.copy(friendActionLoading = true) }
            friendshipRepository.removeFriend(userId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            profile = state.profile?.copy(friendshipStatus = null, friendshipId = null),
                            friendActionLoading = false,
                            friendActionSuccess = "Friend removed"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(friendActionLoading = false, error = e.message) }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(friendActionSuccess = null, error = null) }
    }
}
