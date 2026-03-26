package com.example.sportsbook.ui.screens.player.community

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.sportsbook.domain.model.Lobby
import com.example.sportsbook.domain.repository.CommunityRepository
import com.example.sportsbook.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LobbyDetailUiState(
    val isLoading: Boolean = false,
    val lobby: Lobby? = null,
    val isUserParticipant: Boolean = false,
    val isUserCreator: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class LobbyDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.LobbyDetail>()
    val lobbyId: Long = route.lobbyId

    private val _uiState = MutableStateFlow(LobbyDetailUiState())
    val uiState: StateFlow<LobbyDetailUiState> = _uiState.asStateFlow()

    init {
        loadLobby()
    }

    fun loadLobby() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            communityRepository.getLobbyById(lobbyId)
                .onSuccess { lobby ->
                    _uiState.update { it.copy(isLoading = false, lobby = lobby) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun joinLobby() {
        viewModelScope.launch {
            communityRepository.joinLobby(lobbyId)
                .onSuccess { updated ->
                    _uiState.update { it.copy(lobby = updated, isUserParticipant = true, successMessage = "Joined lobby") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun leaveLobby() {
        viewModelScope.launch {
            communityRepository.leaveLobby(lobbyId)
                .onSuccess { updated ->
                    _uiState.update { it.copy(lobby = updated, isUserParticipant = false, successMessage = "Left lobby") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun makeLobbyPublic() {
        viewModelScope.launch {
            communityRepository.makeLobbyPublic(lobbyId)
                .onSuccess { updated ->
                    _uiState.update { it.copy(lobby = updated, successMessage = "Lobby is now public") }
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
