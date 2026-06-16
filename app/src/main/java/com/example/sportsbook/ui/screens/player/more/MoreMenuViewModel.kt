package com.example.sportsbook.ui.screens.player.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.PlayerLevel
import com.example.sportsbook.domain.model.PlayerStats
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.repository.GamificationRepository
import com.example.sportsbook.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoreMenuUiState(
    val user: User? = null,
    val stats: PlayerStats? = null,
    val level: PlayerLevel? = null,
    val achievementsEarned: Int = 0,
    val achievementsTotal: Int = 0,
    val isLoading: Boolean = false,
)

@HiltViewModel
class MoreMenuViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val gamificationRepository: GamificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoreMenuUiState())
    val uiState: StateFlow<MoreMenuUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadStats()
        loadLevel()
        loadAchievements()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.getProfile()
                .onSuccess { user ->
                    _uiState.update { it.copy(user = user, isLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            gamificationRepository.getMyStats()
                .onSuccess { stats ->
                    _uiState.update { it.copy(stats = stats) }
                }
        }
    }

    private fun loadLevel() {
        viewModelScope.launch {
            gamificationRepository.getMyLevel()
                .onSuccess { level ->
                    _uiState.update { it.copy(level = level) }
                }
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            gamificationRepository.getMyAchievements()
                .onSuccess { earned ->
                    _uiState.update { it.copy(achievementsEarned = earned.size) }
                }
            gamificationRepository.getAllAchievements()
                .onSuccess { all ->
                    _uiState.update { it.copy(achievementsTotal = all.size) }
                }
        }
    }
}
