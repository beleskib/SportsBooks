package com.example.sportsbook.ui.screens.player.booking

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.BookingContactDto
import com.example.sportsbook.data.remote.dto.BookingMessageDto
import com.example.sportsbook.data.remote.dto.SendMessageRequestDto
import com.example.sportsbook.ui.theme.DarkBg
import com.example.sportsbook.ui.theme.DarkBorder
import com.example.sportsbook.ui.theme.DarkSurface
import com.example.sportsbook.ui.theme.DarkTextPrimary
import com.example.sportsbook.ui.theme.DarkTextSecondary
import com.example.sportsbook.ui.theme.GreenAccent
import com.example.sportsbook.ui.theme.GreenDark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Domain-level models used within this screen ───────────────────────────────

data class BookingMessage(
    val id: Long,
    val bookingId: Long,
    val senderId: Long,
    val senderName: String,
    val message: String,
    val createdAt: String,
)

data class BookingContact(
    val name: String,
    val email: String?,
    val phoneNumber: String?,
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class BookingChatViewModel @Inject constructor(
    private val apiService: ApiService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    data class UiState(
        val messages: List<BookingMessage> = emptyList(),
        val contactInfo: BookingContact? = null,
        val newMessage: String = "",
        val isLoading: Boolean = false,
        val isSending: Boolean = false,
        val currentUserId: Long = 0L,
        val error: String? = null,
    )

    private val bookingId: Long = checkNotNull(savedStateHandle["bookingId"])

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        startPolling()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadContactInfo()
            loadMessages()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadContactInfo() {
        runCatching {
            apiService.getBookingContact(bookingId).data
        }.onSuccess { dto ->
            _uiState.update {
                it.copy(
                    contactInfo = BookingContact(
                        name = dto.name,
                        email = dto.email,
                        phoneNumber = dto.phoneNumber,
                    )
                )
            }
        }
    }

    private suspend fun loadMessages() {
        runCatching {
            apiService.getBookingMessages(bookingId).data
        }.onSuccess { dtos ->
            _uiState.update {
                it.copy(messages = dtos.map { dto -> dto.toDomain() })
            }
        }.onFailure { e ->
            _uiState.update { it.copy(error = e.message) }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(5_000L)
                loadMessages()
            }
        }
    }

    fun onMessageTextChange(text: String) {
        _uiState.update { it.copy(newMessage = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.newMessage.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, newMessage = "") }
            runCatching {
                apiService.sendBookingMessage(bookingId, SendMessageRequestDto(message = text)).data
            }.onSuccess { dto ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        messages = it.messages + dto.toDomain()
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        newMessage = text, // restore text on failure
                        error = e.message ?: "Failed to send message"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun BookingMessageDto.toDomain() = BookingMessage(
        id = id,
        bookingId = bookingId,
        senderId = senderId,
        senderName = senderName,
        message = message,
        createdAt = createdAt,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun BookingChatScreen(
    onBack: () -> Unit,
    viewModel: BookingChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Header ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 12.dp),
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
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    uiState.contactInfo?.name ?: "Booking Chat",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary,
                )
                if (uiState.contactInfo?.name != null) {
                    Text("Booking Chat", fontSize = 13.sp, color = DarkTextSecondary)
                }
            }
        }

        // ── Contact info card ─────────────────────────────────────────────
        uiState.contactInfo?.let { contact ->
            ContactInfoCard(
                contact = contact,
                onCallClick = { phoneNumber ->
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                    context.startActivity(intent)
                },
                onEmailClick = { email ->
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        // ── Loading or messages ───────────────────────────────────────────
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = GreenAccent)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(uiState.messages, key = { it.id }) { message ->
                    BookingChatBubble(message = message, isMe = message.senderId == uiState.currentUserId)
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }

        // ── Input bar ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(DarkBg)
                    .border(1.dp, DarkBorder, RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                BasicTextField(
                    value = uiState.newMessage,
                    onValueChange = viewModel::onMessageTextChange,
                    enabled = !uiState.isSending,
                    maxLines = 3,
                    textStyle = TextStyle(color = DarkTextPrimary, fontSize = 15.sp),
                    cursorBrush = SolidColor(GreenAccent),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        Box {
                            if (uiState.newMessage.isEmpty()) {
                                Text("Type a message...", fontSize = 15.sp, color = Color(0xFF555555))
                            }
                            inner()
                        }
                    },
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            val canSend = uiState.newMessage.isNotBlank() && !uiState.isSending
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (canSend) GreenAccent else DarkSurface)
                    .border(1.dp, if (canSend) GreenAccent else DarkBorder, CircleShape)
                    .then(if (canSend) Modifier.clickable { viewModel.sendMessage() } else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = GreenAccent)
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (canSend) Color.White else DarkTextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

// ── Contact Info Card ─────────────────────────────────────────────────────────

@Composable
private fun ContactInfoCard(
    contact: BookingContact,
    onCallClick: (String) -> Unit,
    onEmailClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Text(contact.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)

        contact.email?.let { email ->
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("✉️", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(email, fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GreenAccent.copy(alpha = 0.12f))
                        .border(1.dp, GreenAccent.copy(alpha = 0.4f), CircleShape)
                        .clickable { onEmailClick(email) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✉️", fontSize = 13.sp)
                }
            }
        }

        contact.phoneNumber?.let { phone ->
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("📞", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(phone, fontSize = 13.sp, color = DarkTextSecondary, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GreenAccent.copy(alpha = 0.12f))
                        .border(1.dp, GreenAccent.copy(alpha = 0.4f), CircleShape)
                        .clickable { onCallClick(phone) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📞", fontSize = 13.sp)
                }
            }
        }
    }
}

// ── Chat Bubble ───────────────────────────────────────────────────────────────

@Composable
private fun BookingChatBubble(message: BookingMessage, isMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
    ) {
        if (!isMe) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(GreenDark),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        message.senderName.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(message.senderName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkTextSecondary)
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp,
                    )
                )
                .background(if (isMe) GreenAccent else DarkSurface)
                .padding(12.dp),
        ) {
            Text(
                text = message.message,
                color = if (isMe) Color.White else DarkTextPrimary,
                fontSize = 14.sp,
            )
        }

        Text(
            text = message.createdAt.takeLast(8).take(5),
            fontSize = 11.sp,
            color = DarkTextSecondary,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun BookingChatScreenPreview() {
    val sampleMessages = listOf(
        BookingMessage(id = 1L, bookingId = 10L, senderId = 5L, senderName = "City Tennis Center",
            message = "Hello! Your booking is confirmed for March 25 at 10:00.", createdAt = "2026-03-20T09:00:00Z"),
        BookingMessage(id = 2L, bookingId = 10L, senderId = 99L, senderName = "Me",
            message = "Great, thank you! Do I need to bring my own racket?", createdAt = "2026-03-20T09:02:00Z"),
        BookingMessage(id = 3L, bookingId = 10L, senderId = 5L, senderName = "City Tennis Center",
            message = "We have rackets available for rental at the venue.", createdAt = "2026-03-20T09:04:00Z"),
    )
    val sampleContact = BookingContact(name = "City Tennis Center", email = "info@citytenniscenter.com", phoneNumber = "+1 555-123-4567")
    val currentUserId = 99L

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sampleContact.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                Text("Booking Chat", fontSize = 13.sp, color = DarkTextSecondary)
            }
        }
        ContactInfoCard(contact = sampleContact, onCallClick = {}, onEmailClick = {},
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp))
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            items(sampleMessages, key = { it.id }) { message ->
                BookingChatBubble(message = message, isMe = message.senderId == currentUserId)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(0.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(DarkBg)
                    .border(1.dp, DarkBorder, RoundedCornerShape(22.dp)).padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text("Type a message...", fontSize = 15.sp, color = Color(0xFF555555))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(DarkSurface).border(1.dp, DarkBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = DarkTextSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}
