package com.example.sportsbook.ui.screens.player.friends

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsScreen(
    onBack: () -> Unit,
    viewModel: FriendRequestsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friend Requests") },
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
            uiState.error != null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadRequests,
                modifier = Modifier.padding(padding)
            )
            uiState.requests.isEmpty() -> RequestsEmptyState(
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
                    items(uiState.requests, key = { it.id }) { request ->
                        FriendRequestCard(
                            request = request,
                            onAccept = { viewModel.respondToRequest(request.id, accept = true) },
                            onDecline = { viewModel.respondToRequest(request.id, accept = false) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun RequestsEmptyState(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.MailOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No pending requests",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Friend requests you receive will appear here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun FriendRequestCard(
    request: Friendship,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
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
            FriendAvatar(photoUrl = request.friendPhotoUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.friendName ?: "Unknown Player",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                if (!request.createdAt.isNullOrBlank()) {
                    Text(
                        text = "Requested on ${request.createdAt.take(10)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onAccept) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Accept request",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDecline) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Decline request",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendRequestsScreenPreview() {
    MaterialTheme {
        FriendRequestsScreen(onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendRequestCardPreview() {
    MaterialTheme {
        FriendRequestCard(
            request = Friendship(
                id = 5,
                friendId = 99,
                friendName = "Jordan Smith",
                friendPhotoUrl = null,
                status = "pending",
                createdAt = "2026-03-10T12:00:00Z"
            ),
            onAccept = {},
            onDecline = {}
        )
    }
}
