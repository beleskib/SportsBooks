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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    onNavigateToChats: () -> Unit = {},
    onNavigateToAddFriend: () -> Unit = {},
    onNavigateToCreateParty: () -> Unit = {},
    onFriendClick: (Long, String, String?) -> Unit = { _, _, _ -> },
    onPartyClick: (Long) -> Unit = {},
    onPlayerClick: (Long) -> Unit = {},
) {
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
                SocialIconBtn("🔔", onClick = {})
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
                onAddFriend = onNavigateToAddFriend,
                onCreateParty = onNavigateToCreateParty,
                onFriendClick = onFriendClick,
                onPartyClick = onPartyClick,
            )
            1 -> PartiesTab(onCreateParty = onNavigateToCreateParty, onPartyClick = onPartyClick)
            2 -> FindPlayersTab(onAddFriend = onNavigateToAddFriend, onPlayerClick = onPlayerClick)
        }
    }
}

// ── Tab 1: Friends ────────────────────────────────────────────────────────────

@Composable
private fun FriendsTab(
    onAddFriend: () -> Unit,
    onCreateParty: () -> Unit,
    onFriendClick: (Long, String, String?) -> Unit,
    onPartyClick: (Long) -> Unit,
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

        // Active party
        item {
            SocialSectionHeader("🎮 Active Party", actionLabel = null, onAction = {})
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .clickable { onPartyClick(1L) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Overlapping avatars
                Box(modifier = Modifier.width(80.dp).height(36.dp)) {
                    listOf("B" to 0, "K" to 24, "J" to 48).forEach { (letter, offsetX) ->
                        Box(
                            modifier = Modifier
                                .offset(x = offsetX.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(GreenAccent, Color(0xFF2196F3))))
                                .border(2.dp, DarkBg, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(letter, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Friday Basketball Crew", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                    Text("3/5 players • Looking for match", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GreenAccent)
                        .clickable { onPartyClick(1L) }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text("Open", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }

        // Pending requests
        item {
            SocialSectionHeader("📩 Pending Requests", badge = "2", onAction = {})
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface),
            ) {
                listOf("Marko T." to "Sent 2h ago", "Ana S." to "Sent 1d ago").forEachIndexed { i, (name, time) ->
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
                            Text(time, fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GreenAccent)
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                            ) {
                                Text("Accept", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkBorder)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text("✕", fontSize = 12.sp, color = DarkTextSecondary)
                            }
                        }
                    }
                    if (i == 0) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
                }
            }
        }

        // Friends list
        item {
            SocialSectionHeader("Friends", badge = "3", actionLabel = "See All", onAction = {})
        }
        items(sampleFriends) { friend ->
            FriendRow(friend = friend, onClick = { onFriendClick(friend.id, friend.name, null) })
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ── Tab 2: Parties ────────────────────────────────────────────────────────────

@Composable
private fun PartiesTab(onCreateParty: () -> Unit, onPartyClick: (Long) -> Unit) {
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

        items(sampleParties) { party ->
            PartyCard(party = party, onClick = { onPartyClick(party.id) })
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ── Tab 3: Find Players ───────────────────────────────────────────────────────

@Composable
private fun FindPlayersTab(onAddFriend: () -> Unit, onPlayerClick: (Long) -> Unit) {
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

        // Players grid (2 columns)
        item {
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(700.dp), // constrained height inside LazyColumn
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(samplePlayers) { player ->
                    PlayerCard(player = player, onClick = { onPlayerClick(player.id) })
                }
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
private fun FriendRow(friend: SampleFriend, onClick: () -> Unit) {
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
                    .background(friend.avatarBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(friend.initial, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            if (friend.isOnline) {
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
            Text(friend.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            Text(friend.status, fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(start = 72.dp, end = 16.dp).background(DarkBorder))
}

@Composable
private fun PartyCard(party: SampleParty, onClick: () -> Unit) {
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
            Text(party.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(party.sportColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(party.sport, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = party.sportColor)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Member stack
        Row(verticalAlignment = Alignment.CenterVertically) {
            party.memberInitials.take(5).forEachIndexed { i, initial ->
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
            Text("${party.memberInitials.size}/${party.maxMembers} players", fontSize = 12.sp, color = DarkTextSecondary)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(party.statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(party.statusColor))
                Text(party.status, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = party.statusColor)
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (party.showInvite) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBorder)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text("Invite", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GreenAccent)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(if (party.memberInitials.size < party.maxMembers) "Find Match" else "Find Match", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun PlayerCard(player: SamplePlayer, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(56.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(player.avatarBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(player.initial, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            if (player.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(GreenAccent)
                        .border(2.dp, DarkBg, CircleShape)
                        .align(Alignment.BottomEnd),
                )
            }
            if (player.mutualFriends > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2196F3))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                ) {
                    Text("${player.mutualFriends} mutual", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(player.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            player.sports.forEach { Text(it, fontSize = 14.sp) }
        }
        Text("⚡ ${player.levelText}", fontSize = 10.sp, color = DarkTextSecondary)
        Text("📍 ${player.distance}", fontSize = 10.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (player.requestSent) DarkBorder else GreenAccent)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (player.requestSent) "Request Sent" else "+ Add Friend",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (player.requestSent) DarkTextSecondary else Color.White,
            )
        }
    }
}

// ── Sample data ───────────────────────────────────────────────────────────────

private data class SampleFriend(val id: Long, val initial: String, val name: String, val status: String, val isOnline: Boolean, val avatarBg: Brush)
private data class SampleParty(val id: Long, val name: String, val sport: String, val sportColor: Color, val memberInitials: List<String>, val maxMembers: Int, val status: String, val statusColor: Color, val showInvite: Boolean)
private data class SamplePlayer(val id: Long, val initial: String, val name: String, val sports: List<String>, val levelText: String, val distance: String, val isOnline: Boolean, val mutualFriends: Int, val requestSent: Boolean, val avatarBg: Brush)

private val sampleFriends = listOf(
    SampleFriend(1, "K", "Kristijan M.", "Playing basketball now 🏀", true, Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF2196F3)))),
    SampleFriend(2, "J", "Jana P.", "Last seen 30 min ago", false, Brush.linearGradient(listOf(Color(0xFFAD1457), Color(0xFFE91E63)))),
    SampleFriend(3, "D", "Darko S.", "🏆 Just won a match!", true, Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)))),
)

private val sampleParties = listOf(
    SampleParty(1, "🏀 Friday Basketball Crew", "Basketball", Color(0xFF4CAF50), listOf("B", "K", "J"), 5, "Active", GreenAccent, true),
    SampleParty(2, "⚽ Sunday Futsal", "Football", OrangeAccent, listOf("B", "M", "D", "A", "S"), 5, "Party Full", Color(0xFF2196F3), false),
    SampleParty(3, "🎾 Tennis Doubles", "Tennis", Color(0xFFFFD700), listOf("B", "K"), 4, "Waiting for players", OrangeAccent, true),
)

private val samplePlayers = listOf(
    SamplePlayer(1, "K", "Kristijan V.", listOf("🏀", "⚽"), "Level 5 • 620 XP", "1.2 km away", true, 0, false, Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF2196F3)))),
    SamplePlayer(2, "J", "Jana T.", listOf("🏐", "🎾"), "Level 7 • 980 XP", "2.5 km away", false, 2, false, Brush.linearGradient(listOf(Color(0xFFAD1457), Color(0xFFE91E63)))),
    SamplePlayer(3, "S", "Stefan R.", listOf("⚽"), "Level 4 • 410 XP", "3.1 km away", false, 0, true, Brush.linearGradient(listOf(Color(0xFF6A1B9A), Color(0xFF9C27B0)))),
    SamplePlayer(4, "A", "Ana G.", listOf("🏐", "🏀"), "Level 6 • 750 XP", "4.0 km away", true, 0, false, Brush.linearGradient(listOf(Color(0xFF00796B), Color(0xFF009688)))),
    SamplePlayer(5, "M", "Milan K.", listOf("🏀", "🎾", "⚽"), "Level 9 • 1,340 XP", "5.2 km away", false, 0, false, Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFFF9800)))),
    SamplePlayer(6, "I", "Igor B.", listOf("⚽", "🏐"), "Level 3 • 290 XP", "6.8 km away", true, 3, false, Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)))),
)

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
