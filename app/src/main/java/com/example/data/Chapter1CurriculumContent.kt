package com.example.data

import com.example.model.CodeLineRole
import com.example.model.ConceptRule
import com.example.model.DebuggerCodeLine
import com.example.model.DebuggerStep
import com.example.model.DebuggerWalkthrough
import com.example.model.Lesson
import com.example.model.LessonInteractiveContent
import com.example.model.LessonQuestionKind
import com.example.model.LessonTestQuestion
import com.example.model.PracticeSolutionNote
import com.example.model.PracticeTaskSpec
import com.example.model.VariableWatchEntry
import com.example.model.WordBreakdown

/**
 * Full Phase 2 interactive curriculum for all 4 Chapter 1 lessons:
 * - 1.1 Words & Variables
 * - 1.2 Types & Strings
 * - 1.3 Operators & Ranges
 * - 1.4 Control Decisions
 */
object Chapter1CurriculumContent {

    fun lesson1_1WordsAndVariables(): LessonInteractiveContent = LessonInteractiveContent(
        wordBreakdowns = listOf(
            WordBreakdown(
                id = "l1_1_wb_variable",
                term = "Variable",
                literalMeaning = "Something able to hold or vary a value — from Latin 'variare' (to change or carry).",
                tenYearOldAnalogy = "Picture a wall of school lockers with name tags on the doors. A variable is just one labeled locker in the phone's memory holding a value for you.",
                highlightSnippet = "A variable is a labeled memory locker that stores one piece of data by name.",
            ),
            WordBreakdown(
                id = "l1_1_wb_immutable",
                term = "Immutable (val)",
                literalMeaning = "'Im-' (not) + 'mutable' (changeable) = locked so it cannot be swapped later.",
                tenYearOldAnalogy = "A sealed glass jar labeled 'heroName'. Once you drop \"Kova\" inside and snap the lid shut, nobody can swap it for a different name.",
                highlightSnippet = "'val' creates an immutable read-only label—once assigned, you cannot reassign it.",
            ),
            WordBreakdown(
                id = "l1_1_wb_mutable",
                term = "Mutable (var)",
                literalMeaning = "Able to mutate or change over time.",
                tenYearOldAnalogy = "A mini whiteboard labeled 'dailyXp'. You can write 40 now, erase it after a lesson, and write 55.",
                highlightSnippet = "'var' creates a mutable box whose value can be updated as your app runs.",
            ),
        ),
        conceptRules = listOf(
            ConceptRule(
                id = "l1_1_rule_val_first",
                title = "Reach for 'val' First",
                explanation = "In Kotlin, write 'val' by default and only switch to 'var' when a value truly changes (like a score counter or timer).",
                codeSnippet = "val heroName = \"Kova\"\nvar dailyXp = 40",
                whyItMatters = "When 90% of your boxes are sealed with 'val', bugs can only hide in the tiny handful of 'var' boxes.",
            ),
            ConceptRule(
                id = "l1_1_rule_update",
                title = "Updating a 'var' with +=",
                explanation = "Writing 'dailyXp += 15' is Kotlin shorthand for 'dailyXp = dailyXp + 15'—grab the current number, add 15, and store the new total back.",
                codeSnippet = "dailyXp += 15 // 40 becomes 55",
                whyItMatters = "Shorter, cleaner updates prevent typo bugs when counting points, steps, or streaks.",
            ),
            ConceptRule(
                id = "l1_1_rule_reassign_error",
                title = "Compiler Guardrail on 'val'",
                explanation = "If you try to reassign a 'val' later, Kotlin stops you BEFORE the app even runs with 'Val cannot be reassigned'.",
                codeSnippet = "val maxLessons = 3\n// maxLessons = 5  <- Compile error!",
                whyItMatters = "Catching mistakes while typing saves hours of hunting crashes on a real phone.",
            ),
        ),
        practiceTask = PracticeTaskSpec(
            title = "Hero XP Tracker",
            prompt = "Create an immutable heroName set to \"Kova\", a mutable dailyXp starting at 40, add a 15-point bonus, and print the updated status line.",
            desiredOutput = "Hero: Kova\nXP: 55",
            solutionCode = "val heroName = \"Kova\"\nvar dailyXp = 40\ndailyXp += 15\nprintln(\"Hero: \$heroName\")\nprintln(\"XP: \$dailyXp\")",
            lineNotes = listOf(
                PracticeSolutionNote(1, "'val' seals heroName so the learner's name never changes accidentally."),
                PracticeSolutionNote(2, "'var' makes dailyXp editable because XP will grow."),
                PracticeSolutionNote(3, "Adds 15 to 40 and writes 55 back into dailyXp."),
                PracticeSolutionNote(4, "Prints the first line with \$heroName filled in."),
                PracticeSolutionNote(5, "Prints the second line showing the updated 55 total."),
            ),
        ),
        debugger = DebuggerWalkthrough(
            title = "Live Trace: Hero XP Update",
            subtitle = "Watch val stay locked while var dailyXp transitions 40 -> 55.",
            lines = listOf(
                DebuggerCodeLine(1, "val heroName = \"Kova\"", CodeLineRole.DECLARATION),
                DebuggerCodeLine(2, "var dailyXp = 40", CodeLineRole.DECLARATION),
                DebuggerCodeLine(3, "val lessonBonus = 15", CodeLineRole.DECLARATION),
                DebuggerCodeLine(4, "dailyXp += lessonBonus", CodeLineRole.EXECUTION),
                DebuggerCodeLine(5, "println(\"Hero: \$heroName | XP: \$dailyXp\")", CodeLineRole.OUTPUT),
            ),
            steps = listOf(
                DebuggerStep(
                    stepIndex = 0,
                    activeLineNumber = 1,
                    headline = "Seal 'heroName' jar",
                    explanation = "Kotlin creates a read-only val locker named heroName holding \"Kova\".",
                    variables = listOf(
                        VariableWatchEntry("heroName", "String", "\"Kova\"", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 1,
                    activeLineNumber = 2,
                    headline = "Open 'dailyXp' whiteboard",
                    explanation = "Kotlin creates a changeable var box named dailyXp starting at 40.",
                    variables = listOf(
                        VariableWatchEntry("heroName", "String", "\"Kova\""),
                        VariableWatchEntry("dailyXp", "Int", "40", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 2,
                    activeLineNumber = 3,
                    headline = "Store fixed bonus",
                    explanation = "lessonBonus is a sealed val holding 15 points.",
                    variables = listOf(
                        VariableWatchEntry("heroName", "String", "\"Kova\""),
                        VariableWatchEntry("dailyXp", "Int", "40"),
                        VariableWatchEntry("lessonBonus", "Int", "15", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 3,
                    activeLineNumber = 4,
                    headline = "Update dailyXp: 40 -> 55",
                    explanation = "dailyXp += lessonBonus computes 40 + 15 and writes 55 onto the whiteboard.",
                    variables = listOf(
                        VariableWatchEntry("heroName", "String", "\"Kova\""),
                        VariableWatchEntry("dailyXp", "Int", "55", previousValue = "40", isChanged = true),
                        VariableWatchEntry("lessonBonus", "Int", "15"),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 4,
                    activeLineNumber = 5,
                    headline = "Print to Console",
                    explanation = "println reads both lockers and writes the formatted sentence to console output.",
                    variables = listOf(
                        VariableWatchEntry("heroName", "String", "\"Kova\""),
                        VariableWatchEntry("dailyXp", "Int", "55"),
                        VariableWatchEntry("lessonBonus", "Int", "15"),
                    ),
                    consoleOutput = listOf("Hero: Kova | XP: 55"),
                ),
            ),
        ),
        testQuestions = listOf(
            LessonTestQuestion(
                id = "q_1_1_1",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What prints to the console when this Kotlin snippet runs?",
                codeSnippet = "1 | var coins = 20\n2 | coins += 10\n3 | println(\"Coins: \$coins\")",
                options = listOf("Coins: 20", "Coins: 30", "Coins: 2010", "Compile error"),
                correctIndex = 1,
                plainEnglishExplanation = "'coins' starts at 20 as a mutable 'var', and 'coins += 10' updates it to 30 before println prints it.",
            ),
            LessonTestQuestion(
                id = "q_1_1_2",
                kind = LessonQuestionKind.SPOT_BUG,
                prompt = "Which line causes a Kotlin compile error?",
                codeSnippet = "1 | val lives = 3\n2 | var score = 100\n3 | lives = 2\n4 | score += 50",
                options = listOf("Line 1", "Line 2", "Line 3", "Line 4"),
                correctIndex = 2,
                plainEnglishExplanation = "Line 1 declared 'lives' with 'val' (sealed jar). Trying to change it to 2 on Line 3 breaks the read-only rule—use 'var' if it must change.",
            ),
            LessonTestQuestion(
                id = "q_1_1_3",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "Fill the blank so 'streak' can grow every morning when you complete a lesson:",
                codeSnippet = "1 | ___ streak = 1\n2 | streak += 1",
                options = listOf("val", "var", "const", "fix"),
                correctIndex = 1,
                plainEnglishExplanation = "Because 'streak' changes on Line 2 ('streak += 1'), you must use 'var' (mutable whiteboard).",
            ),
            LessonTestQuestion(
                id = "q_1_1_4",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What is stored in 'total' after Line 3 finishes?",
                codeSnippet = "1 | val base = 12\n2 | var total = base\n3 | total += 8",
                options = listOf("12", "8", "20", "Compile error because base is val"),
                correctIndex = 2,
                plainEnglishExplanation = "Copying a 'val' value into a 'var' is totally fine! 'total' gets 12, then adds 8 to become 20 while 'base' stays 12.",
            ),
            LessonTestQuestion(
                id = "q_1_1_5",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "In everyday English, what does 'immutable' mean for a Kotlin 'val'?",
                codeSnippet = null,
                options = listOf(
                    "It cannot be reassigned after you put a value in it",
                    "It can only hold numbers and never text",
                    "It erases itself after 60 seconds",
                    "It runs faster only on dark mode",
                ),
                correctIndex = 0,
                plainEnglishExplanation = "'Im-' means 'not' and 'mutable' means 'changeable'—an immutable 'val' locks its value in place.",
            ),
        ),
    )

    fun lesson1_2TypesAndStrings(): LessonInteractiveContent = LessonInteractiveContent(
        wordBreakdowns = listOf(
            WordBreakdown(
                id = "l1_2_wb_type",
                term = "Type",
                literalMeaning = "The exact category or mold a piece of data fits into (whole number, decimal, text, true/false).",
                tenYearOldAnalogy = "Think of a shape-sorter toy: Int is the square slot for whole counts (5), Double is the circle slot for decimals (4.5), and String is the long slot for words (\"Kova\").",
                highlightSnippet = "Every Kotlin variable has a strict Type (Int, Double, String, Boolean) checked at compile time.",
            ),
            WordBreakdown(
                id = "l1_2_wb_template",
                term = "String Template",
                literalMeaning = "A fill-in-the-blank sentence where \$ slots drop live variable values straight into text.",
                tenYearOldAnalogy = "Like a birthday invitation card with blank lines: \"Hi ____, you are ____ today!\" Kotlin fills in \$name and \${age + 1} automatically.",
                highlightSnippet = "Use \"\$name\" for one variable and \"\${expression}\" with braces for math or properties.",
            ),
            WordBreakdown(
                id = "l1_2_wb_nullable",
                term = "Nullable (?)",
                literalMeaning = "A box explicitly allowed to hold 'null' (intentionally empty — nothing inside yet).",
                tenYearOldAnalogy = "A normal String box MUST hold a postcard. Adding '?' (String?) puts a sticker on the box saying: 'It's okay if this box is empty today!'",
                highlightSnippet = "Add '?' (like String?) to allow null, and use the Elvis operator '?:' to supply a safe backup.",
            ),
        ),
        conceptRules = listOf(
            ConceptRule(
                id = "l1_2_rule_inference",
                title = "Type Inference vs. Explicit Types",
                explanation = "Kotlin guesses types automatically ('val streak = 5' is Int), or you can write ': Int' explicitly when you want extra clarity.",
                codeSnippet = "val streak: Int = 5\nval rating: Double = 4.8\nval active: Boolean = true",
                whyItMatters = "Once a box is typed as Int, Kotlin stops you from accidentally shoving text like \"five\" into it.",
            ),
            ConceptRule(
                id = "l1_2_rule_braces",
                title = "Curly Braces for Math Inside Strings",
                explanation = "Write \$streak for a simple name, but wrap any math or property inside \${...} so Kotlin evaluates the whole expression first.",
                codeSnippet = "val msg = \"Next: \${streak + 1}d\"",
                whyItMatters = "Without braces, \"\$streak+1\" prints \"5+1\" literally instead of \"6\"!",
            ),
            ConceptRule(
                id = "l1_2_rule_elvis",
                title = "Safe Fallback with Elvis (? :)",
                explanation = "When a nullable variable (String?) might be null, 'tip ?: \"Keep going!\"' picks the tip if present or the backup message if empty.",
                codeSnippet = "val tip: String? = null\nval shown = tip ?: \"Keep going!\"",
                whyItMatters = "Prevents the infamous NullPointerException crash that breaks apps on users' phones.",
            ),
        ),
        practiceTask = PracticeTaskSpec(
            title = "Streak Badge Formatter",
            prompt = "Declare streak = 5, build a badge string using a template with \${streak + 1} for tomorrow's goal, and safely fall back a null tip to \"Keep going!\".",
            desiredOutput = "Streak: 5d -> Goal: 6d\nTip: Keep going!",
            solutionCode = "val streak: Int = 5\nval tip: String? = null\nval safeTip = tip ?: \"Keep going!\"\nprintln(\"Streak: \${streak}d -> Goal: \${streak + 1}d\")\nprintln(\"Tip: \$safeTip\")",
            lineNotes = listOf(
                PracticeSolutionNote(1, "Stores whole number 5 in an Int."),
                PracticeSolutionNote(2, "Marks tip as String? so holding null is safe and legal."),
                PracticeSolutionNote(3, "Elvis operator '?:' replaces null with \"Keep going!\"."),
                PracticeSolutionNote(4, "Uses \${streak} before 'd' and \${streak + 1} to calculate 6."),
                PracticeSolutionNote(5, "Prints the non-null safeTip cleanly."),
            ),
        ),
        debugger = DebuggerWalkthrough(
            title = "Live Trace: Templates & Safe Null",
            subtitle = "See how Kotlin infers types, resolves \${...}, and applies Elvis fallback.",
            lines = listOf(
                DebuggerCodeLine(1, "val streak: Int = 5", CodeLineRole.DECLARATION),
                DebuggerCodeLine(2, "val badge = \"Streak: \${streak}d\"", CodeLineRole.EXECUTION),
                DebuggerCodeLine(3, "val nextGoal = streak + 1", CodeLineRole.EXECUTION),
                DebuggerCodeLine(4, "val tip: String? = null", CodeLineRole.DECLARATION),
                DebuggerCodeLine(5, "val shownTip = tip ?: \"Keep going!\"", CodeLineRole.DECISION),
                DebuggerCodeLine(6, "println(\"\$badge | Goal: \$nextGoal | \$shownTip\")", CodeLineRole.OUTPUT),
            ),
            steps = listOf(
                DebuggerStep(
                    stepIndex = 0,
                    activeLineNumber = 1,
                    headline = "Create Int 'streak'",
                    explanation = "streak stores whole number 5 in an Int slot.",
                    variables = listOf(
                        VariableWatchEntry("streak", "Int", "5", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 1,
                    activeLineNumber = 2,
                    headline = "Assemble String template",
                    explanation = "Kotlin substitutes \${streak} with 5 and glues 'd' right after it -> \"Streak: 5d\".",
                    variables = listOf(
                        VariableWatchEntry("streak", "Int", "5"),
                        VariableWatchEntry("badge", "String", "\"Streak: 5d\"", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 2,
                    activeLineNumber = 3,
                    headline = "Compute nextGoal",
                    explanation = "Reads streak (5), adds 1, and stores 6 in nextGoal.",
                    variables = listOf(
                        VariableWatchEntry("streak", "Int", "5"),
                        VariableWatchEntry("badge", "String", "\"Streak: 5d\""),
                        VariableWatchEntry("nextGoal", "Int", "6", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 3,
                    activeLineNumber = 4,
                    headline = "Create empty Nullable box",
                    explanation = "Because of 'String?', Kotlin allows 'tip' to start as null (empty).",
                    variables = listOf(
                        VariableWatchEntry("streak", "Int", "5"),
                        VariableWatchEntry("badge", "String", "\"Streak: 5d\""),
                        VariableWatchEntry("nextGoal", "Int", "6"),
                        VariableWatchEntry("tip", "String?", "null", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 4,
                    activeLineNumber = 5,
                    headline = "Elvis '?:' picks fallback",
                    explanation = "'tip' is null, so '?:' safely hands \"Keep going!\" into non-null shownTip.",
                    variables = listOf(
                        VariableWatchEntry("streak", "Int", "5"),
                        VariableWatchEntry("badge", "String", "\"Streak: 5d\""),
                        VariableWatchEntry("nextGoal", "Int", "6"),
                        VariableWatchEntry("tip", "String?", "null"),
                        VariableWatchEntry("shownTip", "String", "\"Keep going!\"", previousValue = "null", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 5,
                    activeLineNumber = 6,
                    headline = "Print complete summary",
                    explanation = "println outputs all three pieces cleanly with zero null crashes.",
                    variables = listOf(
                        VariableWatchEntry("streak", "Int", "5"),
                        VariableWatchEntry("badge", "String", "\"Streak: 5d\""),
                        VariableWatchEntry("nextGoal", "Int", "6"),
                        VariableWatchEntry("tip", "String?", "null"),
                        VariableWatchEntry("shownTip", "String", "\"Keep going!\""),
                    ),
                    consoleOutput = listOf("Streak: 5d | Goal: 6 | Keep going!"),
                ),
            ),
        ),
        testQuestions = listOf(
            LessonTestQuestion(
                id = "q_1_2_1",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What does this String template print?",
                codeSnippet = "1 | val level = 4\n2 | println(\"Next: \${level + 1}\")",
                options = listOf("Next: 4+1", "Next: 5", "Next: level + 1", "Compile error"),
                correctIndex = 1,
                plainEnglishExplanation = "Curly braces \${level + 1} tell Kotlin to run the math (4 + 1 = 5) before inserting the result into the text.",
            ),
            LessonTestQuestion(
                id = "q_1_2_2",
                kind = LessonQuestionKind.SPOT_BUG,
                prompt = "Which line breaks Kotlin's null-safety rule?",
                codeSnippet = "1 | val name: String = \"Kova\"\n2 | val hint: String? = null\n3 | val title: String = null\n4 | println(name)",
                options = listOf("Line 1", "Line 2", "Line 3", "Line 4"),
                correctIndex = 2,
                plainEnglishExplanation = "Line 3 uses 'String' without a '?' question mark, so Kotlin forbids storing 'null' in 'title'.",
            ),
            LessonTestQuestion(
                id = "q_1_2_3",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "Fill the blank to give 'nickname' a safe backup when it is null:",
                codeSnippet = "1 | val nickname: String? = null\n2 | val display = nickname ___ \"Learner\"",
                options = listOf("?:", "==", "++", ".."),
                correctIndex = 0,
                plainEnglishExplanation = "The Elvis operator '?:' means: if the left side is null, use the safe fallback on the right.",
            ),
            LessonTestQuestion(
                id = "q_1_2_4",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "Look closely—there are NO curly braces here! What prints?",
                codeSnippet = "1 | val xp = 10\n2 | println(\"\$xp+5\")",
                options = listOf("15", "10+5", "xp+5", "Compile error"),
                correctIndex = 1,
                plainEnglishExplanation = "Without braces, Kotlin only fills in '\$xp' (10) and treats '+5' as ordinary text, printing '10+5'. Use \"\${xp + 5}\" for 15!",
            ),
            LessonTestQuestion(
                id = "q_1_2_5",
                kind = LessonQuestionKind.SPOT_BUG,
                prompt = "Which line has a Type mismatch error?",
                codeSnippet = "1 | val score: Int = 90\n2 | val average: Double = 88.5\n3 | val badge: Int = \"Gold\"\n4 | val ok: Boolean = true",
                options = listOf("Line 1", "Line 2", "Line 3", "Line 4"),
                correctIndex = 2,
                plainEnglishExplanation = "Line 3 tries to put a String (\"Gold\") inside an Int (whole-number) slot—that's putting a text strip into a number shape-sorter!",
            ),
        ),
    )

    fun lesson1_3OperatorsAndRanges(): LessonInteractiveContent = LessonInteractiveContent(
        wordBreakdowns = listOf(
            WordBreakdown(
                id = "l1_3_wb_operator",
                term = "Operator",
                literalMeaning = "A symbol (+, -, *, /, %, >=) that operates on values to produce a new result.",
                tenYearOldAnalogy = "Like buttons on a pocket calculator or a referee's whistle: '+' combines numbers, '%' tells you the leftover slices after sharing pizza equally.",
                highlightSnippet = "Int division '/' drops decimals (85 / 10 == 8), while '%' gives the remainder (85 % 10 == 5).",
            ),
            WordBreakdown(
                id = "l1_3_wb_range",
                term = "Range (..)",
                literalMeaning = "A continuous span of values from a start point to an end point, inclusive of both ends.",
                tenYearOldAnalogy = "Look at 80 to 100 on a wooden ruler. Writing 'score in 80..100' simply asks: 'Does this score land anywhere on that ruler segment, including 80 and 100?'",
                highlightSnippet = "'in 80..100' checks an inclusive range cleanly—both 80 and 100 count as inside.",
            ),
            WordBreakdown(
                id = "l1_3_wb_until",
                term = "Exclusive (until)",
                literalMeaning = "Counts up to—but stops 1 short of—the upper number.",
                tenYearOldAnalogy = "A 4-car train has cars numbered 0, 1, 2, 3. '0 until 4' walks through all 4 cars and stops cleanly before stepping off the end onto nonexistent car #4!",
                highlightSnippet = "'0 until size' excludes the upper bound—essential for preventing off-by-one list crashes.",
            ),
        ),
        conceptRules = listOf(
            ConceptRule(
                id = "l1_3_rule_int_div",
                title = "Whole-Number Division & Remainder",
                explanation = "Dividing two Ints (85 / 10) keeps only the whole count (8). Use modulo '%' (85 % 10) to grab the leftover 5.",
                codeSnippet = "val rank = 85 / 10      // 8\nval leftover = 85 % 10  // 5",
                whyItMatters = "Great for converting seconds into minutes + leftover seconds (e.g., 75s = 1m 15s).",
            ),
            ConceptRule(
                id = "l1_3_rule_in_range",
                title = "Readable 'in' Range Checks",
                explanation = "Instead of writing 'score >= 80 && score <= 100', Kotlin lets you read it like English: 'score in 80..100'.",
                codeSnippet = "val passed = score in 80..100",
                whyItMatters = "Easier to read at a glance and avoids flipping '>' and '<' signs by mistake.",
            ),
            ConceptRule(
                id = "l1_3_rule_until",
                title = "'..' Includes End, 'until' Stops Before End",
                explanation = "'0..3' has 4 numbers (0, 1, 2, 3). '0 until 4' ALSO has those exact 4 numbers (0, 1, 2, 3)—never reaching 4.",
                codeSnippet = "val validIndex = idx in 0 until 4",
                whyItMatters = "Lists start at index 0, so checking '0..list.size' is a classic beginner crash bug.",
            ),
        ),
        practiceTask = PracticeTaskSpec(
            title = "Score & Rank Calculator",
            prompt = "Given score = 85, check if it falls in the passing range 80..100, compute rank = score / 10 and remainder = score % 10, then print the verdict.",
            desiredOutput = "Passed: true\nRank: 8 (Extra: 5)",
            solutionCode = "val score = 85\nval passed = score in 80..100\nval rank = score / 10\nval extra = score % 10\nprintln(\"Passed: \$passed\")\nprintln(\"Rank: \$rank (Extra: \$extra)\")",
            lineNotes = listOf(
                PracticeSolutionNote(1, "Sets score to 85."),
                PracticeSolutionNote(2, "Checks inclusive range 80..100 -> evaluates to true."),
                PracticeSolutionNote(3, "Whole division 85 / 10 produces 8."),
                PracticeSolutionNote(4, "Remainder 85 % 10 produces 5."),
                PracticeSolutionNote(5, "Prints Passed: true."),
                PracticeSolutionNote(6, "Prints Rank: 8 (Extra: 5)."),
            ),
        ),
        debugger = DebuggerWalkthrough(
            title = "Live Trace: Range & Int Math",
            subtitle = "Step through inclusive ranges, integer division, and safe exclusive index bounds.",
            lines = listOf(
                DebuggerCodeLine(1, "val score = 85", CodeLineRole.DECLARATION),
                DebuggerCodeLine(2, "val passed = score in 80..100", CodeLineRole.DECISION),
                DebuggerCodeLine(3, "val rank = score / 10", CodeLineRole.EXECUTION),
                DebuggerCodeLine(4, "val extra = score % 10", CodeLineRole.EXECUTION),
                DebuggerCodeLine(5, "val inSlots = 3 in 0 until 4", CodeLineRole.DECISION),
                DebuggerCodeLine(6, "println(\"Pass=\$passed Rank=\$rank Extra=\$extra\")", CodeLineRole.OUTPUT),
            ),
            steps = listOf(
                DebuggerStep(
                    stepIndex = 0,
                    activeLineNumber = 1,
                    headline = "Store score = 85",
                    explanation = "Creates immutable Int score holding 85.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "85", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 1,
                    activeLineNumber = 2,
                    headline = "Check 85 in 80..100",
                    explanation = "85 sits between 80 and 100 inclusive, so 'passed' becomes true.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "85"),
                        VariableWatchEntry("passed", "Boolean", "true", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 2,
                    activeLineNumber = 3,
                    headline = "Whole division: 85 / 10",
                    explanation = "Int division drops the .5 decimal, storing whole number 8 in rank.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "85"),
                        VariableWatchEntry("passed", "Boolean", "true"),
                        VariableWatchEntry("rank", "Int", "8", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 3,
                    activeLineNumber = 4,
                    headline = "Remainder: 85 % 10",
                    explanation = "10 fits into 85 eight times (80) with 5 left over -> extra = 5.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "85"),
                        VariableWatchEntry("passed", "Boolean", "true"),
                        VariableWatchEntry("rank", "Int", "8"),
                        VariableWatchEntry("extra", "Int", "5", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 4,
                    activeLineNumber = 5,
                    headline = "Check exclusive '0 until 4'",
                    explanation = "0 until 4 contains 0, 1, 2, 3. Since 3 is inside, inSlots is true!",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "85"),
                        VariableWatchEntry("passed", "Boolean", "true"),
                        VariableWatchEntry("rank", "Int", "8"),
                        VariableWatchEntry("extra", "Int", "5"),
                        VariableWatchEntry("inSlots", "Boolean", "true", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 5,
                    activeLineNumber = 6,
                    headline = "Print final math output",
                    explanation = "Writes the evaluated Boolean and Int values to the console.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "85"),
                        VariableWatchEntry("passed", "Boolean", "true"),
                        VariableWatchEntry("rank", "Int", "8"),
                        VariableWatchEntry("extra", "Int", "5"),
                        VariableWatchEntry("inSlots", "Boolean", "true"),
                    ),
                    consoleOutput = listOf("Pass=true Rank=8 Extra=5"),
                ),
            ),
        ),
        testQuestions = listOf(
            LessonTestQuestion(
                id = "q_1_3_1",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What does whole-number Int division print here?",
                codeSnippet = "1 | val score = 85\n2 | println(score / 10)",
                options = listOf("8.5", "8", "9", "5"),
                correctIndex = 1,
                plainEnglishExplanation = "Dividing two Ints drops the fractional part without rounding up, so 85 / 10 equals 8.",
            ),
            LessonTestQuestion(
                id = "q_1_3_2",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What Boolean value prints for '79 in 80..100'?",
                codeSnippet = "1 | val score = 79\n2 | val passed = score in 80..100\n3 | println(passed)",
                options = listOf("true", "false", "80", "Compile error"),
                correctIndex = 1,
                plainEnglishExplanation = "The range 80..100 starts at 80. Because 79 is below 80, '79 in 80..100' is false.",
            ),
            LessonTestQuestion(
                id = "q_1_3_3",
                kind = LessonQuestionKind.SPOT_BUG,
                prompt = "A list has 4 lessons (indices 0, 1, 2, 3). Which line can step out of bounds to index 4?",
                codeSnippet = "1 | val count = 4\n2 | val safeRange = 0 until count\n3 | val riskyRange = 0..count\n4 | println(3 in safeRange)",
                options = listOf("Line 1", "Line 2", "Line 3", "Line 4"),
                correctIndex = 2,
                plainEnglishExplanation = "'0..4' on Line 3 includes 4 (five slots: 0, 1, 2, 3, 4), which crashes a 4-item list! Use '0 until count' instead.",
            ),
            LessonTestQuestion(
                id = "q_1_3_4",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "Fill the keyword that checks if 'age' sits inside the range 10..18:",
                codeSnippet = "1 | val isTeen = age ___ 10..18",
                options = listOf("in", "at", "has", "of"),
                correctIndex = 0,
                plainEnglishExplanation = "Kotlin's 'in' operator checks whether a value sits inside a range cleanly.",
            ),
            LessonTestQuestion(
                id = "q_1_3_5",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What is the remainder printed by the modulo '%' operator?",
                codeSnippet = "1 | val minutes = 75 % 60\n2 | println(minutes)",
                options = listOf("1", "15", "60", "1.25"),
                correctIndex = 1,
                plainEnglishExplanation = "60 goes into 75 once, leaving 15 left over—so 75 % 60 evaluates to 15.",
            ),
        ),
    )

    fun lesson1_4ControlDecisions(): LessonInteractiveContent = LessonInteractiveContent(
        wordBreakdowns = listOf(
            WordBreakdown(
                id = "l1_4_wb_condition",
                term = "Condition",
                literalMeaning = "A yes-or-no test (Boolean) that decides which path the program takes next.",
                tenYearOldAnalogy = "Like a theme-park height sign: 'Are you at least 120cm tall?' Yes (true) -> roller coaster gate opens; No (false) -> go to the carousel.",
                highlightSnippet = "A condition evaluates to true or false and steers execution onto the matching path.",
            ),
            WordBreakdown(
                id = "l1_4_wb_branch",
                term = "Branch",
                literalMeaning = "One specific path the code can follow when its condition matches.",
                tenYearOldAnalogy = "Picture a train track switchboard: one train enters ('score'), and the switch routes it cleanly onto the Gold track, Mint track, or Retry track.",
                highlightSnippet = "Kotlin's 'when' evaluates branches top-to-bottom and runs ONLY the first matching branch.",
            ),
            WordBreakdown(
                id = "l1_4_wb_expression",
                term = "Expression",
                literalMeaning = "A piece of code that hands back (returns) an actual value you can store in a 'val'.",
                tenYearOldAnalogy = "A vending machine: you press a button (input), and a snack (value) drops into your hand. Both 'if' and 'when' hand back a value directly!",
                highlightSnippet = "In Kotlin, 'if' and 'when' are expressions—you can write 'val tier = when (score) { ... }'.",
            ),
        ),
        conceptRules = listOf(
            ConceptRule(
                id = "l1_4_rule_if_expr",
                title = "'if' Returns a Value Directly",
                explanation = "You don't need a mutable 'var' just to pick between two labels—assign 'val status = if (passed) \"Unlocked\" else \"Retry\"' directly.",
                codeSnippet = "val status = if (score >= 80) \"Pass\" else \"Retry\"",
                whyItMatters = "Keeps your variables 'val' (sealed) instead of creating temporary empty 'var' placeholders.",
            ),
            ConceptRule(
                id = "l1_4_rule_when",
                title = "Clean Multi-Way 'when' Switchboard",
                explanation = "'when (score)' replaces messy nested if-else ladders with crisp 'condition -> result' arrows that pair naturally with ranges.",
                codeSnippet = "val tier = when (score) {\n  in 90..100 -> \"Gold\"\n  in 80..89 -> \"Mint\"\n  else -> \"Retry\"\n}",
                whyItMatters = "Every possible score tier reads like a neat table, making edge-case bugs immediately obvious.",
            ),
            ConceptRule(
                id = "l1_4_rule_else",
                title = "Mandatory 'else' for Value-Returning 'when'",
                explanation = "Whenever 'val tier = when (score)' returns a value for an Int, Kotlin REQUIRES an 'else ->' fallback branch so 'tier' is never left blank.",
                codeSnippet = "else -> \"Retry\" // Guarantees tier always gets a value!",
                whyItMatters = "Forgetting 'else' is the #1 Kotlin beginner compiler error when assigning 'when' to a variable.",
            ),
        ),
        practiceTask = PracticeTaskSpec(
            title = "Mastery Tier Switchboard",
            prompt = "Classify score = 86 into \"Gold\" (90..100), \"Mint\" (80..89), or \"Retry\" (else) using a 'when' expression, then print the tier and unlock status.",
            desiredOutput = "Tier: Mint\nGate: Next Lesson Unlocked!",
            solutionCode = "val score = 86\nval tier = when (score) {\n  in 90..100 -> \"Gold\"\n  in 80..89 -> \"Mint\"\n  else -> \"Retry\"\n}\nval gate = if (score >= 80) \"Next Lesson Unlocked!\" else \"Keep Practicing\"\nprintln(\"Tier: \$tier\")\nprintln(\"Gate: \$gate\")",
            lineNotes = listOf(
                PracticeSolutionNote(1, "Stores test score 86."),
                PracticeSolutionNote(2, "Starts a value-returning 'when' expression inspecting score."),
                PracticeSolutionNote(3, "86 is not in 90..100, so Gold is skipped."),
                PracticeSolutionNote(4, "86 matches 80..89, returning \"Mint\" into tier!"),
                PracticeSolutionNote(5, "'else' satisfies Kotlin's exhaustiveness guarantee."),
                PracticeSolutionNote(7, "'if' expression computes the gate banner in one clean line."),
                PracticeSolutionNote(8, "Prints Tier: Mint and Gate: Next Lesson Unlocked!"),
            ),
        ),
        debugger = DebuggerWalkthrough(
            title = "Live Trace: 'when' Switchboard",
            subtitle = "Follow the train switch as score = 86 skips Gold and locks onto Mint.",
            lines = listOf(
                DebuggerCodeLine(1, "val score = 86", CodeLineRole.DECLARATION),
                DebuggerCodeLine(2, "val tier = when (score) {", CodeLineRole.DECISION),
                DebuggerCodeLine(3, "  in 90..100 -> \"Gold\"", CodeLineRole.DECISION),
                DebuggerCodeLine(4, "  in 80..89 -> \"Mint\"", CodeLineRole.DECISION),
                DebuggerCodeLine(5, "  else -> \"Retry\" }", CodeLineRole.DECISION),
                DebuggerCodeLine(6, "val gate = if (score >= 80) \"Unlocked\" else \"Locked\"", CodeLineRole.EXECUTION),
                DebuggerCodeLine(7, "println(\"Tier: \$tier | Gate: \$gate\")", CodeLineRole.OUTPUT),
            ),
            steps = listOf(
                DebuggerStep(
                    stepIndex = 0,
                    activeLineNumber = 1,
                    headline = "Set score = 86",
                    explanation = "Creates immutable score = 86 to feed into the switchboard.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "86", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 1,
                    activeLineNumber = 2,
                    headline = "Enter 'when (score)'",
                    explanation = "Kotlin inspects score (86) and starts testing branches from top to bottom.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "86"),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 2,
                    activeLineNumber = 3,
                    headline = "Check 86 in 90..100 -> false",
                    explanation = "86 is below 90, so Kotlin skips \"Gold\" and moves to the next branch.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "86"),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 3,
                    activeLineNumber = 4,
                    headline = "Match! 86 in 80..89 -> \"Mint\"",
                    explanation = "Branch matches! 'when' immediately returns \"Mint\" into tier and skips 'else'.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "86"),
                        VariableWatchEntry("tier", "String", "\"Mint\"", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 4,
                    activeLineNumber = 6,
                    headline = "Evaluate 'if' expression",
                    explanation = "86 >= 80 is true, so the 'if' expression returns \"Unlocked\" directly into gate.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "86"),
                        VariableWatchEntry("tier", "String", "\"Mint\""),
                        VariableWatchEntry("gate", "String", "\"Unlocked\"", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 5,
                    activeLineNumber = 7,
                    headline = "Print final decision",
                    explanation = "Console displays the chosen tier and gate status.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "86"),
                        VariableWatchEntry("tier", "String", "\"Mint\""),
                        VariableWatchEntry("gate", "String", "\"Unlocked\""),
                    ),
                    consoleOutput = listOf("Tier: Mint | Gate: Unlocked"),
                ),
            ),
        ),
        testQuestions = listOf(
            LessonTestQuestion(
                id = "q_1_4_1",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What prints when score is 94?",
                codeSnippet = "1 | val score = 94\n2 | val tier = when (score) {\n3 |   in 90..100 -> \"Gold\"\n4 |   in 80..89 -> \"Mint\"\n5 |   else -> \"Retry\"\n6 | }\n7 | println(tier)",
                options = listOf("Gold", "Mint", "Retry", "GoldMint"),
                correctIndex = 0,
                plainEnglishExplanation = "94 matches the very first branch 'in 90..100', so 'when' immediately returns \"Gold\" and stops checking.",
            ),
            LessonTestQuestion(
                id = "q_1_4_2",
                kind = LessonQuestionKind.SPOT_BUG,
                prompt = "Why will this code fail to compile on Line 2?",
                codeSnippet = "1 | val score = 85\n2 | val badge = when (score) {\n3 |   in 80..100 -> \"Pass\"\n4 | }",
                options = listOf(
                    "Missing an 'else ->' branch for scores outside 80..100",
                    "'when' cannot use ranges like 80..100",
                    "'badge' must be declared as 'var'",
                    "Strings cannot be returned from 'when'",
                ),
                correctIndex = 0,
                plainEnglishExplanation = "Whenever 'when' assigns a value to a variable ('val badge = when...'), Kotlin requires an 'else ->' fallback so every possible Int produces a value.",
            ),
            LessonTestQuestion(
                id = "q_1_4_3",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "Fill the keyword to route 'day' cleanly to its schedule label:",
                codeSnippet = "1 | val plan = ___ (day) {\n2 |   \"Sat\", \"Sun\" -> \"Rest\"\n3 |   else -> \"Study\"\n4 | }",
                options = listOf("when", "loop", "case", "var"),
                correctIndex = 0,
                plainEnglishExplanation = "Kotlin uses 'when (value) { ... }' as its clean multi-branch switchboard.",
            ),
            LessonTestQuestion(
                id = "q_1_4_4",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What does this single-line 'if' expression store in 'msg'?",
                codeSnippet = "1 | val left = 0\n2 | val msg = if (left > 0) \"Go\" else \"Limit\"\n3 | println(msg)",
                options = listOf("Go", "Limit", "0", "Compile error"),
                correctIndex = 1,
                plainEnglishExplanation = "'left > 0' is false (0 is not greater than 0), so the 'else' branch runs and returns \"Limit\".",
            ),
            LessonTestQuestion(
                id = "q_1_4_5",
                kind = LessonQuestionKind.SPOT_BUG,
                prompt = "A learner with score = 95 got \"Pass\" instead of \"Gold\"! Which line caused this logic bug?",
                codeSnippet = "1 | val score = 95\n2 | val tier = when (score) {\n3 |   in 80..100 -> \"Pass\"\n4 |   in 90..100 -> \"Gold\"\n5 |   else -> \"Retry\"\n6 | }",
                options = listOf(
                    "Line 3 catches 95 first before Line 4 is ever checked",
                    "Line 1 cannot hold 95",
                    "Line 5 should come first",
                    "Line 4 has invalid range syntax",
                ),
                correctIndex = 0,
                plainEnglishExplanation = "'when' stops at the FIRST matching branch! Put the narrower high range ('in 90..100 -> \"Gold\"') above 'in 80..89' so 95 hits Gold first.",
            ),
        ),
    )

    /**
     * Graceful fallback for Chapter 2 preview lessons unlocked via Dev Override.
     */
    fun fallbackForLesson(lesson: Lesson): LessonInteractiveContent = LessonInteractiveContent(
        wordBreakdowns = listOf(
            WordBreakdown(
                id = "${lesson.id}_wb_main",
                term = lesson.title,
                literalMeaning = lesson.subtitle,
                tenYearOldAnalogy = lesson.analogyTeaser,
                highlightSnippet = "${lesson.title}: ${lesson.analogyTeaser}",
            ),
        ),
        conceptRules = listOf(
            ConceptRule(
                id = "${lesson.id}_rule_main",
                title = "Core Pattern · Lesson ${lesson.code}",
                explanation = lesson.subtitle,
                codeSnippet = lesson.codePreview,
                whyItMatters = "Master Chapter 1 foundations first, then build reusable Kotlin functions in Chapter 2.",
            ),
        ),
        practiceTask = PracticeTaskSpec(
            title = "Preview Pattern",
            prompt = "Inspect the Kotlin pattern for ${lesson.title} and trace how the values flow.",
            desiredOutput = "Ready (${lesson.code})",
            solutionCode = lesson.codePreview,
            lineNotes = listOf(
                PracticeSolutionNote(1, "Defines the core Kotlin construct introduced in Lesson ${lesson.code}."),
            ),
        ),
        debugger = DebuggerWalkthrough(
            title = "Guided Preview · ${lesson.code}",
            subtitle = "Step through the preview lines for ${lesson.title}.",
            lines = lesson.codePreview.lines().mapIndexed { idx, line ->
                DebuggerCodeLine(
                    lineNumber = idx + 1,
                    code = line,
                    role = if (idx == 0) CodeLineRole.DECLARATION else CodeLineRole.EXECUTION,
                )
            },
            steps = lesson.codePreview.lines().mapIndexed { idx, line ->
                DebuggerStep(
                    stepIndex = idx,
                    activeLineNumber = idx + 1,
                    headline = "Line ${idx + 1}",
                    explanation = "Executes: ${line.trim()}",
                    variables = listOf(
                        VariableWatchEntry("lesson", "String", "\"${lesson.code}\"", isChanged = idx == 0),
                    ),
                    consoleOutput = if (idx == lesson.codePreview.lines().lastIndex) listOf("Ready (${lesson.code})") else emptyList(),
                )
            },
        ),
        testQuestions = listOf(
            LessonTestQuestion(
                id = "${lesson.id}_q1",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "Which keyword declares a function in Kotlin?",
                codeSnippet = "1 | ___ greet() = \"Hi\"",
                options = listOf("fun", "def", "fn", "func"),
                correctIndex = 0,
                plainEnglishExplanation = "Kotlin uses the short keyword 'fun' to declare every function.",
            ),
            LessonTestQuestion(
                id = "${lesson.id}_q2",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "In Kotlin, default parameter values help you avoid:",
                codeSnippet = null,
                options = listOf(
                    "Writing many repetitive overloaded functions",
                    "Using variables at all",
                    "Checking types",
                    "Printing to the console",
                ),
                correctIndex = 0,
                plainEnglishExplanation = "Default values let callers omit optional arguments instead of maintaining many duplicate overloads.",
            ),
            LessonTestQuestion(
                id = "${lesson.id}_q3",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What does listOf(72, 85, 94).filter { it >= 80 } return?",
                codeSnippet = "1 | val passed = listOf(72, 85, 94).filter { it >= 80 }",
                options = listOf("[85, 94]", "[72]", "[72, 85, 94]", "[]"),
                correctIndex = 0,
                plainEnglishExplanation = "'filter' keeps only elements matching the condition (>= 80), which are 85 and 94.",
            ),
            LessonTestQuestion(
                id = "${lesson.id}_q4",
                kind = LessonQuestionKind.SPOT_BUG,
                prompt = "Which line has an invalid function keyword in Kotlin?",
                codeSnippet = "1 | fun ok(x: Int) = x * 2\n2 | function bad(x: Int) = x + 1",
                options = listOf("Line 1", "Line 2", "Both lines", "Neither line"),
                correctIndex = 1,
                plainEnglishExplanation = "Kotlin uses 'fun', not 'function'—so Line 2 fails to compile.",
            ),
            LessonTestQuestion(
                id = "${lesson.id}_q5",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "Inside a single-parameter lambda like { it >= 80 }, what does 'it' represent?",
                codeSnippet = null,
                options = listOf(
                    "The current item being checked",
                    "The whole app state",
                    "An error code",
                    "A constant 0",
                ),
                correctIndex = 0,
                plainEnglishExplanation = "When a lambda takes one parameter, Kotlin names it 'it' automatically for clean, short expressions.",
            ),
        ),
    )
}
