package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Lesson
import com.example.model.LessonQuestionKind
import com.example.model.LessonTestQuestion
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaScallopedSealBadge
import com.example.ui.components.KovaTactileButton
import com.example.ui.components.KovaTactileCard
import com.example.ui.feedback.LocalKovaFeedback
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors
import kotlin.random.Random

@Immutable
private data class PresentedQuestion(
    val source: LessonTestQuestion,
    val presentedOptions: List<String>,
    val correctPresentedIndex: Int,
)

/**
 * Full-screen closed-book 5-question Integrated Lesson Test overlay.
 * - Hides lesson notes & code solution for genuine mastery evaluation
 * - Supports Predict Output, Spot the Bug, and Fill the Keyword questions
 * - >= 80% passes with celebratory chime + haptics, persists score, unlocks next node, decrements quota, syncs Radar
 * - < 80% shows plain-English explanations for every missed question and offers a shuffled retry
 */
@Composable
fun LessonTestOverlay(
    lesson: Lesson,
    questions: List<LessonTestQuestion>,
    onDismiss: () -> Unit,
    onTestPassed: (scorePercent: Int) -> Unit,
    onReturnToQuestPath: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onDismiss)

    val tactile = LocalKovaTactileColors.current
    val motion = LocalKovaMotion.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current

    var attemptSeed by remember(lesson.id) { mutableIntStateOf(0) }
    val deck = remember(lesson.id, questions, attemptSeed) {
        buildPresentedDeck(questions, shuffle = attemptSeed > 0, seed = attemptSeed)
    }

    var currentIndex by remember(lesson.id, attemptSeed) { mutableIntStateOf(0) }
    val selectedAnswers = remember(lesson.id, attemptSeed) { mutableStateMapOf<Int, Int>() }
    var isFinished by remember(lesson.id, attemptSeed) { mutableStateOf(false) }

    val totalCount = deck.size.coerceAtLeast(1)
    val safeIndex = currentIndex.coerceIn(0, totalCount - 1)
    val progressTarget = if (isFinished) 1f else (safeIndex + 1).toFloat() / totalCount.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = if (motion.enabled) spring(dampingRatio = 0.78f, stiffness = 340f) else snap(),
        label = "lesson_test_progress",
    )

    val correctCount = remember(isFinished, deck, selectedAnswers.size) {
        if (!isFinished) {
            0
        } else {
            deck.indices.count { idx ->
                selectedAnswers[idx] == deck[idx].correctPresentedIndex
            }
        }
    }
    val scorePercent = (correctCount * 100) / totalCount
    val passed = scorePercent >= 80

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .testTag("lesson_test_overlay"),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Top Bar: Close + Progress Bar + Closed-Book Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = tactile.cardFace,
                        border = BorderStroke(tactile.cardBorderWidthDp.dp, tactile.cardOutline),
                    ) {
                        IconButton(
                            onClick = {
                                feedback.tap(view)
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("lesson_test_close_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.cd_close_detail),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(KovaExpressiveShapes.Pill)
                            .background(tactile.ringTrack),
                    ) {
                        val barFace = when {
                            isFinished && passed -> tactile.emeraldFace
                            isFinished && !passed -> tactile.coralFace
                            else -> tactile.cobaltFace
                        }
                        val barLip = when {
                            isFinished && passed -> tactile.emeraldLip
                            isFinished && !passed -> tactile.coralLip
                            else -> tactile.cobaltLip
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress.coerceIn(0.08f, 1f))
                                .fillMaxHeight()
                                .clip(KovaExpressiveShapes.Pill)
                                .background(barLip),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.72f)
                                    .clip(KovaExpressiveShapes.Pill)
                                    .background(barFace),
                            )
                        }
                    }

                    KovaPillBadge(
                        text = if (isFinished) "$scorePercent%" else "${safeIndex + 1}/$totalCount",
                        containerColor = if (isFinished && passed) tactile.emeraldSoft else tactile.cobaltSoft,
                        contentColor = if (isFinished && passed) tactile.onEmeraldSoft else tactile.onCobaltSoft,
                        borderColor = Color.Transparent,
                    )
                }

                // Main Content: Active Question vs. Results View
                AnimatedContent(
                    targetState = if (isFinished) -1 else safeIndex,
                    transitionSpec = {
                        if (motion.enabled) {
                            fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                        } else {
                            fadeIn(snap()) togetherWith fadeOut(snap())
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = "lesson_test_step_switch",
                ) { stepKey ->
                    if (stepKey == -1) {
                        LessonTestResultsBody(
                            lesson = lesson,
                            deck = deck,
                            selectedAnswers = selectedAnswers,
                            correctCount = correctCount,
                            totalCount = totalCount,
                            scorePercent = scorePercent,
                            passed = passed,
                            onRetryShuffled = {
                                feedback.tap(view)
                                attemptSeed += 1
                            },
                            onBackToLesson = {
                                feedback.tap(view)
                                onDismiss()
                            },
                            onBackToQuestPath = {
                                feedback.tap(view)
                                onReturnToQuestPath()
                            },
                        )
                    } else {
                        val item = deck[stepKey]
                        val pickedOption = selectedAnswers[stepKey]
                        LessonTestQuestionBody(
                            lesson = lesson,
                            questionIndex = stepKey,
                            totalCount = totalCount,
                            presented = item,
                            selectedOptionIndex = pickedOption,
                            onSelectOption = { optIdx ->
                                feedback.tap(view)
                                selectedAnswers[stepKey] = optIdx
                            },
                            onPrimaryNext = {
                                if (stepKey < totalCount - 1) {
                                    feedback.splitTick(view)
                                    currentIndex = stepKey + 1
                                } else {
                                    val earnedCorrect = deck.indices.count { idx ->
                                        selectedAnswers[idx] == deck[idx].correctPresentedIndex
                                    }
                                    val finalPercent = (earnedCorrect * 100) / totalCount
                                    isFinished = true
                                    if (finalPercent >= 80) {
                                        feedback.success(view)
                                        onTestPassed(finalPercent)
                                    } else {
                                        feedback.softThud(view)
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonTestQuestionBody(
    lesson: Lesson,
    questionIndex: Int,
    totalCount: Int,
    presented: PresentedQuestion,
    selectedOptionIndex: Int?,
    onSelectOption: (Int) -> Unit,
    onPrimaryNext: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val kindAccent = when (presented.source.kind) {
        LessonQuestionKind.PREDICT_OUTPUT -> tactile.cobaltFace
        LessonQuestionKind.SPOT_BUG -> tactile.coralFace
        LessonQuestionKind.FILL_KEYWORD -> tactile.amberFace
    }
    val kindSoft = when (presented.source.kind) {
        LessonQuestionKind.PREDICT_OUTPUT -> tactile.cobaltSoft
        LessonQuestionKind.SPOT_BUG -> tactile.coralSoft
        LessonQuestionKind.FILL_KEYWORD -> tactile.amberSoft
    }
    val kindOnSoft = when (presented.source.kind) {
        LessonQuestionKind.PREDICT_OUTPUT -> tactile.onCobaltSoft
        LessonQuestionKind.SPOT_BUG -> tactile.onCoralSoft
        LessonQuestionKind.FILL_KEYWORD -> tactile.onAmberSoft
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KovaPillBadge(
                    text = presented.source.kind.label.uppercase(),
                    containerColor = kindSoft,
                    contentColor = kindOnSoft,
                    borderColor = kindAccent.copy(alpha = 0.45f),
                )
                KovaPillBadge(
                    text = "LESSON ${lesson.code} · CLOSED BOOK",
                    containerColor = tactile.cardFace,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    borderColor = tactile.cardOutline,
                )
            }

            KovaTactileCard(
                shape = KovaExpressiveShapes.HeroChapterCard,
                faceColor = tactile.cardFace,
                lipColor = tactile.cardLip,
                borderColor = tactile.cardOutline,
                lipHeight = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = presented.source.prompt,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (!presented.source.codeSnippet.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.25.dp, kindAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = presented.source.codeSnippet,
                                style = KovaCodeTextStyle,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(13.dp),
                            )
                        }
                    }
                }
            }

            val letters = listOf("A", "B", "C", "D")
            presented.presentedOptions.forEachIndexed { idx, optionText ->
                val isSelected = selectedOptionIndex == idx
                KovaTactileCard(
                    shape = KovaExpressiveShapes.StatAndAssignmentCard,
                    faceColor = if (isSelected) tactile.cobaltSoft else tactile.cardFace,
                    lipColor = if (isSelected) tactile.cobaltLip else tactile.cardLip,
                    borderColor = if (isSelected) tactile.cobaltFace else tactile.cardOutline,
                    lipHeight = 4.dp,
                    onClick = { onSelectOption(idx) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lesson_test_option_$idx"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) tactile.cobaltFace else tactile.cardElevatedFace),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = letters.getOrElse(idx) { "${idx + 1}" },
                                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) tactile.onCobaltFace else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (isSelected) tactile.onCobaltSoft else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        val isLast = questionIndex >= totalCount - 1
        val canAdvance = selectedOptionIndex != null
        KovaTactileButton(
            onClick = {
                if (canAdvance) onPrimaryNext()
            },
            faceColor = if (canAdvance) tactile.emeraldFace else tactile.stoneFace,
            lipColor = if (canAdvance) tactile.emeraldLip else tactile.stoneLip,
            contentColor = if (canAdvance) tactile.onEmeraldFace else tactile.stoneLabel,
            minHeight = 52.dp,
            lipHeight = 5.dp,
            playTapSound = false,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lesson_test_next_button"),
        ) {
            Text(
                text = if (isLast) {
                    stringResource(R.string.lesson_test_finish_cta)
                } else {
                    stringResource(R.string.lesson_test_next_cta)
                },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (canAdvance) tactile.onEmeraldFace else tactile.stoneLabel,
            )
        }
    }
}

@Composable
private fun LessonTestResultsBody(
    lesson: Lesson,
    deck: List<PresentedQuestion>,
    selectedAnswers: Map<Int, Int>,
    correctCount: Int,
    totalCount: Int,
    scorePercent: Int,
    passed: Boolean,
    onRetryShuffled: () -> Unit,
    onBackToLesson: () -> Unit,
    onBackToQuestPath: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val missedItems = deck.mapIndexedNotNull { idx, item ->
        val picked = selectedAnswers[idx]
        if (picked != item.correctPresentedIndex) {
            Triple(idx + 1, item, picked)
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("lesson_test_results_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Hero Result Summary Card
            KovaTactileCard(
                shape = KovaExpressiveShapes.HeroChapterCard,
                faceColor = tactile.cardFace,
                lipColor = if (passed) tactile.emeraldLip else tactile.coralLip,
                borderColor = if (passed) tactile.emeraldFace else tactile.coralFace,
                lipHeight = 5.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KovaScallopedSealBadge(
                        icon = if (passed) Icons.Filled.EmojiEvents else Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        containerColor = if (passed) tactile.emeraldFace else tactile.coralFace,
                        lipColor = if (passed) tactile.emeraldLip else tactile.coralLip,
                        contentColor = if (passed) tactile.onEmeraldFace else tactile.onCoralFace,
                        size = 62.dp,
                        iconSize = 30.dp,
                    )
                    Text(
                        text = if (passed) "Lesson ${lesson.code} Mastered!" else "Almost There — Review & Retry",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "$correctCount / $totalCount Correct · $scorePercent%",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (passed) tactile.emeraldFace else tactile.coralFace,
                    )
                    Text(
                        text = if (passed) {
                            "Score saved to disk, next quest node unlocked, and Revision Mastery Radar updated!"
                        } else {
                            "You need 80% (4/5) to unlock the next node. Check the plain-English explanations below and retry with shuffled questions."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (missedItems.isNotEmpty()) {
                Text(
                    text = "PLAIN-ENGLISH EXPLANATIONS (${missedItems.size} MISSED)",
                    style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                    color = tactile.coralFace,
                )

                missedItems.forEach { (qNum, item, pickedIdx) ->
                    val pickedLabel = pickedIdx?.let { item.presentedOptions.getOrNull(it) } ?: "No answer"
                    val correctLabel = item.presentedOptions[item.correctPresentedIndex]

                    KovaTactileCard(
                        shape = KovaExpressiveShapes.StatAndAssignmentCard,
                        faceColor = tactile.cardFace,
                        lipColor = tactile.cardLip,
                        borderColor = tactile.coralFace.copy(alpha = 0.55f),
                        lipHeight = 4.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Text(
                                text = "Q$qNum · ${item.source.prompt}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Your pick: $pickedLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = tactile.coralFace,
                            )
                            Text(
                                text = "Correct answer: $correctLabel",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = tactile.emeraldFace,
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    tint = tactile.amberFace,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(16.dp),
                                )
                                Text(
                                    text = item.source.plainEnglishExplanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            } else if (passed) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = tactile.emeraldSoft,
                    border = BorderStroke(1.25.dp, tactile.emeraldFace),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = tactile.emeraldFace,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = "Flawless 5/5! Zero missed questions—your Kotlin intuition is locked in.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = tactile.onEmeraldSoft,
                        )
                    }
                }
            }
        }

        // Bottom Action Buttons
        if (passed) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KovaTactileButton(
                    onClick = onBackToLesson,
                    faceColor = tactile.cardFace,
                    lipColor = tactile.cardLip,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    minHeight = 50.dp,
                    lipHeight = 4.dp,
                    playTapSound = false,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lesson_test_stay_button"),
                ) {
                    Text(
                        text = "Review Lesson",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                KovaTactileButton(
                    onClick = onBackToQuestPath,
                    faceColor = tactile.emeraldFace,
                    lipColor = tactile.emeraldLip,
                    contentColor = tactile.onEmeraldFace,
                    minHeight = 50.dp,
                    lipHeight = 5.dp,
                    playTapSound = false,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lesson_test_continue_path_button"),
                ) {
                    Text(
                        text = "Quest Path",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = tactile.onEmeraldFace,
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KovaTactileButton(
                    onClick = onBackToLesson,
                    faceColor = tactile.cardFace,
                    lipColor = tactile.cardLip,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    minHeight = 50.dp,
                    lipHeight = 4.dp,
                    playTapSound = false,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lesson_test_review_lesson_button"),
                ) {
                    Text(
                        text = "Study Again",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                KovaTactileButton(
                    onClick = onRetryShuffled,
                    faceColor = tactile.amberFace,
                    lipColor = tactile.amberLip,
                    contentColor = tactile.onAmberFace,
                    minHeight = 50.dp,
                    lipHeight = 5.dp,
                    playTapSound = false,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lesson_test_retry_shuffled_button"),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Replay,
                        contentDescription = null,
                        tint = tactile.onAmberFace,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Retry Shuffled",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = tactile.onAmberFace,
                    )
                }
            }
        }
    }
}

private fun buildPresentedDeck(
    questions: List<LessonTestQuestion>,
    shuffle: Boolean,
    seed: Int,
): List<PresentedQuestion> {
    if (!shuffle) {
        return questions.map { q ->
            PresentedQuestion(
                source = q,
                presentedOptions = q.options,
                correctPresentedIndex = q.correctIndex.coerceIn(0, (q.options.size - 1).coerceAtLeast(0)),
            )
        }
    }
    val rng = Random(seed * 997 + questions.size * 31)
    return questions.shuffled(rng).map { q ->
        val indexed = q.options.mapIndexed { idx, text -> idx to text }.shuffled(rng)
        val newCorrectIdx = indexed.indexOfFirst { it.first == q.correctIndex }.coerceAtLeast(0)
        PresentedQuestion(
            source = q,
            presentedOptions = indexed.map { it.second },
            correctPresentedIndex = newCorrectIdx,
        )
    }
}
