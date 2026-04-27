package com.example.sportsbook.ui.screens.player.notifications

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sportsbook.domain.enums.NotificationType
import com.example.sportsbook.domain.model.Notification
import com.example.sportsbook.ui.common.EmptyStateView
import com.example.sportsbook.ui.common.ErrorView
import com.example.sportsbook.ui.common.LoadingIndicator
import com.example.sportsbook.ui.common.XpChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNotificationClick: (Notification) -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    XpChip()
                    if (uiState.notifications.any { !it.isRead }) {
                        IconButton(onClick = viewModel::markAllAsRead) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark all as read")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorView(
                message = uiState.error!!,
                onRetry = viewModel::loadNotifications,
                modifier = Modifier.padding(padding)
            )
            uiState.notifications.isEmpty() -> EmptyStateView(
                title = "No notifications yet",
                subtitle = "You'll see booking updates and match notifications here",
                modifier = Modifier.padding(padding)
            )
            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    state = pullToRefreshState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.notifications, key = { it.id }) { notification ->
                            NotificationItem(
                                notification = notification,
                                isActionPending = notification.id in uiState.pendingActionIds,
                                resolvedActionLabel = uiState.resolvedActions[notification.id],
                                onClick = {
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification.id)
                                    }
                                    onNotificationClick(notification)
                                },
                                onApprove = { viewModel.respondToJoinRequest(notification, approve = true) },
                                onDecline = { viewModel.respondToJoinRequest(notification, approve = false) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    isActionPending: Boolean = false,
    resolvedActionLabel: String? = null,
    onClick: () -> Unit,
    onApprove: () -> Unit = {},
    onDecline: () -> Unit = {}
) {
    val bgColor = if (!notification.isRead) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    // Show inline approve/decline only for join-request notifications that
    // carry both matchId + participantId in their data payload.
    val isActionable = notification.type == NotificationType.MATCH_JOIN_REQUEST &&
        notification.data["matchId"] != null &&
        notification.data["participantId"] != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(notificationIconColor(notification.type)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = notificationIcon(notification.type),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.createdAt?.takeLast(19)?.take(16)?.replace("T", " ") ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isActionable) {
                Spacer(modifier = Modifier.height(10.dp))
                JoinRequestActionRow(
                    isPending = isActionPending,
                    resolvedLabel = resolvedActionLabel,
                    onApprove = onApprove,
                    onDecline = onDecline
                )
            }
        }

        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun JoinRequestActionRow(
    isPending: Boolean,
    resolvedLabel: String?,
    onApprove: () -> Unit,
    onDecline: () -> Unit
) {
    if (resolvedLabel != null) {
        // Already responded — show outcome pill, no buttons.
        val color = when (resolvedLabel) {
            "Approved" -> MaterialTheme.colorScheme.primary
            "Declined" -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Text(
            text = "$resolvedLabel ✓",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
        return
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        androidx.compose.material3.Button(
            onClick = onApprove,
            enabled = !isPending,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text("Approve", style = MaterialTheme.typography.labelMedium)
        }
        androidx.compose.material3.OutlinedButton(
            onClick = onDecline,
            enabled = !isPending,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text("Decline", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun notificationIcon(type: NotificationType): ImageVector = when (type) {
    NotificationType.MATCH_JOIN_REQUEST,
    NotificationType.MATCH_JOIN_APPROVED,
    NotificationType.MATCH_JOIN_DECLINED -> Icons.Default.Group
    NotificationType.MATCH_CHAT_MESSAGE -> Icons.Default.Message
    NotificationType.MATCH_STARTING_SOON,
    NotificationType.MATCH_CANCELLED -> Icons.Default.SportsScore
    NotificationType.BOOKING_REQUEST,
    NotificationType.BOOKING_APPROVED,
    NotificationType.BOOKING_DECLINED,
    NotificationType.BOOKING_CONFIRMED,
    NotificationType.BOOKING_CANCELLED,
    NotificationType.BOOKING_REMINDER -> Icons.Default.BookOnline
    NotificationType.RATING_RECEIVED -> Icons.Default.Star
    NotificationType.FRIEND_REQUEST,
    NotificationType.FRIEND_REQUEST_ACCEPTED -> Icons.Default.PersonAdd
    NotificationType.PARTY_INVITE,
    NotificationType.PARTY_INVITE_ACCEPTED,
    NotificationType.PARTY_INVITE_DECLINED,
    NotificationType.PARTY_JOINED_MATCH,
    NotificationType.PARTY_DISBANDED -> Icons.Default.Group
    NotificationType.FEED_POST_LIKED -> Icons.Default.Star
    NotificationType.FEED_POST_COMMENTED -> Icons.Default.Message
    NotificationType.GENERAL -> Icons.Default.Notifications
}

@Composable
private fun notificationIconColor(type: NotificationType) = when (type) {
    NotificationType.MATCH_JOIN_REQUEST,
    NotificationType.MATCH_JOIN_APPROVED,
    NotificationType.MATCH_JOIN_DECLINED -> MaterialTheme.colorScheme.primary
    NotificationType.MATCH_CHAT_MESSAGE -> MaterialTheme.colorScheme.tertiary
    NotificationType.MATCH_STARTING_SOON -> MaterialTheme.colorScheme.secondary
    NotificationType.MATCH_CANCELLED -> MaterialTheme.colorScheme.error
    NotificationType.BOOKING_REQUEST -> MaterialTheme.colorScheme.secondary
    NotificationType.BOOKING_APPROVED -> MaterialTheme.colorScheme.primary
    NotificationType.BOOKING_DECLINED -> MaterialTheme.colorScheme.error
    NotificationType.BOOKING_CONFIRMED -> MaterialTheme.colorScheme.primary
    NotificationType.BOOKING_CANCELLED -> MaterialTheme.colorScheme.error
    NotificationType.BOOKING_REMINDER -> MaterialTheme.colorScheme.secondary
    NotificationType.RATING_RECEIVED -> MaterialTheme.colorScheme.tertiary
    NotificationType.FRIEND_REQUEST,
    NotificationType.FRIEND_REQUEST_ACCEPTED -> MaterialTheme.colorScheme.tertiary
    NotificationType.PARTY_INVITE,
    NotificationType.PARTY_INVITE_ACCEPTED,
    NotificationType.PARTY_INVITE_DECLINED -> MaterialTheme.colorScheme.primary
    NotificationType.PARTY_JOINED_MATCH -> MaterialTheme.colorScheme.secondary
    NotificationType.PARTY_DISBANDED -> MaterialTheme.colorScheme.error
    NotificationType.FEED_POST_LIKED -> MaterialTheme.colorScheme.tertiary
    NotificationType.FEED_POST_COMMENTED -> MaterialTheme.colorScheme.tertiary
    NotificationType.GENERAL -> MaterialTheme.colorScheme.primary
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsScreenPreview() {
    val sampleNotifications = listOf(
        Notification(
            id = 1,
            type = NotificationType.BOOKING_CONFIRMED,
            title = "Booking Confirmed",
            body = "Your booking at Arena Sport on Mar 25 at 10:00 has been confirmed.",
            isRead = false,
            createdAt = "2026-03-23T09:00:00Z"
        ),
        Notification(
            id = 2,
            type = NotificationType.FRIEND_REQUEST,
            title = "New Friend Request",
            body = "Jordan Smith sent you a friend request.",
            isRead = true,
            createdAt = "2026-03-22T15:30:00Z"
        ),
        Notification(
            id = 3,
            type = NotificationType.MATCH_JOIN_APPROVED,
            title = "Join Request Approved",
            body = "Your request to join Friday Basketball has been approved.",
            isRead = true,
            createdAt = "2026-03-21T11:00:00Z"
        )
    )
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Notifications") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark all as read")
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(sampleNotifications, key = { it.id }) { notification ->
                    NotificationItem(notification = notification, onClick = {})
                    HorizontalDivider()
                }
            }
        }
    }
}
