package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.VerticalSplit
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.Chapter1CurriculumContent
import com.example.model.HighlightCategory
import com.example.model.HighlightNote
import com.example.model.Lesson
import com.example.model.LessonSplitRatio
import com.example.model.LessonStatus
import com.example.ui.feedback.LocalKovaFeedback
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors

/**
 * Distraction-free Phase 2 Interactive Lesson Focus Workspace:
 * - Portrait (height >= width): Horizontal Lesson Progress Bar along the TOP + Top/Bottom split.
 * - Landscape (width > height): Vertical Lesson Progress Bar along the LEFT edge rail + Left/Right split.
 * - Smooth spring-animated Split Ratio Toggle cycling 75:25 -> 50:50 -> 25:75 -> 75:25.
 * - Lesson Pane: Plain-English Word Breakdowns, Core Concept Rules, Revision-synced Highlighting (+ Send to AI),
 *   Desired Output + collapsible Show Solution, and Integrated 5-Question Test launcher.
 * - Code/IDE Pane: Guided Code Viewer + Step-by-Step Debugger with live Variable & State Watch + Console Output
 *   (auto-expands 75:25 -> 50:50 whenever Play or Step is tapped).
 * - Closed-Book Full-Screen Integrated Test Overlay covering both panes during evaluation.
 */
@Composable
fun LessonFocusWorkspace(
    lesson: Lesson,
    savedHighlights: List<HighlightNote>,
    chapterProgress: Float,
    dailyRemaining: Int,
    splitRatio: LessonSplitRatio,
    onCycleSplitRatio: () -> Unit,
    onSetSplitRatio: (LessonSplitRatio) -> Unit,
    onSaveHighlight: (blockId: String, snippet: String, category: HighlightCategory, learnerNote: String) -> Unit,
    onAskAiHighlight: (blockId: String, snippet: String, learnerNote: String) -> Unit,
    onSubmitLessonTestPass: (scorePercent: Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = LocalKovaMotion.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current
    val isCompleted = lesson.status == LessonStatus.COMPLETED

    val interactiveContent = remember(lesson.id, lesson.interactiveContent) {
        lesson.interactiveContent ?: Chapter1CurriculumContent.fallbackForLesson(lesson)
    }

    var isTestOverlayOpen by rememberSaveable(lesson.id) { mutableStateOf(false) }

    val lessonProgress = if (isCompleted) 1f else (0.35f + chapterProgress * 0.45f).coerceIn(0.35f, 0.85f)
    val animatedProgress by animateFloatAsState(
        targetValue = lessonProgress,
        animationSpec = if (motion.enabled) {
            spring(dampingRatio = 0.76f, stiffness = 340f)
        } else {
            snap()
        },
        label = "lesson_focus_progress",
    )

    val animatedLessonWeight by animateFloatAsState(
        targetValue = splitRatio.lessonWeight,
        animationSpec = if (motion.enabled) {
            spring(dampingRatio = 0.76f, stiffness = 340f)
        } else {
            snap()
        },
        label = "lesson_split_primary_weight",
    )
    val animatedIdeWeight by animateFloatAsState(
        targetValue = splitRatio.ideWeight,
        animationSpec = if (motion.enabled) {
            spring(dampingRatio = 0.76f, stiffness = 340f)
        } else {
            snap()
        },
        label = "lesson_split_ide_weight",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("lesson_focus_workspace"),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // Landscape: Vertical Progress Bar on LEFT side edge rail + Left/Right Split Panes
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LandscapeLeftProgressRail(
                        progress = animatedProgress,
                        isCompleted = isCompleted,
                        splitRatio = splitRatio,
                        onCycleSplitRatio = {
                            feedback.splitTick(view)
                            onCycleSplitRatio()
                        },
                        onClose = {
                            feedback.tap(view)
                            onClose()
                        },
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LessonReaderPane(
                            lesson = lesson,
                            content = interactiveContent,
                            savedHighlights = savedHighlights,
                            dailyRemaining = dailyRemaining,
                            onSaveHighlight = onSaveHighlight,
                            onAskAiHighlight = onAskAiHighlight,
                            onStartIntegratedTest = { isTestOverlayOpen = true },
                            modifier = Modifier
                                .weight(animatedLessonWeight)
                                .fillMaxHeight(),
                        )
                        LessonDebuggerPane(
                            lessonId = lesson.id,
                            walkthrough = interactiveContent.debugger,
                            splitRatio = splitRatio,
                            onAutoExpandToHalf = { onSetSplitRatio(LessonSplitRatio.RATIO_50_50) },
                            modifier = Modifier
                                .weight(animatedIdeWeight)
                                .fillMaxHeight(),
                        )
                    }
                }
            } else {
                // Portrait: Horizontal Progress Bar across TOP + Top/Bottom Split Panes
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PortraitTopProgressBar(
                        progress = animatedProgress,
                        isCompleted = isCompleted,
                        splitRatio = splitRatio,
                        onCycleSplitRatio = {
                            feedback.splitTick(view)
                            onCycleSplitRatio()
                        },
                        onClose = {
                            feedback.tap(view)
                            onClose()
                        },
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LessonReaderPane(
                            lesson = lesson,
                            content = interactiveContent,
                            savedHighlights = savedHighlights,
                            dailyRemaining = dailyRemaining,
                            onSaveHighlight = onSaveHighlight,
                            onAskAiHighlight = onAskAiHighlight,
                            onStartIntegratedTest = { isTestOverlayOpen = true },
                            modifier = Modifier
                                .weight(animatedLessonWeight)
                                .fillMaxWidth(),
                        )
                        LessonDebuggerPane(
                            lessonId = lesson.id,
                            walkthrough = interactiveContent.debugger,
                            splitRatio = splitRatio,
                            onAutoExpandToHalf = { onSetSplitRatio(LessonSplitRatio.RATIO_50_50) },
                            modifier = Modifier
                                .weight(animatedIdeWeight)
                                .fillMaxWidth(),
                        )
                    }
                }
            }
        }

        // Full-Screen Closed-Book Integrated Lesson Test Overlay
        AnimatedVisibility(
            visible = isTestOverlayOpen,
            enter = if (motion.enabled) {
                fadeIn(tween(180)) + scaleIn(
                    initialScale = 0.96f,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 340f),
                )
            } else {
                fadeIn(snap())
            },
            exit = if (motion.enabled) fadeOut(tween(150)) else fadeOut(snap()),
        ) {
            LessonTestOverlay(
                lesson = lesson,
                questions = interactiveContent.testQuestions,
                onDismiss = { isTestOverlayOpen = false },
                onTestPassed = { earnedPercent ->
                    onSubmitLessonTestPass(earnedPercent)
                },
                onReturnToQuestPath = {
                    isTestOverlayOpen = false
                    onClose()
                },
            )
        }
    }
}

@Composable
private fun PortraitTopProgressBar(
    progress: Float,
    isCompleted: Boolean,
    splitRatio: LessonSplitRatio,
    onCycleSplitRatio: () -> Unit,
    onClose: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val fillFace = if (isCompleted) tactile.emeraldFace else tactile.cobaltFace
    val fillLip = if (isCompleted) tactile.emeraldLip else tactile.cobaltLip

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_progress_bar_portrait"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = tactile.cardFace,
            border = BorderStroke(tactile.cardBorderWidthDp.dp, tactile.cardOutline),
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("detail_close_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.cd_close_detail),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Sleek horizontal tactile 3D progress track
        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(KovaExpressiveShapes.Pill)
                .background(tactile.ringTrack),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0.06f, 1f))
                    .fillMaxHeight()
                    .clip(KovaExpressiveShapes.Pill)
                    .background(fillLip),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.72f)
                        .clip(KovaExpressiveShapes.Pill)
                        .background(fillFace),
                )
            }
        }

        // Split Ratio Toggle Pill (75:25 -> 50:50 -> 25:75)
        Surface(
            onClick = onCycleSplitRatio,
            shape = KovaExpressiveShapes.Pill,
            color = tactile.cardFace,
            border = BorderStroke(tactile.cardBorderWidthDp.dp, tactile.cobaltFace.copy(alpha = 0.6f)),
            modifier = Modifier
                .height(44.dp)
                .testTag("lesson_split_ratio_toggle"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ViewAgenda,
                    contentDescription = stringResource(R.string.cd_cycle_split_ratio),
                    tint = tactile.cobaltFace,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = splitRatio.badgeLabel,
                    style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
private fun LandscapeLeftProgressRail(
    progress: Float,
    isCompleted: Boolean,
    splitRatio: LessonSplitRatio,
    onCycleSplitRatio: () -> Unit,
    onClose: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val fillFace = if (isCompleted) tactile.emeraldFace else tactile.cobaltFace
    val fillLip = if (isCompleted) tactile.emeraldLip else tactile.cobaltLip

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(58.dp)
            .padding(vertical = 4.dp)
            .testTag("lesson_progress_bar_landscape"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = tactile.cardFace,
            border = BorderStroke(tactile.cardBorderWidthDp.dp, tactile.cardOutline),
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("detail_close_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.cd_close_detail),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Sleek vertical tactile progress rail along the LEFT edge
        Box(
            modifier = Modifier
                .weight(1f)
                .width(14.dp)
                .clip(KovaExpressiveShapes.Pill)
                .background(tactile.ringTrack),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(progress.coerceIn(0.08f, 1f))
                    .clip(KovaExpressiveShapes.Pill)
                    .background(fillLip),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .fillMaxHeight()
                        .clip(KovaExpressiveShapes.Pill)
                        .background(fillFace),
                )
            }
        }

        Surface(
            onClick = onCycleSplitRatio,
            shape = RoundedCornerShape(16.dp),
            color = tactile.cardFace,
            border = BorderStroke(tactile.cardBorderWidthDp.dp, tactile.cobaltFace.copy(alpha = 0.6f)),
            modifier = Modifier
                .width(56.dp)
                .testTag("lesson_split_ratio_toggle"),
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerticalSplit,
                    contentDescription = stringResource(R.string.cd_cycle_split_ratio),
                    tint = tactile.cobaltFace,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = splitRatio.badgeLabel,
                    style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
        }
    }
}
