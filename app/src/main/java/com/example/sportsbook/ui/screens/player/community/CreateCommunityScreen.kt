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
import com.example.sportsbook.ui.theme.BorderGray
import com.example.sportsbook.ui.theme.CardWhite
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.LightBg
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.TextPrimary
import com.example.sportsbook.ui.theme.TextSecondary
import com.example.sportsbook.ui.theme.TextTertiary

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
        containerColor = LightBg,
        topBar = {
            TopAppBar(
                title = { Text("Create Community", color = GoldAccent) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavBarBg)
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
                focusedBorderColor = GoldAccent,
                unfocusedBorderColor = BorderGray,
                focusedLabelColor = GoldAccent,
                unfocusedLabelColor = TextTertiary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = GoldAccent
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
                    containerColor = CardWhite
                ) {
                    DropdownMenuItem(
                        text = { Text("None", color = TextTertiary) },
                        onClick = {
                            viewModel.onSportTypeChange("")
                            sportDropdownExpanded = false
                        }
                    )
                    SportType.entries.forEach { sport ->
                        DropdownMenuItem(
                            text = { Text(sport.displayName, color = TextPrimary) },
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
                color = TextSecondary,
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
                                selectedColor = GoldAccent,
                                unselectedColor = TextTertiary
                            )
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
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
                        color = TextPrimary
                    )
                    Text(
                        text = "Visible to everyone in Discover",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
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

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::create,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isValid && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = NavBarBg,
                        modifier = Modifier.height(20.dp)
                    )
                } else {
                    Text("Create Community", color = NavBarBg, fontWeight = FontWeight.SemiBold)
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
        containerColor = LightBg,
        topBar = {
            TopAppBar(
                title = { Text("Create Community", color = GoldAccent) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavBarBg)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text("Form preview", color = TextPrimary, modifier = Modifier.align(Alignment.Center))
        }
    }
}
