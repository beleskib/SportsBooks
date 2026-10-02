package com.example.sportsbook.ui.screens.player.social

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Friendship
import com.example.sportsbook.domain.model.Party
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.OrangeAccent

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun SocialHubScreen(
    viewModel: SocialHubViewModel = hiltViewModel(),
    onNavigateToChats: () -> Unit = {},
    onNavigateToAddFriend: () -> Unit = {},
    onNavigateToCreateParty: () -> Unit = {},
    onNavigateToFriendRequests: () -> Unit = {},
    onFriendClick: (Long, String, String?) -> Unit = { _, _, _ -> },
    onPartyClick: (Long) -> Unit = {},
    onPlayerClick: (Long) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Friends", "Parties", "Find Players")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Top bar ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Social Hub", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(36.dp)) {
                    SocialIconBtn("🔔", onClick = onNavigateToFriendRequests)
                    if (uiState.pendingRequests.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.Red),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = uiState.pendingRequests.size.toString(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }
                SocialIconBtn("💬", onClick = onNavigateToChats)
            }
        }

        // ── Tab bar ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(4.dp),
        ) {
            tabs.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == index) GreenAccent else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedTab == index) Color.White else DarkTextSecondary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Tab content ──────────────────────────────────────────────
        when (selectedTab) {
            0 -> FriendsTab(
                friends = uiState.friends,
                pendingRequests = uiState.pendingRequests,
                parties = uiState.parties,
                isLoading = uiState.isLoadingFriends,
                onAddFriend = onNavigateToAddFriend,
                onCreateParty = onNavigateToCreateParty,
                onFriendClick = onFriendClick,
                onPartyClick = onPartyClick,
                onAcceptRequest = viewModel::acceptFriendRequest,
                onDeclineRequest = viewModel::declineFriendRequest,
                onSeeAllFriends = onNavigateToAddFriend,
            )
            1 -> PartiesTab(
                parties = uiState.parties,
                isLoading = uiState.isLoadingParties,
                onCreateParty = onNavigateToCreateParty,
                onPartyClick = onPartyClick,
            )
            2 -> FindPlayersTab(
                players = uiState.nearbyPlayers,
                isLoading = uiState.isLoadingPlayers,
                onAddFriend = onNavigateToAddFriend,
                onPlayerClick = onPlayerClick,
                onSendRequest = viewModel::sendFriendRequest,
            )
        }
    }
}

// ── Tab 1: Friends ────────────────────────────────────────────────────────────

@Composable
private fun FriendsTab(
    friends: List<Friendship>,
    pendingRequests: List<Friendship>,
    parties: List<Party>,
    isLoading: Boolean,
    onAddFriend: () -> Unit,
    onCreateParty: () -> Unit,
    onFriendClick: (Long, String, String?) -> Unit,
    onPartyClick: (Long) -> Unit,
    onAcceptRequest: (Long) -> Unit,
    onDeclineRequest: (Long) -> Unit,
    onSeeAllFriends: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Quick action cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    Triple("👥", "Create Party", "Team up & play") to { onCreateParty() },
                    Triple("➕", "Add Friend", "Search or invite") to { onAddFriend() },
                    Triple("📱", "Share QR", "Quick connect") to {},
                    Triple("🔗", "Invite Link", "Share to friends") to {},
                ).forEach { (info, action) ->
                    val (icon, label, sub) = info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .clickable { action() }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when (label) {
                                        "Create Party" -> GreenAccent.copy(alpha = 0.2f)
                                        "Add Friend" -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                        "Share QR" -> OrangeAccent.copy(alpha = 0.2f)
                                        else -> Color(0xFF9C27B0).copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(icon, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary, textAlign = TextAlign.Center)
                        Text(sub, fontSize = 9.sp, color = DarkTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 1.dp))
                    }
                }
            }
        }

        // Active party (show first active party if exists)
        val activeParty = parties.firstOrNull { it.status == "forming" || it.status == "ready" }
        if (activeParty != null) {
            item {
                SocialSectionHeader("🎮 Active Party", actionLabel = null, onAction = {})
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .clickable { onPartyClick(activeParty.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.width(80.dp).height(36.dp)) {
                        activeParty.members.take(3).forEachIndexed { i, member ->
                            val initial = member.userName?.firstOrNull()?.uppercase() ?: "?"
                            Box(
                                modifier = Modifier
                                    .offset(x = (24 * i).dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(GreenAccent, Color(0xFF2196F3))))
                                    .border(2.dp, DarkBg, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(initial, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeParty.name ?: "Party",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkTextPrimary,
                        )
                        Text(
                            text = "${activeParty.members.size} players • ${activeParty.status.replaceFirstChar { it.uppercase() }}",
                            fontSize = 12.sp,
                            color = DarkTextSecondary,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenAccent)
                            .clickable { onPartyClick(activeParty.id) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text("Open", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }

        // Pending requests
        if (pendingRequests.isNotEmpty()) {
            item {
                SocialSectionHeader("📩 Pending Requests", badge = pendingRequests.size.toString(), onAction = {})
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface),
                ) {
                    pendingRequests.forEachIndexed { i, request ->
                        val name = request.friendName ?: "Unknown"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF2196F3)))),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(name.first().toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                                Text(
                                    text = request.createdAt ?: "",
                                    fontSize = 12.sp,
                                    color = DarkTextSecondary,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GreenAccent)
                                        .clickable { onAcceptRequest(request.id) }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                ) {
                                    Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkBorder)
                                        .clickable { onDeclineRequest(request.id) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    Text("✕", fontSize = 12.sp, color = DarkTextSecondary)
                                }
                            }
                        }
                        if (i < pendingRequests.lastIndex) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
                        }
                    }
                }
            }
        }

        // Friends list
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = GreenAccent, modifier = Modifier.size(32.dp))
                }
            }
        } else if (friends.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = "👥",
                    title = "No friends yet",
                    subtitle = "Add friends to see them here and start playing together!",
                    actionLabel = "Add Friend",
                    onAction = onAddFriend,
                )
            }
        } else {
            item {
                SocialSectionHeader(
                    "Friends",
                    badge = friends.size.toString(),
                    actionLabel = "See All",
                    onAction = onSeeAllFriends,
                )
            }
            items(friends, key = { it.id }) { friend ->
                FriendRow(
                    friendship = friend,
                    onClick = { onFriendClick(friend.friendId, friend.friendName ?: "Unknown", friend.friendPhotoUrl) },
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ── Tab 2: Parties ────────────────────────────────────────────────────────────

@Composable
private fun PartiesTab(
    parties: List<Party>,
    isLoading: Boolean,
    onCreateParty: () -> Unit,
    onPartyClick: (Long) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Create button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF1B3A1E), DarkSurface)))
                    .border(1.dp, GreenAccent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onCreateParty)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("👥", fontSize = 18.sp)
                    Text("Create New Party", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = GreenAccent)
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = GreenAccent, modifier = Modifier.size(32.dp))
                }
            }
        } else if (parties.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = "🎮",
                    title = "No parties yet",
                    subtitle = "Create a party and invite friends to play together!",
                    actionLabel = "Create Party",
                    onAction = onCreateParty,
                )
            }
        } else {
            items(parties, key = { it.id }) { party ->
                PartyCard(party = party, onClick = { onPartyClick(party.id) })
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ── Tab 3: Find Players ───────────────────────────────────────────────────────

@Composable
private fun FindPlayersTab(
    players: List<Friendship>,
    isLoading: Boolean,
    onAddFriend: () -> Unit,
    onPlayerClick: (Long) -> Unit,
    onSendRequest: (Long) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Filter chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Nearby 📍", "🏀 Basketball", "Online 🟢", "Same Level").forEachIndexed { i, label ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (i == 0) GreenAccent else DarkSurface)
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (i == 0) Color.White else DarkTextSecondary)
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = GreenAccent, modifier = Modifier.size(32.dp))
                }
            }
        } else if (players.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = "🔍",
                    title = "No players found",
                    subtitle = "Be the first to join! Invite friends to get started.",
                    actionLabel = "Invite Friends",
                    onAction = onAddFriend,
                )
            }
        } else {
            items(players, key = { it.id }) { player ->
                PlayerRow(
                    friendship = player,
                    onClick = { onPlayerClick(player.friendId) },
                    onSendRequest = { onSendRequest(player.friendId) },
                )
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ── Components ────────────────────────────────────────────────────────────────

@Composable
private fun SocialSectionHeader(
    title: String,
    badge: String? = null,
    actionLabel: String? = "See All",
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        if (badge != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (actionLabel != null) {
            Text(actionLabel, fontSize = 13.sp, color = GreenAccent, modifier = Modifier.clickable(onClick = onAction))
        }
    }
}

@Composable
private fun SocialIconBtn(icon: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(DarkSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(icon, fontSize = 16.sp)
    }
}

@Composable
private fun FriendRow(friendship: Friendship, onClick: () -> Unit) {
    val name = friendship.friendName ?: "Unknown"
    val initial = name.firstOrNull()?.uppercase() ?: "?"
    val isOnline = friendship.status == "accepted"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(44.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarGradient(friendship.friendId.toInt())),
                contentAlignment = Alignment.Center,
            ) {
                Text(initial, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            if (isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(GreenAccent)
                        .border(2.dp, DarkBg, CircleShape)
                        .align(Alignment.BottomEnd),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            Text(
                text = friendship.createdAt ?: "Friend",
                fontSize = 12.sp,
                color = DarkTextSecondary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(start = 72.dp, end = 16.dp).background(DarkBorder))
}

@Composable
private fun PartyCard(party: Party, onClick: () -> Unit) {
    val sportColor = when (party.sportType?.lowercase()) {
        "basketball" -> Color(0xFF4CAF50)
        "football" -> OrangeAccent
        "tennis" -> Color(0xFFFFD700)
        "volleyball" -> Color(0xFF2196F3)
        "paddle" -> Color(0xFF9C27B0)
        else -> GreenAccent
    }
    val statusColor = when (party.status) {
        "forming" -> GreenAccent
        "ready" -> Color(0xFF2196F3)
        "in_match" -> OrangeAccent
        else -> DarkTextSecondary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = party.name ?: "Party",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextPrimary,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(sportColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = party.sportType ?: "Sport",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = sportColor,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Member stack
        Row(verticalAlignment = Alignment.CenterVertically) {
            party.members.take(5).forEachIndexed { i, member ->
                val initial = member.userName?.firstOrNull()?.uppercase() ?: "?"
                Box(
                    modifier = Modifier
                        .offset(x = (-8 * i).dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(avatarGradient(i))
                        .border(2.dp, DarkBg, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(initial, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("${party.members.size} players", fontSize = 12.sp, color = DarkTextSecondary)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                Text(
                    text = party.status.replaceFirstChar { it.uppercase() }.replace("_", " "),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenAccent)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text("Open", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@Composable
private fun PlayerRow(friendship: Friendship, onClick: () -> Unit, onSendRequest: () -> Unit) {
    val name = friendship.friendName ?: "Unknown"
    val initial = name.firstOrNull()?.uppercase() ?: "?"
    val alreadySent = friendship.status == "pending"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(avatarGradient(friendship.friendId.toInt())),
            contentAlignment = Alignment.Center,
        ) {
            Text(initial, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (alreadySent) DarkBorder else GreenAccent)
                .clickable(enabled = !alreadySent, onClick = onSendRequest)
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            Text(
                text = if (alreadySent) "Sent" else "+ Add",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (alreadySent) DarkTextSecondary else Color.White,
            )
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(start = 72.dp, end = 16.dp).background(DarkBorder))
}

@Composable
private fun EmptyStateCard(
    icon: String,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(icon, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, fontSize = 13.sp, color = DarkTextSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(GreenAccent)
                .clickable(onClick = onAction)
                .padding(horizontal = 24.dp, vertical = 10.dp),
        ) {
            Text(actionLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

private fun avatarGradient(index: Int): Brush = when (index % 5) {
    0 -> Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)))
    1 -> Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF2196F3)))
    2 -> Brush.linearGradient(listOf(Color(0xFFAD1457), Color(0xFFE91E63)))
    3 -> Brush.linearGradient(listOf(Color(0xFF6A1B9A), Color(0xFF9C27B0)))
    else -> Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFFF9800)))
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun SocialHubScreenPreview() {
    SocialHubScreen()
}
