package com.example.sportsbook.ui.screens.player.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Sport
import com.example.sportsbook.domain.model.User
import com.example.sportsbook.domain.repository.AuthRepository
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
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val editDisplayName: String = "",
    val editPhoneNumber: String = "",
    val editBio: String = "",
    val selectedSports: List<SportType> = emptyList(),
    val isEditing: Boolean = false
)

@HiltViewModel
class PlayerProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sportRepository: SportRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerProfileUiState())
    val uiState: StateFlow<PlayerProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, saveSuccess = false) }

            val userDeferred = async { userRepository.getProfile() }
            val sportsDeferred = async { sportRepository.getSports() }

            val userResult = userDeferred.await()
            val sportsResult = sportsDeferred.await()

            _uiState.update { current ->
                val user = userResult.getOrNull()
                current.copy(
                    user = user ?: current.user,
                    sports = sportsResult.getOrElse { current.sports },
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

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
