package com.example.sportsbook.ui.screens.player.community

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Lobby
import com.example.sportsbook.domain.model.LobbyParticipant
import com.example.sportsbook.ui.theme.CoralRed
import com.example.sportsbook.ui.theme.LightBg
import com.example.sportsbook.ui.theme.CardWhite
import com.example.sportsbook.ui.theme.NavBarBg
import com.example.sportsbook.ui.theme.SportGreen
import com.example.sportsbook.ui.theme.GoldAccent
import com.example.sportsbook.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyDetailScreen(
    onBack: () -> Unit,
    onBrowseAvailablePlayers: () -> Unit = {},
    viewModel: LobbyDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lobby = uiState.lobby

    Scaffold(
        containerColor = NavBarBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = lobby?.title ?: "Lobby",
                        color = TextPrimary
                    )
                },
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
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldAccent)
                }
            }

            lobby == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error ?: "Lobby not found",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        LobbyHeaderSection(lobby = lobby)
                    }

                    item {
                        PlayerCountSection(
                            currentPlayers = lobby.currentPlayers,
                            maxPlayers = lobby.maxPlayers
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Participants",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    if (lobby.participants.isEmpty()) {
                        item {
                            Text(
                                text = "No participants yet — be the first!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(lobby.participants) { participant ->
                                    ParticipantAvatar(participant = participant)
                                }
                            }
                        }
                    }

                    // Missing players prompt for creator
                    if (uiState.isUserCreator && !lobby.isFull && !lobby.isPublic) {
                        item {
                            MissingPlayersPrompt(
                                onMakePublic = viewModel::makeLobbyPublic,
                                onBrowsePlayers = onBrowseAvailablePlayers
                            )
                        }
                    }

                    // Action buttons
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        LobbyActionButtons(
                            lobby = lobby,
                            isParticipant = uiState.isUserParticipant,
                            isCreator = uiState.isUserCreator,
                            onJoin = viewModel::joinLobby,
                            onLeave = viewModel::leaveLobby,
                            onMakePublic = viewModel::makeLobbyPublic
                        )
                    }

                    if (uiState.error != null) {
                        item {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LobbyHeaderSection(lobby: Lobby) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(16.dp)
    ) {
        SportBadge(sportType = lobby.sportType)
        Spacer(modifier = Modifier.height(8.dp))

        InfoRow(
            icon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp)) },
            text = "${lobby.scheduledDate}  ${lobby.scheduledTime}  (${lobby.durationMinutes} min)"
        )

        if (!lobby.venueName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow(
                icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp)) },
                text = lobby.venueName
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        InfoRow(
            icon = { Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp)) },
            text = "Skill level ${lobby.skillLevelMin}–${lobby.skillLevelMax}"
        )

        if (!lobby.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lobby.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary.copy(alpha = 0.8f)
            )
        }

        if (lobby.isPublic) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Public, contentDescription = null, tint = SportGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Public lobby", style = MaterialTheme.typography.labelSmall, color = SportGreen)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: @Composable () -> Unit, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = TextPrimary.copy(alpha = 0.8f))
    }
}

@Composable
private fun PlayerCountSection(currentPlayers: Int, maxPlayers: Int) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Players",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$currentPlayers / $maxPlayers",
                style = MaterialTheme.typography.titleSmall,
                color = GoldAccent,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        val progress = if (maxPlayers > 0) currentPlayers.toFloat() / maxPlayers else 0f
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = GoldAccent,
            trackColor = LightBg
        )
    }
}

@Composable
private fun ParticipantAvatar(participant: LobbyParticipant) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        MemberAvatar(
            name = participant.displayName,
            photoUrl = participant.photoUrl,
            size = 44
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = participant.displayName.split(" ").firstOrNull() ?: participant.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun MissingPlayersPrompt(
    onMakePublic: () -> Unit,
    onBrowsePlayers: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(LightBg)
            .padding(14.dp)
    ) {
        Text(
            text = "Still need more players?",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Make the lobby public to attract more players, or browse available players directly.",
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onMakePublic,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Public, contentDescription = null, tint = NavBarBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Make Public", color = NavBarBg, style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
                onClick = onBrowsePlayers,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Browse Players", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun LobbyActionButtons(
    lobby: Lobby,
    isParticipant: Boolean,
    isCreator: Boolean,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onMakePublic: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isCreator && !lobby.isPublic) {
            OutlinedButton(
                onClick = onMakePublic,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent)
            ) {
                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Make Lobby Public")
            }
        }

        if (!isCreator) {
            if (isParticipant) {
                OutlinedButton(
                    onClick = onLeave,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed)
                ) {
                    Text("Leave Lobby", fontWeight = FontWeight.SemiBold)
                }
            } else if (lobby.isOpen && !lobby.isFull) {
                Button(
                    onClick = onJoin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Join Lobby", color = NavBarBg, fontWeight = FontWeight.SemiBold)
                }
            } else if (lobby.isFull) {
                Text(
                    text = "Lobby is full",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun LobbyDetailScreenPreview() {
    val lobby = Lobby(
        id = 1,
        title = "Friday Night Basketball",
        sportType = "basketball",
        scheduledDate = "2026-03-27",
        scheduledTime = "19:00",
        durationMinutes = 90,
        maxPlayers = 10,
        currentPlayers = 4,
        venueName = "City Sports Center",
        skillLevelMin = 2,
        skillLevelMax = 4,
        description = "Casual 5v5 game, all welcome!",
        participants = listOf(
            LobbyParticipant(1, 1, 1, "Alice"),
            LobbyParticipant(2, 1, 2, "Bob"),
            LobbyParticipant(3, 1, 3, "Charlie")
        )
    )
    Scaffold(
        containerColor = NavBarBg,
        topBar = {
            TopAppBar(
                title = { Text(lobby.title, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item { LobbyHeaderSection(lobby = lobby) }
            item { PlayerCountSection(currentPlayers = lobby.currentPlayers, maxPlayers = lobby.maxPlayers) }
        }
    }
}
