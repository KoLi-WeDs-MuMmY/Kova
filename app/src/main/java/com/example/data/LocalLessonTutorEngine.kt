package com.example.data

import com.example.model.Lesson

/**
 * Local lesson-scoped tutor reply generator for Phase 2 offline UX testing.
 * Phase 3 replaces this behind the same method contract with live Firebase AI Logic / Gemini.
 */
class LocalLessonTutorEngine {

    fun generateScopedReply(
        prompt: String,
        lessonCode: String,
        lessonTitle: String,
        lesson: Lesson?,
    ): String {
        val normalized = prompt.lowercase()
        val codeSample = lesson?.codePreview ?: "val score = 85\nval passed = score in 80..100"

        return when {
            normalized.contains("10") || normalized.contains("simple") || normalized.contains("analogy") -> {
                "10-Year-Old Version (Lesson $lessonCode · $lessonTitle):\n" +
                    "${lesson?.analogyTeaser ?: "Imagine labeled storage boxes in memory where Kotlin checks every label before running."}\n\n" +
                    "Rule of thumb: read the code left-to-right like an English sentence before touching syntax."
            }
            normalized.contains("word") || normalized.contains("meaning") -> {
                "Literal Word Breakdown (Lesson $lessonCode):\n" +
                    "• val = 'value' (read-only reference locked once assigned)\n" +
                    "• var = 'variable' (changeable container whose value can vary)\n" +
                    "• in / when = readable English checks replacing messy nested if-else chains."
            }
            normalized.contains("fail") || normalized.contains("error") || normalized.contains("bug") -> {
                "Common Gotcha Check for Lesson $lessonCode ($lessonTitle):\n" +
                    "1. Reassigning a 'val' causes a compile error—switch to 'var' only if mutation is intentional.\n" +
                    "2. Missing braces in \"\${expr}\" prints the literal variable name instead of computing the expression.\n" +
                    "3. Off-by-one bug: use '0 until size' instead of '0..size' when indexing lists."
            }
            normalized.contains("drill") || normalized.contains("quiz") || normalized.contains("practice") -> {
                "Mini 20-Second Drill (Lesson $lessonCode):\n" +
                    "Look at this snippet:\n$codeSample\n\n" +
                    "Challenge: Which line holds a fixed reference, and what exact value prints at the end? Tap 'Save to Notes' if you want to turn this into a flashcard!"
            }
            else -> {
                "Scoped Answer · Lesson $lessonCode ($lessonTitle):\n" +
                    "Staying strictly inside $lessonTitle: ${lesson?.subtitle ?: "focus on clear immutable state first."}\n\n" +
                    "Reference pattern:\n$codeSample\n\n" +
                    "Tip: Tap 'Save to Notes' below to pin this rule directly into your Revision Binder."
            }
        }
    }
}
