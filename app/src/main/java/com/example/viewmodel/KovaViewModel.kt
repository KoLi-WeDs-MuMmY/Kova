package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.LearningRepository
import com.example.data.MockLearningRepository
import com.example.model.Chapter
import com.example.model.DetailDestination
import com.example.model.HighlightCategory
import com.example.model.KovaTab
import com.example.model.KovaUiState
import com.example.model.LessonStatus
import com.example.model.LockedShakeEvent
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class KovaViewModel(
    private val repository: LearningRepository = MockLearningRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<KovaUiState> = _uiState.asStateFlow()

    private fun createInitialState(): KovaUiState = KovaUiState(
        selectedTab = KovaTab.LEARN,
        chapters = repository.getInitialChapters(),
        highlights = repository.getInitialHighlights(),
        spacedRepetitionItems = repository.getInitialSpacedRepetitionItems(),
        quota = repository.getInitialQuotaState(),
    )

    fun selectTab(tab: KovaTab) {
        _uiState.update { state ->
            state.copy(selectedTab = tab, activeDetail = null)
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
                    "Passed ${targetLesson.code} ($scorePercent%)! Chapter Assignment unlocked in Learn & Lab."
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
            val message = if (nextChapter != null) {
                "Assignment complete! Chapter ${nextChapter.number}: ${nextChapter.title} is now unlocked."
            } else {
                "Assignment marked complete!"
            }

            state.copy(
                chapters = updatedChapters,
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
        _uiState.update { current ->
            createInitialState().copy(
                themeMode = current.themeMode,
                dynamicColorEnabled = current.dynamicColorEnabled,
                expressiveMotionEnabled = current.expressiveMotionEnabled,
                isFloatingPreview = current.isFloatingPreview,
                isSettingsOpen = false,
                snackbarMessage = "Phase 1 mock progress reset.",
            )
        }
    }

    fun setSettingsOpen(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun selectHighlightFilter(category: HighlightCategory?) {
        _uiState.update { it.copy(selectedHighlightFilter = category) }
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
