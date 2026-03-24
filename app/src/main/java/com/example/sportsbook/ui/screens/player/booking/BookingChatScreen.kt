package com.example.sportsbook.ui.screens.player.booking

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.BookingContactDto
import com.example.sportsbook.data.remote.dto.BookingMessageDto
import com.example.sportsbook.data.remote.dto.SendMessageRequestDto
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.contactInfo?.name ?: "Booking Chat")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Contact Info Card
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Messages list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(uiState.messages, key = { it.id }) { message ->
                        val isMe = message.senderId == uiState.currentUserId
                        BookingChatBubble(message = message, isMe = isMe)
                    }
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                }
            }

            // Message input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.newMessage,
                    onValueChange = viewModel::onMessageTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp),
                    enabled = !uiState.isSending,
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = viewModel::sendMessage,
                    enabled = uiState.newMessage.isNotBlank() && !uiState.isSending
                ) {
                    if (uiState.isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            contact.email?.let { email ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onEmailClick(email) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Send email",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            contact.phoneNumber?.let { phone ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onCallClick(phone) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
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
                .background(
                    if (isMe) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.message,
                color = if (isMe) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = message.createdAt.takeLast(8).take(5),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun BookingChatScreenPreview() {
    val sampleMessages = listOf(
        BookingMessage(
            id = 1L,
            bookingId = 10L,
            senderId = 5L,
            senderName = "City Tennis Center",
            message = "Hello! Your booking is confirmed for March 25 at 10:00.",
            createdAt = "2026-03-20T09:00:00Z"
        ),
        BookingMessage(
            id = 2L,
            bookingId = 10L,
            senderId = 99L,
            senderName = "Me",
            message = "Great, thank you! Do I need to bring my own racket?",
            createdAt = "2026-03-20T09:02:00Z"
        ),
        BookingMessage(
            id = 3L,
            bookingId = 10L,
            senderId = 5L,
            senderName = "City Tennis Center",
            message = "We have rackets available for rental at the venue.",
            createdAt = "2026-03-20T09:04:00Z"
        ),
    )
    val sampleContact = BookingContact(
        name = "City Tennis Center",
        email = "info@citytenniscenter.com",
        phoneNumber = "+1 555-123-4567"
    )
    val currentUserId = 99L

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(sampleContact.name) },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                ContactInfoCard(
                    contact = sampleContact,
                    onCallClick = {},
                    onEmailClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(sampleMessages, key = { it.id }) { message ->
                        BookingChatBubble(
                            message = message,
                            isMe = message.senderId == currentUserId
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {}, enabled = false) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
