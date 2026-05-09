package com.example.sportsbook.ui.screens.player.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Light-theme design tokens ─────────────────────────────────────────────────
private val LightBg = Color(0xFFF9FAFB)
private val CardWhite = Color.White
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val TextTertiary = Color(0xFF9CA3AF)
private val GoldAccent = Color(0xFFFDE047)
private val BorderGray = Color(0xFFE5E7EB)
private val BubbleMe = Color(0xFF111827)       // dark — current user
private val BubbleFriend = Color(0xFFF3F4F6)   // light gray — friend

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendChatScreen(
    friendUserId: Long,
    friendName: String,
    friendPhotoUrl: String?,
    onBack: () -> Unit,
    viewModel: FriendChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Auto-scroll to the bottom whenever a new message arrives.
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Scaffold(
        containerColor = LightBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FriendChatAvatar(photoUrl = friendPhotoUrl, size = 32)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = friendName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = TextPrimary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TextPrimary)
                    }
                }

                uiState.messages.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Start a conversation with $friendName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(uiState.messages, key = { it.id }) { message ->
                            DirectMessageBubble(
                                message = message,
                                isMe = message.senderId == uiState.currentUserId,
                                friendPhotoUrl = friendPhotoUrl,
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }

            // ── Input bar ─────────────────────────────────────────────────
            MessageInputBar(
                value = uiState.inputText,
                onValueChange = viewModel::onInputChange,
                onSend = viewModel::sendMessage,
                isSending = uiState.isSending,
            )
        }
    }
}

// ── Message bubble ────────────────────────────────────────────────────────────

@Composable
private fun DirectMessageBubble(
    message: DirectMessage,
    isMe: Boolean,
    friendPhotoUrl: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Friend avatar on the left
            if (!isMe) {
                FriendChatAvatar(photoUrl = friendPhotoUrl, size = 28)
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp,
                        )
                    )
                    .background(if (isMe) BubbleMe else BubbleFriend)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMe) Color.White else TextPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = formatTimestamp(message.createdAt),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextTertiary,
            modifier = Modifier.padding(horizontal = if (isMe) 0.dp else 34.dp),
        )
    }
}

// ── Input bar ─────────────────────────────────────────────────────────────────

@Composable
private fun MessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = "Message...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                )
            },
            maxLines = 4,
            shape = RoundedCornerShape(24.dp),
            enabled = !isSending,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TextPrimary,
                unfocusedBorderColor = BorderGray,
                cursorColor = TextPrimary,
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (value.isNotBlank() && !isSending) GoldAccent else BorderGray),
            contentAlignment = Alignment.Center,
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = TextPrimary,
                )
            } else {
                IconButton(
                    onClick = onSend,
                    enabled = value.isNotBlank(),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (value.isNotBlank()) TextPrimary else TextTertiary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

// ── Avatar ─────────────────────────────────────────────────────────────────────

@Composable
private fun FriendChatAvatar(
    photoUrl: String?,
    size: Int,
    modifier: Modifier = Modifier,
) {
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Avatar",
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(BorderGray),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "?",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary,
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dateTimeFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

private fun formatTimestamp(epochMillis: Long): String {
    if (epochMillis == 0L) return ""
    val now = System.currentTimeMillis()
    val date = Date(epochMillis)
    return if (now - epochMillis < 24 * 60 * 60 * 1000L) {
        timeFormat.format(date)
    } else {
        dateTimeFormat.format(date)
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun FriendChatScreenPreview() {
    val sampleMessages = listOf(
        DirectMessage(
            id = "1",
            senderId = 42L,
            senderName = "Alex Johnson",
            content = "Hey! Are you up for a tennis match this weekend?",
            createdAt = System.currentTimeMillis() - 3_600_000L,
        ),
        DirectMessage(
            id = "2",
            senderId = 1L,
            senderName = "Me",
            content = "Absolutely! Saturday morning works great.",
            createdAt = System.currentTimeMillis() - 3_500_000L,
        ),
        DirectMessage(
            id = "3",
            senderId = 42L,
            senderName = "Alex Johnson",
            content = "Perfect, I'll book a court at City Tennis Center.",
            createdAt = System.currentTimeMillis() - 3_400_000L,
        ),
    )
    MaterialTheme {
        Scaffold(
            containerColor = LightBg,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FriendChatAvatar(photoUrl = null, size = 32)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Alex Johnson",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = TextPrimary,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite),
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(sampleMessages, key = { it.id }) { message ->
                        DirectMessageBubble(
                            message = message,
                            isMe = message.senderId == 1L,
                            friendPhotoUrl = null,
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
                MessageInputBar(
                    value = "",
                    onValueChange = {},
                    onSend = {},
                    isSending = false,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9FAFB)
@Composable
private fun DirectMessageBubblePreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DirectMessageBubble(
                message = DirectMessage(
                    id = "1",
                    senderId = 42L,
                    senderName = "Alex",
                    content = "Hey! Saturday works for me.",
                    createdAt = System.currentTimeMillis(),
                ),
                isMe = false,
                friendPhotoUrl = null,
            )
            DirectMessageBubble(
                message = DirectMessage(
                    id = "2",
                    senderId = 1L,
                    senderName = "Me",
                    content = "Great, see you at 10am!",
                    createdAt = System.currentTimeMillis(),
                ),
                isMe = true,
                friendPhotoUrl = null,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageInputBarPreview() {
    MaterialTheme {
        MessageInputBar(
            value = "Hello there",
            onValueChange = {},
            onSend = {},
            isSending = false,
        )
    }
}
