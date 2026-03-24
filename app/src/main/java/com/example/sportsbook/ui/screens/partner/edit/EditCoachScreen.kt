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
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.repository.CoachRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCoachViewModel @Inject constructor(
    private val coachRepository: CoachRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val coachId: Long = checkNotNull(savedStateHandle["coachId"])

    data class UiState(
        val coachId: Long = 0,
        val name: String = "",
        val bio: String = "",
        val sportType: SportType = SportType.BASKETBALL,
        val specialization: String = "",
        val experienceYears: String = "",
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
        loadCoach()
    }

    private fun loadCoach() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            coachRepository.getCoachById(coachId)
                .onSuccess { coach ->
                    _uiState.update {
                        it.copy(
                            coachId = coach.id,
                            name = coach.name,
                            bio = coach.bio.orEmpty(),
                            sportType = coach.sportType,
                            specialization = coach.specialization.orEmpty(),
                            experienceYears = coach.experienceYears.toString(),
                            pricePerHour = coach.pricePerHour.toString(),
                            address = coach.address.orEmpty(),
                            city = coach.city.orEmpty(),
                            phoneNumber = coach.phoneNumber.orEmpty(),
                            email = coach.email.orEmpty(),
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
    fun onBioChange(value: String) = _uiState.update { it.copy(bio = value) }
    fun onSportTypeChange(value: SportType) = _uiState.update { it.copy(sportType = value) }
    fun onSpecializationChange(value: String) = _uiState.update { it.copy(specialization = value) }
    fun onExperienceYearsChange(value: String) = _uiState.update { it.copy(experienceYears = value) }
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
        val years = state.experienceYears.toIntOrNull() ?: run {
            _uiState.update { it.copy(error = "Please enter a valid number of experience years") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val updated = Coach(
                id = state.coachId,
                name = state.name,
                bio = state.bio,
                sportType = state.sportType,
                specialization = state.specialization,
                experienceYears = years,
                pricePerHour = price,
                address = state.address,
                city = state.city,
                phoneNumber = state.phoneNumber,
                email = state.email
            )
            coachRepository.updateCoach(updated)
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
fun EditCoachScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditCoachViewModel = hiltViewModel()
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
                title = { Text("Edit Coach Profile") },
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
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.bio,
                        onValueChange = { viewModel.onBioChange(it) },
                        label = { Text("Bio") },
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
                        value = uiState.specialization,
                        onValueChange = { viewModel.onSpecializationChange(it) },
                        label = { Text("Specialization") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.experienceYears,
                        onValueChange = { viewModel.onExperienceYearsChange(it) },
                        label = { Text("Years of Experience") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

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
private fun EditCoachScreenPreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text("Edit Coach Profile") },
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
                        value = "Jane Doe",
                        onValueChange = {},
                        label = { Text("Full Name") },
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
