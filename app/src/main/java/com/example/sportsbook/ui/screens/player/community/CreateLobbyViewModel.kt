package com.example.sportsbook.ui.screens.player.community

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.sportsbook.data.remote.dto.CreateLobbyRequestDto
import com.example.sportsbook.domain.repository.CommunityRepository
import com.example.sportsbook.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateLobbyUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val sportType: String = "",
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val durationMinutes: Int = 60,
    val maxPlayers: Int = 10,
    val skillLevelMin: Int = 1,
    val skillLevelMax: Int = 5,
    val isPublic: Boolean = false,
    val description: String = "",
    val createdLobbyId: Long? = null,
    val error: String? = null
) {
    val isValid: Boolean
        get() = title.isNotBlank() && sportType.isNotBlank() &&
            scheduledDate.isNotBlank() && scheduledTime.isNotBlank()
}

@HiltViewModel
class CreateLobbyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.CreateLobby>()
    val communityId: Long = route.communityId

    private val _uiState = MutableStateFlow(CreateLobbyUiState())
    val uiState: StateFlow<CreateLobbyUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }
    fun onSportTypeChange(value: String) = _uiState.update { it.copy(sportType = value) }
    fun onDateChange(value: String) = _uiState.update { it.copy(scheduledDate = value) }
    fun onTimeChange(value: String) = _uiState.update { it.copy(scheduledTime = value) }
    fun onDurationChange(value: Int) = _uiState.update { it.copy(durationMinutes = value) }
    fun onMaxPlayersChange(value: Int) = _uiState.update { it.copy(maxPlayers = value) }
    fun onSkillMinChange(value: Int) = _uiState.update { it.copy(skillLevelMin = value) }
    fun onSkillMaxChange(value: Int) = _uiState.update { it.copy(skillLevelMax = value) }
    fun onIsPublicChange(value: Boolean) = _uiState.update { it.copy(isPublic = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    fun create() {
        val state = _uiState.value
        if (!state.isValid) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            communityRepository.createLobby(
                communityId,
                CreateLobbyRequestDto(
                    title = state.title.trim(),
                    sportType = state.sportType,
                    scheduledDate = state.scheduledDate,
                    scheduledTime = state.scheduledTime,
                    durationMinutes = state.durationMinutes,
                    maxPlayers = state.maxPlayers,
                    skillLevelMin = state.skillLevelMin,
                    skillLevelMax = state.skillLevelMax,
                    isPublic = state.isPublic,
                    description = state.description.takeIf { it.isNotBlank() }
                )
            ).onSuccess { lobby ->
                _uiState.update { it.copy(isLoading = false, createdLobbyId = lobby.id) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
