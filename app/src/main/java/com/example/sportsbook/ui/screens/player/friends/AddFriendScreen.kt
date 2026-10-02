package com.example.sportsbook.ui.screens.player.friends

import android.widget.Toast
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark

private val avatarColors = listOf(
    Color(0xFF4CAF50), Color(0xFFAB47BC), Color(0xFFFF6B35),
    Color(0xFF2196F3), Color(0xFFFF9800), Color(0xFFE91E63),
)

@Composable
fun AddFriendScreen(
    onBack: () -> Unit,
    viewModel: AddFriendViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ───────────────────────────────────────────────────────
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
            Text("Add Friends", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }

        // ── Search bar ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🔍", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                singleLine = true,
                textStyle = TextStyle(color = DarkTextPrimary, fontSize = 15.sp),
                cursorBrush = SolidColor(GreenAccent),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box {
                        if (uiState.query.isEmpty()) {
                            Text("Search by name or username...", fontSize = 15.sp, color = Color(0xFF555555))
                        }
                        inner()
                    }
                },
            )
            if (uiState.isSearching) {
                CircularProgressIndicator(color = GreenAccent, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Content ──────────────────────────────────────────────────────
        if (uiState.query.length >= 2) {
            // Search results
            when {
                uiState.isSearching -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenAccent)
                    }
                }
                uiState.searchResults.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No players found", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                            Text("Try a different name", fontSize = 13.sp, color = DarkTextSecondary)
                        }
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.searchResults, key = { it.friendId }) { result ->
                            SearchResultRow(
                                result = result,
                                alreadySent = result.friendId in uiState.sentRequests,
                                onSendRequest = { viewModel.sendRequest(result.friendId) },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        } else {
            // Default state — invite card + pending + suggested
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Invite card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(listOf(GreenDark, GreenAccent))
                            )
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("📲", fontSize = 32.sp)
                        Text("Invite friends to SportsBooks", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 8.dp))
                        Text("Share a link and earn 50 XP per friend!", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        val shareContext = LocalContext.current
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable {
                                    Toast.makeText(shareContext, "Invite sharing coming soon", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                        ) {
                            Text("Share Invite Link", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Pending requests section
                if (uiState.pendingRequests.isNotEmpty()) {
                    item {
                        SectionTitle("Pending Requests (${uiState.pendingRequests.size})")
                    }
                    items(uiState.pendingRequests, key = { it.id }) { pending ->
                        PendingRequestRow(
                            friendship = pending,
                            avatarColor = avatarColors[(pending.friendId % avatarColors.size).toInt()],
                            onAccept = { viewModel.acceptRequest(pending.id) },
                            onDecline = { viewModel.declineRequest(pending.id) },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }

                // Suggested section
                if (uiState.suggestedPlayers.isNotEmpty()) {
                    item {
                        SectionTitle("Suggested For You")
                    }
                    items(uiState.suggestedPlayers, key = { it.friendId }) { suggested ->
                        SuggestedPlayerRow(
                            friendship = suggested,
                            avatarColor = avatarColors[(suggested.friendId % avatarColors.size).toInt()],
                            alreadySent = suggested.friendId in uiState.sentRequests,
                            onAdd = { viewModel.sendRequest(suggested.friendId) },
                        )
                    }
                } else if (!uiState.isLoadingSuggested) {
                    item {
                        SectionTitle("Suggested For You")
                        EmptyStateCard("No suggestions yet", "Check back later for player recommendations")
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = DarkTextPrimary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun EmptyStateCard(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextSecondary)
            Text(subtitle, fontSize = 12.sp, color = DarkTextSecondary.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun SearchResultRow(
    result: Friendship,
    alreadySent: Boolean,
    onSendRequest: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(GreenDark),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = result.friendName?.firstOrNull()?.uppercase() ?: "?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(result.friendName ?: "Unknown Player", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            Text("🏅 Player", fontSize = 12.sp, color = DarkTextSecondary)
        }

        if (alreadySent) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBorder)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text("Sent", fontSize = 13.sp, color = DarkTextSecondary, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onSendRequest)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text("+ Add", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PendingRequestRow(
    friendship: Friendship,
    avatarColor: Color,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(avatarColor.copy(alpha = 0.2f))
                .border(2.dp, avatarColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = friendship.friendName?.firstOrNull()?.uppercase() ?: "?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = avatarColor,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(friendship.friendName ?: "Unknown", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            Text("🏅 Wants to be friends", fontSize = 12.sp, color = DarkTextSecondary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onAccept)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(DarkBorder)
                    .clickable(onClick = onDecline),
                contentAlignment = Alignment.Center,
            ) {
                Text("✕", fontSize = 12.sp, color = DarkTextSecondary)
            }
        }
    }
}

@Composable
private fun SuggestedPlayerRow(
    friendship: Friendship,
    avatarColor: Color,
    alreadySent: Boolean,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(avatarColor.copy(alpha = 0.2f))
                .border(2.dp, avatarColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = friendship.friendName?.firstOrNull()?.uppercase() ?: "?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = avatarColor,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(friendship.friendName ?: "Unknown", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            Text("🏅 Player", fontSize = 12.sp, color = DarkTextSecondary)
        }
        if (alreadySent) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBorder)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text("Sent", fontSize = 13.sp, color = DarkTextSecondary, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onAdd)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text("+ Add", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun AddFriendScreenPreview() {
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Add Friends", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        }
        SuggestedPlayerRow(
            friendship = Friendship(id = 1, friendId = 10, friendName = "Alex Kramer", friendPhotoUrl = null, status = "pending", createdAt = ""),
            avatarColor = Color(0xFF4CAF50),
            alreadySent = false,
            onAdd = {},
        )
        Spacer(modifier = Modifier.height(8.dp))
        PendingRequestRow(
            friendship = Friendship(id = 2, friendId = 20, friendName = "James Lee", friendPhotoUrl = null, status = "pending", createdAt = ""),
            avatarColor = Color(0xFFFF9800),
            onAccept = {},
            onDecline = {},
        )
    }
}
