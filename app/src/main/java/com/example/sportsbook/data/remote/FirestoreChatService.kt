package com.example.sportsbook.data.remote

import com.example.sportsbook.domain.model.MatchChatMessage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service that handles real-time match chat using Firebase Firestore.
 *
 * Firestore structure:
 *   match_chats/{matchId}/messages/{autoId}
 *     - senderId: Long
 *     - senderName: String
 *     - senderPhotoUrl: String?
 *     - content: String
 *     - createdAt: Long (epoch millis)
 */
@Singleton
class FirestoreChatService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun messagesCollection(matchId: Long) =
        firestore.collection("match_chats")
            .document(matchId.toString())
            .collection("messages")

    /**
     * Returns a Flow that emits the full list of chat messages every time
     * the Firestore collection changes (real-time listener).
     */
    fun observeMessages(matchId: Long): Flow<List<MatchChatMessage>> = callbackFlow {
        val registration = messagesCollection(matchId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        MatchChatMessage(
                            id = doc.id.hashCode().toLong(),
                            matchId = matchId,
                            senderId = doc.getLong("senderId") ?: 0L,
                            senderName = doc.getString("senderName"),
                            senderPhotoUrl = doc.getString("senderPhotoUrl"),
                            content = doc.getString("content") ?: "",
                            createdAt = doc.getLong("createdAt")?.let { millis ->
                                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                                    .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                                    .format(java.util.Date(millis))
                            }
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { registration.remove() }
    }

    /**
     * Sends a chat message to Firestore. Returns the created message.
     */
    suspend fun sendMessage(
        matchId: Long,
        senderId: Long,
        senderName: String?,
        senderPhotoUrl: String?,
        content: String
    ): MatchChatMessage {
        val now = System.currentTimeMillis()
        val data = hashMapOf(
            "senderId" to senderId,
            "senderName" to (senderName ?: "Player"),
            "senderPhotoUrl" to senderPhotoUrl,
            "content" to content,
            "createdAt" to now
        )

        val docRef = messagesCollection(matchId).add(data).await()

        return MatchChatMessage(
            id = docRef.id.hashCode().toLong(),
            matchId = matchId,
            senderId = senderId,
            senderName = senderName,
            senderPhotoUrl = senderPhotoUrl,
            content = content,
            createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .format(java.util.Date(now))
        )
    }
}
