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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ── Light-theme design tokens (mirrors the rest of the player UI) ─────────────
private val LightBg = Color(0xFFF9FAFB)
private val CardWhite = Color.White
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val TextTertiary = Color(0xFF9CA3AF)
private val BorderGray = Color(0xFFE5E7EB)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(
    onNavigateToBookingChat: (bookingId: Long) -> Unit,
    onNavigateToMatchChat: (matchId: Long) -> Unit,
    onNavigateToFriendChat: (userId: Long, name: String, photoUrl: String?) -> Unit = { _, _, _ -> },
    onBack: () -> Unit,
    viewModel: ChatsListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = LightBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chats",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CardWhite,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // ── Tab row ──────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardWhite,
                contentColor = TextPrimary,
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BorderGray),
                    )
                },
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Bookings",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == 0) TextPrimary else TextTertiary,
                        )
                    },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Matches",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == 1) TextPrimary else TextTertiary,
                        )
                    },
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Friends",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == 2) TextPrimary else TextTertiary,
                        )
                    },
                )
            }

            // ── Content with pull-to-refresh ──────────────────────────────────
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    uiState.isLoading && uiState.bookingChats.isEmpty() && uiState.matchChats.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = TextPrimary)
                        }
                    }

                    uiState.error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = uiState.error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    }

                    selectedTab == 0 -> {
                        ChatConversationList(
                            conversations = uiState.bookingChats,
                            emptyMessage = "No booking chats yet.\nApproved or completed bookings appear here.",
                            onConversationClick = { onNavigateToBookingChat(it.id) },
                        )
                    }

                    selectedTab == 1 -> {
                        ChatConversationList(
                            conversations = uiState.matchChats,
                            emptyMessage = "No match chats yet.\nJoin a match to start chatting.",
                            onConversationClick = { onNavigateToMatchChat(it.id) },
                        )
                    }

                    else -> {
                        ChatConversationList(
                            conversations = uiState.friendChats,
                            emptyMessage = "No direct messages yet.\nMessage a friend from your Friends list.",
                            onConversationClick = { conversation ->
                                val friendId = conversation.friendUserId ?: return@ChatConversationList
                                onNavigateToFriendChat(friendId, conversation.title, null)
                            },
                        )
                    }
                }
            }
        }
    }
}

// ── Conversation list ─────────────────────────────────────────────────────────

@Composable
private fun ChatConversationList(
    conversations: List<ChatConversation>,
    emptyMessage: String,
    onConversationClick: (ChatConversation) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (conversations.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(conversations, key = { "${it.type}-${it.id}" }) { conversation ->
                ChatConversationRow(
                    conversation = conversation,
                    onClick = { onConversationClick(conversation) },
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// ── Single conversation row ───────────────────────────────────────────────────

@Composable
private fun ChatConversationRow(
    conversation: ChatConversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── Leading icon ──────────────────────────────────────────────
            val (iconBg, iconTint, iconVector) = when (conversation.type) {
                ChatType.BOOKING -> Triple(
                    Color(0xFFF0FDF4),   // green-50
                    Color(0xFF16A34A),   // green-600
                    Icons.Default.CalendarToday,
                )
                ChatType.MATCH -> Triple(
                    Color(0xFFEFF6FF),   // blue-50
                    Color(0xFF2563EB),   // blue-600
                    Icons.Default.SportsSoccer,
                )
                ChatType.FRIEND -> Triple(
                    Color(0xFFFEF9C3),   // yellow-100
                    Color(0xFF854D0E),   // yellow-800
                    Icons.Default.Person,
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── Title + subtitle ──────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = conversation.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ── Status badge + chevron ────────────────────────────────────
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(status = conversation.status)
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open chat",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ── Status badge ──────────────────────────────────────────────────────────────

@Composable
private fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "approved" -> Color(0xFFDCFCE7) to Color(0xFF166534)   // green
        "confirmed" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)  // blue
        "completed" -> Color(0xFFF3F4F6) to Color(0xFF374151)  // gray
        "open" -> Color(0xFFDCFCE7) to Color(0xFF166534)       // green
        "ongoing" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)    // blue
        "full" -> Color(0xFFFEF9C3) to Color(0xFF92400E)       // amber
        "cancelled" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)  // red
        else -> Color(0xFFF3F4F6) to Color(0xFF374151)          // gray
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = textColor,
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun ChatsListScreenPreview() {
    val bookingChats = listOf(
        ChatConversation(
            id = 1L,
            title = "City Tennis Center",
            subtitle = "2026-05-10 · 10:00–11:00",
            type = ChatType.BOOKING,
            status = "Approved",
        ),
        ChatConversation(
            id = 2L,
            title = "Coach Maria Garcia",
            subtitle = "2026-05-12 · 14:00–15:00",
            type = ChatType.BOOKING,
            status = "Completed",
        ),
    )
    val matchChats = listOf(
        ChatConversation(
            id = 10L,
            title = "Sunday Basketball 5v5",
            subtitle = "Basketball · 2026-05-11",
            type = ChatType.MATCH,
            status = "Open",
        ),
    )

    androidx.compose.material3.MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().background(LightBg)) {
            // TabRow preview
            var selectedTab by remember { mutableIntStateOf(0) }
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardWhite,
                contentColor = TextPrimary,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Bookings") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Matches") },
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Friends") },
                )
            }

            val conversations = when (selectedTab) {
                0 -> bookingChats
                1 -> matchChats
                else -> emptyList()
            }
            ChatConversationList(
                conversations = conversations,
                emptyMessage = "No chats.",
                onConversationClick = {},
            )
        }
    }
}
