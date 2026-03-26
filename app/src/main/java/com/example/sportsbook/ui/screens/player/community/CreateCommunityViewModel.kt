package com.example.sportsbook.ui.screens.player.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.data.remote.dto.CreateCommunityRequestDto
import com.example.sportsbook.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateCommunityUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val description: String = "",
    val sportType: String = "",
    val maxMembers: Int = 50,
    val isPublic: Boolean = false,
    val invitePolicy: String = "friends_only",
    val createdCommunityId: Long? = null,
    val error: String? = null
) {
    val isValid: Boolean get() = name.isNotBlank()
}

@HiltViewModel
class CreateCommunityViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCommunityUiState())
    val uiState: StateFlow<CreateCommunityUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onSportTypeChange(value: String) = _uiState.update { it.copy(sportType = value) }
    fun onMaxMembersChange(value: Int) = _uiState.update { it.copy(maxMembers = value) }
    fun onIsPublicChange(value: Boolean) = _uiState.update { it.copy(isPublic = value) }
    fun onInvitePolicyChange(value: String) = _uiState.update { it.copy(invitePolicy = value) }

    fun create() {
        val state = _uiState.value
        if (!state.isValid) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            communityRepository.createCommunity(
                CreateCommunityRequestDto(
                    name = state.name.trim(),
                    description = state.description.takeIf { it.isNotBlank() },
                    sportType = state.sportType.takeIf { it.isNotBlank() },
                    maxMembers = state.maxMembers,
                    isPublic = state.isPublic,
                    invitePolicy = state.invitePolicy
                )
            ).onSuccess { community ->
                _uiState.update { it.copy(isLoading = false, createdCommunityId = community.id) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
