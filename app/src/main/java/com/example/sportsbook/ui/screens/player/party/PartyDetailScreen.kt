package com.example.sportsbook.ui.screens.player.party

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.model.Party
import com.example.sportsbook.domain.model.PartyMember
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyDetailScreen(
    onInviteFriends: (Long) -> Unit,
    onFindMatch: () -> Unit,
    onBack: () -> Unit,
    viewModel: PartyDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.disbanded) {
        if (uiState.disbanded) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Party") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null && uiState.party == null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadParty,
                modifier = Modifier.padding(padding)
            )
            uiState.party != null -> {
                val party = uiState.party!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Party info card
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = party.name ?: "Unnamed Party",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    PartyStatusChip(status = party.status)
                                }
                                if (!party.sportType.isNullOrBlank()) {
                                    Text(
                                        text = party.sportType.replace("_", " ")
                                            .replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "Led by ${party.leaderName ?: "Unknown"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Members header
                    item {
                        Text(
                            text = "Members (${party.members.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Member cards
                    items(party.members, key = { it.id }) { member ->
                        PartyMemberCard(member = member)
                    }

                    if (party.members.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No members yet. Invite your friends!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Error message (non-fatal)
                    if (uiState.error != null) {
                        item {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Action buttons
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Invited member: accept / decline
                            if (uiState.isInvitedMember) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.respondToInvite(true) },
                                        enabled = !uiState.isActioning,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Accept")
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.respondToInvite(false) },
                                        enabled = !uiState.isActioning,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Close, null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Decline")
                                    }
                                }
                            }

                            // Leader: invite friends button (status == forming)
                            if (uiState.isLeader && party.status == "forming") {
                                FilledTonalButton(
                                    onClick = { onInviteFriends(party.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PersonAdd, null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Invite Friends")
                                }
                            }

                            // Leader: find match button (status == ready)
                            if (uiState.isLeader && party.status == "ready") {
                                Button(
                                    onClick = onFindMatch,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Search, null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Find a Match")
                                }
                            }

                            // Leader: disband button
                            if (uiState.isLeader) {
                                TextButton(
                                    onClick = viewModel::disbandParty,
                                    enabled = !uiState.isActioning,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (uiState.isActioning) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "Disband Party",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PartyStatusChip(status: String) {
    val (label, containerColor, contentColor) = when (status) {
        "forming" -> Triple(
            "Forming",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        "ready" -> Triple(
            "Ready",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        "in_match" -> Triple(
            "In Match",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        "disbanded" -> Triple(
            "Disbanded",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        else -> Triple(
            status.replaceFirstChar { it.uppercase() },
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = contentColor)
    }
}

@Composable
private fun PartyMemberCard(
    member: PartyMember,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PartyMemberAvatar(photoUrl = member.userPhotoUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.userName ?: "Unknown Player",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = member.status.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (member.status) {
                        "accepted" -> MaterialTheme.colorScheme.primary
                        "declined" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            MemberStatusIcon(status = member.status)
        }
    }
}

@Composable
private fun PartyMemberAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier
) {
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Member avatar",
            modifier = modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun MemberStatusIcon(status: String) {
    when (status) {
        "accepted" -> Icon(
            Icons.Default.Check,
            contentDescription = "Accepted",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        "declined" -> Icon(
            Icons.Default.Close,
            contentDescription = "Declined",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        else -> Icon(
            Icons.Default.Groups,
            contentDescription = "Invited",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PartyDetailScreenPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("The Dream Team", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Football", color = MaterialTheme.colorScheme.primary)
                Text("Led by Alex", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                PartyMemberCard(
                    member = PartyMember(
                        id = 1, userId = 10, userName = "Alex Johnson",
                        status = "accepted"
                    )
                )
                PartyMemberCard(
                    member = PartyMember(
                        id = 2, userId = 11, userName = "Maria Garcia",
                        status = "invited"
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PartyMemberCardPreview() {
    MaterialTheme {
        PartyMemberCard(
            member = PartyMember(
                id = 1, userId = 42,
                userName = "Jordan Williams",
                status = "declined"
            )
        )
    }
}
