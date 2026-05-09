package com.example.sportsbook.ui.screens.player.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.repository.UserRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class DirectMessage(
    val id: String = "",
    val senderId: Long = 0L,
    val senderName: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
)

data class FriendChatUiState(
    val messages: List<DirectMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val currentUserId: Long = 0L,
    val inputText: String = "",
    val error: String? = null,
)

@HiltViewModel
class FriendChatViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val friendUserId: Long = checkNotNull(savedStateHandle["friendUserId"])
    private val friendName: String = checkNotNull(savedStateHandle["friendName"])

    private val _uiState = MutableStateFlow(FriendChatUiState(isLoading = true))
    val uiState: StateFlow<FriendChatUiState> = _uiState.asStateFlow()

    private var currentUserId: Long = 0L
    private var currentUserName: String = ""
    private var chatId: String = ""

    init {
        viewModelScope.launch {
            // Resolve the current user first; chatId depends on it.
            userRepository.getProfile()
                .onSuccess { user ->
                    currentUserId = user.id
                    currentUserName = user.displayName ?: user.email
                    chatId = buildChatId(currentUserId, friendUserId)
                    _uiState.update { it.copy(currentUserId = currentUserId) }
                    ensureChatDocument()
                    startListening()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /**
     * Ensures the parent `direct_messages/{chatId}` document exists with a
     * `participants` array so it can be discovered via array-contains queries.
     */
    private suspend fun ensureChatDocument() {
        val docRef = firestore.collection("direct_messages").document(chatId)
        runCatching {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                docRef.set(
                    mapOf(
                        "participants" to listOf(currentUserId, friendUserId),
                        "friendName" to friendName,
                        "createdAt" to FieldValue.serverTimestamp(),
                    )
                ).await()
            }
        }
    }

    private fun startListening() {
        viewModelScope.launch {
            messageFlow(chatId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { messages ->
                    _uiState.update { it.copy(messages = messages, isLoading = false) }
                }
        }
    }

    private fun messageFlow(chatId: String) = callbackFlow {
        val ref = firestore
            .collection("direct_messages")
            .document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val messages = snapshot?.documents?.mapNotNull { doc ->
                val senderId = doc.getLong("senderId") ?: return@mapNotNull null
                val senderName = doc.getString("senderName") ?: return@mapNotNull null
                val content = doc.getString("content") ?: return@mapNotNull null
                val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                DirectMessage(
                    id = doc.id,
                    senderId = senderId,
                    senderName = senderName,
                    content = content,
                    createdAt = createdAt,
                )
            } ?: emptyList()
            trySend(messages)
        }
        awaitClose { registration.remove() }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || chatId.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, inputText = "") }
            runCatching {
                firestore
                    .collection("direct_messages")
                    .document(chatId)
                    .collection("messages")
                    .add(
                        mapOf(
                            "senderId" to currentUserId,
                            "senderName" to currentUserName,
                            "content" to text,
                            "createdAt" to FieldValue.serverTimestamp(),
                        )
                    ).await()
                // Update last message preview on the parent document.
                firestore
                    .collection("direct_messages")
                    .document(chatId)
                    .update(
                        mapOf(
                            "lastMessage" to text,
                            "lastMessageAt" to FieldValue.serverTimestamp(),
                            "lastSenderId" to currentUserId,
                        )
                    ).await()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        inputText = text,
                        error = e.message ?: "Failed to send message",
                    )
                }
            }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        /** Deterministic chat ID — same for both participants regardless of who opens first. */
        fun buildChatId(userId1: Long, userId2: Long): String {
            val min = minOf(userId1, userId2)
            val max = maxOf(userId1, userId2)
            return "dm_${min}_${max}"
        }
    }
}
