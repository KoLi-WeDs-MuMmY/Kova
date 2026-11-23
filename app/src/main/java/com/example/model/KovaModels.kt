package com.example.model

import androidx.compose.runtime.Immutable
import com.example.ui.theme.ThemeMode

enum class KovaTab {
    LEARN,
    LAB,
    AI,
    REVISION,
}

enum class AiTutorStatus(val label: String) {
    READY("Ready"),
    THINKING("Thinking"),
    OFFLINE("Offline"),
}

enum class LessonStatus {
    COMPLETED,
    UNLOCKED,
    LOCKED,
}

enum class HighlightCategory {
    KEY_IDEA,
    CODE_RULE,
    GOTCHA,
}

enum class NoteSource {
    LESSON_HIGHLIGHT,
    AI_TUTOR,
}

enum class RevisionBinderTab {
    HIGHLIGHTS,
    FLASHCARDS,
    SR_QUEUE,
}

enum class LessonSplitRatio(
    val lessonWeight: Float,
    val ideWeight: Float,
    val badgeLabel: String,
) {
    RATIO_75_25(0.75f, 0.25f, "75:25"),
    RATIO_50_50(0.50f, 0.50f, "50:50"),
    RATIO_25_75(0.25f, 0.75f, "25:75");

    fun next(): LessonSplitRatio = when (this) {
        RATIO_75_25 -> RATIO_50_50
        RATIO_50_50 -> RATIO_25_75
        RATIO_25_75 -> RATIO_75_25
    }
}

@Immutable
data class LessonReturnEvent(
    val lessonId: String,
    val wasNewlyCompleted: Boolean,
    val nonce: Long,
)

@Immutable
data class WordBreakdown(
    val id: String,
    val term: String,
    val literalMeaning: String,
    val tenYearOldAnalogy: String,
    val highlightSnippet: String,
)

@Immutable
data class SymbolBreakdown(
    val symbol: String,
    val plainWords: String,
)

@Immutable
data class ExplainDeepDive(
    val headline: String,
    val realLifeOpening: String,
    val kotlinConnection: String,
    val syntaxSnippet: String,
    val symbolBreakdowns: List<SymbolBreakdown>,
    val takeaway: String,
)

@Immutable
data class PreHighlightedTakeaway(
    val id: String,
    val category: HighlightCategory,
    val headline: String,
    val body: String,
)

@Immutable
data class ConceptRule(
    val id: String,
    val title: String,
    val explanation: String,
    val codeSnippet: String,
    val whyItMatters: String,
    val explainDeepDive: ExplainDeepDive,
)

@Immutable
data class PracticeSolutionNote(
    val lineNumber: Int,
    val note: String,
)

@Immutable
data class PracticeTaskSpec(
    val title: String,
    val prompt: String,
    val desiredOutput: String,
    val solutionCode: String,
    val lineNotes: List<PracticeSolutionNote>,
    val starterCode: String = "",
    val expectedKeywords: List<String> = emptyList(),
    val editorHint: String = "",
)

enum class CodeLineRole(val badgeLabel: String) {
    DECLARATION("DECL"),
    EXECUTION("EXEC"),
    DECISION("BRANCH"),
    OUTPUT("OUTPUT"),
}

@Immutable
data class DebuggerCodeLine(
    val lineNumber: Int,
    val code: String,
    val role: CodeLineRole,
)

@Immutable
data class VariableWatchEntry(
    val name: String,
    val typeName: String,
    val value: String,
    val previousValue: String? = null,
    val isChanged: Boolean = false,
)

@Immutable
data class DebuggerStep(
    val stepIndex: Int,
    val activeLineNumber: Int,
    val headline: String,
    val explanation: String,
    val variables: List<VariableWatchEntry>,
    val consoleOutput: List<String>,
)

@Immutable
data class DebuggerWalkthrough(
    val title: String,
    val subtitle: String,
    val lines: List<DebuggerCodeLine>,
    val steps: List<DebuggerStep>,
)

enum class LessonQuestionKind(val label: String) {
    PREDICT_OUTPUT("Predict the Output"),
    SPOT_BUG("Spot the Bug"),
    FILL_KEYWORD("Fill the Keyword"),
}

@Immutable
data class LessonTestQuestion(
    val id: String,
    val kind: LessonQuestionKind,
    val prompt: String,
    val codeSnippet: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    val plainEnglishExplanation: String,
)

@Immutable
data class LessonInteractiveContent(
    val wordBreakdowns: List<WordBreakdown>,
    val preHighlightedTakeaways: List<PreHighlightedTakeaway> = emptyList(),
    val conceptRules: List<ConceptRule>,
    val practiceTask: PracticeTaskSpec,
    val debugger: DebuggerWalkthrough,
    val testQuestions: List<LessonTestQuestion>,
)

@Immutable
data class Lesson(
    val id: String,
    val chapterId: String,
    val code: String,
    val title: String,
    val subtitle: String,
    val analogyTeaser: String,
    val codePreview: String,
    val estimatedMinutes: Int,
    val status: LessonStatus,
    val passScorePercent: Int? = null,
    val interactiveContent: LessonInteractiveContent? = null,
)

@Immutable
data class ChapterAssignment(
    val id: String,
    val chapterId: String,
    val chapterLabel: String,
    val title: String,
    val subtitle: String,
    val deliverables: List<String>,
    val estimatedMinutes: Int,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
)

@Immutable
data class Chapter(
    val id: String,
    val number: Int,
    val title: String,
    val tagline: String,
    val lessons: List<Lesson>,
    val assignment: ChapterAssignment,
    val revisionBulletPoints: List<String>,
    val isUnlocked: Boolean,
    val lockReason: String? = null,
) {
    val completedLessonCount: Int
        get() = lessons.count { it.status == LessonStatus.COMPLETED }

    val progressFraction: Float
        get() = if (lessons.isEmpty()) 0f else completedLessonCount.toFloat() / lessons.size.toFloat()
}

@Immutable
data class HighlightNote(
    val id: String,
    val lessonCode: String,
    val lessonTitle: String,
    val category: HighlightCategory,
    val highlightedSnippet: String,
    val learnerNote: String,
    val source: NoteSource = NoteSource.LESSON_HIGHLIGHT,
    val savedAtLabel: String,
    val createdAtMillis: Long = 0L,
)

@Immutable
data class SpacedRepetitionItem(
    val id: String,
    val lessonCode: String,
    val title: String,
    val frontPrompt: String,
    val backAnswer: String,
    val codeExample: String,
    val isDueNow: Boolean,
    val countdownLabel: String,
    val retentionPercent: Int,
    val intervalTag: String,
    val reviewCount: Int = 0,
)

@Immutable
data class ChatThread(
    val id: String,
    val title: String,
    val lessonCode: String,
    val lessonTitle: String,
    val isPinned: Boolean,
    val updatedAtLabel: String,
    val updatedAtMillis: Long,
    val previewSnippet: String,
)

@Immutable
data class ChatMessage(
    val id: String,
    val threadId: String,
    val isFromUser: Boolean,
    val content: String,
    val savedNoteId: String? = null,
    val timestampLabel: String,
    val createdAtMillis: Long,
) {
    val isPhase3StatusNote: Boolean
        get() = !isFromUser && content.startsWith(PHASE3_LOCAL_NOTE_PREFIX)

    val displayContent: String
        get() = if (isPhase3StatusNote) {
            content.removePrefix(PHASE3_LOCAL_NOTE_PREFIX).trim()
        } else {
            content
        }

    companion object {
        const val PHASE3_LOCAL_NOTE_PREFIX = "[PHASE3_LOCAL_NOTE] "
    }
}

@Immutable
data class QuotaState(
    val dailyLessonsRemaining: Int,
    val dailyLessonsMax: Int = 3,
    val morningDigestStatus: String = "Ready",
    val activeAiPoolRemaining: Int = 12,
    val activeAiPoolMax: Int = 14,
    val spacedRepetitionReserve: Int = 4,
    val lastQuotaDateIso: String = "",
)

@Immutable
data class MasteryConceptMetric(
    val label: String,
    val lessonCode: String,
    val scorePercent: Int,
)

@Immutable
data class MasteryRadarSummary(
    val strongest: List<MasteryConceptMetric>,
    val weakSpots: List<MasteryConceptMetric>,
    val focusLessonId: String?,
    val focusTitle: String,
    val focusReason: String,
    val preferFlashcardFocus: Boolean,
)

@Immutable
data class FlashcardSprintState(
    val secondsRemaining: Int = 60,
    val totalSeconds: Int = 60,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val gotItCount: Int = 0,
    val reviewAgainCount: Int = 0,
    val isSummaryVisible: Boolean = false,
)

@Immutable
data class LockedShakeEvent(
    val nodeId: String,
    val nonce: Long,
)

sealed interface DetailDestination {
    val sharedBoundsKey: String

    @Immutable
    data class LessonDetail(
        val lessonId: String,
    ) : DetailDestination {
        override val sharedBoundsKey: String = "lesson_card_$lessonId"
    }

    @Immutable
    data class AssignmentDetail(
        val assignmentId: String,
        val originPrefix: String,
    ) : DetailDestination {
        override val sharedBoundsKey: String = "${originPrefix}_assignment_$assignmentId"
    }

    @Immutable
    data class NoteDetail(
        val noteId: String,
    ) : DetailDestination {
        override val sharedBoundsKey: String = "revision_note_$noteId"
    }

    @Immutable
    data class RevisionSheetDetail(
        val chapterId: String,
    ) : DetailDestination {
        override val sharedBoundsKey: String = "revision_sheet_$chapterId"
    }
}

@Immutable
data class KovaUiState(
    val selectedTab: KovaTab = KovaTab.LEARN,
    val chapters: List<Chapter> = emptyList(),
    val expandedChapterIds: Set<String> = setOf("ch_1"),
    val highlights: List<HighlightNote> = emptyList(),
    val spacedRepetitionItems: List<SpacedRepetitionItem> = emptyList(),
    val chatThreads: List<ChatThread> = emptyList(),
    val activeThreadId: String? = null,
    val activeThreadMessages: List<ChatMessage> = emptyList(),
    val chatSearchQuery: String = "",
    val chatComposerText: String = "",
    val selectedBinderTab: RevisionBinderTab = RevisionBinderTab.HIGHLIGHTS,
    val revisionSearchQuery: String = "",
    val selectedHighlightFilter: HighlightCategory? = null,
    val flashcardSprint: FlashcardSprintState = FlashcardSprintState(),
    val quota: QuotaState = QuotaState(dailyLessonsRemaining = 3),
    val activeDetail: DetailDestination? = null,
    val focusedChapterId: String? = null,
    val isChapterAiSheetOpen: Boolean = false,
    val isFlashcardFocusStageOpen: Boolean = false,
    val isAiHistoryVisible: Boolean? = null,
    val lessonSplitRatio: LessonSplitRatio = LessonSplitRatio.RATIO_75_25,
    val isIdePaneOpen: Boolean = false,
    val isDebuggerStageOpen: Boolean = false,
    val aiTutorStatus: AiTutorStatus = AiTutorStatus.READY,
    val scopedAiThreadIds: Set<String> = emptySet(),
    val returnToLessonIdAfterAi: String? = null,
    val returnToChapterIdAfterAi: String? = null,
    val lastActiveLessonId: String? = null,
    val unlockedDebuggerLessonIds: Set<String> = emptySet(),
    val lessonScrollPositions: Map<String, Int> = emptyMap(),
    val activeExplainRuleByLesson: Map<String, String> = emptyMap(),
    val newlyCompletedLessonInSession: String? = null,
    val lessonReturnEvent: LessonReturnEvent? = null,
    val themeMode: ThemeMode = ThemeMode.WARM_DAYLIGHT,
    val dynamicColorEnabled: Boolean = false,
    val expressiveMotionEnabled: Boolean = true,
    val soundAndHapticsEnabled: Boolean = true,
    val isFloatingPreview: Boolean = false,
    val devUnlockAllOverride: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val lockedShakeEvent: LockedShakeEvent? = null,
    val snackbarMessage: String? = null,
) {
    val isLessonFocusMode: Boolean
        get() = activeDetail is DetailDestination.LessonDetail

    val isActiveAiThreadLessonScoped: Boolean
        get() = returnToLessonIdAfterAi != null ||
            (activeThreadId != null && activeThreadId in scopedAiThreadIds)

    val isDebuggerUnlocked: Boolean
        get() {
            val detail = activeDetail as? DetailDestination.LessonDetail ?: return false
            return detail.lessonId in unlockedDebuggerLessonIds
        }

    fun isDebuggerUnlockedFor(lessonId: String): Boolean = lessonId in unlockedDebuggerLessonIds

    fun scrollPositionFor(lessonId: String): Int = lessonScrollPositions[lessonId] ?: 0

    fun activeExplainRuleFor(lessonId: String): String? = activeExplainRuleByLesson[lessonId]

    val isChapterFocusMode: Boolean
        get() = selectedTab == KovaTab.LEARN && focusedChapterId != null

    val isGlobalChromeHidden: Boolean
        get() = isLessonFocusMode || isChapterFocusMode || isFlashcardFocusStageOpen

    val focusedChapter: Chapter?
        get() = focusedChapterId?.let { id -> chapters.firstOrNull { it.id == id } }

    val activeLesson: Lesson?
        get() {
            val allLessons = chapters.flatMap { it.lessons }
            return allLessons.firstOrNull { it.status == LessonStatus.UNLOCKED }
                ?: allLessons.lastOrNull { it.status == LessonStatus.COMPLETED }
                ?: allLessons.firstOrNull()
        }

    val activeThread: ChatThread?
        get() = chatThreads.firstOrNull { it.id == activeThreadId } ?: chatThreads.firstOrNull()

    val filteredChatThreads: List<ChatThread>
        get() {
            val q = chatSearchQuery.trim().lowercase()
            val list = if (q.isEmpty()) {
                chatThreads
            } else {
                chatThreads.filter {
                    it.title.lowercase().contains(q) ||
                        it.lessonCode.lowercase().contains(q) ||
                        it.lessonTitle.lowercase().contains(q) ||
                        it.previewSnippet.lowercase().contains(q)
                }
            }
            return list.sortedWith(
                compareByDescending<ChatThread> { it.isPinned }
                    .thenByDescending { it.updatedAtMillis },
            )
        }

    val filteredHighlights: List<HighlightNote>
        get() {
            val q = revisionSearchQuery.trim().lowercase()
            return highlights.filter { note ->
                val matchesCategory = selectedHighlightFilter == null || note.category == selectedHighlightFilter
                val matchesQuery = q.isEmpty() ||
                    note.highlightedSnippet.lowercase().contains(q) ||
                    note.learnerNote.lowercase().contains(q) ||
                    note.lessonCode.lowercase().contains(q) ||
                    note.lessonTitle.lowercase().contains(q)
                matchesCategory && matchesQuery
            }
        }

    val dueNowSrItems: List<SpacedRepetitionItem>
        get() = spacedRepetitionItems.filter { it.isDueNow }

    val upcomingSrItems: List<SpacedRepetitionItem>
        get() = spacedRepetitionItems.filterNot { it.isDueNow }

    val flashcardDeck: List<SpacedRepetitionItem>
        get() = spacedRepetitionItems.sortedWith(
            compareByDescending<SpacedRepetitionItem> { it.isDueNow }
                .thenBy { it.retentionPercent },
        )

    val masteryRadar: MasteryRadarSummary
        get() {
            val srSortedDesc = spacedRepetitionItems.sortedByDescending { it.retentionPercent }
            val strongestFromSr = srSortedDesc.take(2).map {
                MasteryConceptMetric(
                    label = it.title,
                    lessonCode = it.lessonCode,
                    scorePercent = it.retentionPercent,
                )
            }
            val strongestFromCompleted = chapters.flatMap { it.lessons }
                .filter { it.status == LessonStatus.COMPLETED }
                .map {
                    MasteryConceptMetric(
                        label = it.title,
                        lessonCode = it.code,
                        scorePercent = it.passScorePercent ?: 85,
                    )
                }

            val strongest = (strongestFromCompleted + strongestFromSr)
                .distinctBy { it.label }
                .sortedByDescending { it.scorePercent }
                .take(2)

            val weakFromSr = spacedRepetitionItems
                .sortedBy { it.retentionPercent }
                .take(2)
                .map {
                    MasteryConceptMetric(
                        label = it.title,
                        lessonCode = it.lessonCode,
                        scorePercent = it.retentionPercent,
                    )
                }

            val gotchaCount = highlights.count { it.category == HighlightCategory.GOTCHA }
            val weakestCard = spacedRepetitionItems.minByOrNull { it.retentionPercent }
            val nextLesson = activeLesson
            val preferFlashcards = dueNowSrItems.isNotEmpty() || gotchaCount >= 2

            return MasteryRadarSummary(
                strongest = strongest,
                weakSpots = weakFromSr,
                focusLessonId = nextLesson?.id,
                focusTitle = if (preferFlashcards && weakestCard != null) {
                    "60s Sprint: ${weakestCard.title}"
                } else if (nextLesson != null) {
                    "Lesson ${nextLesson.code}: ${nextLesson.title}"
                } else {
                    "60s Flashcard Recall"
                },
                focusReason = if (preferFlashcards) {
                    "${dueNowSrItems.size} due card(s) & $gotchaCount saved Gotcha note(s)"
                } else {
                    "Continue active quest path to raise Chapter 1 mastery"
                },
                preferFlashcardFocus = preferFlashcards,
            )
        }

    fun findLesson(lessonId: String): Lesson? =
        chapters.asSequence().flatMap { it.lessons.asSequence() }.firstOrNull { it.id == lessonId }

    fun findAssignment(assignmentId: String): ChapterAssignment? =
        chapters.map { it.assignment }.firstOrNull { it.id == assignmentId }

    fun findChapter(chapterId: String): Chapter? =
        chapters.firstOrNull { it.id == chapterId }

    fun findNote(noteId: String): HighlightNote? =
        highlights.firstOrNull { it.id == noteId }
}
