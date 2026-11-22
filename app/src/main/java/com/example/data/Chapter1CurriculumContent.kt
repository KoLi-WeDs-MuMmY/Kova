package com.example.data

import com.example.model.CodeLineRole
import com.example.model.ConceptRule
import com.example.model.DebuggerCodeLine
import com.example.model.DebuggerStep
import com.example.model.DebuggerWalkthrough
import com.example.model.ExplainDeepDive
import com.example.model.HighlightCategory
import com.example.model.Lesson
import com.example.model.LessonInteractiveContent
import com.example.model.LessonQuestionKind
import com.example.model.LessonTestQuestion
import com.example.model.PracticeSolutionNote
import com.example.model.PracticeTaskSpec
import com.example.model.PreHighlightedTakeaway
import com.example.model.SymbolBreakdown
import com.example.model.VariableWatchEntry
import com.example.model.WordBreakdown

/**
 * Full Phase 2.1 interactive curriculum for all 4 Chapter 1 lessons:
 * - 1.1 Words & Variables (strictly val vs var & storing single values — zero compound operators)
 * - 1.2 Types & Strings (shapes of data, string templates, safe null thinking)
 * - 1.3 Operators & Ranges (+=, /, %, !=, inclusive .., exclusive until)
 * - 1.4 Control Decisions (if and when expressions with plain-English conditions)
 *
 * Every lesson includes:
 * - Plain-English Word Breakdowns
 * - Pre-Highlighted Key Takeaways
 * - Crisp 10-year-old Concept Summaries + warm 1-on-1 Conversational "Explain ->" Deep-Dives
 * - Practice Goal + Desired Output + Collapsible "Show Solution" + IDE Starter Code
 * - Step-by-Step Debugger Walkthrough (behind "Unlock Debugger")
 * - 5-Question Closed-Book Integrated Test
 */
object Chapter1CurriculumContent {

    fun lesson1_1WordsAndVariables(): LessonInteractiveContent = LessonInteractiveContent(
        wordBreakdowns = listOf(
            WordBreakdown(
                id = "l1_1_wb_variable",
                term = "Variable",
                literalMeaning = "A named storage box in memory that holds one value at a time.",
                tenYearOldAnalogy = "Picture a wall of school lockers with name tags on the doors. A variable is just one labeled locker holding a single value for your app.",
                highlightSnippet = "A variable is a labeled memory locker that stores one piece of data by name.",
            ),
            WordBreakdown(
                id = "l1_1_wb_immutable",
                term = "Immutable (val)",
                literalMeaning = "'Im-' (not) + 'mutable' (changeable) = sealed once you put a value inside.",
                tenYearOldAnalogy = "A sealed glass jar labeled heroName. Once you drop \"Kova\" inside and snap the lid shut, nobody can swap it for another name.",
                highlightSnippet = "'val' creates an immutable read-only label—once assigned, you cannot replace its value.",
            ),
            WordBreakdown(
                id = "l1_1_wb_mutable",
                term = "Mutable (var)",
                literalMeaning = "Able to change—you can erase the old value and store a brand-new one.",
                tenYearOldAnalogy = "A mini whiteboard labeled dailyXp. You can write 40 now, wipe it clean after finishing a lesson, and write 55.",
                highlightSnippet = "'var' creates a mutable box whose stored value can be replaced later.",
            ),
        ),
        preHighlightedTakeaways = listOf(
            PreHighlightedTakeaway(
                id = "l1_1_takeaway_1",
                category = HighlightCategory.KEY_IDEA,
                headline = "Start with val, switch to var only when needed",
                body = "Use val for facts that stay fixed (like a hero's name). Use var only for values that must be replaced later (like a live score).",
            ),
            PreHighlightedTakeaway(
                id = "l1_1_takeaway_2",
                category = HighlightCategory.CODE_RULE,
                headline = "A single = means 'store this value inside'",
                body = "In Kotlin, dailyXp = 55 does not ask a math question—it tells the device: put the number 55 inside the locker named dailyXp.",
            ),
            PreHighlightedTakeaway(
                id = "l1_1_takeaway_3",
                category = HighlightCategory.GOTCHA,
                headline = "Reassigning a val stops your code before it runs",
                body = "If you try to store a new value inside a val, Kotlin refuses to build and protects you from accidental state bugs.",
            ),
        ),
        conceptRules = listOf(
            ConceptRule(
                id = "l1_1_rule_val_first",
                title = "Seal Fixed Values with val",
                explanation = "Write val whenever a single piece of information should stay locked after you first store it.",
                codeSnippet = "val heroName = \"Kova\"\nval dailyLimit = 3",
                whyItMatters = "When most of your boxes are sealed with val, accidental changes cannot sneak into your app.",
                explainDeepDive = ExplainDeepDive(
                    headline = "Why Kotlin Loves Sealed Jars (val)",
                    realLifeOpening = "In daily life, when you write your name on a permanent library card, you expect that name to stay put—you do not want someone erasing it halfway through the day.",
                    kotlinConnection = "In Kotlin, your phone's memory holds hundreds of values at once. Marking a value with val tells your device: lock this label to its value right now and protect it from accidental edits.",
                    syntaxSnippet = "val heroName = \"Kova\"",
                    symbolBreakdowns = listOf(
                        SymbolBreakdown("val", "Stands for 'value'. Tells Kotlin to create a sealed, read-only locker."),
                        SymbolBreakdown("heroName", "The label you choose so you can read the locker later."),
                        SymbolBreakdown("=", "The assignment sign: 'store the value on the right into the locker on the left'."),
                        SymbolBreakdown("\"Kova\"", "Quotation marks wrap text so Kotlin knows these letters are a word."),
                    ),
                    takeaway = "Whenever you create a new box in Kotlin, ask: 'Does this ever need to be swapped?' If not, seal it with val.",
                ),
            ),
            ConceptRule(
                id = "l1_1_rule_var_update",
                title = "Replace Changeable Values with var",
                explanation = "Write var for a value that changes over time, then assign a fresh value directly using = (for example, replacing 40 with 55).",
                codeSnippet = "var dailyXp = 40\ndailyXp = 55",
                whyItMatters = "Using var intentionally tells every reader: 'Heads up, this number is allowed to change while the app runs.'",
                explainDeepDive = ExplainDeepDive(
                    headline = "How an Erasable Whiteboard (var) Works",
                    realLifeOpening = "In daily life, a classroom scoreboard uses dry-erase markers. When your team goes from 40 points to 55 points, you wipe off 40 and write 55 in the exact same spot.",
                    kotlinConnection = "In Kotlin, var creates that dry-erase slot in memory. First you store the starting value (40). Later, writing the variable name with = replaces what is inside with the new value (55)—no need to write var a second time.",
                    syntaxSnippet = "var dailyXp = 40\ndailyXp = 55",
                    symbolBreakdowns = listOf(
                        SymbolBreakdown("var", "Stands for 'variable'. Tells Kotlin this locker is erasable and changeable."),
                        SymbolBreakdown("dailyXp", "The locker's name. Written once with var to create it, then by name alone to update it."),
                        SymbolBreakdown("= 40", "Stores the starting whole number 40 into the locker."),
                        SymbolBreakdown("= 55", "Erases 40 from dailyXp and stores the new whole number 55 in its place."),
                    ),
                    takeaway = "Only write var on the line that creates the box—when you replace its value later, write just the name and =.",
                ),
            ),
            ConceptRule(
                id = "l1_1_rule_reassign_error",
                title = "The Compiler Guardrail on val",
                explanation = "If you try to store a new number inside a val later, Kotlin stops you immediately with 'Val cannot be reassigned'.",
                codeSnippet = "val maxLessons = 3\n// maxLessons = 5  <- Compile error!",
                whyItMatters = "Catching mistakes before the app even launches saves hours of hunting weird bugs on a real phone.",
                explainDeepDive = ExplainDeepDive(
                    headline = "Your Built-In Safety Inspector",
                    realLifeOpening = "Imagine a smart safe that beeps loudly the second someone tries to pry it open without the key—stopping the mistake before anything goes missing.",
                    kotlinConnection = "Kotlin's compiler checks every single line before your app ever opens on Android. If Line 1 sealed maxLessons with val and Line 2 tries to put 5 inside it, Kotlin blocks the build and points straight at the mistake.",
                    syntaxSnippet = "val maxLessons = 3\n// maxLessons = 5  (Blocked by Kotlin!)",
                    symbolBreakdowns = listOf(
                        SymbolBreakdown("val", "Locks maxLessons permanently to 3."),
                        SymbolBreakdown("//", "Two forward slashes mark a human note (comment) that the device ignores."),
                        SymbolBreakdown("maxLessons = 5", "Attempts to overwrite a sealed locker—which Kotlin forbids unless you change val to var."),
                    ),
                    takeaway = "A compiler error is not bad news—it is Kotlin catching a bug early while it takes two seconds to fix.",
                ),
            ),
        ),
        practiceTask = PracticeTaskSpec(
            title = "Hero Locker & Whiteboard",
            prompt = "Create a sealed val heroName set to \"Kova\", a changeable var dailyXp starting at 40, replace dailyXp directly with 55, and print both lines.",
            desiredOutput = "Hero: Kova\nXP: 55",
            solutionCode = "val heroName = \"Kova\"\nvar dailyXp = 40\ndailyXp = 55\nprintln(\"Hero: \$heroName\")\nprintln(\"XP: \$dailyXp\")",
            starterCode = "val heroName = \"Kova\"\nvar dailyXp = 40\n// Replace dailyXp with 55 below:\ndailyXp = 55\nprintln(\"Hero: \$heroName\")\nprintln(\"XP: \$dailyXp\")",
            expectedKeywords = listOf("val", "heroName", "var", "dailyXp", "55", "println"),
            editorHint = "Make sure heroName uses val, dailyXp uses var, dailyXp is replaced with 55, and both lines use println(...).",
            lineNotes = listOf(
                PracticeSolutionNote(1, "val seals heroName so the learner's name stays locked to \"Kova\"."),
                PracticeSolutionNote(2, "var makes dailyXp erasable because the score will be updated."),
                PracticeSolutionNote(3, "Replaces the old 40 inside dailyXp with the new single value 55."),
                PracticeSolutionNote(4, "Prints the first line with heroName dropped right into the text."),
                PracticeSolutionNote(5, "Prints the second line displaying the updated 55 value."),
            ),
        ),
        debugger = DebuggerWalkthrough(
            title = "Live Trace: val Jar vs var Whiteboard",
            subtitle = "Watch heroName stay sealed while dailyXp changes from 40 to 55.",
            lines = listOf(
                DebuggerCodeLine(1, "val heroName = \"Kova\"", CodeLineRole.DECLARATION),
                DebuggerCodeLine(2, "var dailyXp = 40", CodeLineRole.DECLARATION),
                DebuggerCodeLine(3, "dailyXp = 55", CodeLineRole.EXECUTION),
                DebuggerCodeLine(4, "println(\"Hero: \$heroName\")", CodeLineRole.OUTPUT),
                DebuggerCodeLine(5, "println(\"XP: \$dailyXp\")", CodeLineRole.OUTPUT),
            ),
            steps = listOf(
                DebuggerStep(
                    stepIndex = 0,
                    activeLineNumber = 1,
                    headline = "Seal 'heroName' jar",
                    explanation = "Kotlin creates a read-only val locker named heroName and stores \"Kova\" inside.",
                    variables = listOf(
                        VariableWatchEntry("heroName", "String", "\"Kova\"", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 1,
                    activeLineNumber = 2,
                    headline = "Open 'dailyXp' whiteboard",
                    explanation = "Kotlin creates a changeable var box named dailyXp and writes 40 inside.",
                    variables = listOf(
                        VariableWatchEntry("heroName", "String", "\"Kova\""),
                        VariableWatchEntry("dailyXp", "Int", "40", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 2,
                    activeLineNumber = 3,
                    headline = "Replace dailyXp: 40 -> 55",
                    explanation = "Because dailyXp is a var, Kotlin erases 40 and stores 55 directly into the locker.",
                    variables = listOf(
                        VariableWatchEntry("heroName", "String", "\"Kova\""),
                        VariableWatchEntry("dailyXp", "Int", "55", previousValue = "40", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 3,
                    activeLineNumber = 4,
                    headline = "Print Hero line",
                    explanation = "println reads the sealed heroName locker and outputs Hero: Kova.",
                    variables = listOf(
                        VariableWatchEntry("heroName", "String", "\"Kova\""),
                        VariableWatchEntry("dailyXp", "Int", "55"),
                    ),
                    consoleOutput = listOf("Hero: Kova"),
                ),
                DebuggerStep(
                    stepIndex = 4,
                    activeLineNumber = 5,
                    headline = "Print updated XP line",
                    explanation = "println reads the updated dailyXp whiteboard (55) and outputs XP: 55.",
                    variables = listOf(
                        VariableWatchEntry("heroName", "String", "\"Kova\""),
                        VariableWatchEntry("dailyXp", "Int", "55"),
                    ),
                    consoleOutput = listOf("Hero: Kova", "XP: 55"),
                ),
            ),
        ),
        testQuestions = listOf(
            LessonTestQuestion(
                id = "q_1_1_1",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What prints to the console when this Kotlin snippet runs?",
                codeSnippet = "1 | var coins = 20\n2 | coins = 30\n3 | println(\"Coins: \$coins\")",
                options = listOf("Coins: 20", "Coins: 30", "Coins: 2030", "Compile error"),
                correctIndex = 1,
                plainEnglishExplanation = "coins starts at 20 inside a changeable var box, then Line 2 replaces 20 with 30 before println prints it.",
            ),
            LessonTestQuestion(
                id = "q_1_1_2",
                kind = LessonQuestionKind.SPOT_BUG,
                prompt = "Which line causes a Kotlin compile error?",
                codeSnippet = "1 | val lives = 3\n2 | var score = 100\n3 | lives = 2\n4 | score = 150",
                options = listOf("Line 1", "Line 2", "Line 3", "Line 4"),
                correctIndex = 2,
                plainEnglishExplanation = "Line 1 sealed lives with val. Trying to replace its value with 2 on Line 3 breaks the read-only rule—use var when a value must change.",
            ),
            LessonTestQuestion(
                id = "q_1_1_3",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "Fill the blank so streak can be replaced with a new number tomorrow:",
                codeSnippet = "1 | ___ streak = 1\n2 | streak = 2",
                options = listOf("val", "var", "const", "fix"),
                correctIndex = 1,
                plainEnglishExplanation = "Because streak is given a new value on Line 2 (streak = 2), you must declare it with var (changeable whiteboard).",
            ),
            LessonTestQuestion(
                id = "q_1_1_4",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What is stored in badge after Line 3 finishes?",
                codeSnippet = "1 | val starter = \"Bronze\"\n2 | var badge = starter\n3 | badge = \"Silver\"",
                options = listOf("Bronze", "Silver", "starter", "Compile error because starter is val"),
                correctIndex = 1,
                plainEnglishExplanation = "Copying a val into a var is completely legal! badge starts with \"Bronze\" and is then replaced with \"Silver\" while starter stays \"Bronze\".",
            ),
            LessonTestQuestion(
                id = "q_1_1_5",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "In everyday English, what does 'immutable' mean for a Kotlin val?",
                codeSnippet = null,
                options = listOf(
                    "It cannot be reassigned after you store a value in it",
                    "It can only hold numbers and never text",
                    "It erases itself after 60 seconds",
                    "It works only in dark mode",
                ),
                correctIndex = 0,
                plainEnglishExplanation = "'Im-' means 'not' and 'mutable' means 'changeable'—an immutable val locks its value permanently.",
            ),
        ),
    )

    fun lesson1_2TypesAndStrings(): LessonInteractiveContent = LessonInteractiveContent(
        wordBreakdowns = listOf(
            WordBreakdown(
                id = "l1_2_wb_type",
                term = "Type",
                literalMeaning = "The exact category or mold a piece of data fits into (whole number, decimal, text, or true/false).",
                tenYearOldAnalogy = "Think of a shape-sorter toy: Int is the square slot for whole counts (5), Double is the circle slot for decimals (4.8), and String is the long slot for words (\"Kova\").",
                highlightSnippet = "Every Kotlin variable has a strict Type (Int, Double, String, Boolean) checked before your app runs.",
            ),
            WordBreakdown(
                id = "l1_2_wb_template",
                term = "String Template",
                literalMeaning = "A fill-in-the-blank sentence where \$ slots drop live variable values straight into text.",
                tenYearOldAnalogy = "Like a birthday invitation card with blank lines: \"Hi ____, you are ____ today!\" Kotlin fills in \$name and \${age + 1} automatically.",
                highlightSnippet = "Use \"\$name\" for one variable and \"\${expression}\" with curly braces whenever you run math inside text.",
            ),
            WordBreakdown(
                id = "l1_2_wb_nullable",
                term = "Nullable (?)",
                literalMeaning = "A box explicitly allowed to hold null (intentionally empty—nothing inside yet).",
                tenYearOldAnalogy = "A regular String box MUST hold a postcard. Adding a question mark (String?) puts a sticker on the box saying: 'It is okay if this box is empty today!'",
                highlightSnippet = "Add '?' (like String?) to allow null, and use '?:' to provide a safe backup value when empty.",
            ),
        ),
        preHighlightedTakeaways = listOf(
            PreHighlightedTakeaway(
                id = "l1_2_takeaway_1",
                category = HighlightCategory.KEY_IDEA,
                headline = "Kotlin knows your data shape automatically",
                body = "Writing val streak = 5 automatically sets the type to Int (whole number), so you can never accidentally drop text into it.",
            ),
            PreHighlightedTakeaway(
                id = "l1_2_takeaway_2",
                category = HighlightCategory.CODE_RULE,
                headline = "Wrap math inside \${ ... } curly braces",
                body = "Inside quotes, \${streak + 1} tells Kotlin to add 1 to the current value of streak first, then place the sum into your sentence.",
            ),
            PreHighlightedTakeaway(
                id = "l1_2_takeaway_3",
                category = HighlightCategory.GOTCHA,
                headline = "No '?' means null is forbidden",
                body = "A plain String can never be null. Add '?' (String?) when empty is allowed, and pair it with '?:' for a crash-free backup.",
            ),
        ),
        conceptRules = listOf(
            ConceptRule(
                id = "l1_2_rule_inference",
                title = "Type Inference vs. Explicit Labels",
                explanation = "Kotlin automatically detects whole numbers (Int), decimals (Double), text (String), and true/false (Boolean)—or you can label the type directly after a colon (:).",
                codeSnippet = "val streak: Int = 5\nval rating: Double = 4.8\nval active: Boolean = true",
                whyItMatters = "Once a box is typed as Int, Kotlin stops you from accidentally shoving text like \"five\" into it.",
                explainDeepDive = ExplainDeepDive(
                    headline = "How Kotlin's Shape-Sorter Keeps Data Clean",
                    realLifeOpening = "In daily life, you do not pour orange juice into a paper envelope or mail coins inside a coffee mug—every container has a shape built for what goes inside.",
                    kotlinConnection = "Kotlin gives every variable a strict data shape called a Type. When you write : Int after a name, you tell Kotlin explicitly: 'This box only accepts whole numbers.' Even if you omit : Int, Kotlin looks at 5 and figures it out automatically.",
                    syntaxSnippet = "val streak: Int = 5\nval rating: Double = 4.8",
                    symbolBreakdowns = listOf(
                        SymbolBreakdown(":", "The colon means 'has the exact type of'."),
                        SymbolBreakdown("Int", "Whole numbers with no decimal point (such as 0, 5, or 100)."),
                        SymbolBreakdown("Double", "Numbers with a decimal point (such as 4.8 or 99.5)."),
                        SymbolBreakdown("Boolean", "A simple yes-or-no switch that holds only true or false."),
                    ),
                    takeaway = "Types act like guardrails—if you try to mix text into a number slot, Kotlin warns you instantly.",
                ),
            ),
            ConceptRule(
                id = "l1_2_rule_braces",
                title = "Curly Braces for Math Inside Text",
                explanation = "Write \$streak for a single variable name, but wrap math like \${streak + 1} (add 1 to the current value of streak) inside curly braces so Kotlin computes the answer first.",
                codeSnippet = "val msg = \"Next: \${streak + 1}d\"",
                whyItMatters = "Without curly braces, \"\$streak+1\" prints \"5+1\" literally instead of \"6\"!",
                explainDeepDive = ExplainDeepDive(
                    headline = "Filling Blank Lines Inside Sentences",
                    realLifeOpening = "In daily life, imagine a printed certificate with a blank slot: 'Congratulations! Tomorrow you reach day ___.' You do the quick math in your head (5 plus 1 equals 6) before writing 6 on the line.",
                    kotlinConnection = "Inside Kotlin quotes, the dollar sign \$ opens a live fill-in slot. Putting curly braces \${ ... } around streak + 1 tells your device: first add 1 to the current value of streak, then drop that finished number right into the sentence.",
                    syntaxSnippet = "val msg = \"Next: \${streak + 1}d\"",
                    symbolBreakdowns = listOf(
                        SymbolBreakdown("\$", "Signals Kotlin to insert a live variable value instead of literal letters."),
                        SymbolBreakdown("\${ ... }", "Curly braces group an entire calculation or property so it finishes before turning into text."),
                        SymbolBreakdown("streak + 1", "Reads the current value of streak (5) and adds 1 to produce 6."),
                    ),
                    takeaway = "Single variable? Use \$name. Any math or letter glued right after? Wrap it in \${...}!",
                ),
            ),
            ConceptRule(
                id = "l1_2_rule_elvis",
                title = "Safe Backup Values with Elvis (?:)",
                explanation = "When a nullable box (String?) might be empty (null), writing tip ?: \"Keep going!\" means: use tip if it has text, otherwise fall back to \"Keep going!\".",
                codeSnippet = "val tip: String? = null\nval shown = tip ?: \"Keep going!\"",
                whyItMatters = "Prevents the infamous NullPointerException crash that closes apps unexpectedly on phones.",
                explainDeepDive = ExplainDeepDive(
                    headline = "Never Crash on an Empty Box",
                    realLifeOpening = "In daily life, when you check your backpack for a snack and find it empty, you do not freeze—you have a backup plan: 'If my bag is empty, grab an apple from the kitchen.'",
                    kotlinConnection = "In Kotlin, null means 'nothing is inside this box yet'. Putting ? on String? permits an empty box, and writing ?: (called the Elvis operator) provides a guaranteed backup whenever the left side is empty.",
                    syntaxSnippet = "val tip: String? = null\nval shown = tip ?: \"Keep going!\"",
                    symbolBreakdowns = listOf(
                        SymbolBreakdown("String?", "The question mark means: holds either a String word OR null (empty)."),
                        SymbolBreakdown("null", "Explicitly represents an empty locker with no value inside."),
                        SymbolBreakdown("?:", "Elvis fallback: 'if the left side is null, hand back the safe value on the right instead'."),
                    ),
                    takeaway = "Pair String? with ?: so your app always has a friendly backup text instead of crashing.",
                ),
            ),
        ),
        practiceTask = PracticeTaskSpec(
            title = "Streak Badge Formatter",
            prompt = "Store streak = 5, build a sentence that adds 1 to the current value of streak inside \${streak + 1}, and use ?: to turn a null tip into \"Keep going!\".",
            desiredOutput = "Streak: 5d -> Goal: 6d\nTip: Keep going!",
            solutionCode = "val streak: Int = 5\nval tip: String? = null\nval safeTip = tip ?: \"Keep going!\"\nprintln(\"Streak: \${streak}d -> Goal: \${streak + 1}d\")\nprintln(\"Tip: \$safeTip\")",
            starterCode = "val streak: Int = 5\nval tip: String? = null\nval safeTip = tip ?: \"Keep going!\"\nprintln(\"Streak: \${streak}d -> Goal: \${streak + 1}d\")\nprintln(\"Tip: \$safeTip\")",
            expectedKeywords = listOf("Int", "String?", "?:", "\${streak + 1}", "println"),
            editorHint = "Include String?, the Elvis fallback ?:, and \${streak + 1} inside println(...).",
            lineNotes = listOf(
                PracticeSolutionNote(1, "Stores the whole number 5 in an Int locker."),
                PracticeSolutionNote(2, "Marks tip as String? so holding null is explicitly allowed."),
                PracticeSolutionNote(3, "Elvis operator ?: checks tip; since it is null, safeTip receives \"Keep going!\"."),
                PracticeSolutionNote(4, "Uses \${streak} before 'd' and adds 1 to streak inside \${streak + 1} to produce 6."),
                PracticeSolutionNote(5, "Prints the guaranteed non-null safeTip cleanly."),
            ),
        ),
        debugger = DebuggerWalkthrough(
            title = "Live Trace: Templates & Safe Null",
            subtitle = "See how Kotlin infers types, adds 1 inside \${...}, and applies Elvis fallback.",
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
                    explanation = "streak stores the whole number 5 inside an Int slot.",
                    variables = listOf(
                        VariableWatchEntry("streak", "Int", "5", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 1,
                    activeLineNumber = 2,
                    headline = "Assemble String template",
                    explanation = "Kotlin fills in \${streak} with 5 and attaches 'd' right after it to build \"Streak: 5d\".",
                    variables = listOf(
                        VariableWatchEntry("streak", "Int", "5"),
                        VariableWatchEntry("badge", "String", "\"Streak: 5d\"", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 2,
                    activeLineNumber = 3,
                    headline = "Add 1 to streak (5 + 1 = 6)",
                    explanation = "Reads the current value of streak (5), adds 1 to get 6, and stores 6 inside nextGoal.",
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
                    explanation = "Because of String?, Kotlin allows tip to hold null (intentionally empty).",
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
                    headline = "Elvis '?:' picks backup",
                    explanation = "Because tip is null, ?: safely hands \"Keep going!\" into non-null shownTip.",
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
                plainEnglishExplanation = "Curly braces \${level + 1} tell Kotlin to add 1 to level (4 plus 1 equals 5) before inserting 5 into the sentence.",
            ),
            LessonTestQuestion(
                id = "q_1_2_2",
                kind = LessonQuestionKind.SPOT_BUG,
                prompt = "Which line breaks Kotlin's null-safety rule?",
                codeSnippet = "1 | val name: String = \"Kova\"\n2 | val hint: String? = null\n3 | val title: String = null\n4 | println(name)",
                options = listOf("Line 1", "Line 2", "Line 3", "Line 4"),
                correctIndex = 2,
                plainEnglishExplanation = "Line 3 uses String without a '?' question mark, so Kotlin forbids storing null in title.",
            ),
            LessonTestQuestion(
                id = "q_1_2_3",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "Fill the blank to give nickname a safe backup whenever it is null:",
                codeSnippet = "1 | val nickname: String? = null\n2 | val display = nickname ___ \"Learner\"",
                options = listOf("?:", "==", "!=", ".."),
                correctIndex = 0,
                plainEnglishExplanation = "The Elvis operator ?: means: if the left side is null, use the safe backup value on the right.",
            ),
            LessonTestQuestion(
                id = "q_1_2_4",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "Look closely—there are NO curly braces here! What prints?",
                codeSnippet = "1 | val xp = 10\n2 | println(\"\$xp+5\")",
                options = listOf("15", "10+5", "xp+5", "Compile error"),
                correctIndex = 1,
                plainEnglishExplanation = "Without curly braces, Kotlin only fills in \$xp (10) and treats '+5' as plain text, printing '10+5'. Use \"\${xp + 5}\" to get 15!",
            ),
            LessonTestQuestion(
                id = "q_1_2_5",
                kind = LessonQuestionKind.SPOT_BUG,
                prompt = "Which line has a Type mismatch error?",
                codeSnippet = "1 | val score: Int = 90\n2 | val average: Double = 88.5\n3 | val badge: Int = \"Gold\"\n4 | val ok: Boolean = true",
                options = listOf("Line 1", "Line 2", "Line 3", "Line 4"),
                correctIndex = 2,
                plainEnglishExplanation = "Line 3 tries to put a String (\"Gold\") inside an Int (whole-number) slot—that does not fit the shape-sorter!",
            ),
        ),
    )

    fun lesson1_3OperatorsAndRanges(): LessonInteractiveContent = LessonInteractiveContent(
        wordBreakdowns = listOf(
            WordBreakdown(
                id = "l1_3_wb_compound",
                term = "Compound Update (+=)",
                literalMeaning = "Take the current value, add a number to it, and store the new total right back in the same var.",
                tenYearOldAnalogy = "Like dropping 15 new marbles into a jar that already has 70 marbles—instead of recounting from scratch, you just add 15 to the current value of score to get 85.",
                highlightSnippet = "score += 15 means: add 15 to the current value of score and save the new total back into score.",
            ),
            WordBreakdown(
                id = "l1_3_wb_range",
                term = "Inclusive Range (..)",
                literalMeaning = "Every whole number from the start number up to the end number, including BOTH ends.",
                tenYearOldAnalogy = "Look at 80 to 100 on a wooden ruler. Writing score in 80..100 asks: 'Is score anywhere in every whole number from 80 up to 100, including both 80 and 100?'",
                highlightSnippet = "80..100 means every whole number from 80 up to 100, including both 80 and 100.",
            ),
            WordBreakdown(
                id = "l1_3_wb_until",
                term = "Exclusive Range (until)",
                literalMeaning = "Every whole number starting at the first number and stopping 1 BEFORE the upper number.",
                tenYearOldAnalogy = "A 4-car train has cars numbered 0, 1, 2, and 3. Writing 0 until 4 covers all 4 cars and stops safely before stepping off onto nonexistent car #4!",
                highlightSnippet = "0 until 4 means 0, 1, 2, and 3 (excluding 4)—essential for safe 0-based list indices.",
            ),
        ),
        preHighlightedTakeaways = listOf(
            PreHighlightedTakeaway(
                id = "l1_3_takeaway_1",
                category = HighlightCategory.KEY_IDEA,
                headline = "dailyXp += 15 means 'add 15 to the current value of dailyXp'",
                body = "Compound operators (+=, -=) work on mutable var boxes so you can update totals cleanly without repeating the variable name.",
            ),
            PreHighlightedTakeaway(
                id = "l1_3_takeaway_2",
                category = HighlightCategory.CODE_RULE,
                headline = "80..100 includes 100; 0 until 4 stops at 3",
                body = "Use '..' when both end numbers count (like test scores 80 to 100). Use 'until' when counting 0-based slots so you never step past the last item.",
            ),
            PreHighlightedTakeaway(
                id = "l1_3_takeaway_3",
                category = HighlightCategory.GOTCHA,
                headline = "Whole-number division '/' drops the decimal; '!=' means 'is not equal to'",
                body = "85 / 10 produces whole number 8 (not 8.5), while 85 % 10 gives the leftover 5. And score != 0 checks that score is not equal to 0.",
            ),
        ),
        conceptRules = listOf(
            ConceptRule(
                id = "l1_3_rule_compound_math",
                title = "Updating Totals with += and Checking with !=",
                explanation = "Write dailyXp += 15 to add 15 to the current value of dailyXp and save the new total back. Use != to check whether two values are not equal.",
                codeSnippet = "var dailyXp = 70\ndailyXp += 15      // add 15 to 70 -> 85\nval hasXp = dailyXp != 0  // true (85 is not equal to 0)",
                whyItMatters = "Clean math shortcuts keep scoreboards, timers, and counters short and typo-free.",
                explainDeepDive = ExplainDeepDive(
                    headline = "How += Adds to Your Current Total",
                    realLifeOpening = "In daily life, if your piggy bank holds 70 coins and you drop in 15 more, you do not say 'my piggy bank equals my piggy bank plus 15'—you simply say 'add 15 to what is already inside.'",
                    kotlinConnection = "Kotlin gives you the += shortcut for that exact sentence. Writing dailyXp += 15 tells the device: read the current value of dailyXp (70), add 15 to it (85), and save 85 right back into dailyXp. And != lets you ask 'is this value NOT equal to zero?'",
                    syntaxSnippet = "var dailyXp = 70\ndailyXp += 15\nval hasXp = dailyXp != 0",
                    symbolBreakdowns = listOf(
                        SymbolBreakdown("+=", "Add the number on the right to the current value on the left, then store the new sum back."),
                        SymbolBreakdown("!=", "Means 'is not equal to'—returns true when the two sides are different."),
                        SymbolBreakdown("var", "Required for += because dailyXp changes from 70 to 85."),
                    ),
                    takeaway = "Read dailyXp += 15 aloud as: 'add 15 to the current value of dailyXp.' Read != as: 'is not equal to.'",
                ),
            ),
            ConceptRule(
                id = "l1_3_rule_int_div",
                title = "Whole-Number Division (/) & Remainder (%)",
                explanation = "Dividing two whole numbers (85 / 10) counts how many full groups of 10 fit inside 85 (which is 8). The remainder symbol % (85 % 10) gives the leftover 5.",
                codeSnippet = "val fullTens = 85 / 10   // 8 full groups of 10\nval leftover = 85 % 10   // 5 left over",
                whyItMatters = "Perfect for turning total seconds into full minutes plus leftover seconds (75 seconds = 1 minute and 15 seconds).",
                explainDeepDive = ExplainDeepDive(
                    headline = "Sharing Slices Equally with / and %",
                    realLifeOpening = "In daily life, imagine sharing 85 stickers evenly into packs of 10. You fill 8 complete packs (80 stickers) and have 5 loose stickers left on the table.",
                    kotlinConnection = "When both numbers are Ints in Kotlin, / counts the 8 full packs and drops the partial decimal. The percent-shaped modulo symbol % hands you the exact leftover count (5).",
                    syntaxSnippet = "val fullTens = 85 / 10\nval leftover = 85 % 10",
                    symbolBreakdowns = listOf(
                        SymbolBreakdown("/", "Whole-number division: how many complete groups fit (85 divided by 10 gives 8)."),
                        SymbolBreakdown("%", "Remainder (modulo): how much is left over after full groups are removed (5)."),
                    ),
                    takeaway = "With two Ints, / gives the full groups and % gives the leftovers.",
                ),
            ),
            ConceptRule(
                id = "l1_3_rule_ranges",
                title = "Inclusive Ranges (..) vs Exclusive (until)",
                explanation = "80..100 means every whole number from 80 up to 100, including both 80 and 100. By contrast, 0 until 4 means 0, 1, 2, and 3—stopping 1 before 4.",
                codeSnippet = "val passed = score in 80..100\nval validSlot = index in 0 until 4",
                whyItMatters = "Reads like natural English and prevents off-by-one crashes on 0-indexed lists.",
                explainDeepDive = ExplainDeepDive(
                    headline = "Ruler Segments (..) vs Train Cars (until)",
                    realLifeOpening = "In daily life, a passing grade includes both 80 and 100. On the other hand, if 4 train cars are numbered 0, 1, 2, and 3, there is no car #4—you must stop right before 4.",
                    kotlinConnection = "Kotlin lets you express both ideas clearly. Writing score in 80..100 checks every whole number from 80 up to 100, including both 80 and 100. Writing index in 0 until 4 checks 0, 1, 2, and 3—never reaching 4.",
                    syntaxSnippet = "val passed = score in 80..100\nval validSlot = 3 in 0 until 4",
                    symbolBreakdowns = listOf(
                        SymbolBreakdown("in", "Asks: 'Does the value on the left sit inside the range on the right?'"),
                        SymbolBreakdown("..", "Inclusive range: includes both the start number and the end number."),
                        SymbolBreakdown("until", "Exclusive upper bound: starts at the left number and stops 1 short of the right number."),
                    ),
                    takeaway = "Use '..' when the end number is included (scores 80..100); use 'until' for 0-based counts (0 until size).",
                ),
            ),
        ),
        practiceTask = PracticeTaskSpec(
            title = "Score Boost & Range Check",
            prompt = "Start with var score = 70, add 15 to the current value of score using +=, check if score sits in 80..100, calculate 85 / 10 and 85 % 10, then print the result.",
            desiredOutput = "Score: 85 (Passed: true)\nRank: 8 (Extra: 5)",
            solutionCode = "var score = 70\nscore += 15\nval passed = score in 80..100\nval rank = score / 10\nval extra = score % 10\nprintln(\"Score: \$score (Passed: \$passed)\")\nprintln(\"Rank: \$rank (Extra: \$extra)\")",
            starterCode = "var score = 70\nscore += 15\nval passed = score in 80..100\nval rank = score / 10\nval extra = score % 10\nprintln(\"Score: \$score (Passed: \$passed)\")\nprintln(\"Rank: \$rank (Extra: \$extra)\")",
            expectedKeywords = listOf("var", "+=", "in 80..100", "/ 10", "% 10", "println"),
            editorHint = "Use var score = 70, score += 15, score in 80..100, score / 10, and score % 10.",
            lineNotes = listOf(
                PracticeSolutionNote(1, "Starts mutable score at 70 so it can be boosted."),
                PracticeSolutionNote(2, "Adds 15 to the current value of score (70 plus 15 equals 85)."),
                PracticeSolutionNote(3, "Checks whether 85 falls in every whole number from 80 up to 100 (true)."),
                PracticeSolutionNote(4, "Whole-number division 85 / 10 produces 8 full tens."),
                PracticeSolutionNote(5, "Remainder 85 % 10 produces 5 left over."),
                PracticeSolutionNote(6, "Prints Score: 85 (Passed: true)."),
                PracticeSolutionNote(7, "Prints Rank: 8 (Extra: 5)."),
            ),
        ),
        debugger = DebuggerWalkthrough(
            title = "Live Trace: +=, Ranges & Int Math",
            subtitle = "Watch score jump 70 -> 85 with +=, pass 80..100, and split via / and %.",
            lines = listOf(
                DebuggerCodeLine(1, "var score = 70", CodeLineRole.DECLARATION),
                DebuggerCodeLine(2, "score += 15", CodeLineRole.EXECUTION),
                DebuggerCodeLine(3, "val passed = score in 80..100", CodeLineRole.DECISION),
                DebuggerCodeLine(4, "val rank = score / 10", CodeLineRole.EXECUTION),
                DebuggerCodeLine(5, "val extra = score % 10", CodeLineRole.EXECUTION),
                DebuggerCodeLine(6, "val notZero = score != 0", CodeLineRole.DECISION),
                DebuggerCodeLine(7, "println(\"Score=\$score Pass=\$passed Rank=\$rank Extra=\$extra\")", CodeLineRole.OUTPUT),
            ),
            steps = listOf(
                DebuggerStep(
                    stepIndex = 0,
                    activeLineNumber = 1,
                    headline = "Start score = 70",
                    explanation = "Creates mutable var score holding 70.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "70", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 1,
                    activeLineNumber = 2,
                    headline = "Add 15 to score (70 -> 85)",
                    explanation = "score += 15 adds 15 to the current value of score (70) and writes 85 back.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "85", previousValue = "70", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 2,
                    activeLineNumber = 3,
                    headline = "Check 85 in 80..100",
                    explanation = "85 sits inside every whole number from 80 up to 100 (including both ends), so passed is true.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "85"),
                        VariableWatchEntry("passed", "Boolean", "true", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 3,
                    activeLineNumber = 4,
                    headline = "Whole division: 85 / 10 = 8",
                    explanation = "10 fits into 85 eight complete times, storing whole number 8 in rank.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "85"),
                        VariableWatchEntry("passed", "Boolean", "true"),
                        VariableWatchEntry("rank", "Int", "8", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 4,
                    activeLineNumber = 5,
                    headline = "Remainder: 85 % 10 = 5",
                    explanation = "After 8 full tens (80), there are 5 points left over -> extra = 5.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "85"),
                        VariableWatchEntry("passed", "Boolean", "true"),
                        VariableWatchEntry("rank", "Int", "8"),
                        VariableWatchEntry("extra", "Int", "5", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 5,
                    activeLineNumber = 6,
                    headline = "Check 85 != 0 -> true",
                    explanation = "85 is not equal to 0, so notZero becomes true.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "85"),
                        VariableWatchEntry("passed", "Boolean", "true"),
                        VariableWatchEntry("rank", "Int", "8"),
                        VariableWatchEntry("extra", "Int", "5"),
                        VariableWatchEntry("notZero", "Boolean", "true", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 6,
                    activeLineNumber = 7,
                    headline = "Print final math output",
                    explanation = "Writes the evaluated score, pass flag, rank, and remainder to console output.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "85"),
                        VariableWatchEntry("passed", "Boolean", "true"),
                        VariableWatchEntry("rank", "Int", "8"),
                        VariableWatchEntry("extra", "Int", "5"),
                        VariableWatchEntry("notZero", "Boolean", "true"),
                    ),
                    consoleOutput = listOf("Score=85 Pass=true Rank=8 Extra=5"),
                ),
            ),
        ),
        testQuestions = listOf(
            LessonTestQuestion(
                id = "q_1_3_1",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What prints after adding 15 to the current value of dailyXp?",
                codeSnippet = "1 | var dailyXp = 40\n2 | dailyXp += 15\n3 | println(dailyXp)",
                options = listOf("40", "15", "55", "4015"),
                correctIndex = 2,
                plainEnglishExplanation = "dailyXp += 15 adds 15 to the current value of dailyXp (40 plus 15 equals 55) and stores 55 back.",
            ),
            LessonTestQuestion(
                id = "q_1_3_2",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What Boolean value prints for '79 in 80..100'?",
                codeSnippet = "1 | val score = 79\n2 | val passed = score in 80..100\n3 | println(passed)",
                options = listOf("true", "false", "80", "Compile error"),
                correctIndex = 1,
                plainEnglishExplanation = "80..100 contains every whole number from 80 up to 100 (including both ends). Because 79 is below 80, the result is false.",
            ),
            LessonTestQuestion(
                id = "q_1_3_3",
                kind = LessonQuestionKind.SPOT_BUG,
                prompt = "A list has 4 lessons (slots 0, 1, 2, 3). Which line can step out of bounds to slot 4?",
                codeSnippet = "1 | val count = 4\n2 | val safeRange = 0 until count\n3 | val riskyRange = 0..count\n4 | println(3 in safeRange)",
                options = listOf("Line 1", "Line 2", "Line 3", "Line 4"),
                correctIndex = 2,
                plainEnglishExplanation = "0..4 on Line 3 includes 4 (five slots: 0, 1, 2, 3, 4), which steps past a 4-item list! Use 0 until count instead.",
            ),
            LessonTestQuestion(
                id = "q_1_3_4",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "Fill the symbol that means 'is not equal to' when checking that score is not 0:",
                codeSnippet = "1 | val hasScore = score ___ 0",
                options = listOf("!=", "..", "+=", "%"),
                correctIndex = 0,
                plainEnglishExplanation = "The != operator checks whether the left value is not equal to the right value.",
            ),
            LessonTestQuestion(
                id = "q_1_3_5",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What leftover remainder prints from 75 % 60?",
                codeSnippet = "1 | val leftoverSeconds = 75 % 60\n2 | println(leftoverSeconds)",
                options = listOf("1", "15", "60", "1.25"),
                correctIndex = 1,
                plainEnglishExplanation = "60 fits into 75 one full time, leaving 15 left over—so 75 % 60 equals 15.",
            ),
        ),
    )

    fun lesson1_4ControlDecisions(): LessonInteractiveContent = LessonInteractiveContent(
        wordBreakdowns = listOf(
            WordBreakdown(
                id = "l1_4_wb_condition",
                term = "Condition",
                literalMeaning = "A yes-or-no question (true or false) that decides which code path runs next.",
                tenYearOldAnalogy = "Like a theme-park height sign: 'Is your height greater than or equal to 120cm?' Yes (true) opens the roller coaster gate; No (false) sends you to the carousel.",
                highlightSnippet = "A condition evaluates to true or false and steers execution onto the matching branch.",
            ),
            WordBreakdown(
                id = "l1_4_wb_branch",
                term = "Branch",
                literalMeaning = "One specific path the code can follow when its condition matches.",
                tenYearOldAnalogy = "Picture a train track switchboard: one train enters (score), and the switch routes it cleanly onto the Gold track, Mint track, or Retry track.",
                highlightSnippet = "Kotlin's 'when' checks branches from top to bottom and runs ONLY the first matching branch.",
            ),
            WordBreakdown(
                id = "l1_4_wb_expression",
                term = "Expression",
                literalMeaning = "A piece of code that hands back (returns) a finished value you can store directly inside a val.",
                tenYearOldAnalogy = "Like a vending machine: you press a button (your input), and a snack (the result value) drops straight into your hand. Both 'if' and 'when' hand back a value!",
                highlightSnippet = "In Kotlin, 'if' and 'when' are expressions—you can write val tier = when (score) { ... }.",
            ),
        ),
        preHighlightedTakeaways = listOf(
            PreHighlightedTakeaway(
                id = "l1_4_takeaway_1",
                category = HighlightCategory.KEY_IDEA,
                headline = "Both 'if' and 'when' hand back a value directly into a val",
                body = "Instead of declaring an empty var and overwriting it later, assign val status = if (score >= 80) \"Pass\" else \"Retry\" in one clean expression.",
            ),
            PreHighlightedTakeaway(
                id = "l1_4_takeaway_2",
                category = HighlightCategory.CODE_RULE,
                headline = "'when' checks top-to-bottom and stops on the FIRST match",
                body = "Put your narrowest top range (in 90..100 -> \"Gold\") above broader ranges so high scores hit Gold first.",
            ),
            PreHighlightedTakeaway(
                id = "l1_4_takeaway_3",
                category = HighlightCategory.GOTCHA,
                headline = "Always add 'else ->' when assigning 'when' to a variable",
                body = "Kotlin requires a catch-all else -> fallback for Int values so your val is guaranteed never to be left empty.",
            ),
        ),
        conceptRules = listOf(
            ConceptRule(
                id = "l1_4_rule_if_expr",
                title = "'if' Hands Back a Value Directly",
                explanation = "Ask whether score is greater than or equal to 80 (score >= 80) and assign the winning label straight into a sealed val.",
                codeSnippet = "val status = if (score >= 80) \"Pass\" else \"Retry\"",
                whyItMatters = "Keeps your variables sealed with val instead of juggling temporary empty var placeholders.",
                explainDeepDive = ExplainDeepDive(
                    headline = "How 'if' Works Like a Smart Fork in the Road",
                    realLifeOpening = "In daily life, before leaving the house you glance outside: if it is raining, grab an umbrella; otherwise, grab sunglasses. Either way, you walk out holding one exact item.",
                    kotlinConnection = "Kotlin's if works the exact same way—it evaluates a yes-or-no question (is score greater than or equal to 80?) and hands back the matching text straight into val status.",
                    syntaxSnippet = "val status = if (score >= 80) \"Pass\" else \"Retry\"",
                    symbolBreakdowns = listOf(
                        SymbolBreakdown("if (...)", "Tests the yes-or-no condition inside the parentheses."),
                        SymbolBreakdown(">=", "Means 'is greater than or equal to' (so 80 itself counts as Pass!)."),
                        SymbolBreakdown("else", "Provides the alternate value whenever the condition is false."),
                    ),
                    takeaway = "Because if returns a value, you need both an if result and an else result whenever storing it in a val.",
                ),
            ),
            ConceptRule(
                id = "l1_4_rule_when",
                title = "Multi-Way 'when' Switchboard",
                explanation = "when (score) tests each range top-to-bottom using clean arrows (->): 90 up to 100 returns \"Gold\", 80 up to 89 returns \"Mint\", and anything else returns \"Retry\".",
                codeSnippet = "val tier = when (score) {\n  in 90..100 -> \"Gold\"\n  in 80..89 -> \"Mint\"\n  else -> \"Retry\"\n}",
                whyItMatters = "Every possible tier reads like a neat table, making overlapping-range bugs easy to spot.",
                explainDeepDive = ExplainDeepDive(
                    headline = "Reading a 'when' Switchboard Like a Table",
                    realLifeOpening = "In daily life, a medal podium checks your score against clear brackets: 90 through 100 earns Gold, 80 through 89 earns Mint, and anything lower gets another try.",
                    kotlinConnection = "Instead of stacking messy if-else-if chains, Kotlin's when (score) looks at score once and checks each branch line from top to bottom. The very first matching branch hands back the value on the right side of the arrow (->).",
                    syntaxSnippet = "val tier = when (score) {\n  in 90..100 -> \"Gold\"\n  in 80..89 -> \"Mint\"\n  else -> \"Retry\"\n}",
                    symbolBreakdowns = listOf(
                        SymbolBreakdown("when (score)", "Opens the switchboard and inspects the value inside score."),
                        SymbolBreakdown("{ ... }", "Curly braces hold every branch inside the switchboard."),
                        SymbolBreakdown("in 90..100", "Matches every whole number from 90 up to 100, including both 90 and 100."),
                        SymbolBreakdown("->", "Arrow meaning 'if the left side matches, hand back the result on the right'."),
                    ),
                    takeaway = "Order matters! 'when' stops at the first matching row, so always put specific high ranges near the top.",
                ),
            ),
            ConceptRule(
                id = "l1_4_rule_else",
                title = "Mandatory 'else ->' Catch-All Fallback",
                explanation = "Whenever val tier = when (score) returns a value for an Int, Kotlin requires an else -> branch so tier is never left blank for unexpected scores.",
                codeSnippet = "else -> \"Retry\" // Guarantees tier always gets a value",
                whyItMatters = "Forgetting else -> is the #1 beginner compiler error when assigning when to a variable.",
                explainDeepDive = ExplainDeepDive(
                    headline = "Why Kotlin Insists on an 'else ->' Safety Net",
                    realLifeOpening = "In daily life, imagine a mailroom with bins labeled 'Floor 1' and 'Floor 2'. If a package arrives for Floor 5 and there is no 'Other Mail' bin, the clerk has nowhere to put it.",
                    kotlinConnection = "Because an Int can hold any whole number (even 0 or 50), Kotlin checks whether your when covers every possibility. Adding else -> gives Kotlin a guaranteed catch-all bin so val tier is always filled.",
                    syntaxSnippet = "else -> \"Retry\"",
                    symbolBreakdowns = listOf(
                        SymbolBreakdown("else", "The catch-all branch that runs only when none of the earlier branches matched."),
                        SymbolBreakdown("->", "Hands back \"Retry\" into tier whenever the fallback runs."),
                    ),
                    takeaway = "Assigning when (number) to a variable? Always end with else -> so Kotlin knows every case is safe.",
                ),
            ),
        ),
        practiceTask = PracticeTaskSpec(
            title = "Mastery Tier Switchboard",
            prompt = "Classify score = 86 into \"Gold\" (90..100), \"Mint\" (80..89), or \"Retry\" (else) using when, check if score is greater than or equal to 80 with if, and print both lines.",
            desiredOutput = "Tier: Mint\nGate: Next Lesson Unlocked!",
            solutionCode = "val score = 86\nval tier = when (score) {\n  in 90..100 -> \"Gold\"\n  in 80..89 -> \"Mint\"\n  else -> \"Retry\"\n}\nval gate = if (score >= 80) \"Next Lesson Unlocked!\" else \"Keep Practicing\"\nprintln(\"Tier: \$tier\")\nprintln(\"Gate: \$gate\")",
            starterCode = "val score = 86\nval tier = when (score) {\n  in 90..100 -> \"Gold\"\n  in 80..89 -> \"Mint\"\n  else -> \"Retry\"\n}\nval gate = if (score >= 80) \"Next Lesson Unlocked!\" else \"Keep Practicing\"\nprintln(\"Tier: \$tier\")\nprintln(\"Gate: \$gate\")",
            expectedKeywords = listOf("when", "in 90..100", "in 80..89", "else ->", "if", ">= 80", "println"),
            editorHint = "Include when (score) with in 90..100, in 80..89, else ->, and an if (score >= 80) expression.",
            lineNotes = listOf(
                PracticeSolutionNote(1, "Stores the test score 86 in a sealed val."),
                PracticeSolutionNote(2, "Starts a value-returning when switchboard inspecting score."),
                PracticeSolutionNote(3, "86 is not inside 90 up to 100, so Gold is skipped."),
                PracticeSolutionNote(4, "86 sits inside 80 up to 89, handing back \"Mint\" into tier!"),
                PracticeSolutionNote(5, "else -> guarantees tier always gets a value for any score."),
                PracticeSolutionNote(7, "if checks whether 86 is greater than or equal to 80 and returns \"Next Lesson Unlocked!\"."),
                PracticeSolutionNote(8, "Prints Tier: Mint and Gate: Next Lesson Unlocked!"),
            ),
        ),
        debugger = DebuggerWalkthrough(
            title = "Live Trace: 'when' Switchboard",
            subtitle = "Follow the switchboard as score = 86 skips Gold and locks onto Mint.",
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
                    explanation = "Kotlin inspects score (86) and tests each branch from top to bottom.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "86"),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 2,
                    activeLineNumber = 3,
                    headline = "Check 86 in 90..100 -> false",
                    explanation = "86 is below 90, so Kotlin skips \"Gold\" and moves to the next row.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "86"),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 3,
                    activeLineNumber = 4,
                    headline = "Match! 86 in 80..89 -> \"Mint\"",
                    explanation = "86 sits between 80 and 89 inclusive! 'when' immediately hands back \"Mint\" into tier and skips else.",
                    variables = listOf(
                        VariableWatchEntry("score", "Int", "86"),
                        VariableWatchEntry("tier", "String", "\"Mint\"", isChanged = true),
                    ),
                    consoleOutput = emptyList(),
                ),
                DebuggerStep(
                    stepIndex = 4,
                    activeLineNumber = 6,
                    headline = "Evaluate 'if (score >= 80)'",
                    explanation = "86 is greater than or equal to 80 (true), so if returns \"Unlocked\" directly into gate.",
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
                plainEnglishExplanation = "94 falls inside 90 up to 100 on the very first branch, so 'when' immediately returns \"Gold\" and stops checking.",
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
                plainEnglishExplanation = "Whenever 'when' assigns a value to a variable (val badge = when...), Kotlin requires an else -> fallback so every possible number produces a result.",
            ),
            LessonTestQuestion(
                id = "q_1_4_3",
                kind = LessonQuestionKind.FILL_KEYWORD,
                prompt = "Fill the keyword to route day cleanly to its schedule label:",
                codeSnippet = "1 | val plan = ___ (day) {\n2 |   \"Sat\", \"Sun\" -> \"Rest\"\n3 |   else -> \"Study\"\n4 | }",
                options = listOf("when", "loop", "case", "var"),
                correctIndex = 0,
                plainEnglishExplanation = "Kotlin uses when (value) { ... } as its clean multi-branch switchboard.",
            ),
            LessonTestQuestion(
                id = "q_1_4_4",
                kind = LessonQuestionKind.PREDICT_OUTPUT,
                prompt = "What does this single-line 'if' expression store in msg?",
                codeSnippet = "1 | val left = 0\n2 | val msg = if (left > 0) \"Go\" else \"Limit\"\n3 | println(msg)",
                options = listOf("Go", "Limit", "0", "Compile error"),
                correctIndex = 1,
                plainEnglishExplanation = "0 is not greater than 0 (false), so the else branch runs and returns \"Limit\".",
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
                plainEnglishExplanation = "'when' stops at the FIRST matching branch! Put the narrower top range (in 90..100 -> \"Gold\") above in 80..89 so 95 hits Gold first.",
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
        preHighlightedTakeaways = listOf(
            PreHighlightedTakeaway(
                id = "${lesson.id}_takeaway_main",
                category = HighlightCategory.KEY_IDEA,
                headline = "Chapter 2 Preview · ${lesson.title}",
                body = lesson.analogyTeaser,
            ),
        ),
        conceptRules = listOf(
            ConceptRule(
                id = "${lesson.id}_rule_main",
                title = "Core Pattern · Lesson ${lesson.code}",
                explanation = lesson.subtitle,
                codeSnippet = lesson.codePreview,
                whyItMatters = "Master Chapter 1 foundations first, then build reusable Kotlin functions in Chapter 2.",
                explainDeepDive = ExplainDeepDive(
                    headline = "How ${lesson.title} Works in Plain English",
                    realLifeOpening = "In daily life, when you perform a repeatable task or recipe—like making a cup of tea—you follow a named set of steps that turns ingredients into a finished result.",
                    kotlinConnection = "In Kotlin, functions wrap reusable steps under a clean name so your app can call that task anywhere without copying code.",
                    syntaxSnippet = lesson.codePreview,
                    symbolBreakdowns = listOf(
                        SymbolBreakdown("fun", "Tells your device to create a reusable named task (a function)."),
                        SymbolBreakdown("(...)", "Parentheses hold any input ingredients the task needs."),
                        SymbolBreakdown("{ ... }", "Curly braces hold all the steps inside that task."),
                    ),
                    takeaway = "Name a task once with fun, pass inputs inside (), and reuse it anywhere in your Android app.",
                ),
            ),
        ),
        practiceTask = PracticeTaskSpec(
            title = "Preview Pattern",
            prompt = "Inspect the Kotlin pattern for ${lesson.title} and trace how inputs produce the result.",
            desiredOutput = "Ready (${lesson.code})",
            solutionCode = lesson.codePreview,
            starterCode = lesson.codePreview,
            expectedKeywords = listOf("fun"),
            editorHint = "Keep the Kotlin function keyword 'fun' intact and tap Run / Check.",
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
                plainEnglishExplanation = "'filter' keeps only numbers greater than or equal to 80, which are 85 and 94.",
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
