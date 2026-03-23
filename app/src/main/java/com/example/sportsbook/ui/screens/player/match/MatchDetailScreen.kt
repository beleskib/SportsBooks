package com.example.sportsbook.ui.screens.player.match

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sportsbook.domain.enums.ParticipantStatus
import com.example.sportsbook.ui.common.toDisplayDate
import com.example.sportsbook.ui.screens.player.match.components.MatchStatusBadge
import com.example.sportsbook.ui.screens.player.match.components.ParticipantAvatar
import com.example.sportsbook.ui.screens.player.match.components.SkillRangeBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    onBack: () -> Unit,
    onOpenChat: (Long) -> Unit,
    onRatePlayers: (Long) -> Unit,
    onJoinWithParty: (Long) -> Unit = {},
    onBrowseAvailablePlayers: (matchId: Long, sportType: String, minSkill: Int?, maxSkill: Int?) -> Unit = { _, _, _, _ -> },
    viewModel: MatchDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.match != null -> {
                val match = uiState.match!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title + Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = match.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        MatchStatusBadge(status = match.status)
                    }

                    // Sport + Skill range
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = match.sportType.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (match.minSkillLevel != null && match.maxSkillLevel != null) {
                            SkillRangeBadge(match.minSkillLevel, match.maxSkillLevel)
                        }
                    }

                    // Description
                    if (!match.description.isNullOrBlank()) {
                        Text(
                            text = match.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Details card
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow(Icons.Default.CalendarToday, "Date", match.matchDate.toDisplayDate())
                            DetailRow(Icons.Default.Schedule, "Time", match.displayTime)
                            DetailRow(Icons.Default.LocationOn, "Location", match.displayLocation)
                            DetailRow(Icons.Default.Groups, "Players", "${match.currentPlayers}/${match.maxPlayers}")
                            if (!match.isFree) {
                                Text(
                                    text = "Cost: $${String.format("%.2f", match.costPerPlayer)}/player",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text("Free to join!", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // Host info
                    Text("Hosted by ${match.hostName ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)

                    // Participants
                    val approvedParticipants = match.participants.filter { it.status == ParticipantStatus.APPROVED }
                    if (approvedParticipants.isNotEmpty()) {
                        Text("Players", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(approvedParticipants) { p ->
                                ParticipantAvatar(
                                    name = p.userName,
                                    photoUrl = p.userPhotoUrl,
                                    role = p.role
                                )
                            }
                        }
                    }

                    // Browse available players (host only)
                    if (uiState.isHost) {
                        FilledTonalButton(
                            onClick = {
                                onBrowseAvailablePlayers(
                                    match.id,
                                    match.sportType.name.lowercase(),
                                    match.minSkillLevel,
                                    match.maxSkillLevel
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PersonSearch, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Browse Available Players")
                        }
                    }

                    // Pending join requests (host only)
                    if (uiState.isHost && uiState.pendingRequests.isNotEmpty()) {
                        Text("Join Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        uiState.pendingRequests.forEach { p ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(p.userName ?: "Player")
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { viewModel.respondToJoinRequest(p.id, true) }) {
                                            Text("Accept")
                                        }
                                        OutlinedButton(onClick = { viewModel.respondToJoinRequest(p.id, false) }) {
                                            Text("Decline")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.canJoin) {
                            Button(
                                onClick = { viewModel.joinMatch() },
                                enabled = !uiState.isJoining,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (uiState.isJoining) "Joining..." else "Join Match")
                            }
                        }
                        if (uiState.canJoinWithParty) {
                            FilledTonalButton(
                                onClick = { viewModel.joinWithParty() },
                                enabled = !uiState.isJoining,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Groups, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Join with Party")
                            }
                        }
                        if (uiState.isParticipant && !uiState.isHost) {
                            OutlinedButton(
                                onClick = { viewModel.leaveMatch() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Leave Match")
                            }
                        }
                        if (uiState.isParticipant) {
                            FilledTonalButton(
                                onClick = { onOpenChat(match.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Chat")
                            }
                        }
                    }

                    // Rate players (completed matches)
                    if (match.status.name == "COMPLETED" && uiState.isParticipant) {
                        Button(
                            onClick = { onRatePlayers(match.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Star, null)
                            Spacer(Modifier.width(4.dp))
                            Text("Rate Players")
                        }
                    }

                    // Cancel (host only, if match is open/full)
                    if (uiState.isHost && (match.status.name == "OPEN" || match.status.name == "FULL")) {
                        TextButton(
                            onClick = { viewModel.cancelMatch() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel Match", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
        Text("$label: ", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchDetailScreenPreview() {
    MatchDetailScreen(
        onBack = {},
        onOpenChat = {},
        onRatePlayers = {}
    )
}
