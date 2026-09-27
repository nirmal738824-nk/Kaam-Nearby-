package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

data class ChatUnreadCount(
    val chatId: String,
    val unreadCount: Int
)

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChatFlow(chatId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE chatId = :chatId AND senderRole != :myRole AND isRead = 0")
    fun getUnreadCountForChatFlow(chatId: String, myRole: String): Flow<Int>

    @Query("SELECT chatId, COUNT(*) as unreadCount FROM chat_messages WHERE senderRole != :myRole AND isRead = 0 GROUP BY chatId")
    fun getAllUnreadCountsFlow(myRole: String): Flow<List<ChatUnreadCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET isRead = 1 WHERE chatId = :chatId AND senderRole != :myRole")
    suspend fun markChatAsRead(chatId: String, myRole: String)

    @Query("SELECT * FROM chat_messages WHERE messageId = :messageId LIMIT 1")
    suspend fun getMessageByMessageId(messageId: String): ChatMessageEntity?

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getTotalMessageCount(): Int
}
