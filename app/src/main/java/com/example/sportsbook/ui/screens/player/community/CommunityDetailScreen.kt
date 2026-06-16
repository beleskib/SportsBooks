package com.example.sportsbook.ui.screens.player.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.model.Community
import com.example.sportsbook.domain.model.CommunityMember
import com.example.sportsbook.domain.model.Lobby
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.DarkTextTertiary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.SportGreen

@Composable
fun CommunityDetailScreen(
    onBack: () -> Unit,
    onLobbyClick: (Long) -> Unit,
    onCreateLobby: (Long) -> Unit,
    onInviteFriends: (Long) -> Unit,
    viewModel: CommunityDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val community = uiState.community

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }

            community == null -> {
                // Header still visible even on error
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface).clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                    }
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error ?: "Community not found",
                        color = Color(0xFFEF5350),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // ── Header ────────────────────────────────────────────
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
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = community.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (uiState.isUserAdmin) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface)
                                    .clickable { community.id.let { onInviteFriends(it) } },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Invite", tint = GreenAccent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        // Community header block
                        CommunityHeader(community = community)

                        // Join button
                        if (!uiState.isUserMember && !uiState.isUserAdmin) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GreenAccent)
                                    .clickable(onClick = viewModel::joinCommunity),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("Join Community", color = DarkBg, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            }
                        }

                        // Pending approvals
                        if (uiState.isUserAdmin && uiState.pendingMembers.isNotEmpty()) {
                            PendingApprovals(
                                pendingMembers = uiState.pendingMembers,
                                onApprove = { userId -> viewModel.respondToMember(userId, true) },
                                onReject = { userId -> viewModel.respondToMember(userId, false) },
                            )
                        }

                        // ── Pill tabs ──────────────────────────────────────
                        val tabTitles = listOf("Lobbies", "Members")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            tabTitles.forEachIndexed { index, title ->
                                val isSelected = uiState.selectedTab.ordinal == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(50))
                                        .background(if (isSelected) GreenAccent else DarkSurface)
                                        .border(1.dp, if (isSelected) GreenAccent else DarkBorder, RoundedCornerShape(50))
                                        .clickable { viewModel.selectTab(CommunityDetailTab.entries[index]) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else DarkTextSecondary,
                                    )
                                }
                            }
                        }

                        when (uiState.selectedTab) {
                            CommunityDetailTab.LOBBIES -> LobbiesTab(lobbies = uiState.lobbies, onLobbyClick = onLobbyClick)
                            CommunityDetailTab.MEMBERS -> MembersTab(
                                members = uiState.members,
                                isAdmin = uiState.isUserAdmin,
                                onRemoveMember = viewModel::removeMember,
                            )
                        }
                    }
                }

                // ── FAB: create lobby ──────────────────────────────────────
                if (uiState.isUserMember || uiState.isUserAdmin) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 16.dp, end = 16.dp)
                            .clip(CircleShape)
                            .background(GreenAccent)
                            .clickable { community.id.let { onCreateLobby(it) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Lobby", tint = DarkBg)
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityHeader(community: Community) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkBg),
                contentAlignment = Alignment.Center,
            ) {
                if (!community.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = community.imageUrl,
                        contentDescription = community.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                if (!community.sportType.isNullOrBlank()) {
                    SportBadge(sportType = community.sportType)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = "${community.memberCount} / ${community.maxMembers} members",
                    fontSize = 12.sp,
                    color = DarkTextSecondary,
                )
            }
        }
        if (!community.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = community.description, fontSize = 13.sp, color = DarkTextSecondary)
        }
    }
}

@Composable
private fun PendingApprovals(
    pendingMembers: List<CommunityMember>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .padding(12.dp),
    ) {
        Text("Pending Requests (${pendingMembers.size})", fontSize = 13.sp, color = GreenAccent, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        pendingMembers.forEach { member ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MemberAvatar(name = member.displayName, photoUrl = member.photoUrl, size = 36)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = member.displayName, fontSize = 14.sp, color = DarkTextPrimary, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(SportGreen.copy(alpha = 0.15f))
                        .clickable { onApprove(member.userId) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Approve", tint = SportGreen, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFEF5350).copy(alpha = 0.15f))
                        .clickable { onReject(member.userId) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LobbiesTab(
    lobbies: List<Lobby>,
    onLobbyClick: (Long) -> Unit,
) {
    if (lobbies.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(text = "No lobbies yet. Create one!", fontSize = 14.sp, color = DarkTextTertiary, textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(lobbies, key = { it.id }) { lobby ->
                LobbyCard(lobby = lobby, onClick = { onLobbyClick(lobby.id) })
            }
        }
    }
}

@Composable
internal fun LobbyCard(
    lobby: Lobby,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = lobby.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            SportBadge(sportType = lobby.sportType)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "${lobby.scheduledDate}  ${lobby.scheduledTime}", fontSize = 12.sp, color = DarkTextSecondary)
        if (!lobby.venueName.isNullOrBlank()) {
            Text(text = lobby.venueName, fontSize = 12.sp, color = DarkTextTertiary)
        }
        Spacer(modifier = Modifier.height(8.dp))
        val progress = if (lobby.maxPlayers > 0) lobby.currentPlayers.toFloat() / lobby.maxPlayers else 0f
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(DarkBg)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(progress).height(6.dp).clip(RoundedCornerShape(3.dp)).background(GreenAccent)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("${lobby.currentPlayers}/${lobby.maxPlayers} players", fontSize = 11.sp, color = DarkTextSecondary)
        }
    }
}

@Composable
private fun MembersTab(
    members: List<CommunityMember>,
    isAdmin: Boolean,
    onRemoveMember: (Long) -> Unit,
) {
    if (members.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(text = "No members yet", fontSize = 14.sp, color = DarkTextTertiary)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(members, key = { it.id }) { member ->
                MemberRow(
                    member = member,
                    isAdmin = isAdmin,
                    onRemove = { onRemoveMember(member.userId) },
                )
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: CommunityMember,
    isAdmin: Boolean,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MemberAvatar(name = member.displayName, photoUrl = member.photoUrl, size = 40)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = member.displayName, fontSize = 14.sp, color = DarkTextPrimary, fontWeight = FontWeight.Medium)
            RoleBadge(role = member.role)
        }
        if (isAdmin && !member.isOwner) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFEF5350).copy(alpha = 0.1f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFEF5350).copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
internal fun MemberAvatar(
    name: String,
    photoUrl: String?,
    size: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(size.dp).clip(CircleShape).background(DarkBg),
        contentAlignment = Alignment.Center,
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                fontSize = 12.sp,
                color = GreenAccent,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun RoleBadge(role: String) {
    val (label, color) = when (role) {
        "owner" -> "Owner" to GreenAccent
        "admin" -> "Admin" to SportGreen
        else -> return
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 1.dp),
    ) {
        Text(text = label, fontSize = 11.sp, color = color)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun CommunityDetailScreenPreview() {
    val sampleCommunity = Community(
        id = 1,
        name = "Basketball Crew",
        sportType = "basketball",
        description = "A community for basketball enthusiasts in the city.",
        memberCount = 12,
        maxMembers = 30,
    )
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Basketball Crew", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
        }
        CommunityHeader(community = sampleCommunity)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            listOf("Lobbies" to true, "Members" to false).forEach { (title, isSelected) ->
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(50))
                        .background(if (isSelected) GreenAccent else DarkSurface)
                        .border(1.dp, if (isSelected) GreenAccent else DarkBorder, RoundedCornerShape(50))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (isSelected) Color.White else DarkTextSecondary)
                }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(
                listOf(
                    Lobby(id = 1, title = "Friday Night Game", sportType = "basketball", scheduledDate = "2026-03-27", scheduledTime = "19:00", currentPlayers = 4, maxPlayers = 10),
                    Lobby(id = 2, title = "Weekend Practice", sportType = "basketball", scheduledDate = "2026-03-28", scheduledTime = "10:00", currentPlayers = 8, maxPlayers = 10),
                ),
            ) { lobby ->
                LobbyCard(lobby = lobby, onClick = {})
            }
        }
    }
}
