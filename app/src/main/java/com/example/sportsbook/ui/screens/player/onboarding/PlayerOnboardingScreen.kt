package com.example.sportsbook.ui.screens.player.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.ExperienceDuration
import com.example.sportsbook.domain.enums.SkillLevel
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.ui.common.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerOnboardingScreen(
    onComplete: () -> Unit,
    viewModel: PlayerOnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onPhotoSelected(it) }
    }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onComplete()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        viewModel.onDateOfBirthChange(sdf.format(Date(millis)))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Heading
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Complete Your Profile",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tell us about yourself so we can match you with the right players",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Profile Photo
                item {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            uiState.isUploadingPhoto -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            uiState.uploadedPhotoUrl != null -> {
                                AsyncImage(
                                    model = uiState.uploadedPhotoUrl,
                                    contentDescription = "Profile photo",
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            uiState.photoUri != null -> {
                                AsyncImage(
                                    model = uiState.photoUri,
                                    contentDescription = "Profile photo",
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Add photo",
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Add Photo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Name
                item {
                    OutlinedTextField(
                        value = uiState.displayName,
                        onValueChange = viewModel::onDisplayNameChange,
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Email (read-only)
                item {
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = {},
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Date of Birth
                item {
                    OutlinedTextField(
                        value = uiState.dateOfBirth?.let { formatDateForDisplay(it) } ?: "",
                        onValueChange = {},
                        label = { Text("Date of Birth") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        singleLine = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Interested Sports
                item {
                    Text(
                        text = "What sports are you interested in?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SportType.entries.forEach { sport ->
                            FilterChip(
                                selected = sport in uiState.selectedSports,
                                onClick = { viewModel.toggleSport(sport) },
                                label = { Text(sport.displayName) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sport Expertise for each selected sport
                if (uiState.selectedSports.isNotEmpty()) {
                    item {
                        Text(
                            text = "Set your expertise",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Match with players at your level",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(uiState.selectedSports) { sport ->
                        val expertise = uiState.sportExpertiseMap[sport] ?: SportExpertiseEntry()

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = sport.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Skill Level
                            Text(
                                text = "Skill Level",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SkillLevel.entries.forEach { level ->
                                    FilterChip(
                                        selected = expertise.skillLevel == level,
                                        onClick = { viewModel.onSkillLevelChange(sport, level) },
                                        label = { Text(level.displayName) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Experience Duration
                            Text(
                                text = "Experience",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ExperienceDuration.entries.forEach { duration ->
                                    FilterChip(
                                        selected = expertise.experienceDuration == duration,
                                        onClick = { viewModel.onExperienceDurationChange(sport, duration) },
                                        label = { Text(duration.displayName) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Bio
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.bio,
                        onValueChange = viewModel::onBioChange,
                        label = { Text("Tell us about yourself") },
                        placeholder = { Text("Optional — share your sports journey, goals, or anything you'd like others to know") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        supportingText = {
                            Text("${uiState.bio.length}/500")
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Submit Button
                item {
                    Button(
                        onClick = viewModel::submit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = uiState.displayName.isNotBlank()
                                && uiState.selectedSports.isNotEmpty()
                                && !uiState.isSaving
                                && !uiState.isUploadingPhoto
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Get Started",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private fun formatDateForDisplay(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val date = inputFormat.parse(isoDate)
        date?.let { outputFormat.format(it) } ?: isoDate
    } catch (e: Exception) {
        isoDate
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayerOnboardingScreenPreview() {
    MaterialTheme {
        PlayerOnboardingScreen(onComplete = {})
    }
}
