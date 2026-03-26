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
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.ui.theme.Navy700
import com.example.sportsbook.ui.theme.Navy800
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCommunityScreen(
    onCommunityCreated: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: CreateCommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdCommunityId) {
        uiState.createdCommunityId?.let { onCommunityCreated(it) }
    }

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = { Text("Create Community", color = WarmWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WarmWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = USOpenGold,
                unfocusedBorderColor = WarmWhite.copy(alpha = 0.3f),
                focusedLabelColor = USOpenGold,
                unfocusedLabelColor = WarmWhite.copy(alpha = 0.6f),
                focusedTextColor = WarmWhite,
                unfocusedTextColor = WarmWhite,
                cursorColor = USOpenGold
            )

            // Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Community Name *") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                singleLine = true
            )

            // Description
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                minLines = 3,
                maxLines = 5
            )

            // Sport Type Dropdown
            var sportDropdownExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = sportDropdownExpanded,
                onExpandedChange = { sportDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.sportType.replace("_", " ")
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                        .ifBlank { "Select sport (optional)" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sport Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sportDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = fieldColors
                )
                ExposedDropdownMenu(
                    expanded = sportDropdownExpanded,
                    onDismissRequest = { sportDropdownExpanded = false },
                    containerColor = Navy700
                ) {
                    DropdownMenuItem(
                        text = { Text("None", color = WarmWhite.copy(alpha = 0.6f)) },
                        onClick = {
                            viewModel.onSportTypeChange("")
                            sportDropdownExpanded = false
                        }
                    )
                    SportType.entries.forEach { sport ->
                        DropdownMenuItem(
                            text = { Text(sport.displayName, color = WarmWhite) },
                            onClick = {
                                viewModel.onSportTypeChange(sport.name.lowercase())
                                sportDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Max Members
            OutlinedTextField(
                value = uiState.maxMembers.toString(),
                onValueChange = { value ->
                    value.toIntOrNull()?.let { viewModel.onMaxMembersChange(it) }
                },
                label = { Text("Max Members") },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Invite Policy
            Text(
                text = "Invite Policy",
                style = MaterialTheme.typography.titleSmall,
                color = WarmWhite.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            listOf("friends_only" to "Friends Only", "invite_only" to "Invite Only", "open" to "Open to All")
                .forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = uiState.invitePolicy == value,
                                onClick = { viewModel.onInvitePolicyChange(value) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = uiState.invitePolicy == value,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = USOpenGold,
                                unselectedColor = WarmWhite.copy(alpha = 0.5f)
                            )
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmWhite,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

            // Is Public toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Public Community",
                        style = MaterialTheme.typography.bodyLarge,
                        color = WarmWhite
                    )
                    Text(
                        text = "Visible to everyone in Discover",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmWhite.copy(alpha = 0.5f)
                    )
                }
                Switch(
                    checked = uiState.isPublic,
                    onCheckedChange = viewModel::onIsPublicChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Navy900,
                        checkedTrackColor = USOpenGold
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

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::create,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isValid && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = USOpenGold)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Navy900,
                        modifier = Modifier.height(20.dp)
                    )
                } else {
                    Text("Create Community", color = Navy900, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun CreateCommunityScreenPreview() {
    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = { Text("Create Community", color = WarmWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text("Form preview", color = WarmWhite, modifier = Modifier.align(Alignment.Center))
        }
    }
}
