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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sportsbook.data.remote.dto.MatchPaymentStatusDto
import com.example.sportsbook.data.remote.dto.v2.SplitPaymentShareDto
import com.example.sportsbook.domain.enums.MatchStatus
import com.example.sportsbook.domain.enums.MatchType
import java.time.Duration
import java.time.Instant
import com.example.sportsbook.domain.enums.MatchVisibility
import com.example.sportsbook.domain.enums.ParticipantRole
import com.example.sportsbook.domain.enums.ParticipantStatus
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.MatchParticipant
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
    onPlayerClick: (Long) -> Unit = {},
    viewModel: MatchDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showShareDialog by remember { mutableStateOf(false) }
    var shareCaption by remember { mutableStateOf("") }
    var showRatingReminder by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.shareSuccess) {
        if (uiState.shareSuccess) {
            snackbarHostState.showSnackbar("Match shared to your feed!")
            viewModel.clearShareSuccess()
        }
    }

    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            snackbarHostState.showSnackbar("Payment successful!")
        }
    }

    val match = uiState.match
    LaunchedEffect(match) {
        if (match != null &&
            match.status == MatchStatus.COMPLETED &&
            uiState.isParticipant &&
            match.updatedAt != null
        ) {
            val completedAt = try {
                Instant.parse(match.updatedAt)
            } catch (_: Exception) {
                null
            }
            if (completedAt != null && Duration.between(completedAt, Instant.now()).toHours() < 24) {
                showRatingReminder = true
            }
        }
    }

    // Rating reminder dialog
    if (showRatingReminder && match != null) {
        AlertDialog(
            onDismissRequest = { showRatingReminder = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700)
                )
            },
            title = { Text("Match Completed!") },
            text = {
                Text("Don't forget to rate the players! You have 24 hours to submit your ratings.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRatingReminder = false
                        onRatePlayers(match.id)
                    }
                ) {
                    Text("Rate Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingReminder = false }) {
                    Text("Maybe Later")
                }
            }
        )
    }

    // Share dialog
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false; shareCaption = "" },
            title = { Text("Share to Feed") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Share this match with your followers",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = shareCaption,
                        onValueChange = { shareCaption = it },
                        label = { Text("Add a caption (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.shareMatchToFeed(shareCaption.takeIf { it.isNotBlank() })
                        showShareDialog = false
                        shareCaption = ""
                    },
                    enabled = !uiState.isSharing
                ) {
                    Text(if (uiState.isSharing) "Sharing..." else "Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false; shareCaption = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (uiState.isParticipant || uiState.isHost) {
                        IconButton(onClick = { showShareDialog = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Share to Feed")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                                    text = "Cost: ${String.format("%.0f", match.costPerPlayer)} ден/player",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text("Free to join!", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium)
                            }
                            if (match.paymentType != null && match.paymentType != "host_pays") {
                                Text(
                                    text = "Payment: ${match.paymentType.replace("_", " ").replaceFirstChar { it.uppercase() }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
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
                                    role = p.role,
                                    onClick = { onPlayerClick(p.userId) }
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

                    // Payment Status Section (for split matches)
                    if (uiState.showPaymentSection) {
                        HorizontalDivider()
                        PaymentStatusSection(
                            paymentStatus = uiState.paymentStatus!!,
                            myShare = uiState.myShare,
                            canPayShare = uiState.canPayShare,
                            isPaying = uiState.isPayingShare,
                            onPayShare = { viewModel.payMyShare() }
                        )
                    }

                    // Rate players (completed matches, within 24 hours)
                    val isWithin24Hours = match.updatedAt?.let {
                        try {
                            val completedAt = Instant.parse(it)
                            Duration.between(completedAt, Instant.now()).toHours() < 24
                        } catch (_: Exception) { true }
                    } ?: true

                    if (match.status == MatchStatus.COMPLETED && uiState.isParticipant && isWithin24Hours) {
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
private fun PaymentStatusSection(
    paymentStatus: MatchPaymentStatusDto,
    myShare: SplitPaymentShareDto?,
    canPayShare: Boolean,
    isPaying: Boolean,
    onPayShare: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Payment Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Progress bar
            val progress = if (paymentStatus.totalAmount > 0) {
                (paymentStatus.paidAmount / paymentStatus.totalAmount).toFloat().coerceIn(0f, 1f)
            } else 0f

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${"%.0f".format(paymentStatus.paidAmount)} / ${"%.0f".format(paymentStatus.totalAmount)} ${paymentStatus.currency}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "${(progress * 100).toInt()}% paid",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Individual shares
            paymentStatus.shares.forEach { share ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (share.status == "paid") Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (share.status == "paid") Color(0xFF16A34A) else Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            share.payerName ?: "Player",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        "${"%.0f".format(share.amount)} ${share.currency}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Pay button
            if (canPayShare && myShare != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onPayShare,
                    enabled = !isPaying,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    )
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isPaying) "Processing..."
                        else "Pay Your Share — ${"%.0f".format(myShare.amount)} ${myShare.currency}"
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun MatchDetailScreenPreview() {
    val sampleMatch = Match(
        id = 1L,
        hostId = 10L,
        hostName = "Jordan Lee",
        sportType = SportType.BASKETBALL,
        matchType = MatchType.STANDALONE,
        status = MatchStatus.OPEN,
        visibility = MatchVisibility.PUBLIC,
        title = "Sunday Pickup Basketball",
        description = "Casual game, all levels welcome. Bring water!",
        matchDate = "2026-03-30",
        startTime = "18:00",
        endTime = "19:30",
        minPlayers = 6,
        maxPlayers = 12,
        currentPlayers = 4,
        minSkillLevel = 2,
        maxSkillLevel = 4,
        locationName = "City Sports Center",
        address = "123 Main St",
        isFree = true,
        participants = listOf(
            MatchParticipant(
                id = 1L, matchId = 1L, userId = 10L,
                userName = "Jordan Lee", status = ParticipantStatus.APPROVED,
                role = ParticipantRole.HOST
            )
        )
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match Details") },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sampleMatch.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                MatchStatusBadge(status = sampleMatch.status)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = sampleMatch.sportType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                SkillRangeBadge(2, 4)
            }
            Text(
                text = sampleMatch.description ?: "",
                style = MaterialTheme.typography.bodyMedium
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(Icons.Default.CalendarToday, "Date", sampleMatch.matchDate.toDisplayDate())
                    DetailRow(Icons.Default.Schedule, "Time", sampleMatch.displayTime)
                    DetailRow(Icons.Default.LocationOn, "Location", sampleMatch.displayLocation)
                    DetailRow(Icons.Default.Groups, "Players", "${sampleMatch.currentPlayers}/${sampleMatch.maxPlayers}")
                    Text("Free to join!", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium)
                }
            }
            Text("Hosted by ${sampleMatch.hostName}", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Join Match")
            }
        }
    }
}
