package com.example.sportsbook.ui.screens.player.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SportsIFollowUiState(
    val allSports: List<SportType> = SportType.entries.toList(),
    val selectedSports: Set<SportType> = emptySet(),
    val originalSports: Set<SportType> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
) {
    val hasChanges: Boolean
        get() = selectedSports != originalSports
}

@HiltViewModel
class SportsIFollowViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SportsIFollowUiState())
    val uiState: StateFlow<SportsIFollowUiState> = _uiState.asStateFlow()

    init {
        loadCurrentSports()
    }

    private fun loadCurrentSports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = userRepository.getProfile()
            _uiState.update { current ->
                val user = result.getOrNull()
                val sports = user?.interestedSports?.toSet() ?: emptySet()
                current.copy(
                    selectedSports = sports,
                    originalSports = sports,
                    isLoading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun toggleSport(sport: SportType) {
        _uiState.update { current ->
            val updated = if (sport in current.selectedSports) {
                current.selectedSports - sport
            } else {
                current.selectedSports + sport
            }
            current.copy(selectedSports = updated)
        }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
            val result = userRepository.updateInterestedSports(
                _uiState.value.selectedSports.toList()
            )
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        originalSports = it.selectedSports
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to save"
                    )
                }
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
