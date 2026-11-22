package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.LearningRepository
import com.example.data.LocalLessonTutorEngine
import com.example.data.MockLearningRepository
import com.example.data.local.FileKovaAppStateStore
import com.example.data.local.InMemoryKovaAppStateStore
import com.example.data.local.InMemoryStudyPersistenceRepository
import com.example.data.local.KovaAppStateStore
import com.example.data.local.KovaDatabase
import com.example.data.local.PersistedAppState
import com.example.data.local.PersistedChapterState
import com.example.data.local.PersistedLessonState
import com.example.data.local.RoomStudyPersistenceRepository
import com.example.data.local.StudyPersistenceRepository
import com.example.data.local.currentLocalDateIso
import com.example.model.AiTutorStatus
import com.example.model.Chapter
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.DetailDestination
import com.example.model.FlashcardSprintState
import com.example.model.HighlightCategory
import com.example.model.HighlightNote
import com.example.model.KovaTab
import com.example.model.KovaUiState
import com.example.model.LessonReturnEvent
import com.example.model.LessonSplitRatio
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class KovaViewModel(
    private val repository: LearningRepository = MockLearningRepository(),
    private val persistence: StudyPersistenceRepository = InMemoryStudyPersistenceRepository(repository),
    private val appStateStore: KovaAppStateStore = InMemoryKovaAppStateStore(),
    private val tutorEngine: LocalLessonTutorEngine = LocalLessonTutorEngine(),
    private val dateProvider: () -> String = ::currentLocalDateIso,
    private val workDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    private val timerDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val vmScope = CoroutineScope(SupervisorJob() + workDispatcher)
    private var sprintTimerJob: Job? = null
    private var aiStatusJob: Job? = null

    // Optimistic per-thread message buffer ensuring Room emissions can NEVER drop newly sent messages
    private val pendingLock = Any()
    private val pendingMessagesByThread = mutableMapOf<String, LinkedHashMap<String, ChatMessage>>()

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<KovaUiState> = _uiState.asStateFlow()

    init {
        // Persist immediately if date rollover or first-launch snapshot creation occurred
        persistSnapshot(_uiState.value)

        vmScope.launch {
            persistence.ensureSeeded(repository)
        }

        vmScope.launch {
            persistence.chatThreads.collect { threads ->
                if (threads.isEmpty() && _uiState.value.chatThreads.isNotEmpty()) {
                    return@collect
                }
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
                    if (threadId == null) {
                        flowOf(Pair<String?, List<ChatMessage>>(null, emptyList()))
                    } else {
                        persistence.observeMessages(threadId).map { msgs -> Pair(threadId, msgs) }
                    }
                }
                .collect { (threadId, persistedMessages) ->
                    _uiState.update { state ->
                        if (threadId == null) {
                            state.copy(activeThreadMessages = emptyList())
                        } else if (state.activeThreadId != threadId) {
                            state
                        } else {
                            val merged = mergeWithPendingMessages(threadId, persistedMessages)
                            state.copy(activeThreadMessages = merged)
                        }
                    }
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
        aiStatusJob?.cancel()
        persistSnapshot(_uiState.value)
        vmScope.cancel()
        super.onCleared()
    }

    private fun recordPendingMessages(threadId: String, messages: List<ChatMessage>) {
        synchronized(pendingLock) {
            val map = pendingMessagesByThread.getOrPut(threadId) { LinkedHashMap() }
            messages.forEach { map[it.id] = it }
        }
    }

    private fun mergeWithPendingMessages(
        threadId: String,
        persistedMessages: List<ChatMessage>,
    ): List<ChatMessage> {
        synchronized(pendingLock) {
            val persistedIds = persistedMessages.mapTo(HashSet()) { it.id }
            val pendingMap = pendingMessagesByThread[threadId]
            if (pendingMap != null) {
                persistedIds.forEach { pendingMap.remove(it) }
                if (pendingMap.isEmpty()) {
                    pendingMessagesByThread.remove(threadId)
                }
            }
            val remainingPending = pendingMap?.values?.toList().orEmpty()
            return if (remainingPending.isEmpty()) {
                persistedMessages
            } else {
                (persistedMessages + remainingPending).sortedBy { it.createdAtMillis }
            }
        }
    }

    private fun createInitialState(): KovaUiState {
        val todayIso = dateProvider()
        val baseChapters = repository.getInitialChapters()
        val baseQuota = repository.getInitialQuotaState().copy(lastQuotaDateIso = todayIso)
        val seedThreads = repository.getInitialChatThreads()
        val saved = appStateStore.load()

        if (saved == null) {
            return KovaUiState(
                selectedTab = KovaTab.LEARN,
                chapters = baseChapters,
                highlights = repository.getInitialHighlights(),
                spacedRepetitionItems = repository.getInitialSpacedRepetitionItems(),
                chatThreads = seedThreads,
                activeThreadId = seedThreads.firstOrNull()?.id,
                quota = baseQuota,
            )
        }

        val restoredTheme = runCatching { ThemeMode.valueOf(saved.themeModeName) }
            .getOrDefault(ThemeMode.WARM_DAYLIGHT)
        val restoredSplitRatio = runCatching { LessonSplitRatio.valueOf(saved.lessonSplitRatioName) }
            .getOrDefault(LessonSplitRatio.RATIO_75_25)

        val hydratedChapters = baseChapters.map { chapter ->
            val savedChapter = saved.chapterStates[chapter.id]
            val hydratedLessons = chapter.lessons.map { lesson ->
                val savedLesson = saved.lessonStates[lesson.id]
                if (savedLesson != null) {
                    val status = runCatching { LessonStatus.valueOf(savedLesson.statusName) }
                        .getOrDefault(lesson.status)
                    lesson.copy(
                        status = status,
                        passScorePercent = savedLesson.passScorePercent,
                    )
                } else {
                    lesson
                }
            }
            if (savedChapter != null) {
                chapter.copy(
                    isUnlocked = savedChapter.isUnlocked,
                    lockReason = if (savedChapter.isUnlocked) null else chapter.lockReason,
                    lessons = hydratedLessons,
                    assignment = chapter.assignment.copy(
                        isUnlocked = savedChapter.assignmentUnlocked,
                        isCompleted = savedChapter.assignmentCompleted,
                    ),
                )
            } else {
                chapter.copy(lessons = hydratedLessons)
            }
        }

        val isNewCalendarDay = saved.lastQuotaDateIso != todayIso
        val effectiveDailyRemaining = when {
            saved.devUnlockAllOverride -> baseQuota.dailyLessonsMax
            isNewCalendarDay -> baseQuota.dailyLessonsMax
            else -> saved.dailyLessonsRemaining.coerceIn(0, baseQuota.dailyLessonsMax)
        }
        val effectiveAiPool = if (isNewCalendarDay) {
            baseQuota.activeAiPoolMax
        } else {
            saved.activeAiPoolRemaining.coerceIn(0, baseQuota.activeAiPoolMax)
        }

        val restoredQuota = baseQuota.copy(
            dailyLessonsRemaining = effectiveDailyRemaining,
            activeAiPoolRemaining = effectiveAiPool,
            lastQuotaDateIso = todayIso,
        )

        val restoredSprint = FlashcardSprintState(
            currentIndex = saved.sprintCurrentIndex.coerceAtLeast(0),
            gotItCount = saved.sprintGotItCount.coerceAtLeast(0),
            reviewAgainCount = saved.sprintReviewAgainCount.coerceAtLeast(0),
        )

        return KovaUiState(
            selectedTab = KovaTab.LEARN,
            chapters = hydratedChapters,
            expandedChapterIds = saved.expandedChapterIds.ifEmpty { setOf("ch_1") },
            highlights = repository.getInitialHighlights(),
            spacedRepetitionItems = repository.getInitialSpacedRepetitionItems(),
            chatThreads = seedThreads,
            activeThreadId = saved.activeThreadId ?: seedThreads.firstOrNull()?.id,
            chatComposerText = saved.chatComposerDraft,
            flashcardSprint = restoredSprint,
            quota = restoredQuota,
            lessonSplitRatio = restoredSplitRatio,
            lastActiveLessonId = saved.lastActiveLessonId,
            unlockedDebuggerLessonIds = saved.unlockedDebuggerLessonIds,
            lessonScrollPositions = saved.lessonScrollPositions,
            activeExplainRuleByLesson = saved.activeExplainRuleByLesson,
            themeMode = restoredTheme,
            dynamicColorEnabled = saved.dynamicColorEnabled,
            expressiveMotionEnabled = saved.expressiveMotionEnabled,
            soundAndHapticsEnabled = saved.soundAndHapticsEnabled,
            devUnlockAllOverride = saved.devUnlockAllOverride,
        )
    }

    private fun persistSnapshot(state: KovaUiState = _uiState.value) {
        val todayIso = state.quota.lastQuotaDateIso.ifBlank { dateProvider() }
        val lessonStates = buildMap {
            state.chapters.forEach { chapter ->
                chapter.lessons.forEach { lesson ->
                    put(
                        lesson.id,
                        PersistedLessonState(
                            statusName = lesson.status.name,
                            passScorePercent = lesson.passScorePercent,
                        ),
                    )
                }
            }
        }
        val chapterStates = buildMap {
            state.chapters.forEach { chapter ->
                put(
                    chapter.id,
                    PersistedChapterState(
                        isUnlocked = chapter.isUnlocked,
                        assignmentUnlocked = chapter.assignment.isUnlocked,
                        assignmentCompleted = chapter.assignment.isCompleted,
                    ),
                )
            }
        }
        appStateStore.save(
            PersistedAppState(
                themeModeName = state.themeMode.name,
                dynamicColorEnabled = state.dynamicColorEnabled,
                expressiveMotionEnabled = state.expressiveMotionEnabled,
                soundAndHapticsEnabled = state.soundAndHapticsEnabled,
                devUnlockAllOverride = state.devUnlockAllOverride,
                lessonSplitRatioName = state.lessonSplitRatio.name,
                expandedChapterIds = state.expandedChapterIds,
                dailyLessonsRemaining = state.quota.dailyLessonsRemaining,
                activeAiPoolRemaining = state.quota.activeAiPoolRemaining,
                lastQuotaDateIso = todayIso,
                activeThreadId = state.activeThreadId,
                chatComposerDraft = state.chatComposerText,
                sprintGotItCount = state.flashcardSprint.gotItCount,
                sprintReviewAgainCount = state.flashcardSprint.reviewAgainCount,
                sprintCurrentIndex = state.flashcardSprint.currentIndex,
                lessonStates = lessonStates,
                chapterStates = chapterStates,
                unlockedDebuggerLessonIds = state.unlockedDebuggerLessonIds,
                lessonScrollPositions = state.lessonScrollPositions,
                activeExplainRuleByLesson = state.activeExplainRuleByLesson,
                lastActiveLessonId = state.lastActiveLessonId,
            ),
        )
    }

    fun checkDailyRollover() {
        val todayIso = dateProvider()
        var rolledOver = false
        _uiState.update { state ->
            if (state.quota.lastQuotaDateIso != todayIso) {
                rolledOver = true
                state.copy(
                    quota = state.quota.copy(
                        dailyLessonsRemaining = state.quota.dailyLessonsMax,
                        activeAiPoolRemaining = state.quota.activeAiPoolMax,
                        lastQuotaDateIso = todayIso,
                    ),
                )
            } else {
                state
            }
        }
        if (rolledOver) {
            persistSnapshot()
        }
    }

    fun selectTab(tab: KovaTab) {
        if (tab != KovaTab.REVISION && _uiState.value.flashcardSprint.isRunning) {
            pauseFlashcardSprint()
        }
        _uiState.update { state ->
            state.copy(
                selectedTab = tab,
                activeDetail = null,
                focusedChapterId = if (tab == KovaTab.LEARN) state.focusedChapterId else null,
                isChapterAiSheetOpen = false,
                isFlashcardFocusStageOpen = false,
                returnToLessonIdAfterAi = null,
                returnToChapterIdAfterAi = null,
            )
        }
    }

    fun navigateBackToLearnHome() {
        if (_uiState.value.flashcardSprint.isRunning) {
            pauseFlashcardSprint()
        }
        _uiState.update { state ->
            state.copy(
                selectedTab = KovaTab.LEARN,
                activeDetail = null,
                focusedChapterId = null,
                isChapterAiSheetOpen = false,
                isFlashcardFocusStageOpen = false,
                returnToLessonIdAfterAi = null,
                returnToChapterIdAfterAi = null,
            )
        }
    }

    fun openAiFromLesson(lessonId: String) {
        val current = _uiState.value
        val lesson = current.findLesson(lessonId) ?: return
        val matchingThread = current.chatThreads.firstOrNull { it.lessonCode == lesson.code }
            ?: current.activeThread
        _uiState.update { state ->
            state.copy(
                activeDetail = null,
                isChapterAiSheetOpen = false,
                selectedTab = KovaTab.AI,
                activeThreadId = matchingThread?.id ?: state.activeThreadId,
                returnToLessonIdAfterAi = lessonId,
                returnToChapterIdAfterAi = state.focusedChapterId ?: lesson.chapterId,
            )
        }
        persistSnapshot()
    }

    fun returnFromAiToLesson(): Boolean {
        val current = _uiState.value
        val targetLessonId = current.returnToLessonIdAfterAi ?: return false
        val targetChapterId = current.returnToChapterIdAfterAi
        _uiState.update { state ->
            state.copy(
                selectedTab = KovaTab.LEARN,
                focusedChapterId = targetChapterId,
                activeDetail = DetailDestination.LessonDetail(targetLessonId),
                isChapterAiSheetOpen = false,
                returnToLessonIdAfterAi = null,
                returnToChapterIdAfterAi = null,
            )
        }
        return true
    }

    fun showExitPromptSnackbar(message: String = "Press back again to exit Kova") {
        _uiState.update { it.copy(snackbarMessage = message) }
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
        persistSnapshot()
    }

    fun openChapterFocus(chapterId: String) {
        _uiState.update { state ->
            state.copy(
                selectedTab = KovaTab.LEARN,
                focusedChapterId = chapterId,
                expandedChapterIds = state.expandedChapterIds + chapterId,
                isChapterAiSheetOpen = false,
            )
        }
        persistSnapshot()
    }

    fun closeChapterFocus() {
        _uiState.update { state ->
            state.copy(
                focusedChapterId = null,
                isChapterAiSheetOpen = false,
            )
        }
    }

    fun setChapterAiSheetOpen(open: Boolean) {
        _uiState.update { state ->
            state.copy(isChapterAiSheetOpen = open)
        }
    }

    fun switchFromChapterFocusToAiTab() {
        _uiState.update { state ->
            state.copy(
                isChapterAiSheetOpen = false,
                focusedChapterId = null,
                activeDetail = null,
                selectedTab = KovaTab.AI,
            )
        }
    }

    fun toggleAiHistoryVisibility(isWideTabletLayout: Boolean) {
        _uiState.update { state ->
            val currentlyVisible = state.isAiHistoryVisible ?: isWideTabletLayout
            state.copy(isAiHistoryVisible = !currentlyVisible)
        }
    }

    fun setAiHistoryVisibility(visible: Boolean) {
        _uiState.update { state ->
            state.copy(isAiHistoryVisible = visible)
        }
    }

    fun openLesson(lessonId: String) {
        checkDailyRollover()
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

        _uiState.update {
            it.copy(
                activeDetail = DetailDestination.LessonDetail(lessonId),
                lastActiveLessonId = lessonId,
                newlyCompletedLessonInSession = null,
            )
        }
        persistSnapshot()
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
        _uiState.update { state ->
            val currentDetail = state.activeDetail
            val returnEvent = if (currentDetail is DetailDestination.LessonDetail) {
                LessonReturnEvent(
                    lessonId = currentDetail.lessonId,
                    wasNewlyCompleted = state.newlyCompletedLessonInSession == currentDetail.lessonId,
                    nonce = System.nanoTime(),
                )
            } else {
                state.lessonReturnEvent
            }
            state.copy(
                activeDetail = null,
                newlyCompletedLessonInSession = null,
                lessonReturnEvent = returnEvent,
            )
        }
    }

    fun cycleLessonSplitRatio() {
        _uiState.update { state ->
            state.copy(lessonSplitRatio = state.lessonSplitRatio.next())
        }
        persistSnapshot()
    }

    fun setLessonSplitRatio(ratio: LessonSplitRatio) {
        if (_uiState.value.lessonSplitRatio == ratio) return
        _uiState.update { state ->
            state.copy(lessonSplitRatio = ratio)
        }
        persistSnapshot()
    }

    fun unlockLessonDebugger(lessonId: String) {
        _uiState.update { state ->
            state.copy(
                unlockedDebuggerLessonIds = state.unlockedDebuggerLessonIds + lessonId,
            )
        }
        persistSnapshot()
    }

    fun setLessonDebuggerUnlocked(lessonId: String, unlocked: Boolean) {
        _uiState.update { state ->
            val updated = if (unlocked) {
                state.unlockedDebuggerLessonIds + lessonId
            } else {
                state.unlockedDebuggerLessonIds - lessonId
            }
            state.copy(unlockedDebuggerLessonIds = updated)
        }
        persistSnapshot()
    }

    fun updateLessonScrollPosition(lessonId: String, scrollOffset: Int) {
        val clamped = scrollOffset.coerceAtLeast(0)
        if (_uiState.value.lessonScrollPositions[lessonId] == clamped) return
        _uiState.update { state ->
            state.copy(
                lessonScrollPositions = state.lessonScrollPositions + (lessonId to clamped),
            )
        }
        persistSnapshot()
    }

    fun setLessonExplainRule(lessonId: String, ruleId: String?) {
        if (_uiState.value.activeExplainRuleByLesson[lessonId] == ruleId) return
        _uiState.update { state ->
            val updated = if (ruleId == null) {
                state.activeExplainRuleByLesson - lessonId
            } else {
                state.activeExplainRuleByLesson + (lessonId to ruleId)
            }
            state.copy(activeExplainRuleByLesson = updated)
        }
        persistSnapshot()
    }

    fun setAiTutorStatus(status: AiTutorStatus) {
        _uiState.update { it.copy(aiTutorStatus = status) }
    }

    fun passLessonMockTest(lessonId: String, scorePercent: Int = 85) {
        submitLessonTestPass(lessonId = lessonId, scorePercent = scorePercent)
    }

    fun submitLessonTestPass(lessonId: String, scorePercent: Int) {
        checkDailyRollover()
        val clampedScore = scorePercent.coerceIn(0, 100)
        if (clampedScore < 80) return

        var updatedSrItemToPersist: com.example.model.SpacedRepetitionItem? = null

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
                                passScorePercent = clampedScore,
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

            // Sync matching Spaced-Repetition item so Revision's Concept Mastery Radar updates immediately
            val updatedSrItems = state.spacedRepetitionItems.map { item ->
                if (item.lessonCode == targetLesson.code) {
                    val boostedRetention = maxOf(item.retentionPercent, clampedScore).coerceIn(80, 99)
                    val refreshed = item.copy(
                        isDueNow = false,
                        countdownLabel = "Mastered · Next in 3d",
                        retentionPercent = boostedRetention,
                        intervalTag = "Test $clampedScore%",
                        reviewCount = item.reviewCount + 1,
                    )
                    updatedSrItemToPersist = refreshed
                    refreshed
                } else {
                    item
                }
            }

            val feedback = when {
                wasAlreadyCompleted -> "${targetLesson.code} test passed again ($clampedScore%)!"
                unlockedAssignmentTitle != null ->
                    "Passed ${targetLesson.code} ($clampedScore%)! Chapter Assignment trophy unlocked."
                nextLessonTitle != null ->
                    "Passed ${targetLesson.code} ($clampedScore%)! Unlocked $nextLessonTitle."
                else -> "Passed ${targetLesson.code} ($clampedScore%)!"
            }

            state.copy(
                chapters = updatedChapters,
                spacedRepetitionItems = updatedSrItems,
                quota = updatedQuota,
                newlyCompletedLessonInSession = if (!wasAlreadyCompleted) lessonId else state.newlyCompletedLessonInSession,
                snackbarMessage = feedback,
            )
        }
        persistSnapshot()
        updatedSrItemToPersist?.let { item ->
            vmScope.launch {
                persistence.upsertSpacedRepetitionItem(item)
            }
        }
    }

    fun saveLessonHighlight(
        lessonId: String,
        blockId: String,
        snippet: String,
        category: HighlightCategory,
        learnerNote: String = "",
    ) {
        val state = _uiState.value
        val lesson = state.findLesson(lessonId) ?: return
        val cleanSnippet = snippet.trim()
        if (cleanSnippet.isEmpty()) return

        val now = System.currentTimeMillis()
        val noteId = "hl_${lesson.id}_$blockId"
        val cleanNote = learnerNote.trim().ifBlank {
            when (category) {
                HighlightCategory.KEY_IDEA -> "Key concept highlighted while studying Lesson ${lesson.code}."
                HighlightCategory.CODE_RULE -> "Syntax rule saved from Lesson ${lesson.code} (${lesson.title})."
                HighlightCategory.GOTCHA -> "Watch-out point flagged in Lesson ${lesson.code} — review or ask AI."
            }
        }

        val note = HighlightNote(
            id = noteId,
            lessonCode = lesson.code,
            lessonTitle = lesson.title,
            category = category,
            highlightedSnippet = cleanSnippet,
            learnerNote = cleanNote,
            source = NoteSource.LESSON_HIGHLIGHT,
            savedAtLabel = "Today · ${formatClockTime(now)}",
            createdAtMillis = now,
        )

        val categoryLabel = when (category) {
            HighlightCategory.KEY_IDEA -> "Key Idea"
            HighlightCategory.CODE_RULE -> "Code Rule"
            HighlightCategory.GOTCHA -> "Gotcha / Ask AI"
        }

        _uiState.update { current ->
            current.copy(
                highlights = listOf(note) + current.highlights.filterNot { it.id == noteId },
                snackbarMessage = "Highlighted as $categoryLabel · Synced to Revision (L${lesson.code}).",
            )
        }

        vmScope.launch {
            persistence.saveRevisionNote(note)
        }
    }

    fun askAiAboutLessonHighlight(
        lessonId: String,
        blockId: String,
        snippet: String,
        learnerNote: String = "",
    ) {
        val lesson = _uiState.value.findLesson(lessonId) ?: return
        saveLessonHighlight(
            lessonId = lessonId,
            blockId = blockId,
            snippet = snippet,
            category = HighlightCategory.GOTCHA,
            learnerNote = learnerNote,
        )

        val trimmedNote = learnerNote.trim()
        val prompt = buildString {
            append("Lesson ${lesson.code} (${lesson.title}) Gotcha: \"${snippet.trim()}\"")
            if (trimmedNote.isNotEmpty()) {
                append(" — Note: $trimmedNote")
            }
            append(" Can you explain this like I'm 10 with a quick example?")
        }

        _uiState.update { current ->
            val matchingThread = current.chatThreads.firstOrNull { it.lessonCode == lesson.code }
                ?: current.activeThread
            current.copy(
                activeDetail = null,
                focusedChapterId = null,
                isChapterAiSheetOpen = false,
                selectedTab = KovaTab.AI,
                activeThreadId = matchingThread?.id ?: current.activeThreadId,
                chatComposerText = prompt,
                returnToLessonIdAfterAi = lessonId,
                returnToChapterIdAfterAi = current.focusedChapterId ?: lesson.chapterId,
                snackbarMessage = "Saved Gotcha to Revision & loaded into AI Tutor draft.",
            )
        }
        persistSnapshot()
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
        persistSnapshot()
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
    // AI Workspace (Room-persisted Chat History + Immediate Sent Bubbles)
    // -------------------------------------------------------------------------

    fun selectChatThread(threadId: String) {
        _uiState.update { it.copy(activeThreadId = threadId) }
        persistSnapshot()
    }

    fun createNewLessonThread() {
        val state = _uiState.value
        val activeLesson = state.activeLesson
        val lessonCode = activeLesson?.code ?: "1.1"
        val lessonTitle = activeLesson?.title ?: "Variables & Boxes"
        val now = System.currentTimeMillis()
        val timeLabel = formatClockTime(now)
        val threadId = "thread_$now"

        val newThread = ChatThread(
            id = threadId,
            title = "Lesson $lessonCode · $lessonTitle",
            lessonCode = lessonCode,
            lessonTitle = lessonTitle,
            isPinned = false,
            updatedAtLabel = timeLabel,
            updatedAtMillis = now,
            previewSnippet = "Scoped to Lesson $lessonCode. Ask anything or tap a quick prompt.",
        )

        val welcomeMessage = ChatMessage(
            id = "msg_${now}_welcome",
            threadId = threadId,
            isFromUser = false,
            content = "Scoped Tutor ready for Lesson $lessonCode ($lessonTitle). " +
                "Ask a question or pick a quick prompt chip below—your messages save locally to this device.",
            savedNoteId = null,
            timestampLabel = timeLabel,
            createdAtMillis = now,
        )

        recordPendingMessages(threadId, listOf(welcomeMessage))
        _uiState.update { current ->
            current.copy(
                chatThreads = listOf(newThread) + current.chatThreads.filterNot { it.id == threadId },
                activeThreadId = threadId,
                activeThreadMessages = listOf(welcomeMessage),
            )
        }
        persistSnapshot()
        vmScope.launch {
            persistence.appendMessages(newThread, listOf(welcomeMessage))
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
        synchronized(pendingLock) {
            pendingMessagesByThread.remove(threadId)
        }
        vmScope.launch {
            persistence.deleteThread(threadId)
        }
        _uiState.update { state ->
            val remaining = state.chatThreads.filterNot { it.id == threadId }
            val nextId = if (state.activeThreadId == threadId) {
                remaining.firstOrNull()?.id
            } else {
                state.activeThreadId
            }
            state.copy(
                chatThreads = remaining,
                activeThreadId = nextId,
                activeThreadMessages = if (state.activeThreadId == threadId) emptyList() else state.activeThreadMessages,
                snackbarMessage = "Chat thread deleted.",
            )
        }
        persistSnapshot()
    }

    fun updateChatComposerText(text: String) {
        _uiState.update { it.copy(chatComposerText = text) }
        persistSnapshot()
    }

    /**
     * Fills the editable composer box so the learner can refine before sending,
     * and persists the draft immediately across restarts.
     */
    fun insertQuickPrompt(promptTemplate: String) {
        val activeCode = _uiState.value.activeLesson?.code ?: "1.1"
        val filled = "$promptTemplate (Lesson $activeCode)"
        _uiState.update { it.copy(chatComposerText = filled) }
        persistSnapshot()
    }

    /**
     * Immediately appends the user's message bubble + a subtle Phase 3 local status note
     * to the active thread's StateFlow (single source of truth), records them in the pending
     * optimistic buffer so stale Room snapshots never overwrite them, clears the composer,
     * and persists both atomically in a single Room @Transaction + disk snapshot.
     */
    fun sendChatMessage() {
        val state = _uiState.value
        val prompt = state.chatComposerText.trim()
        if (prompt.isEmpty()) return

        val lastTs = state.activeThreadMessages.maxOfOrNull { it.createdAtMillis } ?: 0L
        val now = maxOf(System.currentTimeMillis(), lastTs + 2L)
        val timeLabel = formatClockTime(now)
        val activeLesson = state.activeLesson
        val currentThread = state.activeThread ?: ChatThread(
            id = "thread_$now",
            title = prompt.take(34),
            lessonCode = activeLesson?.code ?: "1.1",
            lessonTitle = activeLesson?.title ?: "Variables & Boxes",
            isPinned = false,
            updatedAtLabel = timeLabel,
            updatedAtMillis = now,
            previewSnippet = prompt,
        )

        val userMsg = ChatMessage(
            id = "msg_${now}_u",
            threadId = currentThread.id,
            isFromUser = true,
            content = prompt,
            savedNoteId = null,
            timestampLabel = timeLabel,
            createdAtMillis = now,
        )

        val phase3StatusMsg = ChatMessage(
            id = "msg_${now + 1}_status",
            threadId = currentThread.id,
            isFromUser = false,
            content = "${ChatMessage.PHASE3_LOCAL_NOTE_PREFIX}" +
                "Live Gemini replies activate in Phase 3 — your message is saved locally",
            savedNoteId = null,
            timestampLabel = timeLabel,
            createdAtMillis = now + 1L,
        )

        val updatedThread = currentThread.copy(
            updatedAtLabel = timeLabel,
            updatedAtMillis = now + 1L,
            previewSnippet = prompt.take(90),
        )

        val newBatch = listOf(userMsg, phase3StatusMsg)
        recordPendingMessages(updatedThread.id, newBatch)

        val wasOffline = state.aiTutorStatus == AiTutorStatus.OFFLINE
        _uiState.update { current ->
            val updatedThreads = (listOf(updatedThread) + current.chatThreads.filterNot { it.id == updatedThread.id })
                .sortedWith(compareByDescending<ChatThread> { it.isPinned }.thenByDescending { it.updatedAtMillis })
            val baseMessages = if (current.activeThreadId == updatedThread.id) {
                current.activeThreadMessages
            } else {
                emptyList()
            }
            current.copy(
                chatThreads = updatedThreads,
                activeThreadId = updatedThread.id,
                activeThreadMessages = baseMessages + newBatch,
                chatComposerText = "",
                aiTutorStatus = if (wasOffline) AiTutorStatus.OFFLINE else AiTutorStatus.THINKING,
                quota = current.quota.copy(
                    activeAiPoolRemaining = (current.quota.activeAiPoolRemaining - 1).coerceAtLeast(0),
                ),
            )
        }

        persistSnapshot()

        vmScope.launch {
            persistence.appendMessages(updatedThread, newBatch)
        }

        if (!wasOffline) {
            aiStatusJob?.cancel()
            aiStatusJob = vmScope.launch(timerDispatcher) {
                delay(420L)
                _uiState.update { current ->
                    if (current.aiTutorStatus == AiTutorStatus.THINKING) {
                        current.copy(aiTutorStatus = AiTutorStatus.READY)
                    } else {
                        current
                    }
                }
            }
        }
    }

    fun saveAiMessageToNotes(messageId: String, category: HighlightCategory) {
        val state = _uiState.value
        val message = state.activeThreadMessages.firstOrNull { it.id == messageId } ?: return
        val thread = state.activeThread
        val now = System.currentTimeMillis()
        val noteId = "note_ai_$now"

        val lines = message.displayContent.lines().filter { it.isNotBlank() }
        val snippet = lines.firstOrNull()?.take(120) ?: message.displayContent.take(120)
        val details = if (lines.size > 1) {
            lines.drop(1).joinToString("\n")
        } else {
            message.displayContent
        }

        val note = HighlightNote(
            id = noteId,
            lessonCode = thread?.lessonCode ?: state.activeLesson?.code ?: "1.1",
            lessonTitle = thread?.lessonTitle ?: state.activeLesson?.title ?: "Variables & Boxes",
            category = category,
            highlightedSnippet = snippet,
            learnerNote = details,
            source = NoteSource.AI_TUTOR,
            savedAtLabel = "${formatClockTime(now)} · AI Tutor",
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
    // Revision Study Binder, Mastery Radar & 60s 3D Flashcards
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
                isFlashcardFocusStageOpen = true,
            )
        }
        restartFlashcardSprint(autoStart = true)
    }

    fun openFlashcardFocusStage() {
        _uiState.update {
            it.copy(
                selectedTab = KovaTab.REVISION,
                selectedBinderTab = RevisionBinderTab.FLASHCARDS,
                isFlashcardFocusStageOpen = true,
            )
        }
        startOrResumeFlashcardSprint()
    }

    fun closeFlashcardFocusStage() {
        if (_uiState.value.flashcardSprint.isRunning) {
            pauseFlashcardSprint()
        }
        _uiState.update { it.copy(isFlashcardFocusStageOpen = false) }
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
        persistSnapshot()
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
                    persistSnapshot()
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
        persistSnapshot()

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
        persistSnapshot()
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        _uiState.update { it.copy(dynamicColorEnabled = enabled) }
        persistSnapshot()
    }

    fun setExpressiveMotionEnabled(enabled: Boolean) {
        _uiState.update { it.copy(expressiveMotionEnabled = enabled) }
        persistSnapshot()
    }

    fun setSoundAndHapticsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(soundAndHapticsEnabled = enabled) }
        persistSnapshot()
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
        persistSnapshot()
    }

    /**
     * Resets lessons, quota, chats, notes, and flashcard progress while preserving
     * Theme Mode, Dynamic Color, Motion, and Sound & Haptics preferences.
     */
    fun resetDemoProgress() {
        sprintTimerJob?.cancel()
        sprintTimerJob = null
        synchronized(pendingLock) {
            pendingMessagesByThread.clear()
        }
        vmScope.launch {
            persistence.resetAll(repository)
        }
        val todayIso = dateProvider()
        val seedThreads = repository.getInitialChatThreads()
        _uiState.update { current ->
            KovaUiState(
                selectedTab = KovaTab.LEARN,
                chapters = repository.getInitialChapters(),
                expandedChapterIds = setOf("ch_1"),
                highlights = repository.getInitialHighlights(),
                spacedRepetitionItems = repository.getInitialSpacedRepetitionItems(),
                chatThreads = seedThreads,
                activeThreadId = seedThreads.firstOrNull()?.id,
                activeThreadMessages = repository.getInitialChatMessages()
                    .filter { it.threadId == seedThreads.firstOrNull()?.id },
                chatComposerText = "",
                quota = repository.getInitialQuotaState().copy(lastQuotaDateIso = todayIso),
                themeMode = current.themeMode,
                dynamicColorEnabled = current.dynamicColorEnabled,
                expressiveMotionEnabled = current.expressiveMotionEnabled,
                soundAndHapticsEnabled = current.soundAndHapticsEnabled,
                lessonSplitRatio = current.lessonSplitRatio,
                devUnlockAllOverride = false,
                isFloatingPreview = current.isFloatingPreview,
                isSettingsOpen = false,
                snackbarMessage = "Study progress, chats & notes reset. Theme and sound kept.",
            )
        }
        persistSnapshot()
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

    private fun formatClockTime(millis: Long): String =
        SimpleDateFormat("HH:mm", Locale.US).format(Date(millis))
}

class KovaViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val appContext = context.applicationContext
        val db = KovaDatabase.getInstance(appContext)
        val repository = MockLearningRepository()
        val persistence = RoomStudyPersistenceRepository(
            chatDao = db.chatDao(),
            revisionDao = db.revisionDao(),
        )
        val appStateStore = FileKovaAppStateStore(
            File(appContext.filesDir, FileKovaAppStateStore.DEFAULT_FILE_NAME),
        )
        return KovaViewModel(
            repository = repository,
            persistence = persistence,
            appStateStore = appStateStore,
            tutorEngine = LocalLessonTutorEngine(),
            dateProvider = ::currentLocalDateIso,
            workDispatcher = Dispatchers.IO,
            timerDispatcher = Dispatchers.Default,
        ) as T
    }
}
