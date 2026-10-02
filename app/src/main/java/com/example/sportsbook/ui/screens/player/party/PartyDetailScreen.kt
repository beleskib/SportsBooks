package com.example.sportsbook.ui.screens.player.party

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.model.Party
import com.example.sportsbook.domain.model.PartyMember
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun PartyDetailScreen(
    onInviteFriends: (Long) -> Unit,
    onFindMatch: () -> Unit,
    onMatchClick: (Long) -> Unit = {},
    onBack: () -> Unit,
    viewModel: PartyDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.disbanded) {
        if (uiState.disbanded) onBack()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        when {
            uiState.isLoading && uiState.party == null -> LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.error != null && uiState.party == null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadParty,
            )
            else -> {
                val party = uiState.party

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // ── Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface)
                                    .clickable(onClick = onBack),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurface),
                                    contentAlignment = Alignment.Center,
                                ) { Text("↗", fontSize = 16.sp, color = DarkTextPrimary) }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurface),
                                    contentAlignment = Alignment.Center,
                                ) { Text("⋮", fontSize = 16.sp, color = DarkTextPrimary) }
                            }
                        }
                    }

                    // ── Hero card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFFF9800), Color(0xFFFFB74D))))
                                .padding(20.dp),
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 10.dp),
                                ) {
                                    val sportEmoji = partySportEmoji(party?.sportType)
                                    Text(sportEmoji, fontSize = 24.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                    ) {
                                        Text(
                                            party?.sportType?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Sport",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                        )
                                    }
                                    val statusColor = if (party?.status == "active" || party?.status == "ready") Color(0xFF4CAF50) else Color(0xFFFF9800)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(statusColor)
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                    ) {
                                        Text(
                                            (party?.status ?: "forming").replaceFirstChar { it.uppercase() },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                        )
                                    }
                                }
                                Text(
                                    party?.name ?: "Party",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                                Text(
                                    "Led by ${party?.leaderName ?: "Unknown"} • ${party?.members?.size ?: 0} members",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                )
                            }
                        }
                    }

                    // ── Stats row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                "${party?.members?.size ?: 0}/8" to "Members",
                                (if (uiState.partyMatch != null) "1" else "0") to "Matches",
                                (party?.status?.replaceFirstChar { it.uppercase() } ?: "—") to "Status",
                            ).forEach { (value, label) ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DarkSurface)
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DarkTextPrimary)
                                    Text(label, fontSize = 10.sp, color = DarkTextSecondary, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }

                    // ── Action buttons
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            // Find Match
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFF9800))
                                    .clickable(enabled = uiState.isLeader) { onFindMatch() }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("🔍 Find Match", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                            }
                            // Invite
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface)
                                    .clickable(enabled = uiState.isLeader) {
                                        party?.let { onInviteFriends(it.id) }
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("👥 Invite", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                            }
                            // Chat
                            val chatContext = LocalContext.current
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Transparent)
                                    .then(
                                        Modifier.background(
                                            Color.Transparent,
                                        ),
                                    )
                                    .clickable {
                                        Toast.makeText(chatContext, "Party chat coming soon", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 11.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DarkSurface),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("💬 Chat", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTextSecondary, modifier = Modifier.padding(vertical = 12.dp))
                                }
                            }
                        }
                    }

                    // ── Invite accept/decline for invited members
                    if (uiState.isInvitedMember) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GreenAccent)
                                        .clickable(enabled = !uiState.isActioning) { viewModel.respondToInvite(true) }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("✓ Accept", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DarkSurface)
                                        .clickable(enabled = !uiState.isActioning) { viewModel.respondToInvite(false) }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("✕ Decline", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextSecondary)
                                }
                            }
                        }
                    }

                    // ── Members section
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "Members (${party?.members?.size ?: 0}/8)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary,
                            )
                            Text(
                                "Invite +",
                                fontSize = 13.sp,
                                color = GreenAccent,
                                modifier = Modifier.clickable(enabled = uiState.isLeader) {
                                    party?.let { onInviteFriends(it.id) }
                                },
                            )
                        }
                    }

                    val members = party?.members ?: emptyList()
                    if (members.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurface)
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("No members yet", fontSize = 13.sp, color = DarkTextSecondary)
                            }
                        }
                    } else {
                        items(members, key = { it.id }) { member ->
                            MemberRow(
                                member = member,
                                isLeader = member.userId == party?.leaderId,
                                isCurrentUser = member.userId == uiState.currentUserId,
                            )
                        }
                    }

                    // ── Upcoming Matches section
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Upcoming Matches", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                            Text("See All", fontSize = 13.sp, color = GreenAccent)
                        }
                    }

                    item {
                        val partyMatch = uiState.partyMatch
                        if (partyMatch != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1B3A1E))
                                    .clickable { onMatchClick(partyMatch.id) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(GreenAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("⚽", fontSize = 22.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        partyMatch.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkTextPrimary,
                                    )
                                    Text(
                                        "${partyMatch.matchDate} • ${partyMatch.startTime}" +
                                            if (partyMatch.locationName != null) " • ${partyMatch.locationName}" else "",
                                        fontSize = 12.sp,
                                        color = DarkTextSecondary,
                                    )
                                }
                                Text(
                                    partyMatch.status.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GreenAccent,
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurface)
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("No upcoming matches", fontSize = 13.sp, color = DarkTextSecondary)
                            }
                        }
                    }

                    // ── Party Record
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Record",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            val memberCount = party?.members?.filter { it.status == "accepted" }?.size ?: 0
                            val matchCount = if (uiState.partyMatch != null) 1 else 0
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$memberCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
                                Text("Members", fontSize = 11.sp, color = DarkTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$matchCount", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
                                Text("Matches", fontSize = 11.sp, color = DarkTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("—", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkTextSecondary)
                                Text("W/L", fontSize = 11.sp, color = DarkTextSecondary)
                            }
                        }
                    }

                    // ── Chat preview
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 10.dp),
                        ) {
                            Text("Party Chat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("No messages yet", fontSize = 13.sp, color = DarkTextSecondary)
                            }
                            val openChatContext = LocalContext.current
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        Toast.makeText(openChatContext, "Party chat coming soon", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(top = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("Open Chat →", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF9800))
                            }
                        }
                    }

                    // ── Disband button (leader only)
                    if (uiState.isLeader) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEF5350).copy(alpha = 0.1f))
                                    .clickable(enabled = !uiState.isActioning) { viewModel.disbandParty() }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    if (uiState.isActioning) "Disbanding..." else "Disband Party",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFEF5350),
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

// ── Member row ────────────────────────────────────────────────────────────────

@Composable
private fun MemberRow(member: PartyMember, isLeader: Boolean, isCurrentUser: Boolean) {
    val avatarColor = memberAvatarColor(member.userId)
    val initial = member.userName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(40.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(initial, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            // Online dot
            val isOnline = member.status == "accepted"
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFF555555))
                    .align(Alignment.BottomEnd),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(member.userName ?: "Unknown", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            Text(
                when (member.status) {
                    "accepted" -> "Online"
                    "invited" -> "Invited"
                    "declined" -> "Declined"
                    else -> member.status
                },
                fontSize = 11.sp,
                color = DarkTextSecondary,
            )
        }
        when {
            isLeader -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFA726))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) { Text("LEADER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black) }
            }
            isCurrentUser -> {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GreenAccent)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) { Text("YOU", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
    }
}


// ── Helpers ───────────────────────────────────────────────────────────────────

private fun memberAvatarColor(userId: Long): Color {
    val colors = listOf(
        Color(0xFFFFA726), Color(0xFF4CAF50), Color(0xFF2196F3),
        Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF00BCD4),
    )
    return colors[(userId % colors.size).toInt()]
}

private fun partySportEmoji(sport: String?): String = when (sport?.uppercase()) {
    "BASKETBALL" -> "🏀"
    "FOOTBALL" -> "⚽"
    "TENNIS" -> "🎾"
    "VOLLEYBALL" -> "🏐"
    "PADDLE" -> "🎾"
    else -> "🏅"
}


// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PartyDetailScreenPreview() {
    val previewMembers = listOf(
        PartyMember(1L, 10L, "Stefan M.", null, "accepted"),
        PartyMember(2L, 11L, "Bojan B.", null, "accepted"),
        PartyMember(3L, 12L, "Marko T.", null, "invited"),
    )
    Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg),
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFFF9800))))
                            .padding(20.dp),
                    ) {
                        Column {
                            Text("🏀 Basketball • Active", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                            Text("Skopje Ballers", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
                items(previewMembers) { member ->
                    MemberRow(member = member, isLeader = member.userId == 10L, isCurrentUser = member.userId == 11L)
                }
            }
        }
}
