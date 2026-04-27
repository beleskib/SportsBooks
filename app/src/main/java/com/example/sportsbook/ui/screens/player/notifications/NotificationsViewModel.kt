package com.example.sportsbook.ui.screens.player.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Notification
import com.example.sportsbook.domain.repository.MatchRepository
import com.example.sportsbook.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    /** notification ids currently being approved/declined (disables the buttons) */
    val pendingActionIds: Set<Long> = emptySet(),
    /** notification ids whose join request was just resolved — show "Approved" / "Declined" pill instead of buttons */
    val resolvedActions: Map<Long, String> = emptyMap()
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            notificationRepository.getNotifications()
                .onSuccess { notifications ->
                    _uiState.update { it.copy(notifications = notifications, isRefreshing = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isRefreshing = false) }
                }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            notificationRepository.getNotifications()
                .onSuccess { notifications ->
                    _uiState.update { it.copy(notifications = notifications, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(listOf(notificationId))
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.id == notificationId) it.copy(isRead = true) else it
                            }
                        )
                    }
                }
        }
    }

    /**
     * Inline approve/decline for `match_join_request` notifications.
     * Reads matchId + participantId from the notification's data payload,
     * calls the match endpoint, and on success marks the notification read
     * and remembers the resolution so the UI can show "Approved ✓" / "Declined".
     */
    fun respondToJoinRequest(notification: Notification, approve: Boolean) {
        val matchId = notification.data["matchId"]?.toLongOrNull()
        val participantId = notification.data["participantId"]?.toLongOrNull()
        if (matchId == null || participantId == null) return
        if (notification.id in _uiState.value.pendingActionIds) return

        viewModelScope.launch {
            _uiState.update { it.copy(pendingActionIds = it.pendingActionIds + notification.id) }
            matchRepository.respondToJoinRequest(matchId, participantId, approve)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            pendingActionIds = state.pendingActionIds - notification.id,
                            resolvedActions = state.resolvedActions + (notification.id to if (approve) "Approved" else "Declined"),
                            notifications = state.notifications.map {
                                if (it.id == notification.id) it.copy(isRead = true) else it
                            }
                        )
                    }
                    notificationRepository.markAsRead(listOf(notification.id))
                }
                .onFailure { e ->
                    _uiState.update { state ->
                        state.copy(
                            pendingActionIds = state.pendingActionIds - notification.id,
                            error = e.message
                        )
                    }
                }
        }
    }

    fun markAllAsRead() {
        val unreadIds = _uiState.value.notifications.filter { !it.isRead }.map { it.id }
        if (unreadIds.isEmpty()) return
        viewModelScope.launch {
            notificationRepository.markAsRead(unreadIds)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map { it.copy(isRead = true) }
                        )
                    }
                }
        }
    }
}
