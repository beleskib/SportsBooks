package com.example.sportsbook.ui.screens.player.match

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.MatchChatMessage
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val POLL_INTERVAL_MS = 5_000L

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
    private val matchRepository: MatchRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: Long = checkNotNull(savedStateHandle["matchId"])
    private val _uiState = MutableStateFlow(MatchChatUiState())
    val uiState: StateFlow<MatchChatUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadMessages()
        startPolling()
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

    private fun loadMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            matchRepository.getChatMessages(matchId)
                .onSuccess { messages ->
                    _uiState.update { it.copy(messages = messages, isLoading = false, error = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    /**
     * Polls the REST API every [POLL_INTERVAL_MS] ms for new messages using the
     * `since` parameter to fetch only messages newer than the last one received.
     * This avoids re-downloading the entire history on each tick.
     */
    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                delay(POLL_INTERVAL_MS)
                val lastTimestamp = _uiState.value.messages.lastOrNull()?.createdAt
                matchRepository.getChatMessages(matchId, since = lastTimestamp)
                    .onSuccess { newMessages ->
                        if (newMessages.isNotEmpty()) {
                            _uiState.update { state ->
                                val existingIds = state.messages.map { it.id }.toSet()
                                val deduped = newMessages.filter { it.id !in existingIds }
                                state.copy(
                                    messages = state.messages + deduped,
                                    error = null
                                )
                            }
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(error = e.message) }
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

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, messageText = "") }
            matchRepository.sendChatMessage(matchId, text)
                .onSuccess { sent ->
                    _uiState.update { state ->
                        val existingIds = state.messages.map { it.id }.toSet()
                        val updatedMessages = if (sent.id !in existingIds) {
                            state.messages + sent
                        } else {
                            state.messages
                        }
                        state.copy(messages = updatedMessages, isSending = false, error = null)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSending = false, messageText = text, error = e.message)
                    }
                }
        }
    }
}
