package com.example.sportsbook.ui.screens.player.party

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.repository.PartyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PartyCreateUiState(
    val name: String = "",
    val sportType: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdPartyId: Long? = null
)

@HiltViewModel
class PartyCreateViewModel @Inject constructor(
    private val partyRepository: PartyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartyCreateUiState())
    val uiState: StateFlow<PartyCreateUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onSportTypeChange(value: String?) {
        _uiState.update { it.copy(sportType = value) }
    }

    fun createParty() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val name = _uiState.value.name.trim().ifBlank { null }
            val sportType = _uiState.value.sportType
            partyRepository.createParty(name = name, sportType = sportType)
                .onSuccess { party ->
                    _uiState.update { it.copy(isLoading = false, createdPartyId = party.id) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
