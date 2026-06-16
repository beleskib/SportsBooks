package com.example.sportsbook.ui.screens.player.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.enums.ExperienceDuration
import com.example.sportsbook.domain.enums.SkillLevel
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Booking
import com.example.sportsbook.domain.model.Sport
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.model.UserSportExpertise
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.repository.SportRepository
import com.example.sportsbook.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerProfileUiState(
    val user: User? = null,
    val sports: List<Sport> = emptyList(),
    val followers: Int = 0,
    val following: Int = 0,
    val pastBookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val editDisplayName: String = "",
    val editPhoneNumber: String = "",
    val editBio: String = "",
    val selectedSports: List<SportType> = emptyList(),
    val isEditing: Boolean = false,
    // Sport expertise
    val sportExpertise: List<UserSportExpertise> = emptyList(),
    val isEditingExpertise: Boolean = false,
    val editExpertise: List<EditableSportExpertise> = emptyList(),
    val isSavingExpertise: Boolean = false,
)

data class EditableSportExpertise(
    val sportType: SportType,
    val skillLevel: SkillLevel,
    val experienceDuration: ExperienceDuration,
)

@HiltViewModel
class PlayerProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sportRepository: SportRepository,
    private val authRepository: AuthRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerProfileUiState())
    val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadPastBookings()
        loadSportExpertise()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, saveSuccess = false) }

            val userDeferred = async { userRepository.getProfile() }
            val sportsDeferred = async { sportRepository.getSports() }
            val followDeferred = async { userRepository.getFollowCounts() }

            val userResult = userDeferred.await()
            val sportsResult = sportsDeferred.await()
            val followResult = followDeferred.await()
            val followCounts = followResult.getOrNull()

            _uiState.update { current ->
                val user = userResult.getOrNull()
                current.copy(
                    user = user ?: current.user,
                    sports = sportsResult.getOrElse { current.sports },
                    followers = followCounts?.first ?: current.followers,
                    following = followCounts?.second ?: current.following,
                    selectedSports = user?.interestedSports ?: current.selectedSports,
                    editDisplayName = user?.displayName ?: "",
                    editPhoneNumber = user?.phoneNumber ?: "",
                    editBio = user?.bio ?: "",
                    isLoading = false,
                    error = userResult.exceptionOrNull()?.message
                )
            }
        }
    }

    private fun loadPastBookings() {
        viewModelScope.launch {
            bookingRepository.getMyBookings(BookingStatus.COMPLETED)
                .onSuccess { bookings ->
                    _uiState.update { it.copy(pastBookings = bookings.take(3)) }
                }
                .onFailure {
                    // Silently ignore — past bookings are supplementary
                }
        }
    }

    fun startEditing() {
        _uiState.update { current ->
            current.copy(
                isEditing = true,
                editDisplayName = current.user?.displayName ?: "",
                editPhoneNumber = current.user?.phoneNumber ?: "",
                editBio = current.user?.bio ?: "",
                selectedSports = current.user?.interestedSports ?: emptyList(),
                saveSuccess = false
            )
        }
    }

    fun cancelEditing() {
        _uiState.update { current ->
            current.copy(
                isEditing = false,
                editDisplayName = current.user?.displayName ?: "",
                editPhoneNumber = current.user?.phoneNumber ?: "",
                editBio = current.user?.bio ?: "",
                selectedSports = current.user?.interestedSports ?: emptyList()
            )
        }
    }

    fun onDisplayNameChange(value: String) {
        _uiState.update { it.copy(editDisplayName = value) }
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.update { it.copy(editPhoneNumber = value) }
    }

    fun onBioChange(value: String) {
        _uiState.update { it.copy(editBio = value) }
    }

    fun toggleInterestedSport(sportType: SportType) {
        _uiState.update { current ->
            val updated = if (sportType in current.selectedSports) {
                current.selectedSports - sportType
            } else {
                current.selectedSports + sportType
            }
            current.copy(selectedSports = updated)
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }

            val current = _uiState.value
            val updatedUser = current.user?.copy(
                displayName = current.editDisplayName.ifBlank { null },
                phoneNumber = current.editPhoneNumber.ifBlank { null },
                bio = current.editBio.ifBlank { null }
            ) ?: return@launch

            val profileResult = userRepository.updateProfile(updatedUser)
            val sportsResult = userRepository.updateInterestedSports(current.selectedSports)

            if (profileResult.isSuccess && sportsResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        user = profileResult.getOrNull()?.copy(
                            interestedSports = sportsResult.getOrElse { emptyList() }
                        ),
                        isSaving = false,
                        isEditing = false,
                        saveSuccess = true
                    )
                }
            } else {
                val error = profileResult.exceptionOrNull()?.message
                    ?: sportsResult.exceptionOrNull()?.message
                _uiState.update { it.copy(isSaving = false, error = error) }
            }
        }
    }

    // ── Sport Expertise ────────────────────────────────────────────────

    private fun loadSportExpertise() {
        viewModelScope.launch {
            userRepository.getSportExpertise()
                .onSuccess { expertise ->
                    _uiState.update { it.copy(sportExpertise = expertise) }
                }
        }
    }

    fun startEditingExpertise() {
        _uiState.update { current ->
            val editable = current.sportExpertise.map { e ->
                EditableSportExpertise(e.sportType, e.skillLevel, e.experienceDuration)
            }
            current.copy(isEditingExpertise = true, editExpertise = editable)
        }
    }

    fun cancelEditingExpertise() {
        _uiState.update { it.copy(isEditingExpertise = false, editExpertise = emptyList()) }
    }

    fun addExpertiseSport(sportType: SportType) {
        _uiState.update { current ->
            if (current.editExpertise.any { it.sportType == sportType }) return@update current
            current.copy(
                editExpertise = current.editExpertise + EditableSportExpertise(
                    sportType = sportType,
                    skillLevel = SkillLevel.BEGINNER,
                    experienceDuration = ExperienceDuration.LESS_THAN_1_YEAR,
                )
            )
        }
    }

    fun removeExpertiseSport(sportType: SportType) {
        _uiState.update { current ->
            current.copy(editExpertise = current.editExpertise.filter { it.sportType != sportType })
        }
    }

    fun updateExpertiseSkillLevel(sportType: SportType, skillLevel: SkillLevel) {
        _uiState.update { current ->
            current.copy(
                editExpertise = current.editExpertise.map {
                    if (it.sportType == sportType) it.copy(skillLevel = skillLevel) else it
                }
            )
        }
    }

    fun updateExpertiseExperience(sportType: SportType, experience: ExperienceDuration) {
        _uiState.update { current ->
            current.copy(
                editExpertise = current.editExpertise.map {
                    if (it.sportType == sportType) it.copy(experienceDuration = experience) else it
                }
            )
        }
    }

    fun saveExpertise() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingExpertise = true, error = null) }
            val entries = _uiState.value.editExpertise.map {
                Triple(it.sportType, it.skillLevel, it.experienceDuration)
            }
            userRepository.setSportExpertise(entries)
                .onSuccess { saved ->
                    _uiState.update {
                        it.copy(
                            sportExpertise = saved,
                            isEditingExpertise = false,
                            editExpertise = emptyList(),
                            isSavingExpertise = false,
                            saveSuccess = true,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSavingExpertise = false, error = e.message ?: "Failed to save expertise")
                    }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
