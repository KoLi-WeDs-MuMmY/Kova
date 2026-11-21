package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.LearningRepository
import com.example.data.LocalLessonTutorEngine
import com.example.data.MockLearningRepository
import com.example.data.local.InMemoryStudyPersistenceRepository
import com.example.data.local.KovaDatabase
import com.example.data.local.RoomStudyPersistenceRepository
import com.example.data.local.StudyPersistenceRepository
import com.example.model.Chapter
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.DetailDestination
import com.example.model.FlashcardSprintState
import com.example.model.HighlightCategory
import com.example.model.HighlightNote
import com.example.model.KovaTab
import com.example.model.KovaUiState
import com.example.model.LessonStatus
import com.example.model.LockedShakeEvent
import com.example.model.NoteSource
import com.example.model.RevisionBinderTab
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class KovaViewModel(
    private val repository: LearningRepository = MockLearningRepository(),
    private val persistence: StudyPersistenceRepository = InMemoryStudyPersistenceRepository(repository),
    private val tutorEngine: LocalLessonTutorEngine = LocalLessonTutorEngine(),
    private val workDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    private val timerDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val vmScope = CoroutineScope(SupervisorJob() + workDispatcher)
    private var sprintTimerJob: Job? = null

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<KovaUiState> = _uiState.asStateFlow()

    init {
        vmScope.launch {
            persistence.ensureSeeded(repository)
        }

        vmScope.launch {
            persistence.chatThreads.collect { threads ->
                _uiState.update { state ->
                    val currentActive = state.activeThreadId
                    val resolvedActive = when {
                        currentActive != null && threads.any { it.id == currentActive } -> currentActive
                        else -> threads.firstOrNull()?.id
                    }
                    state.copy(
                        chatThreads = threads,
                        activeThreadId = resolvedActive,
                    )
                }
            }
        }

        vmScope.launch {
            _uiState
                .map { it.activeThreadId }
                .distinctUntilChanged()
                .flatMapLatest { threadId ->
                    if (threadId == null) flowOf(emptyList()) else persistence.observeMessages(threadId)
                }
                .collect { messages ->
                    _uiState.update { it.copy(activeThreadMessages = messages) }
                }
        }

        vmScope.launch {
            persistence.revisionNotes.collect { notes ->
                _uiState.update { it.copy(highlights = notes) }
            }
        }

        vmScope.launch {
            persistence.spacedRepetitionItems.collect { items ->
                _uiState.update { state ->
                    val safeIndex = if (items.isEmpty()) {
                        0
                    } else {
                        state.flashcardSprint.currentIndex.coerceIn(0, items.lastIndex)
                    }
                    state.copy(
                        spacedRepetitionItems = items,
                        flashcardSprint = state.flashcardSprint.copy(currentIndex = safeIndex),
                    )
                }
            }
        }
    }

    override fun onCleared() {
        sprintTimerJob?.cancel()
        vmScope.cancel()
        super.onCleared()
    }

    private fun createInitialState(): KovaUiState {
        val seedThreads = repository.getInitialChatThreads()
        return KovaUiState(
            selectedTab = KovaTab.LEARN,
            chapters = repository.getInitialChapters(),
            highlights = repository.getInitialHighlights(),
            spacedRepetitionItems = repository.getInitialSpacedRepetitionItems(),
            chatThreads = seedThreads,
            activeThreadId = seedThreads.firstOrNull()?.id,
            quota = repository.getInitialQuotaState(),
        )
    }

    fun selectTab(tab: KovaTab) {
        if (tab != KovaTab.REVISION && _uiState.value.flashcardSprint.isRunning) {
            pauseFlashcardSprint()
        }
        _uiState.update { state ->
            state.copy(selectedTab = tab, activeDetail = null)
        }
    }

    fun toggleChapterExpanded(chapterId: String) {
        _uiState.update { state ->
            val updated = if (chapterId in state.expandedChapterIds) {
                state.expandedChapterIds - chapterId
            } else {
                state.expandedChapterIds + chapterId
            }
            state.copy(expandedChapterIds = updated)
        }
    }

    fun openLesson(lessonId: String) {
        val current = _uiState.value
        val lesson = current.findLesson(lessonId) ?: return
        val chapter = current.findChapter(lesson.chapterId) ?: return

        val canAccess = current.devUnlockAllOverride ||
            (chapter.isUnlocked && lesson.status != LessonStatus.LOCKED)

        if (!canAccess) {
            triggerLockedNodeFeedback(
                nodeId = lessonId,
                message = if (!chapter.isUnlocked) {
                    chapter.lockReason ?: "Complete previous chapter assignment to unlock."
                } else {
                    "Pass the previous lesson's integrated test (>=80%) to unlock ${lesson.code}."
                },
            )
            return
        }

        if (!current.devUnlockAllOverride &&
            lesson.status == LessonStatus.UNLOCKED &&
            current.quota.dailyLessonsRemaining <= 0
        ) {
            triggerLockedNodeFeedback(
                nodeId = lessonId,
                message = "Daily limit reached (0/3 left). Toggle Dev Override in Settings to keep testing.",
            )
            return
        }

        _uiState.update { it.copy(activeDetail = DetailDestination.LessonDetail(lessonId)) }
    }

    fun openAssignment(assignmentId: String, originPrefix: String) {
        val current = _uiState.value
        val assignment = current.findAssignment(assignmentId) ?: return
        val canOpen = current.devUnlockAllOverride || assignment.isUnlocked || assignment.isCompleted

        if (!canOpen) {
            triggerLockedNodeFeedback(
                nodeId = assignmentId,
                message = "Finish Lesson 1.4 first to unlock ${assignment.title} (or enable Dev Override).",
            )
            return
        }

        _uiState.update {
            it.copy(
                activeDetail = DetailDestination.AssignmentDetail(
                    assignmentId = assignmentId,
                    originPrefix = originPrefix,
                ),
            )
        }
    }

    fun openNote(noteId: String) {
        _uiState.update { it.copy(activeDetail = DetailDestination.NoteDetail(noteId)) }
    }

    fun openRevisionSheet(chapterId: String) {
        _uiState.update { it.copy(activeDetail = DetailDestination.RevisionSheetDetail(chapterId)) }
    }

    fun closeDetail() {
        _uiState.update { it.copy(activeDetail = null) }
    }

    fun passLessonMockTest(lessonId: String, scorePercent: Int = 85) {
        _uiState.update { state ->
            val targetLesson = state.findLesson(lessonId) ?: return@update state
            val wasAlreadyCompleted = targetLesson.status == LessonStatus.COMPLETED

            var nextLessonTitle: String? = null
            var unlockedAssignmentTitle: String? = null

            val updatedChapters = state.chapters.map { chapter ->
                if (chapter.id != targetLesson.chapterId) {
                    chapter
                } else {
                    val targetIndex = chapter.lessons.indexOfFirst { it.id == lessonId }
                    val updatedLessons = chapter.lessons.mapIndexed { idx, lesson ->
                        when {
                            idx == targetIndex -> lesson.copy(
                                status = LessonStatus.COMPLETED,
                                passScorePercent = scorePercent,
                            )
                            idx == targetIndex + 1 && lesson.status == LessonStatus.LOCKED -> {
                                nextLessonTitle = "${lesson.code} ${lesson.title}"
                                lesson.copy(status = LessonStatus.UNLOCKED)
                            }
                            else -> lesson
                        }
                    }

                    val allLessonsComplete = updatedLessons.all { it.status == LessonStatus.COMPLETED }
                    val updatedAssignment = if (allLessonsComplete && !chapter.assignment.isUnlocked) {
                        unlockedAssignmentTitle = chapter.assignment.title
                        chapter.assignment.copy(isUnlocked = true)
                    } else {
                        chapter.assignment
                    }

                    chapter.copy(
                        lessons = updatedLessons,
                        assignment = updatedAssignment,
                    )
                }
            }

            val updatedQuota = if (!wasAlreadyCompleted && !state.devUnlockAllOverride) {
                state.quota.copy(
                    dailyLessonsRemaining = (state.quota.dailyLessonsRemaining - 1).coerceAtLeast(0),
                )
            } else {
                state.quota
            }

            val feedback = when {
                wasAlreadyCompleted -> "${targetLesson.code} reviewed ($scorePercent%)"
                unlockedAssignmentTitle != null ->
                    "Passed ${targetLesson.code} ($scorePercent%)! Chapter Assignment trophy unlocked."
                nextLessonTitle != null ->
                    "Passed ${targetLesson.code} ($scorePercent%)! Unlocked $nextLessonTitle."
                else -> "Passed ${targetLesson.code} ($scorePercent%)!"
            }

            state.copy(
                chapters = updatedChapters,
                quota = updatedQuota,
                snackbarMessage = feedback,
            )
        }
    }

    fun completeAssignment(assignmentId: String) {
        _uiState.update { state ->
            val targetChapterIndex = state.chapters.indexOfFirst { it.assignment.id == assignmentId }
            if (targetChapterIndex == -1) return@update state

            val updatedChapters = state.chapters.mapIndexed { index, chapter ->
                when (index) {
                    targetChapterIndex -> chapter.copy(
                        assignment = chapter.assignment.copy(
                            isUnlocked = true,
                            isCompleted = true,
                        ),
                    )
                    targetChapterIndex + 1 -> unlockChapter(chapter)
                    else -> chapter
                }
            }

            val nextChapter = updatedChapters.getOrNull(targetChapterIndex + 1)
            val updatedExpanded = if (nextChapter != null) {
                state.expandedChapterIds + nextChapter.id
            } else {
                state.expandedChapterIds
            }
            val message = if (nextChapter != null) {
                "Assignment complete! Chapter ${nextChapter.number}: ${nextChapter.title} is now unlocked."
            } else {
                "Assignment marked complete!"
            }

            state.copy(
                chapters = updatedChapters,
                expandedChapterIds = updatedExpanded,
                snackbarMessage = message,
            )
        }
    }

    private fun unlockChapter(chapter: Chapter): Chapter {
        val updatedLessons = chapter.lessons.mapIndexed { idx, lesson ->
            if (idx == 0 && lesson.status == LessonStatus.LOCKED) {
                lesson.copy(status = LessonStatus.UNLOCKED)
            } else {
                lesson
            }
        }
        return chapter.copy(
            isUnlocked = true,
            lockReason = null,
            lessons = updatedLessons,
        )
    }

    // -------------------------------------------------------------------------
    // Phase 2 · AI Workspace (Room-persisted Chat History + Save to Notes)
    // -------------------------------------------------------------------------

    fun selectChatThread(threadId: String) {
        _uiState.update { it.copy(activeThreadId = threadId) }
    }

    fun createNewLessonThread() {
        val state = _uiState.value
        val activeLesson = state.activeLesson
        val lessonCode = activeLesson?.code ?: "1.1"
        val lessonTitle = activeLesson?.title ?: "Variables & Boxes"
        val now = System.currentTimeMillis()
        val threadId = "thread_$now"

        val newThread = ChatThread(
            id = threadId,
            title = "Lesson $lessonCode · $lessonTitle",
            lessonCode = lessonCode,
            lessonTitle = lessonTitle,
            isPinned = false,
            updatedAtLabel = "Just now",
            updatedAtMillis = now,
            previewSnippet = "Scoped to Lesson $lessonCode. Ask anything or tap a quick prompt.",
        )

        val welcomeMessage = ChatMessage(
            id = "msg_${now}_welcome",
            threadId = threadId,
            isFromUser = false,
            content = "Scoped Tutor ready for Lesson $lessonCode ($lessonTitle). " +
                "Ask a question or pick a quick prompt chip below—every answer can be saved straight to your Revision Binder.",
            savedNoteId = null,
            timestampLabel = "Just now",
            createdAtMillis = now,
        )

        _uiState.update { it.copy(activeThreadId = threadId) }
        vmScope.launch {
            persistence.appendMessage(newThread, welcomeMessage)
        }
    }

    fun updateChatSearchQuery(query: String) {
        _uiState.update { it.copy(chatSearchQuery = query) }
    }

    fun togglePinThread(threadId: String) {
        val target = _uiState.value.chatThreads.firstOrNull { it.id == threadId } ?: return
        val nextPinned = !target.isPinned
        vmScope.launch {
            persistence.setThreadPinned(threadId, nextPinned)
        }
        _uiState.update {
            it.copy(
                snackbarMessage = if (nextPinned) "Thread pinned to top." else "Thread unpinned.",
            )
        }
    }

    fun deleteChatThread(threadId: String) {
        vmScope.launch {
            persistence.deleteThread(threadId)
        }
        _uiState.update { state ->
            val nextId = if (state.activeThreadId == threadId) {
                state.chatThreads.firstOrNull { it.id != threadId }?.id
            } else {
                state.activeThreadId
            }
            state.copy(
                activeThreadId = nextId,
                snackbarMessage = "Chat thread deleted.",
            )
        }
    }

    fun updateChatComposerText(text: String) {
        _uiState.update { it.copy(chatComposerText = text) }
    }

    /**
     * Fills the editable composer box so the learner can refine before sending.
     */
    fun insertQuickPrompt(promptTemplate: String) {
        val activeCode = _uiState.value.activeLesson?.code ?: "1.1"
        val filled = "$promptTemplate (Lesson $activeCode)"
        _uiState.update { it.copy(chatComposerText = filled) }
    }

    fun sendChatMessage() {
        val state = _uiState.value
        val prompt = state.chatComposerText.trim()
        if (prompt.isEmpty()) return

        val now = System.currentTimeMillis()
        val activeLesson = state.activeLesson
        val currentThread = state.activeThread ?: ChatThread(
            id = "thread_$now",
            title = prompt.take(34),
            lessonCode = activeLesson?.code ?: "1.1",
            lessonTitle = activeLesson?.title ?: "Variables & Boxes",
            isPinned = false,
            updatedAtLabel = "Just now",
            updatedAtMillis = now,
            previewSnippet = prompt,
        )

        val matchingLesson = state.chapters
            .flatMap { it.lessons }
            .firstOrNull { it.code == currentThread.lessonCode }
            ?: activeLesson

        val userMsg = ChatMessage(
            id = "msg_${now}_u",
            threadId = currentThread.id,
            isFromUser = true,
            content = prompt,
            savedNoteId = null,
            timestampLabel = "Just now",
            createdAtMillis = now,
        )

        val replyText = tutorEngine.generateScopedReply(
            prompt = prompt,
            lessonCode = currentThread.lessonCode,
            lessonTitle = currentThread.lessonTitle,
            lesson = matchingLesson,
        )

        val aiMsg = ChatMessage(
            id = "msg_${now + 1}_ai",
            threadId = currentThread.id,
            isFromUser = false,
            content = replyText,
            savedNoteId = null,
            timestampLabel = "Just now",
            createdAtMillis = now + 1,
        )

        val updatedThread = currentThread.copy(
            updatedAtLabel = "Just now",
            updatedAtMillis = now + 1,
            previewSnippet = replyText.lineSequence().firstOrNull()?.take(90) ?: prompt,
        )

        _uiState.update {
            it.copy(
                activeThreadId = updatedThread.id,
                chatComposerText = "",
                quota = it.quota.copy(
                    activeAiPoolRemaining = (it.quota.activeAiPoolRemaining - 1).coerceAtLeast(0),
                ),
            )
        }

        vmScope.launch {
            persistence.appendMessage(updatedThread, userMsg)
            persistence.appendMessage(updatedThread, aiMsg)
        }
    }

    fun saveAiMessageToNotes(messageId: String, category: HighlightCategory) {
        val state = _uiState.value
        val message = state.activeThreadMessages.firstOrNull { it.id == messageId } ?: return
        val thread = state.activeThread
        val now = System.currentTimeMillis()
        val noteId = "note_ai_$now"

        val lines = message.content.lines().filter { it.isNotBlank() }
        val snippet = lines.firstOrNull()?.take(120) ?: message.content.take(120)
        val details = if (lines.size > 1) {
            lines.drop(1).joinToString("\n")
        } else {
            message.content
        }

        val note = HighlightNote(
            id = noteId,
            lessonCode = thread?.lessonCode ?: state.activeLesson?.code ?: "1.1",
            lessonTitle = thread?.lessonTitle ?: state.activeLesson?.title ?: "Variables & Boxes",
            category = category,
            highlightedSnippet = snippet,
            learnerNote = details,
            source = NoteSource.AI_TUTOR,
            savedAtLabel = "Just now · AI Tutor",
            createdAtMillis = now,
        )

        vmScope.launch {
            persistence.saveRevisionNote(note)
            persistence.markMessageSaved(messageId, noteId)
        }

        val categoryLabel = when (category) {
            HighlightCategory.KEY_IDEA -> "Key Idea"
            HighlightCategory.CODE_RULE -> "Code Rule"
            HighlightCategory.GOTCHA -> "Gotcha"
        }
        _uiState.update {
            it.copy(snackbarMessage = "Saved to Revision Notes ($categoryLabel).")
        }
    }

    // -------------------------------------------------------------------------
    // Phase 2 · Revision Study Binder, Mastery Radar & 60s 3D Flashcards
    // -------------------------------------------------------------------------

    fun selectBinderTab(tab: RevisionBinderTab) {
        if (tab != RevisionBinderTab.FLASHCARDS && _uiState.value.flashcardSprint.isRunning) {
            pauseFlashcardSprint()
        }
        _uiState.update { it.copy(selectedBinderTab = tab) }
    }

    fun updateRevisionSearchQuery(query: String) {
        _uiState.update { it.copy(revisionSearchQuery = query) }
    }

    fun selectHighlightFilter(category: HighlightCategory?) {
        _uiState.update { it.copy(selectedHighlightFilter = category) }
    }

    fun focusNextFromRadar() {
        val radar = _uiState.value.masteryRadar
        if (radar.preferFlashcardFocus && _uiState.value.flashcardDeck.isNotEmpty()) {
            startQuickReviewFromQueue()
        } else if (radar.focusLessonId != null) {
            _uiState.update { it.copy(selectedTab = KovaTab.LEARN) }
            openLesson(radar.focusLessonId)
        } else {
            startQuickReviewFromQueue()
        }
    }

    fun startQuickReviewFromQueue() {
        _uiState.update {
            it.copy(
                selectedTab = KovaTab.REVISION,
                selectedBinderTab = RevisionBinderTab.FLASHCARDS,
            )
        }
        restartFlashcardSprint(autoStart = true)
    }

    fun startOrResumeFlashcardSprint() {
        val sprint = _uiState.value.flashcardSprint
        if (sprint.isSummaryVisible || sprint.secondsRemaining <= 0) {
            restartFlashcardSprint(autoStart = true)
            return
        }
        _uiState.update {
            it.copy(
                flashcardSprint = it.flashcardSprint.copy(
                    isRunning = true,
                    isPaused = false,
                ),
            )
        }
        launchSprintCountdown()
    }

    fun togglePauseFlashcardSprint() {
        val sprint = _uiState.value.flashcardSprint
        if (sprint.isRunning && !sprint.isPaused) {
            pauseFlashcardSprint()
        } else {
            startOrResumeFlashcardSprint()
        }
    }

    private fun pauseFlashcardSprint() {
        sprintTimerJob?.cancel()
        sprintTimerJob = null
        _uiState.update {
            it.copy(
                flashcardSprint = it.flashcardSprint.copy(
                    isRunning = false,
                    isPaused = true,
                ),
            )
        }
    }

    fun restartFlashcardSprint(autoStart: Boolean = true) {
        sprintTimerJob?.cancel()
        sprintTimerJob = null
        _uiState.update {
            it.copy(
                flashcardSprint = FlashcardSprintState(
                    secondsRemaining = 60,
                    totalSeconds = 60,
                    isRunning = autoStart,
                    isPaused = !autoStart,
                    currentIndex = 0,
                    isFlipped = false,
                    gotItCount = 0,
                    reviewAgainCount = 0,
                    isSummaryVisible = false,
                ),
            )
        }
        if (autoStart) {
            launchSprintCountdown()
        }
    }

    private fun launchSprintCountdown() {
        sprintTimerJob?.cancel()
        sprintTimerJob = vmScope.launch(timerDispatcher) {
            while (true) {
                delay(1000L)
                val current = _uiState.value.flashcardSprint
                if (!current.isRunning || current.isPaused || current.isSummaryVisible) break
                val nextSeconds = (current.secondsRemaining - 1).coerceAtLeast(0)
                if (nextSeconds == 0) {
                    _uiState.update {
                        it.copy(
                            flashcardSprint = it.flashcardSprint.copy(
                                secondsRemaining = 0,
                                isRunning = false,
                                isPaused = false,
                                isSummaryVisible = true,
                            ),
                        )
                    }
                    break
                } else {
                    _uiState.update {
                        it.copy(
                            flashcardSprint = it.flashcardSprint.copy(
                                secondsRemaining = nextSeconds,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun flipFlashcard() {
        val sprint = _uiState.value.flashcardSprint
        if (sprint.isSummaryVisible) return
        val shouldAutoStart = !sprint.isRunning && !sprint.isPaused && sprint.secondsRemaining == sprint.totalSeconds
        _uiState.update {
            it.copy(
                flashcardSprint = it.flashcardSprint.copy(
                    isFlipped = !it.flashcardSprint.isFlipped,
                    isRunning = if (shouldAutoStart) true else it.flashcardSprint.isRunning,
                ),
            )
        }
        if (shouldAutoStart) {
            launchSprintCountdown()
        }
    }

    fun rateFlashcardGotIt() {
        rateCurrentFlashcard(gotIt = true)
    }

    fun rateFlashcardReviewAgain() {
        rateCurrentFlashcard(gotIt = false)
    }

    private fun rateCurrentFlashcard(gotIt: Boolean) {
        val state = _uiState.value
        val deck = state.flashcardDeck
        if (deck.isEmpty()) return

        val sprint = state.flashcardSprint
        val card = deck[sprint.currentIndex.coerceIn(0, deck.lastIndex)]

        val updatedCard = if (gotIt) {
            val nextInterval = when (card.intervalTag) {
                "6h" -> "1d"
                "1d" -> "3d"
                "3d" -> "7d"
                else -> "14d"
            }
            card.copy(
                isDueNow = false,
                countdownLabel = "Scheduled in $nextInterval",
                retentionPercent = (card.retentionPercent + 8).coerceAtMost(99),
                intervalTag = nextInterval,
                reviewCount = card.reviewCount + 1,
            )
        } else {
            card.copy(
                isDueNow = true,
                countdownLabel = "Due now · Repeat soon",
                retentionPercent = (card.retentionPercent - 10).coerceAtLeast(35),
                intervalTag = "6h",
                reviewCount = card.reviewCount + 1,
            )
        }

        val nextGotIt = sprint.gotItCount + if (gotIt) 1 else 0
        val nextReviewAgain = sprint.reviewAgainCount + if (gotIt) 0 else 1
        val totalRated = nextGotIt + nextReviewAgain
        val reachedEnd = totalRated >= deck.size
        val nextIndex = (sprint.currentIndex + 1) % deck.size

        if (reachedEnd) {
            sprintTimerJob?.cancel()
            sprintTimerJob = null
        }

        _uiState.update {
            it.copy(
                flashcardSprint = sprint.copy(
                    currentIndex = nextIndex,
                    isFlipped = false,
                    gotItCount = nextGotIt,
                    reviewAgainCount = nextReviewAgain,
                    isRunning = if (reachedEnd) false else sprint.isRunning,
                    isSummaryVisible = reachedEnd,
                ),
                snackbarMessage = if (gotIt) {
                    "Got it! '${card.title}' pushed to ${updatedCard.intervalTag}."
                } else {
                    "Queued '${card.title}' for repeat recall."
                },
            )
        }

        vmScope.launch {
            persistence.upsertSpacedRepetitionItem(updatedCard)
        }
    }

    // -------------------------------------------------------------------------
    // Appearance, Floating Mode & Developer Controls
    // -------------------------------------------------------------------------

    fun toggleFloatingPreview() {
        _uiState.update { it.copy(isFloatingPreview = !it.isFloatingPreview) }
    }

    fun setFloatingPreview(enabled: Boolean) {
        _uiState.update { it.copy(isFloatingPreview = enabled) }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        _uiState.update { it.copy(dynamicColorEnabled = enabled) }
    }

    fun setExpressiveMotionEnabled(enabled: Boolean) {
        _uiState.update { it.copy(expressiveMotionEnabled = enabled) }
    }

    fun setDevUnlockAllOverride(enabled: Boolean) {
        _uiState.update { state ->
            if (enabled) {
                val unlockedChapters = state.chapters.map { chapter ->
                    chapter.copy(
                        isUnlocked = true,
                        lockReason = null,
                        lessons = chapter.lessons.map { lesson ->
                            if (lesson.status == LessonStatus.LOCKED) {
                                lesson.copy(status = LessonStatus.UNLOCKED)
                            } else {
                                lesson
                            }
                        },
                        assignment = chapter.assignment.copy(isUnlocked = true),
                    )
                }
                state.copy(
                    devUnlockAllOverride = true,
                    chapters = unlockedChapters,
                    quota = state.quota.copy(dailyLessonsRemaining = state.quota.dailyLessonsMax),
                    snackbarMessage = "Dev Override ON: All chapters unlocked & daily limit reset (3/3).",
                )
            } else {
                state.copy(
                    devUnlockAllOverride = false,
                    snackbarMessage = "Dev Override OFF: Standard mastery gating resumed.",
                )
            }
        }
    }

    fun resetDemoProgress() {
        sprintTimerJob?.cancel()
        sprintTimerJob = null
        vmScope.launch {
            persistence.resetAll(repository)
        }
        _uiState.update { current ->
            createInitialState().copy(
                themeMode = current.themeMode,
                dynamicColorEnabled = current.dynamicColorEnabled,
                expressiveMotionEnabled = current.expressiveMotionEnabled,
                isFloatingPreview = current.isFloatingPreview,
                isSettingsOpen = false,
                snackbarMessage = "Phase 2 study data & progress reset.",
            )
        }
    }

    fun setSettingsOpen(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun triggerLockedNodeFeedback(nodeId: String, message: String) {
        _uiState.update { state ->
            state.copy(
                lockedShakeEvent = LockedShakeEvent(
                    nodeId = nodeId,
                    nonce = System.nanoTime(),
                ),
                snackbarMessage = message,
            )
        }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

class KovaViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = KovaDatabase.getInstance(context.applicationContext)
        val repository = MockLearningRepository()
        val persistence = RoomStudyPersistenceRepository(
            chatDao = db.chatDao(),
            revisionDao = db.revisionDao(),
        )
        return KovaViewModel(
            repository = repository,
            persistence = persistence,
            tutorEngine = LocalLessonTutorEngine(),
            workDispatcher = Dispatchers.IO,
            timerDispatcher = Dispatchers.Default,
        ) as T
    }
}
