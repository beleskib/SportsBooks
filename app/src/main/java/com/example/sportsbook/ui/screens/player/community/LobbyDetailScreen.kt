package com.example.sportsbook.ui.screens.player.community

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Lobby
import com.example.sportsbook.domain.model.LobbyParticipant
import com.example.sportsbook.ui.theme.CoralRed
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.SportGreen

@Composable
fun LobbyDetailScreen(
    onBack: () -> Unit,
    onBrowseAvailablePlayers: () -> Unit = {},
    viewModel: LobbyDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lobby = uiState.lobby

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
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = lobby?.title ?: "Lobby",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }

            lobby == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error ?: "Lobby not found",
                        color = Color(0xFFEF5350),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    item { LobbyHeaderSection(lobby = lobby) }
                    item { PlayerCountSection(currentPlayers = lobby.currentPlayers, maxPlayers = lobby.maxPlayers) }
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Participants",
                            fontSize = 13.sp,
                            color = DarkTextPrimary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    if (lobby.participants.isEmpty()) {
                        item {
                            Text(
                                text = "No participants yet — be the first!",
                                fontSize = 14.sp,
                                color = DarkTextPrimary.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    } else {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                items(lobby.participants) { participant ->
                                    ParticipantAvatar(participant = participant)
                                }
                            }
                        }
                    }
                    if (uiState.isUserCreator && !lobby.isFull && !lobby.isPublic) {
                        item {
                            MissingPlayersPrompt(
                                onMakePublic = viewModel::makeLobbyPublic,
                                onBrowsePlayers = onBrowseAvailablePlayers,
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        LobbyActionButtons(
                            lobby = lobby,
                            isParticipant = uiState.isUserParticipant,
                            isCreator = uiState.isUserCreator,
                            onJoin = viewModel::joinLobby,
                            onLeave = viewModel::leaveLobby,
                            onMakePublic = viewModel::makeLobbyPublic,
                        )
                    }
                    if (uiState.error != null) {
                        item {
                            Text(
                                text = uiState.error!!,
                                color = Color(0xFFEF5350),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(16.dp),
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
            .background(DarkSurface)
            .padding(16.dp)
    ) {
        SportBadge(sportType = lobby.sportType)
        Spacer(modifier = Modifier.height(8.dp))

        InfoRow(
            icon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(16.dp)) },
            text = "${lobby.scheduledDate}  ${lobby.scheduledTime}  (${lobby.durationMinutes} min)"
        )

        if (!lobby.venueName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow(
                icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(16.dp)) },
                text = lobby.venueName
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        InfoRow(
            icon = { Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(16.dp)) },
            text = "Skill level ${lobby.skillLevelMin}–${lobby.skillLevelMax}"
        )

        if (!lobby.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = lobby.description, fontSize = 14.sp, color = DarkTextPrimary.copy(alpha = 0.8f))
        }

        if (lobby.isPublic) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Public, contentDescription = null, tint = SportGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Public lobby", fontSize = 11.sp, color = SportGreen)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: @Composable () -> Unit, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, color = DarkTextPrimary.copy(alpha = 0.8f))
    }
}

@Composable
private fun PlayerCountSection(currentPlayers: Int, maxPlayers: Int) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Players", fontSize = 13.sp, color = DarkTextPrimary.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold)
            Text(text = "$currentPlayers / $maxPlayers", fontSize = 13.sp, color = GreenAccent, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        val progress = if (maxPlayers > 0) currentPlayers.toFloat() / maxPlayers else 0f
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DarkBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(GreenAccent)
            )
        }
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
            fontSize = 11.sp,
            color = DarkTextPrimary.copy(alpha = 0.7f),
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
            .background(DarkBg)
            .padding(14.dp)
    ) {
        Text(text = "Still need more players?", fontSize = 13.sp, color = DarkTextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Make the lobby public to attract more players, or browse available players directly.",
            fontSize = 12.sp,
            color = DarkTextPrimary.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onMakePublic),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = DarkBg, modifier = Modifier.size(16.dp))
                    Text("Make Public", color = DarkBg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                    .clickable(onClick = onBrowsePlayers),
                contentAlignment = Alignment.Center,
            ) {
                Text("Browse Players", color = DarkTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, GreenAccent, RoundedCornerShape(12.dp))
                    .clickable(onClick = onMakePublic),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Public, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(18.dp))
                    Text("Make Lobby Public", color = GreenAccent, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }

        if (!isCreator) {
            if (isParticipant) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, CoralRed, RoundedCornerShape(12.dp))
                        .clickable(onClick = onLeave),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Leave Lobby", color = CoralRed, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            } else if (lobby.isOpen && !lobby.isFull) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenAccent)
                        .clickable(onClick = onJoin),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Join Lobby", color = DarkBg, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            } else if (lobby.isFull) {
                Text(
                    text = "Lobby is full",
                    fontSize = 14.sp,
                    color = DarkTextPrimary.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
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
            LobbyParticipant(3, 1, 3, "Charlie"),
        ),
    )
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(DarkSurface).padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkBg.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(lobby.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { LobbyHeaderSection(lobby = lobby) }
            item { PlayerCountSection(currentPlayers = lobby.currentPlayers, maxPlayers = lobby.maxPlayers) }
        }
    }
}
