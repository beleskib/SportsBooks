package com.example.sportsbook.ui.screens.player.match

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsbook.domain.enums.MatchStatus
import com.example.sportsbook.domain.enums.ParticipantRole
import com.example.sportsbook.domain.enums.ParticipantStatus
import com.example.sportsbook.domain.model.Match
import com.example.sportsbook.domain.model.MatchParticipant
import com.example.sportsbook.domain.model.Party
import com.example.sportsbook.data.remote.api.ApiService
import com.example.sportsbook.data.remote.dto.CreateFeedPostRequestDto
import com.example.sportsbook.data.remote.dto.MatchPaymentStatusDto
import com.example.sportsbook.data.remote.dto.v2.SplitPaymentShareDto
import com.example.sportsbook.domain.repository.AuthRepository
import com.example.sportsbook.domain.repository.MatchRepository
import com.example.sportsbook.domain.repository.PartyRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchDetailUiState(
    val match: Match? = null,
    val currentUserId: Long? = null,
    val activeParty: Party? = null,
    val isLoading: Boolean = false,
    val isJoining: Boolean = false,
    val isSharing: Boolean = false,
    val error: String? = null,
    val joinSuccess: Boolean = false,
    val leaveSuccess: Boolean = false,
    val shareSuccess: Boolean = false,
    // Payment status (for split matches)
    val paymentStatus: MatchPaymentStatusDto? = null,
    val isLoadingPayment: Boolean = false,
    val isPayingShare: Boolean = false,
    val paymentSuccess: Boolean = false
) {
    val isHost: Boolean get() = match?.hostId == currentUserId
    val isParticipant: Boolean
        get() = match?.participants?.any { it.userId == currentUserId } == true
    val canJoin: Boolean
        get() = !isParticipant && match?.status?.name == "OPEN"
    val pendingRequests: List<MatchParticipant>
        get() = match?.participants?.filter { it.status.name == "PENDING" } ?: emptyList()
    val canJoinWithParty: Boolean
        get() = canJoin && activeParty != null && activeParty.status == "ready"
    val isSplitMatch: Boolean get() = match?.paymentType == "split"
    val matchIsFull: Boolean get() = match?.status?.name == "FULL" || match?.status?.name == "IN_PROGRESS"
    val showPaymentSection: Boolean get() = isSplitMatch && paymentStatus != null && paymentStatus.shares.isNotEmpty()
    val myShare: SplitPaymentShareDto? get() {
        val uid = currentUserId ?: return null
        return paymentStatus?.shares?.find { it.payerUserId == uid }
    }
    val canPayShare: Boolean get() = myShare?.status == "pending" || myShare?.status == "awaiting"
}

// Lightweight snapshot of real-time fields arriving from Firestore.
private data class FirestoreMatchSnapshot(
    val currentPlayers: Int,
    val maxPlayers: Int,
    val status: MatchStatus,
    val participants: List<MatchParticipant>
)

@HiltViewModel
class MatchDetailViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val authRepository: AuthRepository,
    private val partyRepository: PartyRepository,
    private val apiService: ApiService,
    private val firestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: Long = checkNotNull(savedStateHandle["matchId"])
    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    // Held so we can remove it in onCleared() without relying on the Flow collector.
    private var firestoreListenerRegistration: ListenerRegistration? = null

    init {
        loadCurrentUser()
        loadMatch()
        loadActiveParty()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser()?.id?.let { uid ->
                _uiState.update { it.copy(currentUserId = uid) }
            }
        }
    }

    fun loadMatch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            matchRepository.getMatchById(matchId)
                .onSuccess { match ->
                    _uiState.update { it.copy(match = match, isLoading = false) }
                    // Start the real-time Firestore overlay after the full REST load succeeds.
                    startFirestoreListener()
                    // Load payment status for split matches.
                    loadPaymentStatus()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
        }
    }

    /**
     * Converts the Firestore addSnapshotListener callback into a Flow using callbackFlow,
     * and stores the ListenerRegistration so onCleared() can explicitly remove it.
     *
     * If Firestore fails at any point the error is swallowed — the screen continues to show
     * REST-fetched data without interruption.
     */
    private fun startFirestoreListener() {
        // Remove any previously-registered listener before re-registering.
        firestoreListenerRegistration?.remove()

        val snapshotFlow = callbackFlow<FirestoreMatchSnapshot> {
            val registration = firestore
                .collection("matches")
                .document(matchId.toString())
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                    val currentPlayers = (snapshot.getLong("currentPlayers") ?: return@addSnapshotListener).toInt()
                    val maxPlayers = (snapshot.getLong("maxPlayers") ?: return@addSnapshotListener).toInt()

                    val rawStatus = snapshot.getString("status") ?: return@addSnapshotListener
                    val status = parseMatchStatus(rawStatus) ?: return@addSnapshotListener

                    @Suppress("UNCHECKED_CAST")
                    val rawParticipants = snapshot.get("participants") as? List<Map<String, Any?>> ?: emptyList()
                    val participants = rawParticipants.mapNotNull { map ->
                        val userId = (map["userId"] as? Long) ?: (map["userId"] as? Number)?.toLong() ?: return@mapNotNull null
                        val participantStatus = parseParticipantStatus(map["status"] as? String ?: "") ?: return@mapNotNull null
                        val role = parseParticipantRole(map["role"] as? String ?: "") ?: ParticipantRole.PLAYER
                        MatchParticipant(
                            matchId = matchId,
                            userId = userId,
                            userName = map["userName"] as? String,
                            userPhotoUrl = map["userPhotoUrl"] as? String,
                            status = participantStatus,
                            role = role,
                        )
                    }

                    trySend(FirestoreMatchSnapshot(currentPlayers, maxPlayers, status, participants))
                }

            // Store for explicit cleanup in onCleared().
            firestoreListenerRegistration = registration

            awaitClose { registration.remove() }
        }

        snapshotFlow
            .onEach { snapshot -> applyFirestoreSnapshot(snapshot) }
            .catch { /* Swallow — REST data remains the source of truth on failure */ }
            .launchIn(viewModelScope)
    }

    /**
     * Overlays the dynamic Firestore fields onto whatever the REST load populated.
     * All static fields (title, sport, dates, location, cost, etc.) stay from REST.
     */
    private fun applyFirestoreSnapshot(snapshot: FirestoreMatchSnapshot) {
        _uiState.update { state ->
            val currentMatch = state.match ?: return@update state
            state.copy(
                match = currentMatch.copy(
                    currentPlayers = snapshot.currentPlayers,
                    maxPlayers = snapshot.maxPlayers,
                    status = snapshot.status,
                    participants = snapshot.participants
                )
            )
        }
    }

    // -----------------------------------------------------------------------
    // Enum parsing helpers — map Firestore lowercase strings to domain enums.
    // -----------------------------------------------------------------------

    private fun parseMatchStatus(raw: String): MatchStatus? = when (raw.lowercase()) {
        "draft" -> MatchStatus.DRAFT
        "open" -> MatchStatus.OPEN
        "full" -> MatchStatus.FULL
        "in_progress" -> MatchStatus.IN_PROGRESS
        "completed" -> MatchStatus.COMPLETED
        "cancelled" -> MatchStatus.CANCELLED
        else -> null
    }

    private fun parseParticipantStatus(raw: String): ParticipantStatus? = when (raw.lowercase()) {
        "pending" -> ParticipantStatus.PENDING
        "approved" -> ParticipantStatus.APPROVED
        "declined" -> ParticipantStatus.DECLINED
        "left" -> ParticipantStatus.LEFT
        else -> null
    }

    private fun parseParticipantRole(raw: String): ParticipantRole? = when (raw.lowercase()) {
        "host" -> ParticipantRole.HOST
        "player" -> ParticipantRole.PLAYER
        else -> null
    }

    // -----------------------------------------------------------------------
    // Actions
    // -----------------------------------------------------------------------

    private fun loadActiveParty() {
        viewModelScope.launch {
            partyRepository.getActiveParty()
                .onSuccess { party ->
                    _uiState.update { it.copy(activeParty = party) }
                }
        }
    }

    fun joinWithParty() {
        val partyId = _uiState.value.activeParty?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true) }
            partyRepository.joinMatchWithParty(matchId, partyId)
                .onSuccess {
                    _uiState.update { it.copy(isJoining = false, joinSuccess = true) }
                    loadMatch()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isJoining = false, error = e.message) }
                }
        }
    }

    fun joinMatch() {
        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true) }
            matchRepository.joinMatch(matchId)
                .onSuccess {
                    _uiState.update { it.copy(isJoining = false, joinSuccess = true) }
                    loadMatch()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isJoining = false, error = e.message) }
                }
        }
    }

    fun leaveMatch() {
        viewModelScope.launch {
            matchRepository.leaveMatch(matchId)
                .onSuccess {
                    _uiState.update { it.copy(leaveSuccess = true) }
                    loadMatch()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun respondToJoinRequest(participantId: Long, approve: Boolean) {
        viewModelScope.launch {
            matchRepository.respondToJoinRequest(matchId, participantId, approve)
                .onSuccess { loadMatch() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun cancelMatch() {
        viewModelScope.launch {
            matchRepository.cancelMatch(matchId)
                .onSuccess { loadMatch() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun shareMatchToFeed(caption: String?) {
        val match = _uiState.value.match ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSharing = true) }
            try {
                val metadata = mutableMapOf(
                    "matchId" to match.id.toString(),
                    "matchTitle" to match.title,
                    "sport" to match.sportType.displayName,
                    "date" to match.matchDate,
                    "time" to match.displayTime,
                    "location" to match.displayLocation,
                    "players" to "${match.currentPlayers}/${match.maxPlayers}",
                    "status" to match.status.name
                )
                match.hostName?.let { metadata["hostName"] = it }

                val content = caption?.takeIf { it.isNotBlank() }
                    ?: "Come join us for ${match.sportType.displayName} at ${match.displayLocation}!"

                val request = CreateFeedPostRequestDto(
                    postType = "match_share",
                    content = content,
                    metadata = metadata
                )
                apiService.createPost(request)
                _uiState.update { it.copy(isSharing = false, shareSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSharing = false, error = e.message) }
            }
        }
    }

    fun clearShareSuccess() {
        _uiState.update { it.copy(shareSuccess = false) }
    }

    private fun loadPaymentStatus() {
        val match = _uiState.value.match ?: return
        if (match.paymentType != "split") return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPayment = true) }
            try {
                val response = apiService.getMatchPaymentStatus(matchId)
                _uiState.update { it.copy(isLoadingPayment = false, paymentStatus = response.data) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingPayment = false) }
            }
        }
    }

    fun payMyShare() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPayingShare = true, error = null) }
            try {
                val intentResponse = apiService.createMatchPaymentIntent(matchId)
                val intent = intentResponse.data
                if (intent.alreadyPaid) {
                    _uiState.update { it.copy(isPayingShare = false, paymentSuccess = true) }
                    loadPaymentStatus()
                    return@launch
                }
                // Dev mode: auto-confirm since the client secret starts with "dev_secret_"
                if (intent.clientSecret?.startsWith("dev_secret_") == true) {
                    apiService.confirmMatchPayment(matchId)
                    _uiState.update { it.copy(isPayingShare = false, paymentSuccess = true) }
                    loadPaymentStatus()
                } else {
                    // Real Stripe: would launch PaymentSheet here
                    _uiState.update { it.copy(isPayingShare = false, error = "Stripe payments not yet implemented for matches") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isPayingShare = false, error = e.message ?: "Payment failed") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        firestoreListenerRegistration?.remove()
        firestoreListenerRegistration = null
    }
}
