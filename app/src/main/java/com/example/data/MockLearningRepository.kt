package com.example.data

import com.example.model.Chapter
import com.example.model.ChapterAssignment
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.HighlightCategory
import com.example.model.HighlightNote
import com.example.model.Lesson
import com.example.model.LessonStatus
import com.example.model.NoteSource
import com.example.model.QuotaState
import com.example.model.SpacedRepetitionItem

interface LearningRepository {
    fun getInitialChapters(): List<Chapter>
    fun getInitialHighlights(): List<HighlightNote>
    fun getInitialSpacedRepetitionItems(): List<SpacedRepetitionItem>
    fun getInitialChatThreads(): List<ChatThread>
    fun getInitialChatMessages(): List<ChatMessage>
    fun getInitialQuotaState(): QuotaState
}

/**
 * Isolated curriculum & seed repository.
 * Room persists threads, messages, saved revision notes, and spaced-repetition updates on-device.
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
            source = NoteSource.LESSON_HIGHLIGHT,
            savedAtLabel = "Today · 09:14",
            createdAtMillis = 1_732_100_400_000L,
        ),
        HighlightNote(
            id = "hl_2",
            lessonCode = "1.2",
            lessonTitle = "Types & Strings",
            category = HighlightCategory.CODE_RULE,
            highlightedSnippet = "Use \"\${expression}\" for properties or math inside String templates, and \"\$name\" for simple variables.",
            learnerNote = "Remember braces are required for expressions like \${score + 5} or \${user.name}.",
            source = NoteSource.LESSON_HIGHLIGHT,
            savedAtLabel = "Today · 10:02",
            createdAtMillis = 1_732_100_300_000L,
        ),
        HighlightNote(
            id = "hl_3",
            lessonCode = "1.4",
            lessonTitle = "Control Decisions",
            category = HighlightCategory.GOTCHA,
            highlightedSnippet = "Exhaustive 'when' expressions require an 'else' branch unless the compiler knows every sealed/enum case.",
            learnerNote = "Gotcha: forgetting 'else' when assigning 'val tier = when (score) { ... }' fails compilation immediately.",
            source = NoteSource.AI_TUTOR,
            savedAtLabel = "Yesterday · 20:45",
            createdAtMillis = 1_732_100_200_000L,
        ),
        HighlightNote(
            id = "hl_4",
            lessonCode = "1.3",
            lessonTitle = "Operators & Ranges",
            category = HighlightCategory.CODE_RULE,
            highlightedSnippet = "'in 80..100' is inclusive on both ends, while 'until' excludes the upper bound for 0-based list indices.",
            learnerNote = "Crucial for preventing off-by-one IndexOutOfBounds crashes when stepping through list indices.",
            source = NoteSource.LESSON_HIGHLIGHT,
            savedAtLabel = "Yesterday · 19:18",
            createdAtMillis = 1_732_100_100_000L,
        ),
    )

    override fun getInitialSpacedRepetitionItems(): List<SpacedRepetitionItem> = listOf(
        SpacedRepetitionItem(
            id = "sr_1",
            lessonCode = "1.3",
            title = "Inclusive Ranges (..) vs until",
            frontPrompt = "What is the key difference between `0..4` and `0 until 4` when looping over a 4-item list?",
            backAnswer = "`0..4` includes 4 (5 numbers total, causing an off-by-one crash on a 4-item list). `0 until 4` stops at 3—matching 0-based indices 0, 1, 2, 3.",
            codeExample = "for (i in 0 until items.size) { ... }",
            isDueNow = true,
            countdownLabel = "Due Now",
            retentionPercent = 58,
            intervalTag = "Weak Spot",
            reviewCount = 1,
        ),
        SpacedRepetitionItem(
            id = "sr_2",
            lessonCode = "1.4",
            title = "Exhaustive `when` Expression Rule",
            frontPrompt = "Why does `val grade = when (score) { in 80..100 -> \"Pass\" }` fail to compile?",
            backAnswer = "When `when` returns a value into a variable, Kotlin demands every possible input be covered—so Int ranges need an `else ->` fallback branch.",
            codeExample = "val grade = when (score) {\n  in 80..100 -> \"Pass\"\n  else -> \"Retry\"\n}",
            isDueNow = true,
            countdownLabel = "Due Now",
            retentionPercent = 66,
            intervalTag = "D+1 Due",
            reviewCount = 2,
        ),
        SpacedRepetitionItem(
            id = "sr_3",
            lessonCode = "1.1",
            title = "val vs var & Reference Locking",
            frontPrompt = "Explain the difference between `val` and `var` to a 10-year-old.",
            backAnswer = "`val` is a sealed glass jar—once you place a value in it, you cannot swap it for another. `var` is a whiteboard you can erase and rewrite.",
            codeExample = "val appName = \"Kova\"\nvar streak = 1\nstreak += 1",
            isDueNow = false,
            countdownLabel = "In 3h 20m",
            retentionPercent = 92,
            intervalTag = "D+3 Pop-up",
            reviewCount = 4,
        ),
        SpacedRepetitionItem(
            id = "sr_4",
            lessonCode = "1.2",
            title = "String Templates with Braces",
            frontPrompt = "When do you need curly braces `\${...}` inside a Kotlin String template?",
            backAnswer = "Use `\$name` for a single variable, and `\${expression}` whenever you access a property (`\${user.name}`) or run math (`\${score + 5}`).",
            codeExample = "val msg = \"Left: \${max - done}\"",
            isDueNow = false,
            countdownLabel = "Tomorrow · 09:00",
            retentionPercent = 84,
            intervalTag = "Morning Reserve",
            reviewCount = 3,
        ),
    )

    override fun getInitialChatThreads(): List<ChatThread> = listOf(
        ChatThread(
            id = "thread_ch1_val_var",
            title = "Why prefer val over var?",
            lessonCode = "1.1",
            lessonTitle = "Words & Variables",
            isPinned = true,
            updatedAtLabel = "Today · 09:20",
            updatedAtMillis = 1_732_101_000_000L,
            previewSnippet = "Because val locks the label once—preventing accidental state rewrites.",
        ),
        ChatThread(
            id = "thread_ch1_ranges",
            title = "String braces & range gotchas",
            lessonCode = "1.3",
            lessonTitle = "Operators & Ranges",
            isPinned = false,
            updatedAtLabel = "Yesterday · 20:15",
            updatedAtMillis = 1_732_100_500_000L,
            previewSnippet = "Use 0 until size for indices and 80..100 for inclusive score tiers.",
        ),
    )

    override fun getInitialChatMessages(): List<ChatMessage> = listOf(
        ChatMessage(
            id = "msg_1",
            threadId = "thread_ch1_val_var",
            isFromUser = true,
            content = "Explain like I'm 10: why should I start with val instead of var?",
            savedNoteId = null,
            timestampLabel = "09:18",
            createdAtMillis = 1_732_100_900_000L,
        ),
        ChatMessage(
            id = "msg_2",
            threadId = "thread_ch1_val_var",
            isFromUser = false,
            content = "Imagine labeling a lunchbox with permanent marker (`val`) vs sticky notes (`var`). With `val`, nobody in a 500-line screen can accidentally swap what's inside—so when a bug shows up, you only have to inspect the few `var` boxes that actually change.",
            savedNoteId = null,
            timestampLabel = "09:18",
            createdAtMillis = 1_732_100_950_000L,
        ),
        ChatMessage(
            id = "msg_3",
            threadId = "thread_ch1_ranges",
            isFromUser = true,
            content = "Why did `0..list.size` crash my loop?",
            savedNoteId = null,
            timestampLabel = "20:14",
            createdAtMillis = 1_732_100_450_000L,
        ),
        ChatMessage(
            id = "msg_4",
            threadId = "thread_ch1_ranges",
            isFromUser = false,
            content = "Gotcha spotted! A 4-item list only has positions 0, 1, 2, 3. Writing `0..4` tries to open locker #4 which doesn't exist! Switch to `0 until list.size` (or `list.indices`) so Kotlin stops cleanly at 3.",
            savedNoteId = null,
            timestampLabel = "20:15",
            createdAtMillis = 1_732_100_500_000L,
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
