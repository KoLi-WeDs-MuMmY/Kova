package com.example.data

import com.example.model.Chapter
import com.example.model.ChapterAssignment
import com.example.model.HighlightCategory
import com.example.model.HighlightNote
import com.example.model.Lesson
import com.example.model.LessonStatus
import com.example.model.QuotaState
import com.example.model.SpacedRepetitionItem

interface LearningRepository {
    fun getInitialChapters(): List<Chapter>
    fun getInitialHighlights(): List<HighlightNote>
    fun getInitialSpacedRepetitionItems(): List<SpacedRepetitionItem>
    fun getInitialQuotaState(): QuotaState
}

/**
 * Isolated Phase 1 mock data repository.
 * Phase 2 will replace this with Room/Firestore curriculum storage without modifying UI shells.
 */
class MockLearningRepository : LearningRepository {

    override fun getInitialChapters(): List<Chapter> = listOf(
        Chapter(
            id = "ch_1",
            number = 1,
            title = "Kotlin Foundations",
            tagline = "Literal word meanings, labeled memory boxes, and rock-solid branching logic.",
            isUnlocked = true,
            lockReason = null,
            lessons = listOf(
                Lesson(
                    id = "l_1_1",
                    chapterId = "ch_1",
                    code = "1.1",
                    title = "Words & Variables",
                    subtitle = "Why 'val' is a sealed glass jar and 'var' is a whiteboard.",
                    analogyTeaser = "Think of RAM as a wall of labeled lockers. 'val' locks the label permanently; 'var' lets you swap what's inside.",
                    codePreview = "val heroName = \"Kova\"\nvar dailyXp = 40\ndailyXp += 15",
                    estimatedMinutes = 8,
                    status = LessonStatus.UNLOCKED,
                ),
                Lesson(
                    id = "l_1_2",
                    chapterId = "ch_1",
                    code = "1.2",
                    title = "Types & Strings",
                    subtitle = "Shapes of data, string templates, and safe null thinking.",
                    analogyTeaser = "Every variable has a slot shape: Int holds whole steps, Double holds measurements, and String holds sentences.",
                    codePreview = "val streak: Int = 5\nval badge = \"Streak: \${streak}d\"\nval tip: String? = null",
                    estimatedMinutes = 10,
                    status = LessonStatus.LOCKED,
                ),
                Lesson(
                    id = "l_1_3",
                    chapterId = "ch_1",
                    code = "1.3",
                    title = "Operators & Ranges",
                    subtitle = "Math operators, comparisons, and expressive 'in 1..10' checks.",
                    analogyTeaser = "A range like 80..100 is a ruler segment—ask 'score in 80..100' instead of juggling two comparisons.",
                    codePreview = "val score = 85\nval passed = score in 80..100\nval nextRank = score / 10",
                    estimatedMinutes = 9,
                    status = LessonStatus.LOCKED,
                ),
                Lesson(
                    id = "l_1_4",
                    chapterId = "ch_1",
                    code = "1.4",
                    title = "Control Decisions",
                    subtitle = "Using 'if' and 'when' as expressions that return values.",
                    analogyTeaser = "'when' works like a clean train switchboard—one input track smoothly chooses the exact destination value.",
                    codePreview = "val tier = when (score) {\n  in 90..100 -> \"Gold\"\n  in 80..89 -> \"Mint\"\n  else -> \"Retry\"\n}",
                    estimatedMinutes = 12,
                    status = LessonStatus.LOCKED,
                ),
            ),
            assignment = ChapterAssignment(
                id = "asg_ch_1",
                chapterId = "ch_1",
                chapterLabel = "CHAPTER 1 ASSIGNMENT",
                title = "Build a CLI Study Streak Evaluator",
                subtitle = "Combine val/var, string templates, ranges, and a when-expression into a single clean Kotlin program.",
                deliverables = listOf(
                    "Declare immutable learner profile values and mutable session counters",
                    "Use range checks (in 80..100) to classify daily mastery tiers",
                    "Format a clean multi-line console summary with string templates",
                ),
                estimatedMinutes = 20,
                isUnlocked = false,
                isCompleted = false,
            ),
            revisionBulletPoints = listOf(
                "Prefer 'val' by default; reach for 'var' only when a value genuinely mutates over time.",
                "Kotlin infers static types at compile time—explicit type annotations clarify intent on public APIs.",
                "Ranges (1..10, 0 until size) pair naturally with 'in' checks and 'when' branches.",
                "Both 'if' and 'when' are expressions in Kotlin, meaning they evaluate directly to a result.",
            ),
        ),
        Chapter(
            id = "ch_2",
            number = 2,
            title = "Functions & Lambdas",
            tagline = "Reusable mental blueprints, default arguments, and passing behavior like data.",
            isUnlocked = false,
            lockReason = "Complete Chapter 1 Assignment to Unlock",
            lessons = listOf(
                Lesson(
                    id = "l_2_1",
                    chapterId = "ch_2",
                    code = "2.1",
                    title = "Defining Functions",
                    subtitle = "Parameters, return types, and single-expression clarity.",
                    analogyTeaser = "A function is a coffee recipe card: hand it ingredients (parameters) and it returns the exact drink.",
                    codePreview = "fun masteryLabel(score: Int): String =\n  if (score >= 80) \"Ready\" else \"Review\"",
                    estimatedMinutes = 10,
                    status = LessonStatus.LOCKED,
                ),
                Lesson(
                    id = "l_2_2",
                    chapterId = "ch_2",
                    code = "2.2",
                    title = "Default & Named Args",
                    subtitle = "Self-documenting call sites without endless overloads.",
                    analogyTeaser = "Named arguments work like labeled shipping forms—order never causes mix-ups.",
                    codePreview = "fun scheduleReview(\n  topic: String,\n  hoursLater: Int = 24\n) = \"\$topic in \${hoursLater}h\"",
                    estimatedMinutes = 9,
                    status = LessonStatus.LOCKED,
                ),
                Lesson(
                    id = "l_2_3",
                    chapterId = "ch_2",
                    code = "2.3",
                    title = "Lambdas & Higher-Order",
                    subtitle = "Passing small actions directly into Compose buttons & collections.",
                    analogyTeaser = "A lambda is a sticky-note instruction you hand to a button: 'When tapped, do this exact action.'",
                    codePreview = "val scores = listOf(72, 85, 94)\nval passed = scores.filter { it >= 80 }",
                    estimatedMinutes = 14,
                    status = LessonStatus.LOCKED,
                ),
            ),
            assignment = ChapterAssignment(
                id = "asg_ch_2",
                chapterId = "ch_2",
                chapterLabel = "CHAPTER 2 ASSIGNMENT",
                title = "Compose Action Pipeline Utility",
                subtitle = "Create reusable higher-order filter and formatter helpers for lesson quiz scores.",
                deliverables = listOf(
                    "Write single-expression grading functions with sensible default thresholds",
                    "Pass custom lambda formatters into a summary printer",
                ),
                estimatedMinutes = 25,
                isUnlocked = false,
                isCompleted = false,
            ),
            revisionBulletPoints = listOf(
                "Single-expression functions (=) remove boilerplate braces for pure computations.",
                "Default + named arguments eliminate Java-style telescoping overloads.",
                "Trailing lambda syntax is the foundation of Jetpack Compose UI trees.",
            ),
        ),
    )

    override fun getInitialHighlights(): List<HighlightNote> = listOf(
        HighlightNote(
            id = "hl_1",
            lessonCode = "1.1",
            lessonTitle = "Words & Variables",
            category = HighlightCategory.KEY_IDEA,
            highlightedSnippet = "Choose 'val' first—immutability makes state bugs dramatically harder to create.",
            learnerNote = "Whenever I track UI state in Compose, keeping fields 'val' inside data classes forces clean copies.",
            savedAtLabel = "Today · 09:14",
        ),
        HighlightNote(
            id = "hl_2",
            lessonCode = "1.2",
            lessonTitle = "Types & Strings",
            category = HighlightCategory.CODE_RULE,
            highlightedSnippet = "Use \"\${expression}\" for properties or math inside String templates, and \"\$name\" for simple variables.",
            learnerNote = "Remember braces are required for expressions like \${score + 5} or \${user.name}.",
            savedAtLabel = "Today · 10:02",
        ),
        HighlightNote(
            id = "hl_3",
            lessonCode = "1.4",
            lessonTitle = "Control Decisions",
            category = HighlightCategory.ASK_AI,
            highlightedSnippet = "Exhaustive 'when' expressions require an 'else' branch unless the compiler knows every possible sealed/enum case.",
            learnerNote = "Ask Scoped Tutor in Phase 3: How does 'when' with sealed interfaces avoid needing an else branch?",
            savedAtLabel = "Yesterday · 20:45",
        ),
        HighlightNote(
            id = "hl_4",
            lessonCode = "1.3",
            lessonTitle = "Operators & Ranges",
            category = HighlightCategory.CODE_RULE,
            highlightedSnippet = "'in 80..100' is inclusive on both ends, while 'until' excludes the upper bound for 0-based list indices.",
            learnerNote = "Crucial for preventing off-by-one crashes when stepping through array indices.",
            savedAtLabel = "Yesterday · 19:18",
        ),
    )

    override fun getInitialSpacedRepetitionItems(): List<SpacedRepetitionItem> = listOf(
        SpacedRepetitionItem(
            id = "sr_1",
            lessonCode = "1.1",
            title = "val vs var & Type Inference",
            countdownLabel = "In 1h 45m",
            retentionPercent = 92,
            intervalTag = "D+1 Pop-up",
        ),
        SpacedRepetitionItem(
            id = "sr_2",
            lessonCode = "1.2",
            title = "String Templates & Nullable Types",
            countdownLabel = "In 5h 20m",
            retentionPercent = 78,
            intervalTag = "D+3 Pop-up",
        ),
        SpacedRepetitionItem(
            id = "sr_3",
            lessonCode = "1.3",
            title = "Inclusive Ranges vs until",
            countdownLabel = "Tomorrow · 09:00",
            retentionPercent = 64,
            intervalTag = "Morning Reserve",
        ),
    )

    override fun getInitialQuotaState(): QuotaState = QuotaState(
        dailyLessonsRemaining = 3,
        dailyLessonsMax = 3,
        morningDigestStatus = "Ready",
        activeAiPoolRemaining = 12,
        activeAiPoolMax = 14,
        spacedRepetitionReserve = 4,
    )
}
