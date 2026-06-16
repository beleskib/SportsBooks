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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.ExperienceDuration
import com.example.sportsbook.domain.enums.SkillLevel
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
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
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tell us about yourself so we can match you with the right players",
                        fontSize = 14.sp,
                        color = DarkTextSecondary,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Profile Photo
                item {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
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
                                    color = GreenAccent
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
                                        tint = GreenAccent
                                    )
                                    Text(
                                        text = "Add Photo",
                                        fontSize = 10.sp,
                                        color = GreenAccent,
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
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary,
                            focusedBorderColor = GreenAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = GreenAccent,
                            unfocusedLabelColor = DarkTextSecondary,
                            cursorColor = GreenAccent,
                        )
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
                            disabledTextColor = DarkTextPrimary,
                            disabledBorderColor = DarkBorder,
                            disabledLabelColor = DarkTextSecondary,
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
                            disabledTextColor = DarkTextPrimary,
                            disabledBorderColor = DarkBorder,
                            disabledLabelColor = DarkTextSecondary,
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Interested Sports
                item {
                    Text(
                        text = "What sports are you interested in?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextPrimary,
                        modifier = Modifier.fillMaxWidth(),
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
                            val selected = sport in uiState.selectedSports
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) GreenAccent else DarkSurface)
                                    .clickable { viewModel.toggleSport(sport) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = sport.displayName,
                                    fontSize = 13.sp,
                                    color = if (selected) Color.White else DarkTextSecondary,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sport Expertise for each selected sport
                if (uiState.selectedSports.isNotEmpty()) {
                    item {
                        Text(
                            text = "Set your expertise",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkTextPrimary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Match with players at your level",
                            fontSize = 12.sp,
                            color = DarkTextSecondary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(uiState.selectedSports) { sport ->
                        val expertise = uiState.sportExpertiseMap[sport] ?: SportExpertiseEntry()

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = sport.displayName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = GreenAccent,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Skill Level
                            Text(
                                text = "Skill Level",
                                fontSize = 12.sp,
                                color = DarkTextSecondary,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SkillLevel.entries.forEach { level ->
                                    val selected = expertise.skillLevel == level
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (selected) GreenAccent else DarkSurface)
                                            .clickable { viewModel.onSkillLevelChange(sport, level) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                    ) {
                                        Text(
                                            text = level.displayName,
                                            fontSize = 13.sp,
                                            color = if (selected) Color.White else DarkTextSecondary,
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Experience Duration
                            Text(
                                text = "Experience",
                                fontSize = 12.sp,
                                color = DarkTextSecondary,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ExperienceDuration.entries.forEach { duration ->
                                    val selected = expertise.experienceDuration == duration
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (selected) GreenAccent else DarkSurface)
                                            .clickable { viewModel.onExperienceDurationChange(sport, duration) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                    ) {
                                        Text(
                                            text = duration.displayName,
                                            fontSize = 13.sp,
                                            color = if (selected) Color.White else DarkTextSecondary,
                                        )
                                    }
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
                        placeholder = { Text("Optional — share your sports journey, goals, or anything you'd like others to know", color = DarkTextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        supportingText = {
                            Text("${uiState.bio.length}/500", color = DarkTextSecondary)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DarkTextPrimary,
                            unfocusedTextColor = DarkTextPrimary,
                            focusedBorderColor = GreenAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedLabelColor = GreenAccent,
                            unfocusedLabelColor = DarkTextSecondary,
                            cursorColor = GreenAccent,
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Submit Button
                item {
                    val canSubmit = uiState.displayName.isNotBlank()
                            && uiState.selectedSports.isNotEmpty()
                            && !uiState.isSaving
                            && !uiState.isUploadingPhoto
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (canSubmit) GreenAccent else Color(0xFF2A3D2B))
                            .clickable(enabled = canSubmit, onClick = viewModel::submit),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Get Started",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
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

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PlayerOnboardingScreenPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Complete Your Profile",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tell us about yourself so we can match you with the right players",
                    fontSize = 14.sp,
                    color = DarkTextSecondary,
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Add photo",
                            modifier = Modifier.size(32.dp),
                            tint = GreenAccent
                        )
                        Text(
                            text = "Add Photo",
                            fontSize = 10.sp,
                            color = GreenAccent,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                OutlinedTextField(
                    value = "Alex Johnson",
                    onValueChange = {},
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary,
                        focusedBorderColor = GreenAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = GreenAccent,
                        unfocusedLabelColor = DarkTextSecondary,
                        cursorColor = GreenAccent,
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                OutlinedTextField(
                    value = "alex.johnson@email.com",
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = DarkTextPrimary,
                        disabledBorderColor = DarkBorder,
                        disabledLabelColor = DarkTextSecondary,
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                Text(
                    text = "What sports are you interested in?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTextPrimary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(SportType.BASKETBALL, SportType.TENNIS, SportType.FOOTBALL, SportType.PADDLE).forEach { sport ->
                        val selected = sport == SportType.BASKETBALL || sport == SportType.TENNIS
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) GreenAccent else DarkSurface)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = sport.displayName,
                                fontSize = 13.sp,
                                color = if (selected) Color.White else DarkTextSecondary,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                OutlinedTextField(
                    value = "Passionate about sports and always looking to improve.",
                    onValueChange = {},
                    label = { Text("Tell us about yourself") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkTextPrimary,
                        unfocusedTextColor = DarkTextPrimary,
                        focusedBorderColor = GreenAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = GreenAccent,
                        unfocusedLabelColor = DarkTextSecondary,
                        cursorColor = GreenAccent,
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(GreenAccent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
