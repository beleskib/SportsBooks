package com.example.sportsbook.ui.screens.player.match

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.data.remote.dto.CreatePlayerRatingRequestDto
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.MatchParticipant
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerRatingInput(
    val userId: Long,
    val userName: String?,
    val userPhotoUrl: String?,
    val skillRating: Int = 0,
    val sportsmanshipRating: Int = 0,
    val punctualityRating: Int = 0,
    val comment: String = ""
) {
    val isValid: Boolean get() = skillRating > 0 && sportsmanshipRating > 0 && punctualityRating > 0
}

data class RatePlayersUiState(
    val match: Match? = null,
    val ratings: List<PlayerRatingInput> = emptyList(),
    val currentUserId: Long? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
    val error: String? = null
) {
    val allRatingsValid: Boolean get() = ratings.all { it.isValid }
}

@HiltViewModel
class RatePlayersViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: Long = checkNotNull(savedStateHandle["matchId"])
    private val _uiState = MutableStateFlow(RatePlayersUiState())
    val uiState: StateFlow<RatePlayersUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadMatch()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()?.id?.let { uid ->
                _uiState.update { it.copy(currentUserId = uid) }
                // Re-generate ratings list if match already loaded
                _uiState.value.match?.let { match ->
                    generateRatingInputs(match, uid)
                }
            }
        }
    }

    fun loadMatch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            matchRepository.getMatchById(matchId)
                .onSuccess { match ->
                    _uiState.update { it.copy(match = match, isLoading = false) }
                    _uiState.value.currentUserId?.let { uid ->
                        generateRatingInputs(match, uid)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    private fun generateRatingInputs(match: Match, currentUserId: Long) {
        val otherParticipants = match.participants.filter {
            it.userId != currentUserId && it.status.name == "APPROVED"
        }
        _uiState.update {
            it.copy(
                ratings = otherParticipants.map { p ->
                    PlayerRatingInput(
                        userId = p.userId,
                        userName = p.userName,
                        userPhotoUrl = p.userPhotoUrl
                    )
                }
            )
        }
    }

    fun updateRating(userId: Long, field: String, value: Int) {
        _uiState.update { state ->
            state.copy(
                ratings = state.ratings.map { r ->
                    if (r.userId == userId) {
                        when (field) {
                            "skill" -> r.copy(skillRating = value)
                            "sportsmanship" -> r.copy(sportsmanshipRating = value)
                            "punctuality" -> r.copy(punctualityRating = value)
                            else -> r
                        }
                    } else r
                }
            )
        }
    }

    fun updateComment(userId: Long, comment: String) {
        _uiState.update { state ->
            state.copy(
                ratings = state.ratings.map { r ->
                    if (r.userId == userId) r.copy(comment = comment) else r
                }
            )
        }
    }

    fun submitRatings() {
        val ratings = _uiState.value.ratings
        if (!_uiState.value.allRatingsValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            var hasError = false
            for (rating in ratings) {
                matchRepository.ratePlayer(
                    matchId,
                    CreatePlayerRatingRequestDto(
                        ratedId = rating.userId,
                        skillRating = rating.skillRating,
                        sportsmanshipRating = rating.sportsmanshipRating,
                        punctualityRating = rating.punctualityRating,
                        comment = rating.comment.ifBlank { null }
                    )
                ).onFailure {
                    hasError = true
                    _uiState.update { s -> s.copy(error = it.message) }
                }
            }
            _uiState.update { it.copy(isSubmitting = false, submitted = !hasError) }
        }
    }
}
