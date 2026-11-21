package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lessonCode: String,
    val lessonTitle: String,
    val isPinned: Boolean,
    val updatedAtLabel: String,
    val updatedAtMillis: Long,
    val previewSnippet: String,
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatThreadEntity::class,
            parentColumns = ["id"],
            childColumns = ["threadId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["threadId"])],
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val isFromUser: Boolean,
    val content: String,
    val savedNoteId: String?,
    val timestampLabel: String,
    val createdAtMillis: Long,
)

@Entity(tableName = "revision_notes")
data class RevisionNoteEntity(
    @PrimaryKey val id: String,
    val lessonCode: String,
    val lessonTitle: String,
    val categoryName: String,
    val highlightedSnippet: String,
    val learnerNote: String,
    val sourceName: String,
    val savedAtLabel: String,
    val createdAtMillis: Long,
)

@Entity(tableName = "spaced_repetition_items")
data class SpacedRepetitionEntity(
    @PrimaryKey val id: String,
    val lessonCode: String,
    val title: String,
    val frontPrompt: String,
    val backAnswer: String,
    val codeExample: String,
    val isDueNow: Boolean,
    val countdownLabel: String,
    val retentionPercent: Int,
    val intervalTag: String,
    val reviewCount: Int,
)
