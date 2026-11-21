package com.example.data.local

import com.example.data.LearningRepository
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.HighlightCategory
import com.example.model.HighlightNote
import com.example.model.NoteSource
import com.example.model.SpacedRepetitionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

interface StudyPersistenceRepository {
    val chatThreads: Flow<List<ChatThread>>
    val revisionNotes: Flow<List<HighlightNote>>
    val spacedRepetitionItems: Flow<List<SpacedRepetitionItem>>

    fun observeMessages(threadId: String): Flow<List<ChatMessage>>
    suspend fun ensureSeeded(seedSource: LearningRepository)
    suspend fun upsertThread(thread: ChatThread)
    suspend fun appendMessage(thread: ChatThread, message: ChatMessage)
    suspend fun appendMessages(thread: ChatThread, messages: List<ChatMessage>)
    suspend fun setThreadPinned(threadId: String, pinned: Boolean)
    suspend fun deleteThread(threadId: String)
    suspend fun markMessageSaved(messageId: String, noteId: String)
    suspend fun saveRevisionNote(note: HighlightNote)
    suspend fun upsertSpacedRepetitionItem(item: SpacedRepetitionItem)
    suspend fun resetAll(seedSource: LearningRepository)
}

class RoomStudyPersistenceRepository(
    private val chatDao: ChatDao,
    private val revisionDao: RevisionDao,
) : StudyPersistenceRepository {

    override val chatThreads: Flow<List<ChatThread>> =
        chatDao.observeThreads().map { list -> list.map { it.toDomain() } }

    override val revisionNotes: Flow<List<HighlightNote>> =
        revisionDao.observeRevisionNotes().map { list -> list.map { it.toDomain() } }

    override val spacedRepetitionItems: Flow<List<SpacedRepetitionItem>> =
        revisionDao.observeSpacedRepetitionItems().map { list -> list.map { it.toDomain() } }

    override fun observeMessages(threadId: String): Flow<List<ChatMessage>> =
        chatDao.observeMessagesForThread(threadId).map { list -> list.map { it.toDomain() } }

    override suspend fun ensureSeeded(seedSource: LearningRepository) {
        if (chatDao.getThreadCount() == 0) {
            chatDao.upsertThreads(seedSource.getInitialChatThreads().map { it.toEntity() })
            chatDao.upsertMessages(seedSource.getInitialChatMessages().map { it.toEntity() })
        }
        if (revisionDao.getRevisionNoteCount() == 0) {
            revisionDao.upsertNotes(seedSource.getInitialHighlights().map { it.toEntity() })
        }
        if (revisionDao.getSpacedRepetitionCount() == 0) {
            revisionDao.upsertSpacedRepetitionItems(
                seedSource.getInitialSpacedRepetitionItems().map { it.toEntity() },
            )
        }
    }

    override suspend fun upsertThread(thread: ChatThread) {
        chatDao.upsertThread(thread.toEntity())
    }

    override suspend fun appendMessage(thread: ChatThread, message: ChatMessage) {
        chatDao.appendMessagesTransaction(thread.toEntity(), listOf(message.toEntity()))
    }

    override suspend fun appendMessages(thread: ChatThread, messages: List<ChatMessage>) {
        chatDao.appendMessagesTransaction(thread.toEntity(), messages.map { it.toEntity() })
    }

    override suspend fun setThreadPinned(threadId: String, pinned: Boolean) {
        chatDao.updateThreadPinned(threadId, pinned)
    }

    override suspend fun deleteThread(threadId: String) {
        chatDao.deleteThreadById(threadId)
    }

    override suspend fun markMessageSaved(messageId: String, noteId: String) {
        chatDao.markMessageSavedAsNote(messageId, noteId)
    }

    override suspend fun saveRevisionNote(note: HighlightNote) {
        revisionDao.upsertNote(note.toEntity())
    }

    override suspend fun upsertSpacedRepetitionItem(item: SpacedRepetitionItem) {
        revisionDao.upsertSpacedRepetitionItem(item.toEntity())
    }

    override suspend fun resetAll(seedSource: LearningRepository) {
        chatDao.clearAllThreads()
        revisionDao.clearAllNotes()
        revisionDao.clearAllSpacedRepetitionItems()
        ensureSeeded(seedSource)
    }
}

/**
 * In-memory implementation for fast JVM unit tests without requiring Android Context.
 */
class InMemoryStudyPersistenceRepository(
    seedSource: LearningRepository,
) : StudyPersistenceRepository {
    private val _threads = MutableStateFlow(seedSource.getInitialChatThreads())
    private val _messages = MutableStateFlow(seedSource.getInitialChatMessages())
    private val _notes = MutableStateFlow(seedSource.getInitialHighlights())
    private val _srItems = MutableStateFlow(seedSource.getInitialSpacedRepetitionItems())

    override val chatThreads: Flow<List<ChatThread>> = _threads
    override val revisionNotes: Flow<List<HighlightNote>> = _notes
    override val spacedRepetitionItems: Flow<List<SpacedRepetitionItem>> = _srItems

    override fun observeMessages(threadId: String): Flow<List<ChatMessage>> =
        _messages.map { all -> all.filter { it.threadId == threadId }.sortedBy { it.createdAtMillis } }

    override suspend fun ensureSeeded(seedSource: LearningRepository) = Unit

    override suspend fun upsertThread(thread: ChatThread) {
        _threads.update { list -> list.filterNot { it.id == thread.id } + thread }
    }

    override suspend fun appendMessage(thread: ChatThread, message: ChatMessage) {
        appendMessages(thread, listOf(message))
    }

    override suspend fun appendMessages(thread: ChatThread, messages: List<ChatMessage>) {
        upsertThread(thread)
        val incomingIds = messages.map { it.id }.toSet()
        _messages.update { list -> list.filterNot { it.id in incomingIds } + messages }
    }

    override suspend fun setThreadPinned(threadId: String, pinned: Boolean) {
        _threads.update { list ->
            list.map { if (it.id == threadId) it.copy(isPinned = pinned) else it }
        }
    }

    override suspend fun deleteThread(threadId: String) {
        _threads.update { list -> list.filterNot { it.id == threadId } }
        _messages.update { list -> list.filterNot { it.threadId == threadId } }
    }

    override suspend fun markMessageSaved(messageId: String, noteId: String) {
        _messages.update { list ->
            list.map { if (it.id == messageId) it.copy(savedNoteId = noteId) else it }
        }
    }

    override suspend fun saveRevisionNote(note: HighlightNote) {
        _notes.update { list -> listOf(note) + list.filterNot { it.id == note.id } }
    }

    override suspend fun upsertSpacedRepetitionItem(item: SpacedRepetitionItem) {
        _srItems.update { list -> list.filterNot { it.id == item.id } + item }
    }

    override suspend fun resetAll(seedSource: LearningRepository) {
        _threads.value = seedSource.getInitialChatThreads()
        _messages.value = seedSource.getInitialChatMessages()
        _notes.value = seedSource.getInitialHighlights()
        _srItems.value = seedSource.getInitialSpacedRepetitionItems()
    }
}

private fun ChatThreadEntity.toDomain() = ChatThread(
    id = id,
    title = title,
    lessonCode = lessonCode,
    lessonTitle = lessonTitle,
    isPinned = isPinned,
    updatedAtLabel = updatedAtLabel,
    updatedAtMillis = updatedAtMillis,
    previewSnippet = previewSnippet,
)

private fun ChatThread.toEntity() = ChatThreadEntity(
    id = id,
    title = title,
    lessonCode = lessonCode,
    lessonTitle = lessonTitle,
    isPinned = isPinned,
    updatedAtLabel = updatedAtLabel,
    updatedAtMillis = updatedAtMillis,
    previewSnippet = previewSnippet,
)

private fun ChatMessageEntity.toDomain() = ChatMessage(
    id = id,
    threadId = threadId,
    isFromUser = isFromUser,
    content = content,
    savedNoteId = savedNoteId,
    timestampLabel = timestampLabel,
    createdAtMillis = createdAtMillis,
)

private fun ChatMessage.toEntity() = ChatMessageEntity(
    id = id,
    threadId = threadId,
    isFromUser = isFromUser,
    content = content,
    savedNoteId = savedNoteId,
    timestampLabel = timestampLabel,
    createdAtMillis = createdAtMillis,
)

private fun RevisionNoteEntity.toDomain() = HighlightNote(
    id = id,
    lessonCode = lessonCode,
    lessonTitle = lessonTitle,
    category = runCatching { HighlightCategory.valueOf(categoryName) }.getOrDefault(HighlightCategory.KEY_IDEA),
    highlightedSnippet = highlightedSnippet,
    learnerNote = learnerNote,
    source = runCatching { NoteSource.valueOf(sourceName) }.getOrDefault(NoteSource.LESSON_HIGHLIGHT),
    savedAtLabel = savedAtLabel,
    createdAtMillis = createdAtMillis,
)

private fun HighlightNote.toEntity() = RevisionNoteEntity(
    id = id,
    lessonCode = lessonCode,
    lessonTitle = lessonTitle,
    categoryName = category.name,
    highlightedSnippet = highlightedSnippet,
    learnerNote = learnerNote,
    sourceName = source.name,
    savedAtLabel = savedAtLabel,
    createdAtMillis = createdAtMillis,
)

private fun SpacedRepetitionEntity.toDomain() = SpacedRepetitionItem(
    id = id,
    lessonCode = lessonCode,
    title = title,
    frontPrompt = frontPrompt,
    backAnswer = backAnswer,
    codeExample = codeExample,
    isDueNow = isDueNow,
    countdownLabel = countdownLabel,
    retentionPercent = retentionPercent,
    intervalTag = intervalTag,
    reviewCount = reviewCount,
)

private fun SpacedRepetitionItem.toEntity() = SpacedRepetitionEntity(
    id = id,
    lessonCode = lessonCode,
    title = title,
    frontPrompt = frontPrompt,
    backAnswer = backAnswer,
    codeExample = codeExample,
    isDueNow = isDueNow,
    countdownLabel = countdownLabel,
    retentionPercent = retentionPercent,
    intervalTag = intervalTag,
    reviewCount = reviewCount,
)
