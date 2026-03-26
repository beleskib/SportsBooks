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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.ui.theme.Navy700
import com.example.sportsbook.ui.theme.Navy800
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteFriendsScreen(
    onBack: () -> Unit,
    onInvitesSent: () -> Unit,
    viewModel: CommunityDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedIds = remember { mutableStateSetOf<Long>() }

    LaunchedEffect(Unit) {
        viewModel.loadFriends()
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage == "Invitation sent") {
            onInvitesSent()
        }
    }

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = { Text("Invite Friends", color = WarmWhite) },
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
        ) {
            when {
                uiState.friends.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = WarmWhite.copy(alpha = 0.3f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No friends to invite yet",
                                color = WarmWhite.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.friends, key = { it.friendId }) { friendship ->
                            FriendInviteRow(
                                friendship = friendship,
                                isSelected = friendship.friendId in selectedIds,
                                onToggle = {
                                    if (friendship.friendId in selectedIds) {
                                        selectedIds.remove(friendship.friendId)
                                    } else {
                                        selectedIds.add(friendship.friendId)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Invite button footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Navy800)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = {
                        selectedIds.forEach { userId -> viewModel.inviteFriend(userId) }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedIds.isNotEmpty() && !uiState.isInviteLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = USOpenGold)
                ) {
                    if (uiState.isInviteLoading) {
                        CircularProgressIndicator(color = Navy900, modifier = Modifier.height(20.dp))
                    } else {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Navy900,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedIds.isEmpty()) "Select friends to invite"
                            else "Invite ${selectedIds.size} Friend${if (selectedIds.size > 1) "s" else ""}",
                            color = Navy900,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendInviteRow(
    friendship: Friendship,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemberAvatar(
            name = friendship.friendName ?: "?",
            photoUrl = friendship.friendPhotoUrl,
            size = 42
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = friendship.friendName ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmWhite,
            modifier = Modifier.weight(1f)
        )
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = USOpenGold,
                uncheckedColor = WarmWhite.copy(alpha = 0.4f),
                checkmarkColor = Navy900
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun InviteFriendsScreenPreview() {
    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = { Text("Invite Friends", color = WarmWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    listOf(
                        Friendship(id = 1, friendId = 10, friendName = "Alice Smith"),
                        Friendship(id = 2, friendId = 11, friendName = "Bob Jones"),
                        Friendship(id = 3, friendId = 12, friendName = "Charlie Brown")
                    )
                ) { friendship ->
                    FriendInviteRow(friendship = friendship, isSelected = false, onToggle = {})
                }
            }
        }
    }
}
