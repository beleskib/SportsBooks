package com.example.sportsbook.ui.screens.player.match

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.sportsbook.domain.enums.MatchStatus
import com.example.sportsbook.domain.enums.MatchType
import com.example.sportsbook.domain.enums.MatchVisibility
import com.example.sportsbook.domain.enums.ParticipantRole
import com.example.sportsbook.domain.enums.ParticipantStatus
import com.example.sportsbook.domain.enums.SportType
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.MatchChatMessage
import com.example.sportsbook.domain.model.MatchParticipant
import com.example.sportsbook.ui.common.toFriendlyDate
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkSurfaceLight
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.OrangeAccent
import java.time.Duration
import java.time.Instant

@Composable
fun MatchDetailScreen(
    onBack: () -> Unit,
    onOpenChat: (Long) -> Unit,
    onRatePlayers: (Long) -> Unit,
    onJoinWithParty: (Long) -> Unit = {},
    onBrowseAvailablePlayers: (matchId: Long, sportType: String, minSkill: Int?, maxSkill: Int?) -> Unit = { _, _, _, _ -> },
    onPlayerClick: (Long) -> Unit = {},
    viewModel: MatchDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRatingReminder by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.shareSuccess) {
        if (uiState.shareSuccess) {
            snackbarHostState.showSnackbar("Match shared to your feed!")
            viewModel.clearShareSuccess()
        }
    }

    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            snackbarHostState.showSnackbar("Payment successful!")
        }
    }

    val match = uiState.match
    LaunchedEffect(match) {
        if (match != null &&
            match.status == MatchStatus.COMPLETED &&
            uiState.isParticipant &&
            match.updatedAt != null
        ) {
            val completedAt = try {
                Instant.parse(match.updatedAt)
            } catch (_: Exception) {
                null
            }
            if (completedAt != null && Duration.between(completedAt, Instant.now()).toHours() < 24) {
                showRatingReminder = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenAccent)
                }
            }

            uiState.error != null && uiState.match == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            uiState.match != null -> {
                val currentMatch = uiState.match!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (uiState.match != null) 80.dp else 0.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    MatchDetailHeader(onBack = onBack)
                    Spacer(modifier = Modifier.height(8.dp))
                    MatchHeroCard(match = currentMatch, isLive = currentMatch.status == MatchStatus.OPEN)
                    Spacer(modifier = Modifier.height(12.dp))
                    MatchInfoCardsRow(match = currentMatch)
                    Spacer(modifier = Modifier.height(16.dp))
                    val approvedParticipants = currentMatch.participants.filter { it.status == ParticipantStatus.APPROVED }
                    MatchPlayersSection(
                        participants = approvedParticipants,
                        maxPlayers = currentMatch.maxPlayers,
                        currentUserId = uiState.currentUserId,
                        onPlayerClick = onPlayerClick,
                        onInvite = {
                            onBrowseAvailablePlayers(
                                currentMatch.id,
                                currentMatch.sportType.name.lowercase(),
                                currentMatch.minSkillLevel,
                                currentMatch.maxSkillLevel,
                            )
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    MatchVenueCard(match = currentMatch)
                    Spacer(modifier = Modifier.height(16.dp))
                    MatchChatPreviewSection(chatMessages = uiState.chatPreview, onOpenChat = { onOpenChat(currentMatch.id) })
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (uiState.match != null) {
            MatchDetailBottomBar(
                uiState = uiState,
                onOpenChat = { onOpenChat(uiState.match!!.id) },
                onJoin = { viewModel.joinMatch() },
                onLeave = { viewModel.leaveMatch() },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MatchDetailHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DarkSurface)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Notification icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = DarkTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 3-dot icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DarkSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = DarkTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Match hero card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MatchHeroCard(match: Match, isLive: Boolean) {
    val sportEmoji = sportEmoji(match.sportType)
    val sportGradient = sportGradient(match.sportType)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Sport emoji circle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(sportGradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = sportEmoji, fontSize = 32.sp)
                }

                // Badges column
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // "Competitive" badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B3A1B))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Competitive",
                            color = GreenAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // LIVE badge (animated pulse when OPEN)
                    if (isLive) {
                        LiveBadge()
                    }
                }
            }

            // Match title
            Text(
                text = match.title,
                color = DarkTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 26.sp
            )

            // Hosted by
            Text(
                text = "Hosted by ${match.hostName ?: "Unknown"}",
                color = DarkTextSecondary,
                fontSize = 14.sp
            )

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))

            // Meta row: date / time / skill range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetaChip(
                    icon = Icons.Default.CalendarToday,
                    text = match.matchDate.toFriendlyDate()
                )
                MetaChip(
                    icon = Icons.Default.Schedule,
                    text = match.displayTime
                )
                if (match.minSkillLevel != null && match.maxSkillLevel != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1A2A3A))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Skill ${match.minSkillLevel}-${match.maxSkillLevel}",
                            color = Color(0xFF2196F3),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33EF4444))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = alpha))
            )
            Text(
                text = "LIVE",
                color = Color(0xFFEF4444),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MetaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DarkTextSecondary,
            modifier = Modifier.size(14.dp)
        )
        Text(text = text, color = DarkTextSecondary, fontSize = 12.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Info cards row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MatchInfoCardsRow(match: Match) {
    val costText = when {
        match.isFree -> "Free"
        match.pricePerPlayer > 0 -> "${"%.0f".format(match.pricePerPlayer)} ${match.currency}"
        match.costPerPlayer > 0 -> "${"%.0f".format(match.costPerPlayer)} ${match.currency}"
        else -> "Free"
    }

    // Calculate duration from start/end time
    val durationText = try {
        val parts1 = match.startTime.split(":")
        val parts2 = match.endTime.split(":")
        val start = parts1[0].toInt() * 60 + parts1[1].toInt()
        val end = parts2[0].toInt() * 60 + parts2[1].toInt()
        val diff = end - start
        if (diff > 0) "${diff / 60}h ${if (diff % 60 != 0) "${diff % 60}m" else ""}".trim() else "—"
    } catch (_: Exception) {
        "—"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Groups,
            value = "${match.currentPlayers}/${match.maxPlayers}",
            label = "Players"
        )
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Filled.Chat,
            value = costText,
            label = "Per player"
        )
        InfoCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Schedule,
            value = durationText,
            label = "Duration"
        )
    }
}

@Composable
private fun InfoCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                color = DarkTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = DarkTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Players section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MatchPlayersSection(
    participants: List<MatchParticipant>,
    maxPlayers: Int,
    currentUserId: Long?,
    onPlayerClick: (Long) -> Unit,
    onInvite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Players (${participants.size}/$maxPlayers)",
                color = DarkTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1B3A1B))
                    .clickable { onInvite() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Invite",
                        tint = GreenAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Invite +",
                        color = GreenAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Participant rows
        participants.forEach { participant ->
            ParticipantRow(
                participant = participant,
                isCurrentUser = participant.userId == currentUserId,
                onClick = { onPlayerClick(participant.userId) }
            )
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: MatchParticipant,
    isCurrentUser: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar initial
        val name = participant.userName ?: "?"
        val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(GreenAccent, Color(0xFF2E7D32)))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = DarkTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Name + level info
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = name,
                color = DarkTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = participant.role.displayName,
                color = DarkTextSecondary,
                fontSize = 12.sp
            )
        }

        // Tags (HOST / YOU)
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (participant.role == ParticipantRole.HOST) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1A2A3A))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "HOST",
                        color = Color(0xFF2196F3),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (isCurrentUser) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1B3A1B))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "YOU",
                        color = GreenAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Venue card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MatchVenueCard(match: Match) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Venue",
                color = DarkTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Venue emoji box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1A2A3A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🏟", fontSize = 24.sp)
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = match.venueName ?: match.locationName ?: "Venue TBD",
                        color = DarkTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = match.address ?: match.locationName ?: "Address TBD",
                        color = DarkTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Directions button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceLight)
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = "Directions",
                        tint = GreenAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Directions",
                        color = GreenAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Call button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceLight)
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Call",
                        color = Color(0xFF2196F3),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Match Chat preview section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MatchChatPreviewSection(chatMessages: List<MatchChatMessage>, onOpenChat: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Match Chat",
                color = DarkTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            if (chatMessages.isEmpty()) {
                Text(
                    text = "No messages yet — start the conversation!",
                    color = DarkTextSecondary,
                    fontSize = 13.sp
                )
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
            } else {
                chatMessages.forEach { msg ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = msg.senderName ?: "Unknown",
                                color = GreenAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = msg.createdAt?.takeLast(8)?.take(5) ?: "",
                                color = DarkTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = msg.content,
                            color = DarkTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
                }
            }

            // Open Chat row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenChat() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Open Chat",
                        color = GreenAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "→",
                    color = GreenAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sticky bottom bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MatchDetailBottomBar(
    uiState: MatchDetailUiState,
    onOpenChat: () -> Unit,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBg)
            .border(width = 1.dp, color = DarkBorder, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val chatWeight = if (uiState.canJoin || (uiState.isParticipant && !uiState.isHost)) 0.45f else 1f
            // Chat button
            Box(
                modifier = Modifier
                    .weight(chatWeight)
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .clickable(onClick = onOpenChat),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp), tint = DarkTextPrimary)
                    Text(text = "Chat", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
                }
            }

            // Join Match button
            if (uiState.canJoin) {
                val match = uiState.match
                val priceLabel = when {
                    match == null -> "Join Match"
                    match.isFree -> "Join Free"
                    match.pricePerPlayer > 0 -> "Join · ${"%.0f".format(match.pricePerPlayer)} ${match.currency}"
                    match.costPerPlayer > 0 -> "Join · ${"%.0f".format(match.costPerPlayer)} ${match.currency}"
                    else -> "Join Match"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!uiState.isJoining) GreenAccent else GreenAccent.copy(alpha = 0.5f))
                        .then(if (!uiState.isJoining) Modifier.clickable(onClick = onJoin) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (uiState.isJoining) "Joining..." else priceLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBg,
                    )
                }
            }

            // Leave Match button
            if (uiState.isParticipant && !uiState.isHost) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEF4444))
                        .clickable(onClick = onLeave),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "Leave Match", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun sportEmoji(sportType: SportType): String = when (sportType) {
    SportType.BASKETBALL -> "🏀"
    SportType.FOOTBALL -> "⚽"
    SportType.TENNIS -> "🎾"
    SportType.PADDLE -> "🏓"
    SportType.VOLLEYBALL -> "🏐"
    SportType.SWIMMING -> "🏊"
    SportType.BOXING -> "🥊"
    SportType.MMA -> "🥋"
    SportType.YOGA -> "🧘"
    SportType.PILATES -> "🤸"
    SportType.CROSSFIT -> "💪"
    SportType.RUNNING -> "🏃"
    SportType.CYCLING -> "🚴"
    SportType.GOLF -> "⛳"
    SportType.BADMINTON -> "🏸"
    SportType.TABLE_TENNIS -> "🏓"
    SportType.HANDBALL -> "🤾"
    SportType.BASEBALL -> "⚾"
    SportType.CRICKET -> "🏏"
}

private fun sportGradient(sportType: SportType): List<Color> = when (sportType) {
    SportType.BASKETBALL -> listOf(Color(0xFF7B3A10), Color(0xFF2A1A05))
    SportType.FOOTBALL -> listOf(Color(0xFF1A3A1A), Color(0xFF0A1A0A))
    SportType.TENNIS -> listOf(Color(0xFF2A3A10), Color(0xFF0A1A05))
    SportType.PADDLE -> listOf(Color(0xFF102A3A), Color(0xFF051020))
    SportType.VOLLEYBALL -> listOf(Color(0xFF103A3A), Color(0xFF051A1A))
    SportType.SWIMMING -> listOf(Color(0xFF102040), Color(0xFF051020))
    else -> listOf(Color(0xFF1B3A1B), Color(0xFF0A1A0A))
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun MatchDetailScreenPreview() {
    val sampleMatch = Match(
        id = 1L,
        hostId = 10L,
        hostName = "Jordan Lee",
        sportType = SportType.BASKETBALL,
        matchType = MatchType.STANDALONE,
        status = MatchStatus.OPEN,
        visibility = MatchVisibility.PUBLIC,
        title = "Sunday Pickup Basketball",
        matchDate = "2026-06-08",
        startTime = "18:00",
        endTime = "19:30",
        minPlayers = 6,
        maxPlayers = 12,
        currentPlayers = 4,
        minSkillLevel = 2,
        maxSkillLevel = 4,
        locationName = "City Sports Center",
        venueName = "City Sports Arena",
        address = "123 Main St",
        isFree = false,
        costPerPlayer = 200.0,
        currency = "MKD",
        participants = listOf(
            MatchParticipant(
                id = 1L, matchId = 1L, userId = 10L,
                userName = "Jordan Lee",
                status = ParticipantStatus.APPROVED,
                role = ParticipantRole.HOST
            ),
            MatchParticipant(
                id = 2L, matchId = 1L, userId = 20L,
                userName = "Alex M.",
                status = ParticipantStatus.APPROVED,
                role = ParticipantRole.PLAYER
            )
        )
    )

    val previewUiState = MatchDetailUiState(
        match = sampleMatch,
        currentUserId = 20L
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            MatchDetailHeader(onBack = {})
            Spacer(modifier = Modifier.height(8.dp))
            MatchHeroCard(match = sampleMatch, isLive = true)
            Spacer(modifier = Modifier.height(12.dp))
            MatchInfoCardsRow(match = sampleMatch)
            Spacer(modifier = Modifier.height(16.dp))
            MatchPlayersSection(
                participants = sampleMatch.participants.filter { it.status == ParticipantStatus.APPROVED },
                maxPlayers = sampleMatch.maxPlayers,
                currentUserId = 20L,
                onPlayerClick = {},
                onInvite = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            MatchVenueCard(match = sampleMatch)
            Spacer(modifier = Modifier.height(16.dp))
            MatchChatPreviewSection(chatMessages = emptyList(), onOpenChat = {})
            Spacer(modifier = Modifier.height(88.dp))
        }
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            MatchDetailBottomBar(
                uiState = previewUiState,
                onOpenChat = {},
                onJoin = {},
                onLeave = {}
            )
        }
    }
}
