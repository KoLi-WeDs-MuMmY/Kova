package com.example

import com.example.data.local.FileKovaAppStateStore
import com.example.model.AiTutorStatus
import com.example.model.DetailDestination
import com.example.model.HighlightCategory
import com.example.model.KovaTab
import com.example.model.LessonSplitRatio
import com.example.model.LessonStatus
import com.example.model.NoteSource
import com.example.model.RevisionBinderTab
import com.example.ui.theme.ThemeMode
import com.example.viewmodel.KovaViewModel
import java.io.File
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
        val userBubble = state.activeThreadMessages[state.activeThreadMessages.lastIndex - 1]
        val statusNote = state.activeThreadMessages.last()
        assertTrue(userBubble.isFromUser)
        assertTrue(userBubble.content.contains("Explain like I'm 10"))
        assertFalse(statusNote.isFromUser)
        assertTrue(statusNote.isPhase3StatusNote)
        assertTrue(statusNote.displayContent.contains("Phase 3"))
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

    @Test
    fun fileStateStorePersistsProgressSettingsDraftAndSprintAcrossRestart() {
        val tempFile = File.createTempFile("kova_state_test_", ".json")
        try {
            val fixedDate = "2026-11-21"
            val store1 = FileKovaAppStateStore(tempFile)
            val vm1 = KovaViewModel(appStateStore = store1, dateProvider = { fixedDate })

            vm1.setThemeMode(ThemeMode.AMOLED_BLACK)
            vm1.setSoundAndHapticsEnabled(false)
            vm1.setExpressiveMotionEnabled(false)
            vm1.cycleLessonSplitRatio() // 50:50
            vm1.passLessonMockTest("l_1_1", scorePercent = 92)
            vm1.updateChatComposerText("Explain var vs val with a quick example")
            vm1.flipFlashcard()
            vm1.rateFlashcardGotIt()
            vm1.flipFlashcard()
            vm1.rateFlashcardReviewAgain()
            vm1.togglePauseFlashcardSprint()

            // Simulate full app kill & cold restart reading from the same file on disk
            val store2 = FileKovaAppStateStore(tempFile)
            val vm2 = KovaViewModel(appStateStore = store2, dateProvider = { fixedDate })
            val restored = vm2.uiState.value

            assertEquals(ThemeMode.AMOLED_BLACK, restored.themeMode)
            assertFalse(restored.soundAndHapticsEnabled)
            assertFalse(restored.expressiveMotionEnabled)
            assertEquals(LessonSplitRatio.RATIO_50_50, restored.lessonSplitRatio)
            assertEquals(LessonStatus.COMPLETED, restored.findLesson("l_1_1")?.status)
            assertEquals(92, restored.findLesson("l_1_1")?.passScorePercent)
            assertEquals(LessonStatus.UNLOCKED, restored.findLesson("l_1_2")?.status)
            assertEquals(2, restored.quota.dailyLessonsRemaining)
            assertEquals("Explain var vs val with a quick example", restored.chatComposerText)
            assertEquals(1, restored.flashcardSprint.gotItCount)
            assertEquals(1, restored.flashcardSprint.reviewAgainCount)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun dailyQuotaRollsOverOnNewCalendarDateWhileKeepingMastery() {
        val tempFile = File.createTempFile("kova_rollover_test_", ".json")
        try {
            val vmDay1 = KovaViewModel(
                appStateStore = FileKovaAppStateStore(tempFile),
                dateProvider = { "2026-11-21" },
            )
            vmDay1.passLessonMockTest("l_1_1", scorePercent = 88)
            vmDay1.passLessonMockTest("l_1_2", scorePercent = 91)
            assertEquals(1, vmDay1.uiState.value.quota.dailyLessonsRemaining)

            // Cold start on the next calendar day
            val vmDay2 = KovaViewModel(
                appStateStore = FileKovaAppStateStore(tempFile),
                dateProvider = { "2026-11-22" },
            )
            val stateDay2 = vmDay2.uiState.value

            assertEquals(3, stateDay2.quota.dailyLessonsRemaining)
            assertEquals("2026-11-22", stateDay2.quota.lastQuotaDateIso)
            assertEquals(LessonStatus.COMPLETED, stateDay2.findLesson("l_1_1")?.status)
            assertEquals(LessonStatus.COMPLETED, stateDay2.findLesson("l_1_2")?.status)
            assertEquals(LessonStatus.UNLOCKED, stateDay2.findLesson("l_1_3")?.status)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun resetStudyDataClearsProgressButPreservesThemeAndSoundSettings() {
        val tempFile = File.createTempFile("kova_reset_test_", ".json")
        try {
            val vm = KovaViewModel(
                appStateStore = FileKovaAppStateStore(tempFile),
                dateProvider = { "2026-11-21" },
            )
            vm.setThemeMode(ThemeMode.AMOLED_BLACK)
            vm.setSoundAndHapticsEnabled(false)
            vm.setDynamicColorEnabled(true)
            vm.passLessonMockTest("l_1_1", scorePercent = 95)
            vm.updateChatComposerText("Unsent draft")

            vm.resetDemoProgress()

            val afterReset = vm.uiState.value
            // Study data reset
            assertEquals(LessonStatus.UNLOCKED, afterReset.findLesson("l_1_1")?.status)
            assertEquals(LessonStatus.LOCKED, afterReset.findLesson("l_1_2")?.status)
            assertEquals(3, afterReset.quota.dailyLessonsRemaining)
            assertEquals("", afterReset.chatComposerText)
            assertEquals(0, afterReset.flashcardSprint.gotItCount)

            // Preferences preserved in memory AND on disk
            assertEquals(ThemeMode.AMOLED_BLACK, afterReset.themeMode)
            assertFalse(afterReset.soundAndHapticsEnabled)
            assertTrue(afterReset.dynamicColorEnabled)

            val reloaded = KovaViewModel(
                appStateStore = FileKovaAppStateStore(tempFile),
                dateProvider = { "2026-11-21" },
            ).uiState.value
            assertEquals(ThemeMode.AMOLED_BLACK, reloaded.themeMode)
            assertFalse(reloaded.soundAndHapticsEnabled)
            assertEquals(LessonStatus.UNLOCKED, reloaded.findLesson("l_1_1")?.status)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun rapidConsecutiveAiSendsKeepAllUserMessagesPermanentlyWithoutVanishing() {
        val vm = KovaViewModel()
        val initialCount = vm.uiState.value.activeThreadMessages.size

        vm.updateChatComposerText("First question about val vs var")
        vm.sendChatMessage()
        vm.updateChatComposerText("Second follow-up about type inference")
        vm.sendChatMessage()
        vm.updateChatComposerText("Third question about null safety")
        vm.sendChatMessage()

        val state = vm.uiState.value
        assertEquals(initialCount + 6, state.activeThreadMessages.size)
        val userContents = state.activeThreadMessages.filter { it.isFromUser }.map { it.content }
        assertTrue(userContents.any { it.contains("First question about val vs var") })
        assertTrue(userContents.any { it.contains("Second follow-up about type inference") })
        assertTrue(userContents.any { it.contains("Third question about null safety") })
        val activeThread = state.chatThreads.first { it.id == state.activeThreadId }
        assertTrue(activeThread.previewSnippet.contains("Third question about null safety"))
    }

    @Test
    fun chapterFocusModeHidesGlobalChromeAndSupportsAiSheetToAiLabSwitch() {
        val vm = KovaViewModel()

        // Learn tab starts on clean Chapter Overview (not Chapter Focus Mode)
        assertFalse(vm.uiState.value.isChapterFocusMode)
        assertFalse(vm.uiState.value.isGlobalChromeHidden)

        // Enter Chapter 1 focus mode
        vm.openChapterFocus("ch_1")
        assertTrue(vm.uiState.value.isChapterFocusMode)
        assertEquals("ch_1", vm.uiState.value.focusedChapterId)
        assertTrue(vm.uiState.value.isGlobalChromeHidden)

        // Open slide-over AI sheet while staying inside focused chapter
        vm.setChapterAiSheetOpen(true)
        assertTrue(vm.uiState.value.isChapterAiSheetOpen)
        assertEquals(KovaTab.LEARN, vm.uiState.value.selectedTab)

        // Switch from slide-over sheet directly to full AI Lab tab
        vm.switchFromChapterFocusToAiTab()
        assertEquals(KovaTab.AI, vm.uiState.value.selectedTab)
        assertFalse(vm.uiState.value.isChapterAiSheetOpen)
        assertFalse(vm.uiState.value.isChapterFocusMode)
        assertFalse(vm.uiState.value.isGlobalChromeHidden)

        // Re-enter chapter focus and return to Chapter Overview via top bar
        vm.selectTab(KovaTab.LEARN)
        vm.openChapterFocus("ch_1")
        assertTrue(vm.uiState.value.isChapterFocusMode)
        vm.closeChapterFocus()
        assertFalse(vm.uiState.value.isChapterFocusMode)
        assertFalse(vm.uiState.value.isGlobalChromeHidden)
    }

    @Test
    fun aiHistoryVisibilityDefaultsByFormFactorAndTogglesCleanly() {
        val vm = KovaViewModel()
        assertEquals(null, vm.uiState.value.isAiHistoryVisible)

        // Phone default (false) toggles to true, then back to false
        vm.toggleAiHistoryVisibility(isWideTabletLayout = false)
        assertEquals(true, vm.uiState.value.isAiHistoryVisible)
        vm.toggleAiHistoryVisibility(isWideTabletLayout = false)
        assertEquals(false, vm.uiState.value.isAiHistoryVisible)

        // Tablet default (true) toggles cleanly via explicit setter
        vm.setAiHistoryVisibility(true)
        assertEquals(true, vm.uiState.value.isAiHistoryVisible)
        vm.toggleAiHistoryVisibility(isWideTabletLayout = true)
        assertEquals(false, vm.uiState.value.isAiHistoryVisible)
    }

    @Test
    fun flashcardFocusStageHidesGlobalChromeAndPausesTimerOnClose() {
        val vm = KovaViewModel()
        assertFalse(vm.uiState.value.isFlashcardFocusStageOpen)

        vm.openFlashcardFocusStage()
        assertTrue(vm.uiState.value.isFlashcardFocusStageOpen)
        assertTrue(vm.uiState.value.isGlobalChromeHidden)

        vm.closeFlashcardFocusStage()
        assertFalse(vm.uiState.value.isFlashcardFocusStageOpen)
        assertFalse(vm.uiState.value.isGlobalChromeHidden)
        assertFalse(vm.uiState.value.flashcardSprint.isRunning)
    }

    @Test
    fun allFourChapter1LessonsContainFullPhase2InteractiveCurriculum() {
        val vm = KovaViewModel()
        val ch1 = vm.uiState.value.findChapter("ch_1")!!
        assertEquals(4, ch1.lessons.size)

        ch1.lessons.forEach { lesson ->
            val content = lesson.interactiveContent
            assertNotNull("Missing Phase 2 content for ${lesson.code}", content)
            assertTrue("${lesson.code} needs word breakdowns", content!!.wordBreakdowns.size >= 3)
            assertTrue("${lesson.code} needs concept rules", content.conceptRules.size >= 3)
            assertTrue("${lesson.code} needs desired output", content.practiceTask.desiredOutput.isNotBlank())
            assertTrue("${lesson.code} needs solution notes", content.practiceTask.lineNotes.isNotEmpty())
            assertTrue("${lesson.code} needs multi-step debugger", content.debugger.steps.size >= 5)
            assertTrue(
                "${lesson.code} debugger final step should emit console output",
                content.debugger.steps.last().consoleOutput.isNotEmpty(),
            )
            assertEquals("${lesson.code} needs 5 integrated test questions", 5, content.testQuestions.size)
        }
    }

    @Test
    fun savingLessonHighlightSyncsToRevisionAndGotchaSendsDraftToAi() {
        val vm = KovaViewModel()
        val initialCount = vm.uiState.value.highlights.size

        vm.saveLessonHighlight(
            lessonId = "l_1_1",
            blockId = "wb_immutable",
            snippet = "'val' creates an immutable read-only label.",
            category = HighlightCategory.KEY_IDEA,
            learnerNote = "Remember sealed glass jar analogy.",
        )

        val afterSave = vm.uiState.value
        assertEquals(initialCount + 1, afterSave.highlights.size)
        val saved = afterSave.highlights.first()
        assertEquals("1.1", saved.lessonCode)
        assertEquals(HighlightCategory.KEY_IDEA, saved.category)
        assertEquals(NoteSource.LESSON_HIGHLIGHT, saved.source)
        assertEquals("Remember sealed glass jar analogy.", saved.learnerNote)

        // Send Gotcha to AI saves note, routes to AI tab, and loads composer draft
        vm.openLesson("l_1_1")
        vm.askAiAboutLessonHighlight(
            lessonId = "l_1_1",
            blockId = "rule_val_first",
            snippet = "Val cannot be reassigned.",
            learnerNote = "When should I pick var?",
        )

        val afterAi = vm.uiState.value
        assertEquals(KovaTab.AI, afterAi.selectedTab)
        assertEquals(null, afterAi.activeDetail)
        assertTrue(afterAi.chatComposerText.contains("Lesson 1.1"))
        assertTrue(afterAi.chatComposerText.contains("Val cannot be reassigned."))
        assertEquals(HighlightCategory.GOTCHA, afterAi.highlights.first().category)
    }

    @Test
    fun integratedLessonTestEnforces80PercentGateAndUpdatesMasteryRadar() {
        val vm = KovaViewModel()
        val initialQuota = vm.uiState.value.quota.dailyLessonsRemaining

        // Below 80% must NOT unlock next lesson or spend quota
        vm.submitLessonTestPass("l_1_1", scorePercent = 60)
        assertEquals(LessonStatus.UNLOCKED, vm.uiState.value.findLesson("l_1_1")?.status)
        assertEquals(LessonStatus.LOCKED, vm.uiState.value.findLesson("l_1_2")?.status)
        assertEquals(initialQuota, vm.uiState.value.quota.dailyLessonsRemaining)

        // Passing >= 80% unlocks next lesson, saves score, decrements quota, updates SR retention
        vm.submitLessonTestPass("l_1_3", scorePercent = 100)
        assertEquals(LessonStatus.COMPLETED, vm.uiState.value.findLesson("l_1_3")?.status)
        assertEquals(100, vm.uiState.value.findLesson("l_1_3")?.passScorePercent)
        assertEquals(initialQuota - 1, vm.uiState.value.quota.dailyLessonsRemaining)

        val sr13 = vm.uiState.value.spacedRepetitionItems.first { it.lessonCode == "1.3" }
        assertFalse(sr13.isDueNow)
        assertEquals(99, sr13.retentionPercent)
        assertTrue(vm.uiState.value.masteryRadar.strongest.any { it.lessonCode == "1.3" })
    }

    @Test
    fun setLessonSplitRatioAutoExpandsFrom75_25To50_50() {
        val vm = KovaViewModel()
        assertEquals(LessonSplitRatio.RATIO_75_25, vm.uiState.value.lessonSplitRatio)

        vm.setLessonSplitRatio(LessonSplitRatio.RATIO_50_50)
        assertEquals(LessonSplitRatio.RATIO_50_50, vm.uiState.value.lessonSplitRatio)
    }

    @Test
    fun lesson11HasZeroCompoundPlusEqualsAndLesson13TeachesPlusEqualsAndNotEquals() {
        val vm = KovaViewModel()
        val lesson11 = vm.uiState.value.findLesson("l_1_1")!!
        val content11 = lesson11.interactiveContent!!

        val allText11 = buildString {
            appendLine(lesson11.codePreview)
            content11.preHighlightedTakeaways.forEach { appendLine(it.headline); appendLine(it.body) }
            content11.wordBreakdowns.forEach {
                appendLine(it.term); appendLine(it.literalMeaning); appendLine(it.tenYearOldAnalogy)
            }
            content11.conceptRules.forEach { rule ->
                appendLine(rule.title)
                appendLine(rule.explanation)
                appendLine(rule.codeSnippet)
                appendLine(rule.whyItMatters)
                val dive = rule.explainDeepDive
                appendLine(dive.realLifeOpening)
                appendLine(dive.kotlinConnection)
                appendLine(dive.syntaxSnippet)
                dive.symbolBreakdowns.forEach { appendLine(it.symbol); appendLine(it.plainWords) }
            }
            appendLine(content11.practiceTask.prompt)
            appendLine(content11.practiceTask.starterCode)
            appendLine(content11.practiceTask.desiredOutput)
            appendLine(content11.practiceTask.solutionCode)
            content11.practiceTask.lineNotes.forEach { appendLine(it.note) }
            content11.debugger.lines.forEach { appendLine(it.code) }
            content11.debugger.steps.forEach { appendLine(it.explanation) }
            content11.testQuestions.forEach { q ->
                appendLine(q.prompt)
                q.codeSnippet?.let { appendLine(it) }
                q.options.forEach { appendLine(it) }
                appendLine(q.plainEnglishExplanation)
            }
        }
        assertFalse("Lesson 1.1 must not contain '+=' anywhere", allText11.contains("+="))

        val lesson13 = vm.uiState.value.findLesson("l_1_3")!!
        val content13 = lesson13.interactiveContent!!
        val allText13 = buildString {
            appendLine(lesson13.codePreview)
            content13.conceptRules.forEach { appendLine(it.codeSnippet); appendLine(it.explanation) }
            appendLine(content13.practiceTask.solutionCode)
        }
        assertTrue("Lesson 1.3 should teach '+='", allText13.contains("+="))
        assertTrue("Lesson 1.3 should teach '!='", allText13.contains("!="))
    }

    @Test
    fun allChapter1LessonsIncludeExplainDeepDivesPreHighlightsAndIdeStarterCode() {
        val vm = KovaViewModel()
        val ch1 = vm.uiState.value.findChapter("ch_1")!!

        ch1.lessons.forEach { lesson ->
            val content = lesson.interactiveContent!!
            assertTrue(
                "${lesson.code} needs pre-highlighted takeaways",
                content.preHighlightedTakeaways.size >= 2,
            )
            assertTrue(
                "${lesson.code} needs starterCode for IDE workspace",
                content.practiceTask.starterCode.isNotBlank(),
            )
            content.conceptRules.forEach { rule ->
                val dive = rule.explainDeepDive
                assertTrue(dive.realLifeOpening.isNotBlank())
                assertTrue(dive.kotlinConnection.isNotBlank())
                assertTrue(dive.syntaxSnippet.isNotBlank())
                assertTrue(dive.symbolBreakdowns.isNotEmpty())
            }
        }
    }

    @Test
    fun debuggerDefaultsLockedAndUnlocksPerLesson() {
        val vm = KovaViewModel()
        assertFalse(vm.uiState.value.isDebuggerUnlockedFor("l_1_1"))
        assertFalse(vm.uiState.value.isDebuggerUnlocked)

        vm.openLesson("l_1_1")
        assertFalse(vm.uiState.value.isDebuggerUnlocked)

        vm.unlockLessonDebugger("l_1_1")
        assertTrue(vm.uiState.value.isDebuggerUnlockedFor("l_1_1"))
        assertTrue(vm.uiState.value.isDebuggerUnlocked)
        assertFalse(vm.uiState.value.isDebuggerUnlockedFor("l_1_2"))
    }

    @Test
    fun smartBackNavigationReturnsLessonToAiBackToExactLessonAndOtherTabsToLearnHome() {
        val vm = KovaViewModel()
        vm.openChapterFocus("ch_1")
        vm.openLesson("l_1_1")
        vm.setLessonSplitRatio(LessonSplitRatio.RATIO_50_50)
        vm.unlockLessonDebugger("l_1_1")

        // Jump from Lesson 1.1 to full-screen AI
        vm.openAiFromLesson("l_1_1")
        assertEquals(KovaTab.AI, vm.uiState.value.selectedTab)
        assertEquals(null, vm.uiState.value.activeDetail)
        assertEquals("l_1_1", vm.uiState.value.returnToLessonIdAfterAi)

        // System Back returns directly to Lesson 1.1 preserving split ratio and debugger state
        val returned = vm.returnFromAiToLesson()
        assertTrue(returned)
        assertEquals(DetailDestination.LessonDetail("l_1_1"), vm.uiState.value.activeDetail)
        assertEquals(KovaTab.LEARN, vm.uiState.value.selectedTab)
        assertEquals(LessonSplitRatio.RATIO_50_50, vm.uiState.value.lessonSplitRatio)
        assertTrue(vm.uiState.value.isDebuggerUnlockedFor("l_1_1"))
        assertEquals(null, vm.uiState.value.returnToLessonIdAfterAi)

        // Gotcha -> Send to AI also records returnToLessonIdAfterAi
        vm.askAiAboutLessonHighlight(
            lessonId = "l_1_1",
            blockId = "rule_val_first",
            snippet = "val cannot be reassigned",
            learnerNote = "Quick check",
        )
        assertEquals(KovaTab.AI, vm.uiState.value.selectedTab)
        assertEquals("l_1_1", vm.uiState.value.returnToLessonIdAfterAi)
        assertTrue(vm.returnFromAiToLesson())
        assertEquals(DetailDestination.LessonDetail("l_1_1"), vm.uiState.value.activeDetail)

        // Opening Lab or Revision directly -> Back navigates to Learn Overview home
        vm.closeDetail()
        vm.selectTab(KovaTab.LAB)
        assertEquals(null, vm.uiState.value.returnToLessonIdAfterAi)
        vm.navigateBackToLearnHome()
        assertEquals(KovaTab.LEARN, vm.uiState.value.selectedTab)
        assertEquals(null, vm.uiState.value.focusedChapterId)

        vm.selectTab(KovaTab.REVISION)
        vm.navigateBackToLearnHome()
        assertEquals(KovaTab.LEARN, vm.uiState.value.selectedTab)
    }

    @Test
    fun debuggerUnlockExactScrollPositionAndExplainStatePersistAcrossAiDetourAndRestart() {
        val tempFile = File.createTempFile("kova_phase21_persist_", ".json")
        try {
            val fixedDate = "2026-11-22"
            val vm1 = KovaViewModel(
                appStateStore = FileKovaAppStateStore(tempFile),
                dateProvider = { fixedDate },
            )

            vm1.openChapterFocus("ch_1")
            vm1.openLesson("l_1_1")
            vm1.unlockLessonDebugger("l_1_1")
            vm1.updateLessonScrollPosition("l_1_1", 640)
            vm1.setLessonExplainRule("l_1_1", "rule_val_first")

            // Jump to AI and return -> exact lesson, scroll offset, explain rule, and debugger unlock preserved
            vm1.openAiFromLesson("l_1_1")
            assertEquals(KovaTab.AI, vm1.uiState.value.selectedTab)
            assertTrue(vm1.returnFromAiToLesson())
            assertEquals(DetailDestination.LessonDetail("l_1_1"), vm1.uiState.value.activeDetail)
            assertEquals(640, vm1.uiState.value.scrollPositionFor("l_1_1"))
            assertEquals("rule_val_first", vm1.uiState.value.activeExplainRuleFor("l_1_1"))
            assertTrue(vm1.uiState.value.isDebuggerUnlockedFor("l_1_1"))

            // Cold app restart -> all Phase 2.1 state persists cleanly from disk
            val vm2 = KovaViewModel(
                appStateStore = FileKovaAppStateStore(tempFile),
                dateProvider = { fixedDate },
            )
            val restored = vm2.uiState.value
            assertTrue(restored.isDebuggerUnlockedFor("l_1_1"))
            assertEquals(640, restored.scrollPositionFor("l_1_1"))
            assertEquals("rule_val_first", restored.activeExplainRuleFor("l_1_1"))
            assertEquals("l_1_1", restored.lastActiveLessonId)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun aiTutorStatusSupportsReadyThinkingAndOfflineStates() {
        val vm = KovaViewModel()
        assertEquals(AiTutorStatus.READY, vm.uiState.value.aiTutorStatus)

        vm.setAiTutorStatus(AiTutorStatus.OFFLINE)
        assertEquals(AiTutorStatus.OFFLINE, vm.uiState.value.aiTutorStatus)

        vm.updateChatComposerText("Offline check")
        vm.sendChatMessage()
        assertEquals(AiTutorStatus.OFFLINE, vm.uiState.value.aiTutorStatus)

        vm.setAiTutorStatus(AiTutorStatus.READY)
        assertEquals(AiTutorStatus.READY, vm.uiState.value.aiTutorStatus)
    }

    @Test
    fun lessonOpensWithIdeOffByDefaultAndTogglesToHalfSplitAndBack() {
        val vm = KovaViewModel()
        vm.openChapterFocus("ch_1")
        vm.openLesson("l_1_1")

        // Phase 2.2: Split IDE defaults to OFF so the lesson reader opens full-screen
        assertFalse(vm.uiState.value.isIdePaneOpen)
        assertFalse(vm.uiState.value.isDebuggerStageOpen)

        // Toggling IDE opens split mode at 50:50
        vm.toggleIdePane()
        assertTrue(vm.uiState.value.isIdePaneOpen)
        assertEquals(LessonSplitRatio.RATIO_50_50, vm.uiState.value.lessonSplitRatio)

        // Toggling IDE again hides it cleanly
        vm.toggleIdePane()
        assertFalse(vm.uiState.value.isIdePaneOpen)

        // Opening via "Open IDE to Practice" button also launches 50:50 split
        vm.openIdePaneAtHalf()
        assertTrue(vm.uiState.value.isIdePaneOpen)
        assertEquals(LessonSplitRatio.RATIO_50_50, vm.uiState.value.lessonSplitRatio)
    }

    @Test
    fun debuggerStageOpensUnlocksPerLessonAndClosesCleanly() {
        val vm = KovaViewModel()
        vm.openChapterFocus("ch_1")
        vm.openLesson("l_1_1")
        vm.openIdePaneAtHalf()

        assertFalse(vm.uiState.value.isDebuggerStageOpen)
        assertFalse(vm.uiState.value.isDebuggerUnlockedFor("l_1_1"))

        // Opening the 70:30 Debugger Stage unlocks the lesson debugger and displays the overlay
        vm.openDebuggerStage("l_1_1")
        assertTrue(vm.uiState.value.isDebuggerStageOpen)
        assertTrue(vm.uiState.value.isDebuggerUnlockedFor("l_1_1"))

        // Closing the 70:30 Debugger Stage keeps the lesson & IDE split intact
        vm.closeDebuggerStage()
        assertFalse(vm.uiState.value.isDebuggerStageOpen)
        assertTrue(vm.uiState.value.isIdePaneOpen)
        assertEquals(LessonSplitRatio.RATIO_50_50, vm.uiState.value.lessonSplitRatio)

        // Detouring to AI and returning preserves IDE open state and split ratio
        vm.openAiFromLesson("l_1_1")
        assertEquals(KovaTab.AI, vm.uiState.value.selectedTab)
        assertTrue(vm.returnFromAiToLesson())
        assertEquals(DetailDestination.LessonDetail("l_1_1"), vm.uiState.value.activeDetail)
        assertTrue(vm.uiState.value.isIdePaneOpen)
        assertEquals(LessonSplitRatio.RATIO_50_50, vm.uiState.value.lessonSplitRatio)
    }

    @Test
    fun aiScopedBannerAppliesOnlyToLessonChatsAndPersistsScopedIds() {
        val tempFile = File.createTempFile("kova_phase22_scoped_ai_", ".json")
        try {
            val fixedDate = "2026-11-22"
            val vm1 = KovaViewModel(
                appStateStore = FileKovaAppStateStore(tempFile),
                dateProvider = { fixedDate },
            )

            // Fresh general chat is NOT lesson-scoped and starts empty for the centered emblem
            vm1.selectTab(KovaTab.AI)
            vm1.createNewGeneralThread()
            assertFalse(vm1.uiState.value.isActiveAiThreadLessonScoped)
            assertTrue(vm1.uiState.value.activeThreadMessages.isEmpty())
            assertEquals("GEN", vm1.uiState.value.activeThread?.lessonCode)

            // Launching AI from Lesson 1.1 marks that thread lesson-scoped
            vm1.openChapterFocus("ch_1")
            vm1.openLesson("l_1_1")
            vm1.openAiFromLesson("l_1_1")
            val scopedThreadId = vm1.uiState.value.activeThreadId
            assertTrue(scopedThreadId != null)
            assertTrue(vm1.uiState.value.isActiveAiThreadLessonScoped)
            assertTrue(scopedThreadId in vm1.uiState.value.scopedAiThreadIds)

            // Returning to lesson and reopening a fresh general chat hides the scoped banner
            assertTrue(vm1.returnFromAiToLesson())
            vm1.closeDetail()
            vm1.selectTab(KovaTab.AI)
            vm1.createNewGeneralThread()
            assertFalse(vm1.uiState.value.isActiveAiThreadLessonScoped)

            // Persisted scopedAiThreadIds survives cold app restart
            val vm2 = KovaViewModel(
                appStateStore = FileKovaAppStateStore(tempFile),
                dateProvider = { fixedDate },
            )
            assertTrue(scopedThreadId in vm2.uiState.value.scopedAiThreadIds)
        } finally {
            tempFile.delete()
        }
    }
}
