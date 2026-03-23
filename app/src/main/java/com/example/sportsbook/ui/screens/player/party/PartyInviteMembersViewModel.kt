package com.example.sportsbook.ui.screens.player.party

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.domain.repository.FriendshipRepository
import com.example.sportsbook.domain.repository.PartyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PartyInviteMembersUiState(
    val friends: List<Friendship> = emptyList(),
    val selectedUserIds: Set<Long> = emptySet(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val invitesSent: Boolean = false
)

@HiltViewModel
class PartyInviteMembersViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository,
    private val partyRepository: PartyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val partyId: Long = checkNotNull(savedStateHandle["partyId"])
    private val _uiState = MutableStateFlow(PartyInviteMembersUiState())
    val uiState: StateFlow<PartyInviteMembersUiState> = _uiState.asStateFlow()

    init {
        loadFriends()
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

    fun toggleSelection(userId: Long) {
        _uiState.update { state ->
            val current = state.selectedUserIds.toMutableSet()
            if (userId in current) current.remove(userId) else current.add(userId)
            state.copy(selectedUserIds = current)
        }
    }

    fun sendInvites() {
        val userIds = _uiState.value.selectedUserIds.toList()
        if (userIds.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            partyRepository.inviteToParty(partyId, userIds)
                .onSuccess {
                    _uiState.update { it.copy(isSending = false, invitesSent = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isSending = false) }
                }
        }
    }
}
