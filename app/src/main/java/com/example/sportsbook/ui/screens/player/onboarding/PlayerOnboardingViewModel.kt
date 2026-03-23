package com.example.sportsbook.ui.screens.player.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.ExperienceDuration
import com.example.sportsbook.domain.enums.SkillLevel
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.repository.ImageRepository
import com.example.sportsbook.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SportExpertiseEntry(
    val skillLevel: SkillLevel = SkillLevel.NEWBIE,
    val experienceDuration: ExperienceDuration = ExperienceDuration.LESS_THAN_1_YEAR
)

data class PlayerOnboardingUiState(
    val photoUri: Uri? = null,
    val uploadedPhotoUrl: String? = null,
    val isUploadingPhoto: Boolean = false,
    val displayName: String = "",
    val email: String = "",
    val dateOfBirth: String? = null,
    val selectedSports: List<SportType> = emptyList(),
    val sportExpertiseMap: Map<SportType, SportExpertiseEntry> = emptyMap(),
    val bio: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class PlayerOnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerOnboardingUiState())
    val uiState: StateFlow<PlayerOnboardingUiState> = _uiState

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.getProfile()
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            displayName = user.displayName ?: "",
                            email = user.email,
                            uploadedPhotoUrl = user.photoUrl,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
        }
    }

    fun onPhotoSelected(uri: Uri) {
        _uiState.update { it.copy(photoUri = uri, isUploadingPhoto = true) }
        viewModelScope.launch {
            imageRepository.uploadAndGetUrl(uri, "users/profile/${System.currentTimeMillis()}.jpg")
                .onSuccess { url ->
                    _uiState.update {
                        it.copy(uploadedPhotoUrl = url, isUploadingPhoto = false)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isUploadingPhoto = false, error = "Failed to upload photo: ${e.message}")
                    }
                }
        }
    }

    fun onDisplayNameChange(name: String) {
        _uiState.update { it.copy(displayName = name) }
    }

    fun onDateOfBirthChange(dob: String?) {
        _uiState.update { it.copy(dateOfBirth = dob) }
    }

    fun onBioChange(bio: String) {
        if (bio.length <= 500) {
            _uiState.update { it.copy(bio = bio) }
        }
    }

    fun toggleSport(sport: SportType) {
        _uiState.update { state ->
            val currentSports = state.selectedSports.toMutableList()
            val currentExpertise = state.sportExpertiseMap.toMutableMap()
            if (sport in currentSports) {
                currentSports.remove(sport)
                currentExpertise.remove(sport)
            } else {
                currentSports.add(sport)
                currentExpertise[sport] = SportExpertiseEntry()
            }
            state.copy(
                selectedSports = currentSports,
                sportExpertiseMap = currentExpertise
            )
        }
    }

    fun onSkillLevelChange(sport: SportType, level: SkillLevel) {
        _uiState.update { state ->
            val currentExpertise = state.sportExpertiseMap.toMutableMap()
            val entry = currentExpertise[sport] ?: SportExpertiseEntry()
            currentExpertise[sport] = entry.copy(skillLevel = level)
            state.copy(sportExpertiseMap = currentExpertise)
        }
    }

    fun onExperienceDurationChange(sport: SportType, duration: ExperienceDuration) {
        _uiState.update { state ->
            val currentExpertise = state.sportExpertiseMap.toMutableMap()
            val entry = currentExpertise[sport] ?: SportExpertiseEntry()
            currentExpertise[sport] = entry.copy(experienceDuration = duration)
            state.copy(sportExpertiseMap = currentExpertise)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.displayName.isBlank() || state.selectedSports.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val expertiseList = state.sportExpertiseMap.map { (sport, entry) ->
                Triple(sport, entry.skillLevel, entry.experienceDuration)
            }

            userRepository.completeOnboarding(
                displayName = state.displayName.takeIf { it.isNotBlank() },
                photoUrl = state.uploadedPhotoUrl,
                dateOfBirth = state.dateOfBirth,
                bio = state.bio.takeIf { it.isNotBlank() },
                interestedSports = state.selectedSports,
                expertise = expertiseList
            )
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, isComplete = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSaving = false, error = e.message ?: "Failed to save profile")
                    }
                }
        }
    }
}
