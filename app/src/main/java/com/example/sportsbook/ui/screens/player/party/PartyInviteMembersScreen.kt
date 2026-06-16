package com.example.sportsbook.ui.screens.player.party

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.screens.player.friends.FriendAvatar
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkBg)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Invite Friends", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }

            // ── Content ──────────────────────────────────────────────────
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.error != null && uiState.friends.isEmpty() -> ErrorView(
                    message = uiState.error!!,
                    onRetry = viewModel::loadFriends,
                )
                uiState.friends.isEmpty() -> FriendsEmptyForInvite()
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        item {
                            Text(
                                text = "Select friends to invite to your party:",
                                fontSize = 14.sp,
                                color = DarkTextSecondary,
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
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }

        // ── Bottom bar ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(16.dp),
        ) {
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = Color(0xFFEF5350),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            val canSend = uiState.selectedUserIds.isNotEmpty() && !uiState.isSending
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (canSend) GreenAccent else Color(0xFF2A3D2B))
                    .clickable(enabled = canSend, onClick = viewModel::sendInvites)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isSending) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                    )
                } else {
                    val count = uiState.selectedUserIds.size
                    Text(
                        text = if (count == 0) "Select friends to invite"
                        else "Send Invites ($count)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
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
                tint = DarkTextSecondary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No friends to invite",
                fontSize = 16.sp,
                color = DarkTextSecondary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add friends first from the Friends screen",
                fontSize = 12.sp,
                color = DarkTextSecondary.copy(alpha = 0.7f),
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) GreenAccent.copy(alpha = 0.15f) else DarkSurface)
            .border(1.dp, if (isSelected) GreenAccent.copy(alpha = 0.4f) else DarkBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FriendAvatar(photoUrl = friend.friendPhotoUrl)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.friendName ?: "Unknown Player",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) GreenAccent else DarkTextPrimary,
            )
        }
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = GreenAccent,
                uncheckedColor = DarkTextSecondary,
                checkmarkColor = Color.White,
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PartyInviteMembersScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Invite Friends", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun FriendInviteCardPreview() {
    Box(modifier = Modifier.padding(16.dp).background(DarkBg)) {
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
