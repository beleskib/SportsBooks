package com.example.sportsbook.ui.screens.player.party

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.screens.player.friends.FriendAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyInviteMembersScreen(
    onInvitesSent: () -> Unit,
    onBack: () -> Unit,
    viewModel: PartyInviteMembersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.invitesSent) {
        if (uiState.invitesSent) {
            onInvitesSent()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite Friends") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = viewModel::sendInvites,
                    enabled = uiState.selectedUserIds.isNotEmpty() && !uiState.isSending,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSending) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        val count = uiState.selectedUserIds.size
                        Text(
                            text = if (count == 0) "Select friends to invite"
                            else "Send Invites ($count)"
                        )
                    }
                }
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null && uiState.friends.isEmpty() -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadFriends,
                modifier = Modifier.padding(padding)
            )
            uiState.friends.isEmpty() -> FriendsEmptyForInvite(
                modifier = Modifier.padding(padding)
            )
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item {
                        Text(
                            text = "Select friends to invite to your party:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(uiState.friends, key = { it.friendId }) { friend ->
                        FriendInviteCard(
                            friend = friend,
                            isSelected = friend.friendId in uiState.selectedUserIds,
                            onToggle = { viewModel.toggleSelection(friend.friendId) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FriendsEmptyForInvite(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No friends to invite",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add friends first from the Friends screen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun FriendInviteCard(
    friend: Friendship,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FriendAvatar(photoUrl = friend.friendPhotoUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.friendName ?: "Unknown Player",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PartyInviteMembersScreenPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Invite Friends", style = MaterialTheme.typography.headlineSmall)
                FriendInviteCard(
                    friend = Friendship(
                        id = 1, friendId = 10,
                        friendName = "Alex Johnson", status = "accepted"
                    ),
                    isSelected = true,
                    onToggle = {}
                )
                FriendInviteCard(
                    friend = Friendship(
                        id = 2, friendId = 11,
                        friendName = "Maria Garcia", status = "accepted"
                    ),
                    isSelected = false,
                    onToggle = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendInviteCardPreview() {
    MaterialTheme {
        FriendInviteCard(
            friend = Friendship(
                id = 1, friendId = 42,
                friendName = "Jordan Williams", status = "accepted"
            ),
            isSelected = false,
            onToggle = {}
        )
    }
}
