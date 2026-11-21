package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_threads ORDER BY isPinned DESC, updatedAtMillis DESC")
    fun observeThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY createdAtMillis ASC")
    fun observeMessagesForThread(threadId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_threads")
    suspend fun getThreadCount(): Int

    /**
     * CRITICAL: Uses @Upsert instead of @Insert(onConflict = REPLACE) because SQLite implements
     * INSERT OR REPLACE as DELETE + INSERT, which triggers ForeignKey.CASCADE and wipes all
     * child rows in chat_messages!
     */
    @Upsert
    suspend fun upsertThread(thread: ChatThreadEntity)

    @Upsert
    suspend fun upsertThreads(threads: List<ChatThreadEntity>)

    @Upsert
    suspend fun upsertMessage(message: ChatMessageEntity)

    @Upsert
    suspend fun upsertMessages(messages: List<ChatMessageEntity>)

    @Transaction
    suspend fun appendMessagesTransaction(
        thread: ChatThreadEntity,
        messages: List<ChatMessageEntity>,
    ) {
        upsertThread(thread)
        upsertMessages(messages)
    }

    @Query("UPDATE chat_threads SET isPinned = :pinned WHERE id = :threadId")
    suspend fun updateThreadPinned(threadId: String, pinned: Boolean)

    @Query("UPDATE chat_messages SET savedNoteId = :noteId WHERE id = :messageId")
    suspend fun markMessageSavedAsNote(messageId: String, noteId: String)

    @Query("DELETE FROM chat_threads WHERE id = :threadId")
    suspend fun deleteThreadById(threadId: String)

    @Query("DELETE FROM chat_threads")
    suspend fun clearAllThreads()
}

@Dao
interface RevisionDao {
    @Query("SELECT * FROM revision_notes ORDER BY createdAtMillis DESC")
    fun observeRevisionNotes(): Flow<List<RevisionNoteEntity>>

    @Query("SELECT * FROM spaced_repetition_items ORDER BY isDueNow DESC, retentionPercent ASC")
    fun observeSpacedRepetitionItems(): Flow<List<SpacedRepetitionEntity>>

    @Query("SELECT COUNT(*) FROM revision_notes")
    suspend fun getRevisionNoteCount(): Int

    @Query("SELECT COUNT(*) FROM spaced_repetition_items")
    suspend fun getSpacedRepetitionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: RevisionNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotes(notes: List<RevisionNoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSpacedRepetitionItem(item: SpacedRepetitionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSpacedRepetitionItems(items: List<SpacedRepetitionEntity>)

    @Query("DELETE FROM revision_notes")
    suspend fun clearAllNotes()

    @Query("DELETE FROM spaced_repetition_items")
    suspend fun clearAllSpacedRepetitionItems()
}
