package com.example.model

import androidx.compose.runtime.Immutable
import com.example.ui.theme.ThemeMode

enum class KovaTab {
    LEARN,
    LAB,
    AI,
    REVISION,
}

enum class LessonStatus {
    COMPLETED,
    UNLOCKED,
    LOCKED,
}

enum class HighlightCategory {
    KEY_IDEA,
    CODE_RULE,
    ASK_AI,
}

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
    val savedAtLabel: String,
)

@Immutable
data class SpacedRepetitionItem(
    val id: String,
    val lessonCode: String,
    val title: String,
    val countdownLabel: String,
    val retentionPercent: Int,
    val intervalTag: String,
)

@Immutable
data class QuotaState(
    val dailyLessonsRemaining: Int,
    val dailyLessonsMax: Int = 3,
    val morningDigestStatus: String = "Ready",
    val activeAiPoolRemaining: Int = 12,
    val activeAiPoolMax: Int = 14,
    val spacedRepetitionReserve: Int = 4,
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
    val quota: QuotaState = QuotaState(dailyLessonsRemaining = 3),
    val selectedHighlightFilter: HighlightCategory? = null,
    val activeDetail: DetailDestination? = null,
    val themeMode: ThemeMode = ThemeMode.WARM_DAYLIGHT,
    val dynamicColorEnabled: Boolean = false,
    val expressiveMotionEnabled: Boolean = true,
    val isFloatingPreview: Boolean = false,
    val devUnlockAllOverride: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val lockedShakeEvent: LockedShakeEvent? = null,
    val snackbarMessage: String? = null,
) {
    val activeLesson: Lesson?
        get() {
            val allLessons = chapters.flatMap { it.lessons }
            return allLessons.firstOrNull { it.status == LessonStatus.UNLOCKED }
                ?: allLessons.lastOrNull { it.status == LessonStatus.COMPLETED }
                ?: allLessons.firstOrNull()
        }

    val filteredHighlights: List<HighlightNote>
        get() = if (selectedHighlightFilter == null) {
            highlights
        } else {
            highlights.filter { it.category == selectedHighlightFilter }
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
