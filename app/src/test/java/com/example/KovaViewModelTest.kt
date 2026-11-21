package com.example

import com.example.model.HighlightCategory
import com.example.model.KovaTab
import com.example.model.LessonSplitRatio
import com.example.model.LessonStatus
import com.example.model.NoteSource
import com.example.model.RevisionBinderTab
import com.example.ui.theme.ThemeMode
import com.example.viewmodel.KovaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KovaViewModelTest {

    @Test
    fun defaultsToWarmDaylightWithChapter1ExpandedOnly() {
        val vm = KovaViewModel()
        val state = vm.uiState.value

        assertEquals(ThemeMode.WARM_DAYLIGHT, state.themeMode)
        assertTrue("ch_1" in state.expandedChapterIds)
        assertFalse("ch_2" in state.expandedChapterIds)
        assertTrue(state.chatThreads.size >= 2)
    }

    @Test
    fun togglingChapterDropDownUpdatesExpandedSet() {
        val vm = KovaViewModel()

        vm.toggleChapterExpanded("ch_2")
        assertTrue("ch_2" in vm.uiState.value.expandedChapterIds)

        vm.toggleChapterExpanded("ch_1")
        assertFalse("ch_1" in vm.uiState.value.expandedChapterIds)
    }

    @Test
    fun passingLessonsUnlocksNextLessonAndDecrementsDailyQuota() {
        val vm = KovaViewModel()
        assertEquals(3, vm.uiState.value.quota.dailyLessonsRemaining)
        assertEquals(LessonStatus.UNLOCKED, vm.uiState.value.findLesson("l_1_1")?.status)
        assertEquals(LessonStatus.LOCKED, vm.uiState.value.findLesson("l_1_2")?.status)

        vm.passLessonMockTest("l_1_1", scorePercent = 85)

        assertEquals(2, vm.uiState.value.quota.dailyLessonsRemaining)
        assertEquals(LessonStatus.COMPLETED, vm.uiState.value.findLesson("l_1_1")?.status)
        assertEquals(LessonStatus.UNLOCKED, vm.uiState.value.findLesson("l_1_2")?.status)
    }

    @Test
    fun chapter2RemainsLockedUntilChapter1AssignmentCompleted() {
        val vm = KovaViewModel()
        vm.setDevUnlockAllOverride(false)

        vm.passLessonMockTest("l_1_1")
        vm.passLessonMockTest("l_1_2")
        vm.passLessonMockTest("l_1_3")
        vm.passLessonMockTest("l_1_4")

        val ch1 = vm.uiState.value.findChapter("ch_1")!!
        val ch2Before = vm.uiState.value.findChapter("ch_2")!!
        assertTrue(ch1.assignment.isUnlocked)
        assertFalse(ch1.assignment.isCompleted)
        assertFalse(ch2Before.isUnlocked)

        vm.completeAssignment("asg_ch_1")

        val ch1After = vm.uiState.value.findChapter("ch_1")!!
        val ch2After = vm.uiState.value.findChapter("ch_2")!!
        assertTrue(ch1After.assignment.isCompleted)
        assertTrue(ch2After.isUnlocked)
        assertTrue("ch_2" in vm.uiState.value.expandedChapterIds)
        assertEquals(LessonStatus.UNLOCKED, ch2After.lessons.first().status)
    }

    @Test
    fun devOverrideUnlocksAllAndResetsDailyQuota() {
        val vm = KovaViewModel()
        vm.passLessonMockTest("l_1_1")
        assertEquals(2, vm.uiState.value.quota.dailyLessonsRemaining)

        vm.setDevUnlockAllOverride(true)

        assertEquals(3, vm.uiState.value.quota.dailyLessonsRemaining)
        assertTrue(vm.uiState.value.findChapter("ch_2")!!.isUnlocked)
        assertTrue(vm.uiState.value.findAssignment("asg_ch_1")!!.isUnlocked)
    }

    @Test
    fun themeModeMotionAndHighlightFiltersUpdateState() {
        val vm = KovaViewModel()
        vm.setThemeMode(ThemeMode.WARM_DARK)
        assertEquals(ThemeMode.WARM_DARK, vm.uiState.value.themeMode)

        vm.setThemeMode(ThemeMode.AMOLED_BLACK)
        vm.setDynamicColorEnabled(true)
        vm.setExpressiveMotionEnabled(false)
        vm.selectHighlightFilter(HighlightCategory.CODE_RULE)

        val state = vm.uiState.value
        assertEquals(ThemeMode.AMOLED_BLACK, state.themeMode)
        assertTrue(state.dynamicColorEnabled)
        assertFalse(state.expressiveMotionEnabled)
        assertTrue(state.filteredHighlights.isNotEmpty())
        assertTrue(state.filteredHighlights.all { it.category == HighlightCategory.CODE_RULE })
    }

    @Test
    fun aiQuickPromptFillsComposerAndSendingAppendsScopedReply() {
        val vm = KovaViewModel()
        val initialPool = vm.uiState.value.quota.activeAiPoolRemaining

        vm.insertQuickPrompt("Explain like I'm 10")
        assertTrue(vm.uiState.value.chatComposerText.contains("Explain like I'm 10"))

        val beforeCount = vm.uiState.value.activeThreadMessages.size
        vm.sendChatMessage()

        val state = vm.uiState.value
        assertEquals("", state.chatComposerText)
        assertEquals(initialPool - 1, state.quota.activeAiPoolRemaining)
        assertEquals(beforeCount + 2, state.activeThreadMessages.size)
        assertFalse(state.activeThreadMessages.last().isFromUser)
    }

    @Test
    fun savingAiAnswerAddsTaggedNoteToRevisionBinder() {
        val vm = KovaViewModel()
        val aiMessage = vm.uiState.value.activeThreadMessages.first { !it.isFromUser }
        val beforeNotesCount = vm.uiState.value.highlights.size

        vm.saveAiMessageToNotes(aiMessage.id, HighlightCategory.GOTCHA)

        val state = vm.uiState.value
        assertEquals(beforeNotesCount + 1, state.highlights.size)
        val savedNote = state.highlights.first()
        assertEquals(HighlightCategory.GOTCHA, savedNote.category)
        assertEquals(NoteSource.AI_TUTOR, savedNote.source)
        assertNotNull(state.activeThreadMessages.first { it.id == aiMessage.id }.savedNoteId)
    }

    @Test
    fun flashcardSprintRatesCardsAndUpdatesSpacedRepetitionSchedule() {
        val vm = KovaViewModel()
        val firstCard = vm.uiState.value.flashcardDeck.first()
        val initialRetention = firstCard.retentionPercent

        vm.startQuickReviewFromQueue()
        assertEquals(KovaTab.REVISION, vm.uiState.value.selectedTab)
        assertEquals(RevisionBinderTab.FLASHCARDS, vm.uiState.value.selectedBinderTab)

        vm.flipFlashcard()
        assertTrue(vm.uiState.value.flashcardSprint.isFlipped)

        vm.rateFlashcardGotIt()
        val updatedCard = vm.uiState.value.spacedRepetitionItems.first { it.id == firstCard.id }
        assertFalse(updatedCard.isDueNow)
        assertTrue(updatedCard.retentionPercent > initialRetention)
        assertEquals(1, vm.uiState.value.flashcardSprint.gotItCount)

        vm.togglePauseFlashcardSprint()
    }

    @Test
    fun lessonSplitRatioCyclesSmoothlyThroughAllThreePresets() {
        val vm = KovaViewModel()
        assertEquals(LessonSplitRatio.RATIO_75_25, vm.uiState.value.lessonSplitRatio)

        vm.cycleLessonSplitRatio()
        assertEquals(LessonSplitRatio.RATIO_50_50, vm.uiState.value.lessonSplitRatio)

        vm.cycleLessonSplitRatio()
        assertEquals(LessonSplitRatio.RATIO_25_75, vm.uiState.value.lessonSplitRatio)

        vm.cycleLessonSplitRatio()
        assertEquals(LessonSplitRatio.RATIO_75_25, vm.uiState.value.lessonSplitRatio)
    }

    @Test
    fun soundAndHapticsDefaultsOnAndCanBeToggled() {
        val vm = KovaViewModel()
        assertTrue(vm.uiState.value.soundAndHapticsEnabled)

        vm.setSoundAndHapticsEnabled(false)
        assertFalse(vm.uiState.value.soundAndHapticsEnabled)

        vm.setSoundAndHapticsEnabled(true)
        assertTrue(vm.uiState.value.soundAndHapticsEnabled)
    }

    @Test
    fun lessonFocusModeAndReturnEventTrackNormalAndCelebratoryExits() {
        val vm = KovaViewModel()
        assertFalse(vm.uiState.value.isLessonFocusMode)

        // Open and close without completing -> non-celebratory settle event
        vm.openLesson("l_1_1")
        assertTrue(vm.uiState.value.isLessonFocusMode)

        vm.closeDetail()
        assertFalse(vm.uiState.value.isLessonFocusMode)
        val firstReturn = vm.uiState.value.lessonReturnEvent
        assertNotNull(firstReturn)
        assertEquals("l_1_1", firstReturn!!.lessonId)
        assertFalse(firstReturn.wasNewlyCompleted)

        // Open, pass mock test, then exit -> celebratory bounce event
        vm.openLesson("l_1_1")
        vm.passLessonMockTest("l_1_1", scorePercent = 90)
        vm.closeDetail()

        val secondReturn = vm.uiState.value.lessonReturnEvent
        assertNotNull(secondReturn)
        assertEquals("l_1_1", secondReturn!!.lessonId)
        assertTrue(secondReturn.wasNewlyCompleted)
        assertTrue(secondReturn.nonce > firstReturn.nonce)
    }
}
