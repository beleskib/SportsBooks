package com.example.sportsbook.ui.screens.partner.edit

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Venue
import com.example.sportsbook.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditVenueViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val venueId: Long = checkNotNull(savedStateHandle["venueId"])

    data class UiState(
        val venueId: Long = 0,
        val name: String = "",
        val description: String = "",
        val sportType: SportType = SportType.BASKETBALL,
        val pricePerHour: String = "",
        val address: String = "",
        val city: String = "",
        val phoneNumber: String = "",
        val email: String = "",
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val error: String? = null,
        val saveSuccess: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadVenue()
    }

    private fun loadVenue() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            venueRepository.getVenueById(venueId)
                .onSuccess { venue ->
                    _uiState.update {
                        it.copy(
                            venueId = venue.id,
                            name = venue.name,
                            description = venue.description.orEmpty(),
                            sportType = venue.sportType,
                            pricePerHour = venue.pricePerHour.toString(),
                            address = venue.address,
                            city = venue.city.orEmpty(),
                            phoneNumber = venue.phoneNumber.orEmpty(),
                            email = venue.email.orEmpty(),
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onSportTypeChange(value: SportType) = _uiState.update { it.copy(sportType = value) }
    fun onPriceChange(value: String) = _uiState.update { it.copy(pricePerHour = value) }
    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value) }
    fun onCityChange(value: String) = _uiState.update { it.copy(city = value) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phoneNumber = value) }
    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value) }

    fun saveChanges() {
        val state = _uiState.value
        val price = state.pricePerHour.toDoubleOrNull() ?: run {
            _uiState.update { it.copy(error = "Please enter a valid price") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val updated = Venue(
                id = state.venueId,
                name = state.name,
                description = state.description,
                sportType = state.sportType,
                pricePerHour = price,
                address = state.address,
                city = state.city,
                phoneNumber = state.phoneNumber,
                email = state.email
            )
            venueRepository.updateVenue(updated)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isSaving = false) }
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVenueScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditVenueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var sportTypeDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Venue") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Venue Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.onDescriptionChange(it) },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.pricePerHour,
                        onValueChange = { viewModel.onPriceChange(it) },
                        label = { Text("Price per Hour") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = sportTypeDropdownExpanded,
                        onExpandedChange = { sportTypeDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.sportType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sport Type") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Expand sport type menu"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = sportTypeDropdownExpanded,
                            onDismissRequest = { sportTypeDropdownExpanded = false }
                        ) {
                            SportType.entries.forEach { sportType ->
                                DropdownMenuItem(
                                    text = { Text(sportType.displayName) },
                                    onClick = {
                                        viewModel.onSportTypeChange(sportType)
                                        sportTypeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.address,
                        onValueChange = { viewModel.onAddressChange(it) },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.city,
                        onValueChange = { viewModel.onCityChange(it) },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.phoneNumber,
                        onValueChange = { viewModel.onPhoneChange(it) },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    uiState.error?.let { errorMessage ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.saveChanges() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .height(20.dp)
                                    .padding(end = 8.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        Text("Save Changes")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditVenueScreenPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Edit Venue") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = "City Sports Hall",
                        onValueChange = {},
                        label = { Text("Venue Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
