package com.example.sportsbook.ui.screens.partner.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.domain.repository.VenueRepository
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.model.Venue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PartnerSetupUiState(
    val name: String = "",
    val description: String = "",
    val sportType: SportType = SportType.BASKETBALL,
    val pricePerHour: String = "",
    val address: String = "",
    val city: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class PartnerSetupViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    private val coachRepository: CoachRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartnerSetupUiState())
    val uiState: StateFlow<PartnerSetupUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onSportTypeChange(sportType: SportType) {
        _uiState.update { it.copy(sportType = sportType) }
    }

    fun onPriceChange(price: String) {
        _uiState.update { it.copy(pricePerHour = price) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onCityChange(city: String) {
        _uiState.update { it.copy(city = city) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phoneNumber = phone) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun createVenueListing() {
        val state = _uiState.value
        val price = state.pricePerHour.toDoubleOrNull() ?: run {
            _uiState.update { it.copy(error = "Please enter a valid price") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val venue = Venue(
                id = 0,
                ownerId = 0,
                name = state.name,
                description = state.description,
                sportType = state.sportType,
                pricePerHour = price,
                address = state.address,
                city = state.city,
                country = "",
                phoneNumber = state.phoneNumber,
                email = state.email
            )
            venueRepository.createVenue(venue)
                .onSuccess {
                    _uiState.update { it.copy(isSuccess = true, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun createCoachListing() {
        val state = _uiState.value
        val price = state.pricePerHour.toDoubleOrNull() ?: run {
            _uiState.update { it.copy(error = "Please enter a valid price") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val coach = Coach(
                id = 0,
                userId = 0,
                name = state.name,
                bio = state.description,
                sportType = state.sportType,
                pricePerHour = price,
                address = state.address,
                city = state.city,
                country = "",
                phoneNumber = state.phoneNumber,
                email = state.email
            )
            coachRepository.createCoach(coach)
                .onSuccess {
                    _uiState.update { it.copy(isSuccess = true, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }
}
