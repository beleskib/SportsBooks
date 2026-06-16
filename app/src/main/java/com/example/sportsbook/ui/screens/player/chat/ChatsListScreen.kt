package com.example.sportsbook.ui.screens.player.chat

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun ChatsListScreen(
    onNavigateToBookingChat: (bookingId: Long) -> Unit,
    onNavigateToMatchChat: (matchId: Long) -> Unit,
    onNavigateToFriendChat: (userId: Long, name: String, photoUrl: String?) -> Unit = { _, _, _ -> },
    onNavigateToPartyChat: (partyId: Long) -> Unit = {},
    onCreateParty: () -> Unit = {},
    onBack: () -> Unit,
    viewModel: ChatsListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    val allChats = uiState.friendChats + uiState.matchChats + uiState.bookingChats + uiState.partyChats
    val tabs = listOf("All" to allChats.size, "Direct" to uiState.friendChats.size, "Groups" to uiState.matchChats.size + uiState.partyChats.size, "Bookings" to uiState.bookingChats.size)

    val displayedChats = when (selectedTab) {
        0 -> allChats
        1 -> uiState.friendChats
        2 -> uiState.matchChats + uiState.partyChats
        3 -> uiState.bookingChats
        else -> allChats
    }

    val isEmpty = displayedChats.isEmpty() && !uiState.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Messages", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .clickable { },
                contentAlignment = Alignment.Center,
            ) {
                Text("✏", fontSize = 16.sp)
            }
        }

        // ── Search bar ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🔍", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Search conversations...", fontSize = 14.sp, color = DarkTextSecondary)
        }

        // ── Tab bar ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tabs.forEachIndexed { index, (label, count) ->
                val isActive = selectedTab == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) GreenAccent else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isActive) Color.White else DarkTextSecondary)
                        if (count > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isActive) Color.White.copy(alpha = 0.3f) else DarkSurface)
                                    .padding(horizontal = 6.dp, vertical = 1.dp),
                            ) {
                                Text("$count", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isActive) Color.White else DarkTextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // ── Content ──────────────────────────────────────────────────
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.fillMaxSize())
            isEmpty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No conversations yet.\nStart chatting after a booking or match!",
                        fontSize = 14.sp,
                        color = DarkTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(displayedChats) { conv ->
                        ChatConversationRow(
                            conv = conv,
                            onClick = {
                                when (conv.type) {
                                    ChatType.BOOKING -> onNavigateToBookingChat(conv.id)
                                    ChatType.MATCH -> onNavigateToMatchChat(conv.id)
                                    ChatType.FRIEND -> onNavigateToFriendChat(conv.friendUserId ?: conv.id, conv.title, conv.friendPhotoUrl)
                                    ChatType.PARTY -> onNavigateToPartyChat(conv.id)
                                }
                            },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

// ── Chat row ──────────────────────────────────────────────────────────────────

@Composable
private fun ChatConversationRow(conv: ChatConversation, onClick: () -> Unit) {
    val (emoji, avatarBg) = when (conv.type) {
        ChatType.FRIEND -> "👤" to Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF2196F3)))
        ChatType.MATCH -> "⚽" to Brush.linearGradient(listOf(Color(0xFF2E7D32), Color(0xFF4CAF50)))
        ChatType.BOOKING -> "📅" to Brush.linearGradient(listOf(Color(0xFF5E35B1), Color(0xFF9C27B0)))
        ChatType.PARTY -> "🎉" to Brush.linearGradient(listOf(Color(0xFFAD1457), Color(0xFFE91E63)))
    }
    val typeLabel = when (conv.type) {
        ChatType.MATCH -> "Match"
        ChatType.BOOKING -> "Booking"
        ChatType.PARTY -> "Party"
        else -> null
    }
    val typeLabelColor = when (conv.type) {
        ChatType.MATCH -> GreenAccent
        ChatType.BOOKING -> Color(0xFF9C27B0)
        ChatType.PARTY -> Color(0xFFE91E63)
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(avatarBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (typeLabel != null && typeLabelColor != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeLabelColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    ) {
                        Text(typeLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = typeLabelColor)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(conv.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(conv.subtitle, fontSize = 12.sp, color = DarkTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp))
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(start = 80.dp, end = 16.dp).background(DarkBorder))
}


// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun ChatsListScreenPreview() {
    ChatsListScreen(
            onNavigateToBookingChat = {},
            onNavigateToMatchChat = {},
            onBack = {},
        )
}
