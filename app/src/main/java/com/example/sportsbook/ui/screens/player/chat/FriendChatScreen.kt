package com.example.sportsbook.ui.screens.player.chat

import android.widget.Toast
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.DarkTextTertiary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Local design tokens ──────────────────────────────────────────────────────
private val BubbleMe = GreenAccent
private val BubbleFriend = Color(0xFF252525)

// ── Screen ───────────────────────────────────────────────────────────────────

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .imePadding(),
    ) {
        // ── Header ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .border(width = 0.5.dp, color = DarkBorder, shape = RoundedCornerShape(0.dp))
                .padding(start = 12.dp, end = 16.dp, top = 48.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Avatar with online indicator
            Box(modifier = Modifier.size(40.dp)) {
                FriendChatAvatar(photoUrl = friendPhotoUrl, name = friendName, size = 40)
                // Online dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(GreenAccent)
                        .border(2.dp, DarkSurface, CircleShape)
                        .align(Alignment.BottomEnd),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(friendName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                Text("Online", fontSize = 12.sp, color = GreenAccent)
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderActionButton("📞")
                HeaderActionButton("⋯")
            }
        }

        // ── Messages ─────────────────────────────────────────────────────
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = GreenAccent, modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💬", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Start a conversation with $friendName",
                            fontSize = 14.sp,
                            color = DarkTextSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                    items(uiState.messages, key = { it.id }) { message ->
                        DirectMessageBubble(
                            message = message,
                            isMe = message.senderId == uiState.currentUserId,
                            friendPhotoUrl = friendPhotoUrl,
                            friendName = friendName,
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }

        // ── Input bar ─────────────────────────────────────────────────────
        MessageInputBar(
            value = uiState.inputText,
            onValueChange = viewModel::onInputChange,
            onSend = viewModel::sendMessage,
            isSending = uiState.isSending,
        )
    }
}

// ── Header action button ──────────────────────────────────────────────────────

@Composable
private fun HeaderActionButton(emoji: String) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .clickable {
                Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = 16.sp)
    }
}

// ── Message bubble ────────────────────────────────────────────────────────────

@Composable
private fun DirectMessageBubble(
    message: DirectMessage,
    isMe: Boolean,
    friendPhotoUrl: String?,
    friendName: String,
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
            if (!isMe) {
                FriendChatAvatar(photoUrl = friendPhotoUrl, name = friendName, size = 28)
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
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = formatTimestamp(message.createdAt),
            fontSize = 10.sp,
            color = DarkTextTertiary,
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
            .background(DarkSurface)
            .border(width = 0.5.dp, color = DarkBorder, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Attach button
        val attachContext = LocalContext.current
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
                .clickable {
                    Toast.makeText(attachContext, "Attachments coming soon", Toast.LENGTH_SHORT).show()
                },
            contentAlignment = Alignment.Center,
        ) {
            Text("+", fontSize = 20.sp, color = DarkTextSecondary)
        }

        // Text field
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = !isSending,
            textStyle = TextStyle(color = DarkTextPrimary, fontSize = 15.sp, lineHeight = 22.sp),
            cursorBrush = SolidColor(GreenAccent),
            maxLines = 4,
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    if (value.isEmpty()) {
                        Text("Type a message...", fontSize = 15.sp, color = Color(0xFF555555))
                    }
                    inner()
                }
            },
        )

        // Send button
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (value.isNotBlank() && !isSending) GreenAccent else DarkBorder)
                .clickable(enabled = value.isNotBlank() && !isSending, onClick = onSend),
            contentAlignment = Alignment.Center,
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text("➤", fontSize = 16.sp, color = if (value.isNotBlank()) Color.White else DarkTextTertiary)
            }
        }
    }
}

// ── Avatar ────────────────────────────────────────────────────────────────────

@Composable
private fun FriendChatAvatar(
    photoUrl: String?,
    name: String,
    size: Int,
    modifier: Modifier = Modifier,
) {
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Avatar",
            modifier = modifier.size(size.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(GreenDark),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                fontSize = (size * 0.4f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
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

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun FriendChatScreenPreview() {
    val sampleMessages = listOf(
        DirectMessage(id = "1", senderId = 42L, senderName = "Marco", content = "Hey! Are you free tomorrow evening? 🎾", createdAt = System.currentTimeMillis() - 300_000L),
        DirectMessage(id = "2", senderId = 1L, senderName = "Me", content = "Yeah! What time were you thinking?", createdAt = System.currentTimeMillis() - 240_000L),
        DirectMessage(id = "3", senderId = 42L, senderName = "Marco", content = "How about 6pm at Champions Tennis? I already booked Court 3 💪", createdAt = System.currentTimeMillis() - 180_000L),
        DirectMessage(id = "4", senderId = 1L, senderName = "Me", content = "Perfect! I'm in 🙌", createdAt = System.currentTimeMillis() - 60_000L),
    )
    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
            // Header preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(start = 12.dp, end = 16.dp, top = 48.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.05f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(GreenDark), contentAlignment = Alignment.Center) {
                    Text("M", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Marco Silva", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                    Text("Online", fontSize = 12.sp, color = GreenAccent)
                }
            }
            // Messages preview
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(modifier = Modifier.height(12.dp)) }
                items(sampleMessages, key = { it.id }) { message ->
                    DirectMessageBubble(message = message, isMe = message.senderId == 1L, friendPhotoUrl = null, friendName = "Marco")
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
            // Input bar preview
            MessageInputBar(value = "", onValueChange = {}, onSend = {}, isSending = false)
        }
}
