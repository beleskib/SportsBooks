package com.example.sportsbook.ui.screens.player.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent

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

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // ── Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkBg.copy(alpha = 0.5f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Create Lobby", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenAccent,
                unfocusedBorderColor = DarkBorder,
                focusedLabelColor = GreenAccent,
                unfocusedLabelColor = DarkTextSecondary,
                focusedTextColor = DarkTextPrimary,
                unfocusedTextColor = DarkTextPrimary,
                cursorColor = GreenAccent,
            )

            OutlinedTextField(
                value = uiState.title, onValueChange = viewModel::onTitleChange,
                label = { Text("Lobby Title *") }, modifier = Modifier.fillMaxWidth(),
                colors = fieldColors, singleLine = true,
            )

            var sportExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = sportExpanded, onExpandedChange = { sportExpanded = it }) {
                OutlinedTextField(
                    value = uiState.sportType.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() }.ifBlank { "Select sport *" },
                    onValueChange = {}, readOnly = true,
                    label = { Text("Sport Type *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sportExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = fieldColors,
                )
                ExposedDropdownMenu(expanded = sportExpanded, onDismissRequest = { sportExpanded = false }, containerColor = DarkBg) {
                    SportType.entries.forEach { sport ->
                        DropdownMenuItem(
                            text = { Text(sport.displayName, color = DarkTextPrimary) },
                            onClick = { viewModel.onSportTypeChange(sport.name.lowercase()); sportExpanded = false },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.scheduledDate, onValueChange = viewModel::onDateChange,
                label = { Text("Date (YYYY-MM-DD) *") }, modifier = Modifier.fillMaxWidth(),
                colors = fieldColors, singleLine = true,
            )

            OutlinedTextField(
                value = uiState.scheduledTime, onValueChange = viewModel::onTimeChange,
                label = { Text("Time (HH:MM) *") }, modifier = Modifier.fillMaxWidth(),
                colors = fieldColors, singleLine = true,
            )

            OutlinedTextField(
                value = uiState.durationMinutes.toString(),
                onValueChange = { value -> value.toIntOrNull()?.let { viewModel.onDurationChange(it) } },
                label = { Text("Duration (minutes)") }, modifier = Modifier.fillMaxWidth(),
                colors = fieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
            )

            OutlinedTextField(
                value = uiState.maxPlayers.toString(),
                onValueChange = { value -> value.toIntOrNull()?.let { viewModel.onMaxPlayersChange(it) } },
                label = { Text("Max Players") }, modifier = Modifier.fillMaxWidth(),
                colors = fieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.skillLevelMin.toString(),
                    onValueChange = { value -> value.toIntOrNull()?.coerceIn(1, 5)?.let { viewModel.onSkillMinChange(it) } },
                    label = { Text("Skill Min") }, modifier = Modifier.weight(1f),
                    colors = fieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.skillLevelMax.toString(),
                    onValueChange = { value -> value.toIntOrNull()?.coerceIn(1, 5)?.let { viewModel.onSkillMaxChange(it) } },
                    label = { Text("Skill Max") }, modifier = Modifier.weight(1f),
                    colors = fieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                )
            }

            OutlinedTextField(
                value = uiState.description, onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth(),
                colors = fieldColors, minLines = 2, maxLines = 4,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("Public Lobby", fontSize = 15.sp, color = DarkTextPrimary)
                    Text("Visible to all players", fontSize = 12.sp, color = DarkTextPrimary.copy(alpha = 0.5f))
                }
                Switch(
                    checked = uiState.isPublic,
                    onCheckedChange = viewModel::onIsPublicChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = DarkBg, checkedTrackColor = GreenAccent),
                )
            }

            if (uiState.error != null) {
                Text(text = uiState.error!!, color = Color(0xFFEF5350), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            val canCreate = uiState.isValid && !uiState.isLoading
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (canCreate) GreenAccent else GreenAccent.copy(alpha = 0.4f))
                    .then(if (canCreate) Modifier.clickable(onClick = viewModel::create) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create Lobby", color = DarkBg, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CreateLobbyScreenPreview() {
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(DarkSurface).padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkBg.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Create Lobby", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Form preview", color = DarkTextSecondary, modifier = Modifier.align(Alignment.Center))
        }
    }
}
