package com.example.sportsbook.ui.screens.player.community

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent

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
            Spacer(modifier = Modifier.width(16.dp))
            Text("Invite Friends", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }

        when {
            uiState.friends.isEmpty() -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = DarkTextPrimary.copy(alpha = 0.3f), modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No friends to invite yet",
                            color = DarkTextPrimary.copy(alpha = 0.6f),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
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
                            },
                        )
                    }
                }
            }
        }

        // ── Invite footer ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (uiState.error != null) {
                Text(text = uiState.error!!, color = Color(0xFFEF5350), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            val canInvite = selectedIds.isNotEmpty() && !uiState.isInviteLoading
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (canInvite) GreenAccent else GreenAccent.copy(alpha = 0.4f))
                    .then(if (canInvite) Modifier.clickable { selectedIds.forEach { userId -> viewModel.inviteFriend(userId) } } else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isInviteLoading) {
                    CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (selectedIds.isNotEmpty()) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = DarkBg, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = if (selectedIds.isEmpty()) "Select friends to invite"
                            else "Invite ${selectedIds.size} Friend${if (selectedIds.size > 1) "s" else ""}",
                            color = DarkBg,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
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
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MemberAvatar(name = friendship.friendName ?: "?", photoUrl = friendship.friendPhotoUrl, size = 42)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = friendship.friendName ?: "Unknown", fontSize = 14.sp, color = DarkTextPrimary, modifier = Modifier.weight(1f))
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = GreenAccent,
                uncheckedColor = DarkTextPrimary.copy(alpha = 0.4f),
                checkmarkColor = DarkBg,
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun InviteFriendsScreenPreview() {
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(DarkSurface).padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkBg.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Invite Friends", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(
                listOf(
                    Friendship(id = 1, friendId = 10, friendName = "Alice Smith"),
                    Friendship(id = 2, friendId = 11, friendName = "Bob Jones"),
                    Friendship(id = 3, friendId = 12, friendName = "Charlie Brown"),
                ),
            ) { friendship ->
                FriendInviteRow(friendship = friendship, isSelected = false, onToggle = {})
            }
        }
    }
}
