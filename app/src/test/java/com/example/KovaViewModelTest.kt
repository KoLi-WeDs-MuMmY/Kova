package com.example

import com.example.model.HighlightCategory
import com.example.model.LessonStatus
import com.example.ui.theme.ThemeMode
import com.example.viewmodel.KovaViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KovaViewModelTest {

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

        // Complete all 4 Chapter 1 lessons
        vm.passLessonMockTest("l_1_1")
        vm.passLessonMockTest("l_1_2")
        vm.passLessonMockTest("l_1_3")
        vm.passLessonMockTest("l_1_4")

        val ch1 = vm.uiState.value.findChapter("ch_1")!!
        val ch2Before = vm.uiState.value.findChapter("ch_2")!!
        assertTrue(ch1.assignment.isUnlocked)
        assertFalse(ch1.assignment.isCompleted)
        assertFalse(ch2Before.isUnlocked)

        // Complete Chapter 1 Assignment (synced across Learn & Lab)
        vm.completeAssignment("asg_ch_1")

        val ch1After = vm.uiState.value.findChapter("ch_1")!!
        val ch2After = vm.uiState.value.findChapter("ch_2")!!
        assertTrue(ch1After.assignment.isCompleted)
        assertTrue(ch2After.isUnlocked)
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
}
