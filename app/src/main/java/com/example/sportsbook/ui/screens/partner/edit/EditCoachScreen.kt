package com.example.sportsbook.ui.screens.partner.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Coach
import com.example.sportsbook.domain.repository.CoachRepository
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
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
    viewModel: EditCoachViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var sportTypeDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Edit Coach Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                item { CoachInputField("Full Name", uiState.name, viewModel::onNameChange, singleLine = true) }
                item { CoachInputField("Bio", uiState.bio, viewModel::onBioChange, singleLine = false, minLines = 3) }
                item { CoachInputField("Price per Hour (MKD)", uiState.pricePerHour, viewModel::onPriceChange, singleLine = true, keyboardType = KeyboardType.Number) }

                item {
                    ExposedDropdownMenuBox(
                        expanded = sportTypeDropdownExpanded,
                        onExpandedChange = { sportTypeDropdownExpanded = it },
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                        ) {
                            Text("Sport Type", fontSize = 12.sp, color = DarkTextSecondary)
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(uiState.sportType.displayName, fontSize = 15.sp, color = DarkTextPrimary, modifier = Modifier.weight(1f))
                                Icon(Icons.Filled.ArrowDropDown, null, tint = DarkTextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = sportTypeDropdownExpanded,
                            onDismissRequest = { sportTypeDropdownExpanded = false },
                        ) {
                            SportType.entries.forEach { sportType ->
                                DropdownMenuItem(
                                    text = { Text(sportType.displayName) },
                                    onClick = {
                                        viewModel.onSportTypeChange(sportType)
                                        sportTypeDropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                item { CoachInputField("Specialization", uiState.specialization, viewModel::onSpecializationChange, singleLine = true) }
                item { CoachInputField("Years of Experience", uiState.experienceYears, viewModel::onExperienceYearsChange, singleLine = true, keyboardType = KeyboardType.Number) }
                item { CoachInputField("Address", uiState.address, viewModel::onAddressChange, singleLine = true) }
                item { CoachInputField("City", uiState.city, viewModel::onCityChange, singleLine = true) }
                item { CoachInputField("Phone Number", uiState.phoneNumber, viewModel::onPhoneChange, singleLine = true, keyboardType = KeyboardType.Phone) }
                item { CoachInputField("Email", uiState.email, viewModel::onEmailChange, singleLine = true, keyboardType = KeyboardType.Email) }

                uiState.error?.let { errorMessage ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFB71C1C).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFEF5350).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage, fontSize = 13.sp, color = Color(0xFFEF9A9A))
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (uiState.isSaving) DarkBorder else GreenAccent)
                            .clickable(enabled = !uiState.isSaving) { viewModel.saveChanges() }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun CoachInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(label, fontSize = 12.sp, color = DarkTextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines,
            textStyle = TextStyle(color = DarkTextPrimary, fontSize = 15.sp),
            cursorBrush = SolidColor(GreenAccent),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(label, fontSize = 15.sp, color = Color(0xFF555555))
                    }
                    inner()
                }
            },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun EditCoachScreenPreview() {
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Edit Coach Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { CoachInputField("Full Name", "Jane Doe", {}, singleLine = true) }
                item { CoachInputField("Bio", "Certified tennis coach with 10 years of experience", {}, singleLine = false, minLines = 3) }
                item { CoachInputField("Price per Hour (MKD)", "2500", {}, singleLine = true, keyboardType = KeyboardType.Number) }
                item {
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(GreenAccent).padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
}
