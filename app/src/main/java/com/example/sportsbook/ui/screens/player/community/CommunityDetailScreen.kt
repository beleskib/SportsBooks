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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.domain.model.Community
import com.example.sportsbook.domain.model.CommunityMember
import com.example.sportsbook.domain.model.Lobby
import com.example.sportsbook.ui.theme.Navy700
import com.example.sportsbook.ui.theme.Navy800
import com.example.sportsbook.ui.theme.Navy900
import com.example.sportsbook.ui.theme.SportGreen
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = community?.name ?: "Community",
                        color = WarmWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WarmWhite
                        )
                    }
                },
                actions = {
                    if (uiState.isUserAdmin) {
                        IconButton(onClick = { community?.id?.let { onInviteFriends(it) } }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Invite Friends", tint = USOpenGold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        },
        floatingActionButton = {
            if (uiState.isUserMember || uiState.isUserAdmin) {
                FloatingActionButton(
                    onClick = { community?.id?.let { onCreateLobby(it) } },
                    containerColor = USOpenGold
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Lobby", tint = Navy900)
                }
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = USOpenGold)
                }
            }

            community == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = uiState.error ?: "Community not found",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Community header
                    CommunityHeader(community = community)

                    // Join button if not a member
                    if (!uiState.isUserMember && !uiState.isUserAdmin) {
                        Button(
                            onClick = viewModel::joinCommunity,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = USOpenGold)
                        ) {
                            Text("Join Community", color = Navy900, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Pending approvals (admin only)
                    if (uiState.isUserAdmin && uiState.pendingMembers.isNotEmpty()) {
                        PendingApprovals(
                            pendingMembers = uiState.pendingMembers,
                            onApprove = { userId -> viewModel.respondToMember(userId, true) },
                            onReject = { userId -> viewModel.respondToMember(userId, false) }
                        )
                    }

                    // Tabs
                    val tabTitles = listOf("Lobbies", "Members")
                    TabRow(
                        selectedTabIndex = uiState.selectedTab.ordinal,
                        containerColor = Navy800,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                                color = USOpenGold
                            )
                        }
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = uiState.selectedTab.ordinal == index,
                                onClick = { viewModel.selectTab(CommunityDetailTab.entries[index]) },
                                text = {
                                    Text(
                                        text = title,
                                        color = if (uiState.selectedTab.ordinal == index) USOpenGold
                                        else WarmWhite.copy(alpha = 0.6f)
                                    )
                                }
                            )
                        }
                    }

                    when (uiState.selectedTab) {
                        CommunityDetailTab.LOBBIES -> LobbiesTab(
                            lobbies = uiState.lobbies,
                            onLobbyClick = onLobbyClick
                        )
                        CommunityDetailTab.MEMBERS -> MembersTab(
                            members = uiState.members,
                            isAdmin = uiState.isUserAdmin,
                            onRemoveMember = viewModel::removeMember
                        )
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
            .background(Navy800)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Navy900),
                contentAlignment = Alignment.Center
            ) {
                if (!community.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = community.imageUrl,
                        contentDescription = community.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        tint = USOpenGold,
                        modifier = Modifier.size(32.dp)
                    )
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
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmWhite.copy(alpha = 0.7f)
                )
            }
        }
        if (!community.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = community.description,
                style = MaterialTheme.typography.bodyMedium,
                color = WarmWhite.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun PendingApprovals(
    pendingMembers: List<CommunityMember>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Navy700),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Pending Requests (${pendingMembers.size})",
                style = MaterialTheme.typography.titleSmall,
                color = USOpenGold,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            pendingMembers.forEach { member ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MemberAvatar(name = member.displayName, photoUrl = member.photoUrl, size = 36)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = member.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmWhite,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onApprove(member.userId) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Approve", tint = SportGreen)
                    }
                    IconButton(
                        onClick = { onReject(member.userId) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun LobbiesTab(
    lobbies: List<Lobby>,
    onLobbyClick: (Long) -> Unit
) {
    if (lobbies.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No lobbies yet. Create one!",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmWhite.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Navy700),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = lobby.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SportBadge(sportType = lobby.sportType)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${lobby.scheduledDate}  ${lobby.scheduledTime}",
                style = MaterialTheme.typography.bodySmall,
                color = WarmWhite.copy(alpha = 0.7f)
            )
            if (!lobby.venueName.isNullOrBlank()) {
                Text(
                    text = lobby.venueName,
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmWhite.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Player count progress
            val progress = if (lobby.maxPlayers > 0) lobby.currentPlayers.toFloat() / lobby.maxPlayers else 0f
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = USOpenGold,
                    trackColor = Navy900
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${lobby.currentPlayers}/${lobby.maxPlayers} players",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmWhite.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun MembersTab(
    members: List<CommunityMember>,
    isAdmin: Boolean,
    onRemoveMember: (Long) -> Unit
) {
    if (members.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No members yet",
                style = MaterialTheme.typography.bodyLarge,
                color = WarmWhite.copy(alpha = 0.5f)
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(members, key = { it.id }) { member ->
                MemberRow(
                    member = member,
                    isAdmin = isAdmin,
                    onRemove = { onRemoveMember(member.userId) }
                )
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: CommunityMember,
    isAdmin: Boolean,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemberAvatar(name = member.displayName, photoUrl = member.photoUrl, size = 40)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = WarmWhite,
                fontWeight = FontWeight.Medium
            )
            RoleBadge(role = member.role)
        }
        if (isAdmin && !member.isOwner) {
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
internal fun MemberAvatar(
    name: String,
    photoUrl: String?,
    size: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Navy800),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.labelMedium,
                color = USOpenGold,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RoleBadge(role: String) {
    val (label, color) = when (role) {
        "owner" -> "Owner" to USOpenGold
        "admin" -> "Admin" to SportGreen
        else -> return
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 1.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun CommunityDetailScreenPreview() {
    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = { Text("Basketball Crew", color = WarmWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CommunityHeader(
                community = Community(
                    id = 1,
                    name = "Basketball Crew",
                    sportType = "basketball",
                    description = "A community for basketball enthusiasts in the city.",
                    memberCount = 12,
                    maxMembers = 30
                )
            )
            TabRow(selectedTabIndex = 0, containerColor = Navy800) {
                Tab(selected = true, onClick = {}, text = { Text("Lobbies") })
                Tab(selected = false, onClick = {}, text = { Text("Members") })
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    listOf(
                        Lobby(id = 1, title = "Friday Night Game", sportType = "basketball", scheduledDate = "2026-03-27", scheduledTime = "19:00", currentPlayers = 4, maxPlayers = 10),
                        Lobby(id = 2, title = "Weekend Practice", sportType = "basketball", scheduledDate = "2026-03-28", scheduledTime = "10:00", currentPlayers = 8, maxPlayers = 10)
                    )
                ) { lobby ->
                    LobbyCard(lobby = lobby, onClick = {})
                }
            }
        }
    }
}
