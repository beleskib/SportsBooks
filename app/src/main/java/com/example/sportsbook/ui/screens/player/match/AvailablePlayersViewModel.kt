package com.example.sportsbook.ui.screens.player.match

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.AvailablePlayer
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.AvailablePlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvailablePlayersUiState(
    val isLoading: Boolean = true,
    val availablePlayers: List<AvailablePlayer> = emptyList(),
    val myAvailability: List<AvailablePlayer> = emptyList(),
    val selectedSport: SportType = SportType.BASKETBALL,
    val isRegistering: Boolean = false,
    val registerSkillLevel: Int = 3,
    val registerNote: String = "",
    val error: String? = null,
    val inviteSuccess: String? = null,
    val matchId: Long? = null,
    // Skill-level filtering
    val skillFilterEnabled: Boolean = false,
    val filterMinSkill: Int = 1,
    val filterMaxSkill: Int = 5,
    // Match requirements (from navigation)
    val matchMinSkill: Int? = null,
    val matchMaxSkill: Int? = null,
    val hasMatchRequirements: Boolean = false,
)

@HiltViewModel
class AvailablePlayersViewModel @Inject constructor(
    private val availablePlayerRepository: AvailablePlayerRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvailablePlayersUiState())
    val uiState: StateFlow<AvailablePlayersUiState> = _uiState.asStateFlow()

    init {
        val matchId: Long? = savedStateHandle["matchId"]
        val sportType: String? = savedStateHandle["sportType"]
        val minSkillLevel: Int? = savedStateHandle["minSkillLevel"]
        val maxSkillLevel: Int? = savedStateHandle["maxSkillLevel"]

        // Determine initial sport from navigation params
        val initialSport = sportType?.let { st ->
            SportType.entries.find { it.name.equals(st, ignoreCase = true) }
        } ?: SportType.BASKETBALL

        // If match has skill requirements, auto-enable skill filter
        val hasRequirements = minSkillLevel != null || maxSkillLevel != null

        _uiState.update {
            it.copy(
                matchId = matchId,
                selectedSport = initialSport,
                matchMinSkill = minSkillLevel,
                matchMaxSkill = maxSkillLevel,
                hasMatchRequirements = hasRequirements,
                skillFilterEnabled = hasRequirements,
                filterMinSkill = minSkillLevel ?: 1,
                filterMaxSkill = maxSkillLevel ?: 5
            )
        }

        loadMyAvailability()
        loadPlayers()
    }

    fun loadPlayers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val state = _uiState.value
            val sport = state.selectedSport

            val skillMin = if (state.skillFilterEnabled) state.filterMinSkill else null
            val skillMax = if (state.skillFilterEnabled) state.filterMaxSkill else null

            availablePlayerRepository.listBySport(
                sportType = sport.name.lowercase(),
                skillMin = skillMin,
                skillMax = skillMax
            )
                .onSuccess { players ->
                    _uiState.update { it.copy(availablePlayers = players, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun loadMyAvailability() {
        viewModelScope.launch {
            availablePlayerRepository.getMyAvailability()
                .onSuccess { mine ->
                    _uiState.update { it.copy(myAvailability = mine) }
                }
        }
    }

    fun selectSport(sport: SportType) {
        _uiState.update { it.copy(selectedSport = sport) }
        loadPlayers()
    }

    fun toggleSkillFilter(enabled: Boolean) {
        _uiState.update { it.copy(skillFilterEnabled = enabled) }
        loadPlayers()
    }

    fun updateFilterMinSkill(level: Int) {
        val clamped = level.coerceIn(1, 5)
        _uiState.update {
            it.copy(
                filterMinSkill = clamped,
                filterMaxSkill = maxOf(clamped, it.filterMaxSkill)
            )
        }
        loadPlayers()
    }

    fun updateFilterMaxSkill(level: Int) {
        val clamped = level.coerceIn(1, 5)
        _uiState.update {
            it.copy(
                filterMaxSkill = clamped,
                filterMinSkill = minOf(clamped, it.filterMinSkill)
            )
        }
        loadPlayers()
    }

    fun applyMatchRequirements() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                skillFilterEnabled = true,
                filterMinSkill = state.matchMinSkill ?: 1,
                filterMaxSkill = state.matchMaxSkill ?: 5
            )
        }
        loadPlayers()
    }

    fun clearSkillFilter() {
        _uiState.update {
            it.copy(
                skillFilterEnabled = false,
                filterMinSkill = 1,
                filterMaxSkill = 5
            )
        }
        loadPlayers()
    }

    fun updateSkillLevel(level: Int) {
        _uiState.update { it.copy(registerSkillLevel = level.coerceIn(1, 5)) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(registerNote = note) }
    }

    fun register() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isRegistering = true, error = null) }
            availablePlayerRepository.register(
                sportType = state.selectedSport.name.lowercase(),
                skillLevel = state.registerSkillLevel,
                note = state.registerNote.takeIf { it.isNotBlank() }
            ).onSuccess { player ->
                _uiState.update { it.copy(
                    isRegistering = false,
                    myAvailability = it.myAvailability + player
                ) }
            }.onFailure { e ->
                _uiState.update { it.copy(isRegistering = false, error = e.message) }
            }
        }
    }

    fun unregister(sportType: String) {
        viewModelScope.launch {
            availablePlayerRepository.unregister(sportType)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            myAvailability = state.myAvailability.filterNot {
                                it.sportType.name.equals(sportType, ignoreCase = true)
                            }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun inviteToMatch(userId: Long, displayName: String?) {
        val matchId = _uiState.value.matchId ?: return
        viewModelScope.launch {
            availablePlayerRepository.inviteToMatch(matchId, userId)
                .onSuccess {
                    _uiState.update { it.copy(inviteSuccess = "Invited ${displayName ?: "player"} to the match!") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearInviteSuccess() {
        _uiState.update { it.copy(inviteSuccess = null) }
    }
}
