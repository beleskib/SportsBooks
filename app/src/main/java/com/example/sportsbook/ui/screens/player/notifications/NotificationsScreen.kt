package com.example.sportsbook.ui.screens.player.notifications

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.sportsbook.domain.enums.NotificationType
import com.example.sportsbook.domain.model.Notification
import com.example.sportsbook.ui.common.EmptyStateView
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.OrangeAccent

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNotificationClick: (Notification) -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
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
            Spacer(modifier = Modifier.width(12.dp))
            Text("Notifications", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.weight(1f))
            Text(
                text = "Mark all read",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GreenAccent,
                modifier = Modifier.clickable { viewModel.markAllAsRead() },
            )
        }

        // ── Content ──────────────────────────────────────────────────────
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.fillMaxWidth())
            uiState.error != null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadNotifications,
                modifier = Modifier.fillMaxWidth(),
            )
            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (uiState.notifications.isEmpty()) {
                        EmptyStateView(
                            title = "No notifications",
                            subtitle = "You're all caught up!",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        val unread = uiState.notifications.filter { !it.isRead }
                        val read = uiState.notifications.filter { it.isRead }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (unread.isNotEmpty()) {
                                item { SectionLabel("New") }
                                items(unread, key = { it.id }) { notif ->
                                    NotificationItem(
                                        notif = notif,
                                        isUnread = true,
                                        pendingAction = notif.id in uiState.pendingActionIds,
                                        resolvedAction = uiState.resolvedActions[notif.id],
                                        onAcceptJoin = { viewModel.respondToJoinRequest(notif, approve = true) },
                                        onDeclineJoin = { viewModel.respondToJoinRequest(notif, approve = false) },
                                        onClick = { onNotificationClick(notif) },
                                    )
                                }
                            }
                            if (read.isNotEmpty()) {
                                item { SectionLabel("Earlier") }
                                items(read, key = { it.id }) { notif ->
                                    NotificationItem(
                                        notif = notif,
                                        isUnread = false,
                                        pendingAction = notif.id in uiState.pendingActionIds,
                                        resolvedAction = uiState.resolvedActions[notif.id],
                                        onAcceptJoin = { viewModel.respondToJoinRequest(notif, approve = true) },
                                        onDeclineJoin = { viewModel.respondToJoinRequest(notif, approve = false) },
                                        onClick = { onNotificationClick(notif) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Notification item ─────────────────────────────────────────────────────────

@Composable
private fun NotificationItem(
    notif: Notification,
    isUnread: Boolean,
    pendingAction: Boolean,
    resolvedAction: String?,
    onAcceptJoin: () -> Unit,
    onDeclineJoin: () -> Unit,
    onClick: () -> Unit,
) {
    val (iconEmoji, iconBg) = notifIconAndBg(notif.type)
    val showActions = (notif.type == NotificationType.MATCH_JOIN_REQUEST ||
            notif.type == NotificationType.FRIEND_REQUEST ||
            notif.type == NotificationType.MATCH_INVITE) &&
            resolvedAction == null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isUnread) Color(0xFF1A1F1A) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Text(iconEmoji, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(notif.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTextPrimary)
            Text(
                text = notif.body,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.67f),
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 2.dp),
            )

            if (showActions && !pendingAction) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val acceptLabel = when (notif.type) {
                        NotificationType.MATCH_INVITE, NotificationType.MATCH_JOIN_REQUEST -> "Join Match"
                        NotificationType.FRIEND_REQUEST -> "Accept"
                        else -> "Accept"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenAccent)
                            .clickable(onClick = onAcceptJoin)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text(acceptLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2A2A2A))
                            .clickable(onClick = onDeclineJoin)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Text("Decline", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkTextSecondary)
                    }
                }
            }

            if (resolvedAction != null) {
                val (resolvedBg, resolvedColor) = if (resolvedAction == "Approved") {
                    GreenAccent.copy(alpha = 0.15f) to GreenAccent
                } else {
                    Color(0xFF2A2A2A) to DarkTextSecondary
                }
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(resolvedBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(resolvedAction, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = resolvedColor)
                }
            }

            val time = notif.createdAt ?: ""
            if (time.isNotBlank()) {
                Text(time, fontSize = 11.sp, color = Color(0xFF555555), modifier = Modifier.padding(top = 4.dp))
            }
        }

        // Unread dot
        if (isUnread) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(GreenAccent),
            )
        }
    }

    // Divider
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1E1E1E)))
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = DarkTextSecondary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

private fun notifIconAndBg(type: NotificationType): Pair<String, Color> = when (type) {
    NotificationType.BOOKING_CONFIRMED, NotificationType.BOOKING_APPROVED -> "🏀" to Color(0xFF1B3A1E)
    NotificationType.BOOKING_REQUEST -> "📅" to Color(0xFF1B3A1E)
    NotificationType.BOOKING_CANCELLED, NotificationType.BOOKING_DECLINED -> "❌" to Color(0xFF3A1B1B)
    NotificationType.BOOKING_REMINDER -> "⏰" to Color(0xFF3A2E1B)
    NotificationType.MATCH_JOIN_REQUEST, NotificationType.MATCH_INVITE -> "⚔️" to Color(0xFF1B3A1E)
    NotificationType.MATCH_JOIN_APPROVED -> "✅" to Color(0xFF1B3A1E)
    NotificationType.MATCH_JOIN_DECLINED -> "❌" to Color(0xFF3A1B1B)
    NotificationType.MATCH_CHAT_MESSAGE, NotificationType.MATCH_STARTING_SOON -> "⚡" to Color(0xFF1E1E1E)
    NotificationType.MATCH_CANCELLED -> "❌" to Color(0xFF3A1B1B)
    NotificationType.MATCH_COMPLETED -> "🏆" to Color(0xFF1E1E1E)
    NotificationType.MATCH_LOBBY_FULL -> "👥" to Color(0xFF1E1E1E)
    NotificationType.FRIEND_REQUEST -> "👥" to Color(0xFF1B2D3A)
    NotificationType.FRIEND_REQUEST_ACCEPTED -> "👍" to Color(0xFF1B2D3A)
    NotificationType.PARTY_INVITE, NotificationType.PARTY_INVITE_ACCEPTED -> "🎉" to Color(0xFF1E1E1E)
    NotificationType.PARTY_INVITE_DECLINED -> "❌" to Color(0xFF3A1B1B)
    NotificationType.PARTY_JOINED_MATCH -> "🎯" to Color(0xFF1E1E1E)
    NotificationType.PARTY_DISBANDED -> "💔" to Color(0xFF3A1B1B)
    NotificationType.FEED_POST_LIKED -> "❤️" to Color(0xFF1E1E1E)
    NotificationType.FEED_POST_COMMENTED -> "💬" to Color(0xFF1E1E1E)
    NotificationType.RATING_RECEIVED, NotificationType.REVIEW_REMINDER -> "⭐" to Color(0xFF1E1E1E)
    NotificationType.SPLIT_PAYMENT_REQUEST -> "💰" to Color(0xFF1E1E1E)
    NotificationType.GENERAL -> "🔔" to Color(0xFF1E1E1E)
}

// ── Preview ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun NotificationsScreenPreview() {
    val sampleNotifs = listOf(
        Notification(id = 1L, type = NotificationType.BOOKING_CONFIRMED, title = "Booking Confirmed", body = "Your booking at Arena Sport Center for today at 18:00 has been confirmed.", isRead = false, createdAt = "5 minutes ago"),
        Notification(id = 2L, type = NotificationType.FRIEND_REQUEST, title = "Friend Request", body = "Marko T. wants to be your friend", isRead = false, createdAt = "12 minutes ago"),
        Notification(id = 3L, type = NotificationType.MATCH_COMPLETED, title = "Achievement Unlocked!", body = "You earned \"Court Regular\" - Book 10 venues. +100 XP!", isRead = false, createdAt = "1 hour ago"),
        Notification(id = 4L, type = NotificationType.FEED_POST_COMMENTED, title = "New Message", body = "Stefan M.: \"Let's warm up at 14:45\"", isRead = true, createdAt = "3 hours ago"),
        Notification(id = 5L, type = NotificationType.RATING_RECEIVED, title = "New Review", body = "Arena Sport Center received a new 5-star review", isRead = true, createdAt = "Yesterday, 14:30"),
    )
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Notifications", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary, modifier = Modifier.weight(1f))
                Text("Mark all read", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GreenAccent)
            }
            SectionLabel("New")
            sampleNotifs.filter { !it.isRead }.forEach { notif ->
                NotificationItem(notif = notif, isUnread = true, pendingAction = false, resolvedAction = null, onAcceptJoin = {}, onDeclineJoin = {}, onClick = {})
            }
            SectionLabel("Earlier Today")
            sampleNotifs.filter { it.isRead }.forEach { notif ->
                NotificationItem(notif = notif, isUnread = false, pendingAction = false, resolvedAction = null, onAcceptJoin = {}, onDeclineJoin = {}, onClick = {})
            }
        }
}
