package com.example.sportsbook.ui.screens.player.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.BookingStatus
import com.example.sportsbook.domain.repository.BookingRepository
import com.example.sportsbook.domain.repository.MatchRepository
import com.example.sportsbook.domain.repository.PartyRepository
import com.example.sportsbook.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChatType { BOOKING, MATCH, FRIEND, PARTY }

data class ChatConversation(
    val id: Long,
    val title: String,
    val subtitle: String,
    val type: ChatType,
    val status: String,
    // Extra fields for DM navigation
    val friendUserId: Long? = null,
    val friendPhotoUrl: String? = null,
)

data class ChatsListUiState(
    val bookingChats: List<ChatConversation> = emptyList(),
    val matchChats: List<ChatConversation> = emptyList(),
    val friendChats: List<ChatConversation> = emptyList(),
    val partyChats: List<ChatConversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

/** Statuses that unlock the booking chat feature (partner has responded). */
private val CHAT_ELIGIBLE_STATUSES = setOf(
    BookingStatus.APPROVED,
    BookingStatus.CONFIRMED,
    BookingStatus.COMPLETED,
)

@HiltViewModel
class ChatsListViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val matchRepository: MatchRepository,
    private val partyRepository: PartyRepository,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsListUiState())
    val uiState: StateFlow<ChatsListUiState> = _uiState.asStateFlow()

    init {
        load()
        loadFriendChats()
    }

    fun refresh() {
        load()
        loadFriendChats()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val bookingsDeferred = async { bookingRepository.getMyBookings() }
            val matchesDeferred = async { matchRepository.getMyMatches() }
            val partyDeferred = async { partyRepository.getActiveParties() }

            val bookingsResult = bookingsDeferred.await()
            val matchesResult = matchesDeferred.await()
            val partyResult = partyDeferred.await()

            val bookingChats = bookingsResult
                .getOrNull()
                ?.filter { it.status in CHAT_ELIGIBLE_STATUSES }
                ?.map { booking ->
                    val entityName = booking.venue?.name
                        ?: booking.coach?.name
                        ?: "Booking #${booking.id}"
                    ChatConversation(
                        id = booking.id,
                        title = entityName,
                        subtitle = booking.timeSlot?.let {
                            "${it.slotDate} · ${it.startTime}–${it.endTime}"
                        } ?: "Booking",
                        type = ChatType.BOOKING,
                        status = booking.status.name.lowercase()
                            .replaceFirstChar { it.uppercaseChar() },
                    )
                }
                ?: emptyList()

            val matchChats = matchesResult
                .getOrNull()
                ?.map { match ->
                    ChatConversation(
                        id = match.id,
                        title = match.title,
                        subtitle = "${match.sportType.name.lowercase().replaceFirstChar { it.uppercaseChar() }} · ${match.matchDate}",
                        type = ChatType.MATCH,
                        status = match.status.name.lowercase()
                            .replaceFirstChar { it.uppercaseChar() },
                    )
                }
                ?: emptyList()

            val partyChats = partyResult
                .getOrNull()
                ?.map { party ->
                    ChatConversation(
                        id = party.id,
                        title = party.name ?: "Party",
                        subtitle = "${party.sportType ?: "Sports"} · ${party.members.size} members · ${party.status}",
                        type = ChatType.PARTY,
                        status = party.status,
                    )
                }
                ?: emptyList()

            val combinedError = when {
                bookingsResult.isFailure && matchesResult.isFailure ->
                    "Failed to load chats. Pull down to retry."
                bookingsResult.isFailure ->
                    "Could not load booking chats."
                matchesResult.isFailure ->
                    "Could not load match chats."
                else -> null
            }

            _uiState.update {
                it.copy(
                    bookingChats = bookingChats,
                    matchChats = matchChats,
                    partyChats = partyChats,
                    isLoading = false,
                    error = combinedError,
                )
            }
        }
    }

    /**
     * Listens to all `direct_messages` parent documents where the current user
     * is a participant. The parent doc contains a `participants` array field
     * written by [FriendChatViewModel.ensureChatDocument] when the first chat
     * is opened.
     */
    private fun loadFriendChats() {
        viewModelScope.launch {
            userRepository.getProfile()
                .onSuccess { user ->
                    friendDmFlow(user.id)
                        .catch { /* silently ignore — Friends tab shows empty */ }
                        .collect { conversations ->
                            _uiState.update { it.copy(friendChats = conversations) }
                        }
                }
        }
    }

    private fun friendDmFlow(currentUserId: Long) = callbackFlow {
        val registration = firestore
            .collection("direct_messages")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    val participants = doc.get("participants") as? List<*> ?: return@mapNotNull null
                    val friendId = participants
                        .firstOrNull { entry ->
                            val entryId = (entry as? Long) ?: (entry as? Number)?.toLong()
                            entryId != null && entryId != currentUserId
                        }
                        ?.let { (it as? Long) ?: (it as? Number)?.toLong() }
                        ?: return@mapNotNull null
                    // Prefer the `names` map (new documents); fall back to `friendName` for
                    // legacy documents created before this schema change.
                    @Suppress("UNCHECKED_CAST")
                    val names = doc.get("names") as? Map<String, String> ?: emptyMap()
                    val friendName = names.entries
                        .firstOrNull { it.key != currentUserId.toString() }
                        ?.value
                        ?: doc.getString("friendName")
                        ?: "Friend"
                    val lastMessage = doc.getString("lastMessage") ?: "No messages yet"
                    val lastMessageAt = doc.getTimestamp("lastMessageAt")?.toDate()?.time ?: 0L
                    ChatConversation(
                        // Use a synthetic Long ID (hash of the doc ID) for the list key
                        id = doc.id.hashCode().toLong(),
                        title = friendName,
                        subtitle = lastMessage,
                        type = ChatType.FRIEND,
                        status = "DM",
                        friendUserId = friendId,
                    )
                }?.sortedByDescending { it.id } ?: emptyList()
                trySend(conversations)
            }
        awaitClose { registration.remove() }
    }
}
