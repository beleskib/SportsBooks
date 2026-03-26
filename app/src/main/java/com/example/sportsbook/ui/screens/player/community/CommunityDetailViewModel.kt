package com.example.sportsbook.ui.screens.player.community

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.sportsbook.domain.model.Community
import com.example.sportsbook.domain.model.CommunityMember
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.domain.model.Lobby
import com.example.sportsbook.domain.repository.CommunityRepository
import com.example.sportsbook.domain.repository.FriendshipRepository
import com.example.sportsbook.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityDetailUiState(
    val isLoading: Boolean = false,
    val community: Community? = null,
    val lobbies: List<Lobby> = emptyList(),
    val members: List<CommunityMember> = emptyList(),
    val pendingMembers: List<CommunityMember> = emptyList(),
    val friends: List<Friendship> = emptyList(),
    val selectedTab: CommunityDetailTab = CommunityDetailTab.LOBBIES,
    val isUserMember: Boolean = false,
    val isUserAdmin: Boolean = false,
    val isInviteLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

enum class CommunityDetailTab { LOBBIES, MEMBERS }

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val communityRepository: CommunityRepository,
    private val friendshipRepository: FriendshipRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.CommunityDetail>()
    val communityId: Long = route.communityId

    private val _uiState = MutableStateFlow(CommunityDetailUiState())
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectTab(tab: CommunityDetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val communityResult = communityRepository.getCommunityById(communityId)
            val lobbiesResult = communityRepository.getCommunityLobbies(communityId)
            val membersResult = communityRepository.getCommunityMembers(communityId)
            val pendingResult = communityRepository.getCommunityMembers(communityId, "pending")
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    community = communityResult.getOrElse { state.community },
                    lobbies = lobbiesResult.getOrElse { state.lobbies },
                    members = membersResult.getOrElse { state.members },
                    pendingMembers = pendingResult.getOrElse { state.pendingMembers },
                    error = communityResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun loadFriends() {
        viewModelScope.launch {
            friendshipRepository.getMyFriends()
                .onSuccess { friends ->
                    _uiState.update { it.copy(friends = friends) }
                }
        }
    }

    fun joinCommunity() {
        viewModelScope.launch {
            communityRepository.joinCommunity(communityId)
                .onSuccess { member ->
                    _uiState.update { state ->
                        state.copy(
                            isUserMember = true,
                            successMessage = "Joined community",
                            members = state.members + member
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun inviteFriend(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInviteLoading = true) }
            communityRepository.inviteToCommunity(communityId, userId)
                .onSuccess {
                    _uiState.update { it.copy(isInviteLoading = false, successMessage = "Invitation sent") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isInviteLoading = false, error = e.message) }
                }
        }
    }

    fun respondToMember(userId: Long, approve: Boolean) {
        viewModelScope.launch {
            communityRepository.respondToMember(communityId, userId, approve)
                .onSuccess { updated ->
                    _uiState.update { state ->
                        state.copy(
                            pendingMembers = state.pendingMembers.filter { it.userId != userId },
                            members = if (approve) state.members + updated else state.members
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun removeMember(userId: Long) {
        viewModelScope.launch {
            communityRepository.removeCommunityMember(communityId, userId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(members = state.members.filter { it.userId != userId })
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
