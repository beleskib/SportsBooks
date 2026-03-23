package com.example.sportsbook.ui.screens.player.match

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.data.remote.FirestoreChatService
import com.example.sportsbook.domain.model.MatchChatMessage
import com.example.sportsbook.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchChatUiState(
    val messages: List<MatchChatMessage> = emptyList(),
    val currentUserId: Long? = null,
    val currentUserName: String? = null,
    val currentUserPhotoUrl: String? = null,
    val messageText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MatchChatViewModel @Inject constructor(
    private val firestoreChatService: FirestoreChatService,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: Long = checkNotNull(savedStateHandle["matchId"])
    private val _uiState = MutableStateFlow(MatchChatUiState())
    val uiState: StateFlow<MatchChatUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        observeMessages()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()?.let { user ->
                _uiState.update {
                    it.copy(
                        currentUserId = user.id,
                        currentUserName = user.displayName,
                        currentUserPhotoUrl = user.photoUrl
                    )
                }
            }
        }
    }

    /**
     * Observes Firestore messages in real-time via snapshot listener.
     * No more polling — messages appear instantly.
     */
    private fun observeMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            firestoreChatService.observeMessages(matchId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { messages ->
                    _uiState.update {
                        it.copy(messages = messages, isLoading = false)
                    }
                }
        }
    }

    fun updateMessageText(text: String) {
        _uiState.update { it.copy(messageText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.messageText.trim()
        if (text.isBlank()) return
        val state = _uiState.value
        val userId = state.currentUserId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, messageText = "") }
            try {
                firestoreChatService.sendMessage(
                    matchId = matchId,
                    senderId = userId,
                    senderName = state.currentUserName,
                    senderPhotoUrl = state.currentUserPhotoUrl,
                    content = text
                )
                // Message will appear via the snapshot listener — no need to manually add it
                _uiState.update { it.copy(isSending = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSending = false, messageText = text, error = e.message)
                }
            }
        }
    }
}
