package com.example.sportsbook.ui.screens.player.friends

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark

@Composable
fun FriendsListScreen(
    onBack: () -> Unit,
    onAddFriend: () -> Unit,
    onViewRequests: () -> Unit,
    onMessageFriend: (userId: Long, name: String, photoUrl: String?) -> Unit = { _, _, _ -> },
    viewModel: FriendsListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Friends", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                if (uiState.friends.isNotEmpty()) {
                    Text("${uiState.friends.size} friends", fontSize = 13.sp, color = DarkTextSecondary)
                }
            }
            // Requests button with badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable(onClick = onViewRequests),
                contentAlignment = Alignment.Center,
            ) {
                Text("📬", fontSize = 18.sp)
                if (uiState.pendingCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(uiState.pendingCount.toString(), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenAccent)
                    .clickable(onClick = onAddFriend),
                contentAlignment = Alignment.Center,
            ) {
                Text("+", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }
            uiState.error != null -> ErrorView(message = uiState.error!!, onRetry = viewModel::loadFriends)
            uiState.friends.isEmpty() -> FriendsEmptyState()
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(uiState.friends, key = { it.id }) { friend ->
                        FriendCard(
                            friend = friend,
                            onRemove = { viewModel.removeFriend(friend.friendId) },
                            onMessage = {
                                onMessageFriend(friend.friendId, friend.friendName ?: "Friend", friend.friendPhotoUrl)
                            },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun FriendsEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Text("👥", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No friends yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            Text("Search for players and send friend requests", fontSize = 14.sp, color = DarkTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun FriendCard(
    friend: Friendship,
    onRemove: () -> Unit,
    onMessage: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FriendAvatar(photoUrl = friend.friendPhotoUrl, name = friend.friendName ?: "?")
        Column(modifier = Modifier.weight(1f)) {
            Text(friend.friendName ?: "Unknown Player", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            if (!friend.createdAt.isNullOrBlank()) {
                Text("Friends since ${friend.createdAt.take(10)}", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
            }
        }
        // Message
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GreenAccent.copy(alpha = 0.12f))
                .border(1.dp, GreenAccent.copy(alpha = 0.4f), CircleShape)
                .clickable(onClick = onMessage),
            contentAlignment = Alignment.Center,
        ) {
            Text("💬", fontSize = 16.sp)
        }
        // Remove
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFB71C1C).copy(alpha = 0.12f))
                .border(1.dp, Color(0xFFEF5350).copy(alpha = 0.3f), CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Text("🗑", fontSize = 14.sp)
        }
    }
}

@Composable
internal fun FriendAvatar(
    photoUrl: String?,
    name: String = "?",
    modifier: Modifier = Modifier,
    size: Int = 44,
) {
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Avatar",
            modifier = modifier.size(size.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(GreenDark),
            contentAlignment = Alignment.Center,
        ) {
            Text(name.firstOrNull()?.uppercase() ?: "?", fontSize = (size * 0.4f).sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun FriendsListScreenPreview() {
    val sampleFriends = listOf(
        Friendship(id = 1, friendId = 42, friendName = "Alex Johnson", friendPhotoUrl = null, status = "accepted", createdAt = "2026-02-14T08:00:00Z"),
        Friendship(id = 2, friendId = 55, friendName = "Maria Garcia", friendPhotoUrl = null, status = "accepted", createdAt = "2026-03-01T10:00:00Z"),
        Friendship(id = 3, friendId = 71, friendName = "David Chen", friendPhotoUrl = null, status = "accepted", createdAt = "2026-03-15T14:00:00Z"),
    )
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Friends", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                    Text("3 friends", fontSize = 13.sp, color = DarkTextSecondary)
                }
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface), contentAlignment = Alignment.Center) {
                    Text("📬", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(GreenAccent), contentAlignment = Alignment.Center) {
                    Text("+", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(sampleFriends) { friend -> FriendCard(friend = friend, onRemove = {}) }
            }
        }
}
