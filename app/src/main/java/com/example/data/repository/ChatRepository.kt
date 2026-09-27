package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.dao.ChatDao
import com.example.data.model.ChatMessageEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(
    private val context: Context,
    private val chatDao: ChatDao
) {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:771224509431:android:b41e8e9044ff")
                    .setProjectId("ais-asia-southeast1-9c9517addd")
                    .setApiKey("AIzaSyDWnBW3-rm_J7K3rKirAZmSceF9uuJEhV4")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("ChatRepository", "Firestore not available, running in local-first mode", e)
            null
        }
    }

    private val activeListeners = mutableMapOf<String, ListenerRegistration>()

    fun getMessagesFlow(chatId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForChatFlow(chatId)
    }

    fun getUnreadCountFlow(chatId: String, currentRole: String): Flow<Int> {
        return chatDao.getUnreadCountForChatFlow(chatId, currentRole)
    }

    fun getAllUnreadCountsFlow(currentRole: String): Flow<Map<String, Int>> {
        return chatDao.getAllUnreadCountsFlow(currentRole).map { list ->
            list.associate { it.chatId to it.unreadCount }
        }
    }

    suspend fun sendMessage(
        chatId: String,
        jobId: String,
        applicationId: String,
        senderRole: String,
        senderName: String,
        text: String
    ): ChatMessageEntity = withContext(Dispatchers.IO) {
        val messageId = "msg_${UUID.randomUUID()}"
        val timestamp = System.currentTimeMillis()
        val entity = ChatMessageEntity(
            messageId = messageId,
            chatId = chatId,
            jobId = jobId,
            applicationId = applicationId,
            senderRole = senderRole,
            senderName = senderName,
            text = text.trim(),
            timestamp = timestamp,
            isRead = false
        )

        // 1. Local Room insertion for instant feedback
        val localId = chatDao.insertMessage(entity)
        val savedEntity = entity.copy(id = localId)

        // 2. Cloud Firestore sync
        try {
            firestore?.let { db ->
                val msgMap = hashMapOf(
                    "messageId" to messageId,
                    "chatId" to chatId,
                    "jobId" to jobId,
                    "applicationId" to applicationId,
                    "senderRole" to senderRole,
                    "senderName" to senderName,
                    "text" to text.trim(),
                    "timestamp" to timestamp,
                    "isRead" to false
                )

                db.collection("job_chats")
                    .document(chatId)
                    .collection("messages")
                    .document(messageId)
                    .set(msgMap)
                    .addOnFailureListener { e ->
                        Log.w("ChatRepository", "Firestore write error: ${e.message}")
                    }

                // Update chat metadata document
                val metaMap = hashMapOf(
                    "chatId" to chatId,
                    "jobId" to jobId,
                    "applicationId" to applicationId,
                    "lastMessage" to text.trim(),
                    "lastSenderRole" to senderRole,
                    "lastTimestamp" to timestamp
                )
                db.collection("job_chats")
                    .document(chatId)
                    .set(metaMap, SetOptions.merge())
            }
        } catch (e: Exception) {
            Log.w("ChatRepository", "Error syncing message to Firestore", e)
        }

        savedEntity
    }

    suspend fun markChatAsRead(chatId: String, currentRole: String) = withContext(Dispatchers.IO) {
        // Update local room database
        chatDao.markChatAsRead(chatId, currentRole)

        // Update Firestore if available
        try {
            firestore?.let { db ->
                db.collection("job_chats")
                    .document(chatId)
                    .collection("messages")
                    .whereNotEqualTo("senderRole", currentRole)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        for (doc in snapshot.documents) {
                            if (doc.getBoolean("isRead") == false) {
                                doc.reference.update("isRead", true)
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            Log.w("ChatRepository", "Error marking read in Firestore", e)
        }
    }

    fun listenToChat(chatId: String, scope: CoroutineScope) {
        if (activeListeners.containsKey(chatId)) return

        try {
            val db = firestore ?: return
            val registration = db.collection("job_chats")
                .document(chatId)
                .collection("messages")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch(Dispatchers.IO) {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            try {
                                val text = doc.getString("text") ?: return@mapNotNull null
                                val senderRole = doc.getString("senderRole") ?: "WORKER"
                                val senderName = doc.getString("senderName") ?: ""
                                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                val isRead = doc.getBoolean("isRead") ?: false
                                val messageId = doc.getString("messageId") ?: doc.id
                                val jobId = doc.getString("jobId") ?: ""
                                val applicationId = doc.getString("applicationId") ?: ""

                                ChatMessageEntity(
                                    messageId = messageId,
                                    chatId = chatId,
                                    jobId = jobId,
                                    applicationId = applicationId,
                                    senderRole = senderRole,
                                    senderName = senderName,
                                    text = text,
                                    timestamp = timestamp,
                                    isRead = isRead
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (messages.isNotEmpty()) {
                            chatDao.insertMessages(messages)
                        }
                    }
                }
            activeListeners[chatId] = registration
        } catch (e: Exception) {
            Log.w("ChatRepository", "Failed to start Firestore listener", e)
        }
    }

    fun stopListening(chatId: String) {
        activeListeners.remove(chatId)?.remove()
    }
}
