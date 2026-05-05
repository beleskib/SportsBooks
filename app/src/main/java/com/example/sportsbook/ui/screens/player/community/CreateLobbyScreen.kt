package com.example.sportsbook.ui.screens.player.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.ui.theme.LightBg
import com.example.sportsbook.ui.theme.CardWhite
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLobbyScreen(
    onLobbyCreated: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateLobbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdLobbyId) {
        uiState.createdLobbyId?.let { onLobbyCreated(it) }
    }

    Scaffold(
        containerColor = NavBarBg,
        topBar = {
            TopAppBar(
                title = { Text("Create Lobby", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = TextPrimary.copy(alpha = 0.3f),
                focusedLabelColor = GoldAccent,
                unfocusedLabelColor = TextPrimary.copy(alpha = 0.6f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = GoldAccent
            )

            // Title
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Lobby Title *") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                singleLine = true
            )

            // Sport Type
            var sportExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = sportExpanded,
                onExpandedChange = { sportExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.sportType.replace("_", " ")
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                        .ifBlank { "Select sport *" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sport Type *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sportExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = fieldColors
                )
                ExposedDropdownMenu(
                    expanded = sportExpanded,
                    onDismissRequest = { sportExpanded = false },
                    containerColor = LightBg
                ) {
                    SportType.entries.forEach { sport ->
                        DropdownMenuItem(
                            text = { Text(sport.displayName, color = TextPrimary) },
                            onClick = {
                                viewModel.onSportTypeChange(sport.name.lowercase())
                                sportExpanded = false
                            }
                        )
                    }
                }
            }

            // Date
            OutlinedTextField(
                value = uiState.scheduledDate,
                onValueChange = viewModel::onDateChange,
                label = { Text("Date (YYYY-MM-DD) *") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                singleLine = true
            )

            // Time
            OutlinedTextField(
                value = uiState.scheduledTime,
                onValueChange = viewModel::onTimeChange,
                label = { Text("Time (HH:MM) *") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                singleLine = true
            )

            // Duration
            OutlinedTextField(
                value = uiState.durationMinutes.toString(),
                onValueChange = { value -> value.toIntOrNull()?.let { viewModel.onDurationChange(it) } },
                label = { Text("Duration (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Max Players
            OutlinedTextField(
                value = uiState.maxPlayers.toString(),
                onValueChange = { value -> value.toIntOrNull()?.let { viewModel.onMaxPlayersChange(it) } },
                label = { Text("Max Players") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Skill range
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.skillLevelMin.toString(),
                    onValueChange = { value -> value.toIntOrNull()?.coerceIn(1, 5)?.let { viewModel.onSkillMinChange(it) } },
                    label = { Text("Skill Min") },
                    modifier = Modifier.weight(1f),
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.skillLevelMax.toString(),
                    onValueChange = { value -> value.toIntOrNull()?.coerceIn(1, 5)?.let { viewModel.onSkillMaxChange(it) } },
                    label = { Text("Skill Max") },
                    modifier = Modifier.weight(1f),
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                minLines = 2,
                maxLines = 4
            )

            // Is Public
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Public Lobby", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                    Text(
                        "Visible to all players",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary.copy(alpha = 0.5f)
                    )
                }
                Switch(
                    checked = uiState.isPublic,
                    onCheckedChange = viewModel::onIsPublicChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NavBarBg,
                        checkedTrackColor = GoldAccent
                    )
                )
            }

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = viewModel::create,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isValid && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = NavBarBg, modifier = Modifier.height(20.dp))
                } else {
                    Text("Create Lobby", color = NavBarBg, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun CreateLobbyScreenPreview() {
    Scaffold(
        containerColor = NavBarBg,
        topBar = {
            TopAppBar(
                title = { Text("Create Lobby", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text("Form preview", color = TextPrimary, modifier = Modifier.align(Alignment.Center))
        }
    }
}
