package com.example.sportsbook.ui.screens.player.match

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.AvailablePlayer
import com.example.sportsbook.ui.theme.BorderGray
import com.example.sportsbook.ui.theme.LightBg
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.SportGreen
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailablePlayersScreen(
    matchId: Long? = null,
    matchSportType: String? = null,
    matchMinSkill: Int? = null,
    matchMaxSkill: Int? = null,
    onBack: () -> Unit,
    viewModel: AvailablePlayersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.inviteSuccess) {
        uiState.inviteSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearInviteSuccess()
        }
    }

    Scaffold(
        containerColor = NavBarBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Available Players",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBg)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // My Availability toggle section
            item {
                AvailabilityToggleCard(
                    uiState = uiState,
                    onRegister = { viewModel.register() },
                    onUnregister = { sportType -> viewModel.unregister(sportType) },
                    onSkillLevelChange = { viewModel.updateSkillLevel(it) },
                    onNoteChange = { viewModel.updateNote(it) }
                )
            }

            // Sport filter chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(SportType.entries.toList()) { sport ->
                        FilterChip(
                            selected = uiState.selectedSport == sport,
                            onClick = { viewModel.selectSport(sport) },
                            label = {
                                Text(
                                    text = sport.displayName,
                                    color = if (uiState.selectedSport == sport) NavBarBg else TextPrimary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                containerColor = BorderGray
                            )
                        )
                    }
                }
            }

            // Match requirements banner (when navigating from a match)
            if (uiState.hasMatchRequirements) {
                item {
                    MatchRequirementsBanner(
                        minSkill = uiState.matchMinSkill,
                        maxSkill = uiState.matchMaxSkill,
                        isApplied = uiState.skillFilterEnabled &&
                                uiState.filterMinSkill == (uiState.matchMinSkill ?: 1) &&
                                uiState.filterMaxSkill == (uiState.matchMaxSkill ?: 5),
                        onApply = { viewModel.applyMatchRequirements() },
                        onClear = { viewModel.clearSkillFilter() }
                    )
                }
            }

            // Skill filter controls
            item {
                SkillFilterCard(
                    isEnabled = uiState.skillFilterEnabled,
                    minSkill = uiState.filterMinSkill,
                    maxSkill = uiState.filterMaxSkill,
                    onToggle = { viewModel.toggleSkillFilter(it) },
                    onMinChange = { viewModel.updateFilterMinSkill(it) },
                    onMaxChange = { viewModel.updateFilterMaxSkill(it) }
                )
            }

            // Results count
            if (!uiState.isLoading && uiState.availablePlayers.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.availablePlayers.size} player${if (uiState.availablePlayers.size != 1) "s" else ""} found",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary.copy(alpha = 0.7f)
                        )
                        if (uiState.skillFilterEnabled) {
                            Text(
                                text = "Skill ${uiState.filterMinSkill}–${uiState.filterMaxSkill}",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldAccent
                            )
                        }
                    }
                }
            }

            // Players list
            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GoldAccent)
                        }
                    }
                }
                uiState.availablePlayers.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TextPrimary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (uiState.skillFilterEnabled)
                                        "No players match skill level ${uiState.filterMinSkill}–${uiState.filterMaxSkill}"
                                    else
                                        "No players available for this sport yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                                if (uiState.skillFilterEnabled) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = { viewModel.clearSkillFilter() }) {
                                        Text("Clear Skill Filter", color = GoldAccent)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    items(uiState.availablePlayers, key = { it.id }) { player ->
                        AvailablePlayerCard(
                            player = player,
                            showInviteButton = matchId != null,
                            matchMinSkill = if (uiState.hasMatchRequirements) uiState.matchMinSkill else null,
                            matchMaxSkill = if (uiState.hasMatchRequirements) uiState.matchMaxSkill else null,
                            onInvite = { viewModel.inviteToMatch(player.userId, player.displayName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchRequirementsBanner(
    minSkill: Int?,
    maxSkill: Int?,
    isApplied: Boolean,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isApplied) SportGreen.copy(alpha = 0.15f) else GoldAccent.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isApplied) SportGreen.copy(alpha = 0.4f) else GoldAccent.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = if (isApplied) SportGreen else GoldAccent,
                modifier = Modifier.size(20.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Match Requirements",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Skill level ${minSkill ?: 1} – ${maxSkill ?: 5}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary.copy(alpha = 0.7f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                        repeat(5) { i ->
                            val starIndex = i + 1
                            val inRange = starIndex >= (minSkill ?: 1) && starIndex <= (maxSkill ?: 5)
                            Icon(
                                imageVector = if (inRange) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (inRange) GoldAccent else TextPrimary.copy(alpha = 0.3f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            if (isApplied) {
                TextButton(onClick = onClear) {
                    Text("Clear", color = TextPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = NavBarBg
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Apply", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SkillFilterCard(
    isEnabled: Boolean,
    minSkill: Int,
    maxSkill: Int,
    onToggle: (Boolean) -> Unit,
    onMinChange: (Int) -> Unit,
    onMaxChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Toggle row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Skill Filter",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NavBarBg,
                        checkedTrackColor = GoldAccent,
                        uncheckedThumbColor = TextPrimary.copy(alpha = 0.6f),
                        uncheckedTrackColor = BorderGray
                    )
                )
            }

            // Skill range selector (visible when enabled)
            AnimatedVisibility(
                visible = isEnabled,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = TextPrimary.copy(alpha = 0.1f))

                    // Min skill
                    Text(
                        text = "Minimum Skill",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary.copy(alpha = 0.7f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            val starIndex = index + 1
                            val isSelected = starIndex <= minSkill
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) GoldAccent.copy(alpha = 0.2f)
                                        else BorderGray
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(
                                            1.dp,
                                            GoldAccent.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        else Modifier
                                    )
                                    .clickable { onMinChange(starIndex) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Skill $starIndex",
                                    tint = if (isSelected) GoldAccent else TextPrimary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$minSkill",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Max skill
                    Text(
                        text = "Maximum Skill",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary.copy(alpha = 0.7f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            val starIndex = index + 1
                            val isSelected = starIndex <= maxSkill
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) GoldAccent.copy(alpha = 0.2f)
                                        else BorderGray
                                    )
                                    .then(
                                        if (isSelected) Modifier.border(
                                            1.dp,
                                            GoldAccent.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        else Modifier
                                    )
                                    .clickable { onMaxChange(starIndex) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Skill $starIndex",
                                    tint = if (isSelected) GoldAccent else TextPrimary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$maxSkill",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Visual range display
                    Text(
                        text = "Showing players with skill level $minSkill – $maxSkill",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailabilityToggleCard(
    uiState: AvailablePlayersUiState,
    onRegister: () -> Unit,
    onUnregister: (String) -> Unit,
    onSkillLevelChange: (Int) -> Unit,
    onNoteChange: (String) -> Unit
) {
    var showRegisterForm by remember { mutableStateOf(false) }

    val currentSportAvailability = uiState.myAvailability.find {
        it.sportType == uiState.selectedSport
    }
    val isRegisteredForCurrentSport = currentSportAvailability != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Your Availability",
                style = MaterialTheme.typography.titleMedium,
                color = GoldAccent,
                fontWeight = FontWeight.Bold
            )

            if (isRegisteredForCurrentSport) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SportGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Listed for ${uiState.selectedSport.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SportGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    OutlinedButton(
                        onClick = { onUnregister(uiState.selectedSport.name.lowercase()) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.4f))
                    ) {
                        Text("Remove")
                    }
                }
            } else {
                if (!showRegisterForm) {
                    Button(
                        onClick = { showRegisterForm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = NavBarBg
                        )
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("I'm Available!", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Skill level selector
                    Text(
                        text = "Skill Level",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary.copy(alpha = 0.7f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) { index ->
                            val starIndex = index + 1
                            IconButton(
                                onClick = { onSkillLevelChange(starIndex) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (starIndex <= uiState.registerSkillLevel)
                                        Icons.Default.Star
                                    else
                                        Icons.Default.StarBorder,
                                    contentDescription = "Skill level $starIndex",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${uiState.registerSkillLevel}/5",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary.copy(alpha = 0.7f)
                        )
                    }

                    // Note field
                    OutlinedTextField(
                        value = uiState.registerNote,
                        onValueChange = onNoteChange,
                        label = {
                            Text(
                                "Note (optional)",
                                color = TextPrimary.copy(alpha = 0.6f)
                            )
                        },
                        placeholder = {
                            Text(
                                "e.g. Available for 2 hours, intermediate level",
                                color = TextPrimary.copy(alpha = 0.4f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = TextPrimary.copy(alpha = 0.3f),
                            cursorColor = GoldAccent
                        ),
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRegisterForm = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            border = BorderStroke(1.dp, TextPrimary.copy(alpha = 0.4f))
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                onRegister()
                                showRegisterForm = false
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isRegistering,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = NavBarBg
                            )
                        ) {
                            Text(
                                text = if (uiState.isRegistering) "Registering..." else "Register",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailablePlayerCard(
    player: AvailablePlayer,
    showInviteButton: Boolean,
    matchMinSkill: Int? = null,
    matchMaxSkill: Int? = null,
    onInvite: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine if player meets match requirements
    val meetsRequirements = if (matchMinSkill != null || matchMaxSkill != null) {
        val skill = player.skillLevel ?: 0
        val minOk = matchMinSkill == null || skill >= matchMinSkill
        val maxOk = matchMaxSkill == null || skill <= matchMaxSkill
        minOk && maxOk
    } else {
        null // No requirements set
    }

    val borderColor = when (meetsRequirements) {
        true -> SportGreen.copy(alpha = 0.6f)
        false -> MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
        null -> BorderGray
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BorderGray),
        shape = RoundedCornerShape(12.dp),
        border = if (meetsRequirements != null) BorderStroke(1.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            if (player.photoUrl != null) {
                AsyncImage(
                    model = player.photoUrl,
                    contentDescription = player.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(LightBg),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = player.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = player.displayName ?: "Anonymous",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    // Match compatibility badge
                    if (meetsRequirements == true) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Meets requirements",
                            tint = SportGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Skill level stars
                if (player.skillLevel != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        repeat(5) { index ->
                            val inMatchRange = if (matchMinSkill != null || matchMaxSkill != null) {
                                (index + 1) >= (matchMinSkill ?: 1) && (index + 1) <= (matchMaxSkill ?: 5)
                            } else true

                            Icon(
                                imageVector = if (index < player.skillLevel)
                                    Icons.Default.Star
                                else
                                    Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (index < player.skillLevel) {
                                    if (inMatchRange || matchMinSkill == null) GoldAccent
                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                } else {
                                    TextPrimary.copy(alpha = 0.2f)
                                },
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${player.skillLevel}/5",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary.copy(alpha = 0.5f)
                        )
                    }
                }

                // Note
                if (!player.note.isNullOrBlank()) {
                    Text(
                        text = player.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Invite button (only shown when navigating from a match)
            if (showInviteButton) {
                IconButton(
                    onClick = onInvite,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GoldAccent)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Invite to match",
                        tint = NavBarBg,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun AvailablePlayersScreenPreview() {
    val previewState = AvailablePlayersUiState(
        isLoading = false,
        selectedSport = SportType.BASKETBALL,
        availablePlayers = listOf(
            AvailablePlayer(
                id = 1L,
                userId = 10L,
                sportType = SportType.BASKETBALL,
                skillLevel = 3,
                note = "Looking for a pickup game",
                latitude = null,
                longitude = null,
                availableUntil = null,
                createdAt = "2026-03-20T10:00:00Z",
                displayName = "Alex Rivera",
                photoUrl = null
            ),
            AvailablePlayer(
                id = 2L,
                userId = 11L,
                sportType = SportType.BASKETBALL,
                skillLevel = 4,
                note = null,
                latitude = null,
                longitude = null,
                availableUntil = null,
                createdAt = "2026-03-20T11:00:00Z",
                displayName = "Sam Torres",
                photoUrl = null
            )
        )
    )
    Scaffold(
        containerColor = NavBarBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Available Players",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightBg)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AvailabilityToggleCard(
                    uiState = previewState,
                    onRegister = {},
                    onUnregister = {},
                    onSkillLevelChange = {},
                    onNoteChange = {}
                )
            }
            items(previewState.availablePlayers, key = { it.id }) { player ->
                AvailablePlayerCard(
                    player = player,
                    showInviteButton = false,
                    onInvite = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF132A44)
@Composable
private fun AvailablePlayerCardPreview() {
    AvailablePlayerCard(
        player = AvailablePlayer(
            id = 1L,
            userId = 42L,
            sportType = SportType.BASKETBALL,
            skillLevel = 3,
            note = "Looking for a pickup game this afternoon",
            latitude = null,
            longitude = null,
            availableUntil = null,
            createdAt = "2026-03-18T10:00:00Z",
            displayName = "Alex Rivera",
            photoUrl = null
        ),
        showInviteButton = true,
        matchMinSkill = 2,
        matchMaxSkill = 4,
        onInvite = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF132A44)
@Composable
private fun SkillFilterCardPreview() {
    SkillFilterCard(
        isEnabled = true,
        minSkill = 2,
        maxSkill = 4,
        onToggle = {},
        onMinChange = {},
        onMaxChange = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1628)
@Composable
private fun MatchRequirementsBannerPreview() {
    MatchRequirementsBanner(
        minSkill = 2,
        maxSkill = 5,
        isApplied = true,
        onApply = {},
        onClear = {}
    )
}
