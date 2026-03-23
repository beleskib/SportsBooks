package com.example.sportsbook.ui.screens.player.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.model.Notification
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
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
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
