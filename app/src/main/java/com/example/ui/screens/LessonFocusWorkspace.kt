package com.example.ui.screens

import android.os.Build
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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
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
import androidx.compose.ui.draw.blur
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
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.KovaMonoBadgeTextStyle
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors

/**
 * Phase 2.2 Distraction-Free Lesson Focus Workspace:
 * - Split IDE pane defaults to OFF (isIdePaneOpen = false) so lessons open clean and full-screen.
 * - Tapping the IDE toggle opens the dark Kotlin workbench at 50:50 and exposes the split-ratio
 *   cycle pill (50:50 -> 25:75 -> 75:25) or closes the IDE back to OFF.
 * - Entering Debugger blurs the underlying lesson workspace (Android 12+ RenderEffect blur +
 *   dark frosted scrim) and opens the Adaptive 70:30 DebuggerFocusStageOverlay.
 * - Closed-Book Full-Screen Integrated Test Overlay covers the workspace during grading.
 */
@Composable
fun LessonFocusWorkspace(
    lesson: Lesson,
    savedHighlights: List<HighlightNote>,
    chapterProgress: Float,
    dailyRemaining: Int,
    splitRatio: LessonSplitRatio,
    isIdePaneOpen: Boolean,
    isDebuggerUnlocked: Boolean,
    isDebuggerStageOpen: Boolean,
    savedScrollPosition: Int = 0,
    activeExplainRuleId: String? = null,
    onScrollPositionChanged: (Int) -> Unit = {},
    onActiveExplainRuleChanged: (String?) -> Unit = {},
    onToggleIdePane: () -> Unit,
    onOpenIdeAtHalf: () -> Unit,
    onOpenDebuggerStage: () -> Unit,
    onCloseDebuggerStage: () -> Unit,
    onOpenAiFromLesson: () -> Unit,
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

    val blurModifier = if (isDebuggerStageOpen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(18.dp)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("lesson_focus_workspace"),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .then(blurModifier)
                .navigationBarsPadding(),
        ) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // Landscape: Vertical Progress Bar on LEFT side edge rail + Full/Split Panes
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LandscapeLeftProgressRail(
                        progress = animatedProgress,
                        isCompleted = isCompleted,
                        isIdePaneOpen = isIdePaneOpen,
                        splitRatio = splitRatio,
                        onToggleIdePane = {
                            feedback.chapterSwoosh(view)
                            onToggleIdePane()
                        },
                        onOpenAiFromLesson = {
                            feedback.tap(view)
                            onOpenAiFromLesson()
                        },
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
                            savedScrollPosition = savedScrollPosition,
                            activeExplainRuleId = activeExplainRuleId,
                            onScrollPositionChanged = onScrollPositionChanged,
                            onActiveExplainRuleChanged = onActiveExplainRuleChanged,
                            onOpenIdeForPractice = onOpenIdeAtHalf,
                            onSaveHighlight = onSaveHighlight,
                            onAskAiHighlight = onAskAiHighlight,
                            onStartIntegratedTest = { isTestOverlayOpen = true },
                            modifier = Modifier
                                .weight(if (isIdePaneOpen) animatedLessonWeight else 1f)
                                .fillMaxHeight(),
                        )
                        if (isIdePaneOpen) {
                            LessonDebuggerPane(
                                lessonId = lesson.id,
                                practiceTask = interactiveContent.practiceTask,
                                walkthrough = interactiveContent.debugger,
                                splitRatio = splitRatio,
                                isDebuggerUnlocked = isDebuggerUnlocked,
                                onOpenDebuggerStage = onOpenDebuggerStage,
                                onAutoExpandToHalf = { onSetSplitRatio(LessonSplitRatio.RATIO_50_50) },
                                modifier = Modifier
                                    .weight(animatedIdeWeight)
                                    .fillMaxHeight(),
                            )
                        }
                    }
                }
            } else {
                // Portrait: Horizontal Progress Bar across TOP + Full/Split Panes
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PortraitTopProgressBar(
                        progress = animatedProgress,
                        isCompleted = isCompleted,
                        isIdePaneOpen = isIdePaneOpen,
                        splitRatio = splitRatio,
                        onToggleIdePane = {
                            feedback.chapterSwoosh(view)
                            onToggleIdePane()
                        },
                        onOpenAiFromLesson = {
                            feedback.tap(view)
                            onOpenAiFromLesson()
                        },
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
                            savedScrollPosition = savedScrollPosition,
                            activeExplainRuleId = activeExplainRuleId,
                            onScrollPositionChanged = onScrollPositionChanged,
                            onActiveExplainRuleChanged = onActiveExplainRuleChanged,
                            onOpenIdeForPractice = onOpenIdeAtHalf,
                            onSaveHighlight = onSaveHighlight,
                            onAskAiHighlight = onAskAiHighlight,
                            onStartIntegratedTest = { isTestOverlayOpen = true },
                            modifier = Modifier
                                .weight(if (isIdePaneOpen) animatedLessonWeight else 1f)
                                .fillMaxWidth(),
                        )
                        if (isIdePaneOpen) {
                            LessonDebuggerPane(
                                lessonId = lesson.id,
                                practiceTask = interactiveContent.practiceTask,
                                walkthrough = interactiveContent.debugger,
                                splitRatio = splitRatio,
                                isDebuggerUnlocked = isDebuggerUnlocked,
                                onOpenDebuggerStage = onOpenDebuggerStage,
                                onAutoExpandToHalf = { onSetSplitRatio(LessonSplitRatio.RATIO_50_50) },
                                modifier = Modifier
                                    .weight(animatedIdeWeight)
                                    .fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }

        // Blurred-Background Adaptive 70:30 Debugger Stage Overlay
        AnimatedVisibility(
            visible = isDebuggerStageOpen,
            enter = if (motion.enabled) {
                fadeIn(tween(200)) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 340f),
                )
            } else {
                fadeIn(snap())
            },
            exit = if (motion.enabled) fadeOut(tween(160)) else fadeOut(snap()),
        ) {
            DebuggerFocusStageOverlay(
                lessonId = lesson.id,
                lessonCode = lesson.code,
                walkthrough = interactiveContent.debugger,
                onClose = onCloseDebuggerStage,
            )
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
    isIdePaneOpen: Boolean,
    splitRatio: LessonSplitRatio,
    onToggleIdePane: () -> Unit,
    onOpenAiFromLesson: () -> Unit,
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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

        // Compact AI Tutor shortcut (supports Lesson -> AI -> System Back -> Exact Lesson)
        Surface(
            shape = CircleShape,
            color = tactile.cobaltSoft,
            border = BorderStroke(tactile.cardBorderWidthDp.dp, tactile.cobaltFace.copy(alpha = 0.55f)),
        ) {
            IconButton(
                onClick = onOpenAiFromLesson,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("lesson_open_ai_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = stringResource(R.string.nav_ai),
                    tint = tactile.cobaltFace,
                    modifier = Modifier.size(19.dp),
                )
            }
        }

        // IDE Toggle Button (OFF by default; opens split IDE at 50:50)
        Surface(
            onClick = onToggleIdePane,
            shape = KovaExpressiveShapes.Pill,
            color = if (isIdePaneOpen) tactile.codeWorkbenchChrome else tactile.cardFace,
            border = BorderStroke(
                tactile.cardBorderWidthDp.dp,
                if (isIdePaneOpen) tactile.emeraldFace else tactile.cardOutline,
            ),
            modifier = Modifier
                .height(44.dp)
                .testTag("lesson_ide_toggle_button"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Code,
                    contentDescription = stringResource(R.string.cd_toggle_ide_pane),
                    tint = if (isIdePaneOpen) tactile.emeraldFace else tactile.cobaltFace,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.ide_toggle_label),
                    style = KovaMonoBadgeTextStyle,
                    color = if (isIdePaneOpen) tactile.codeWorkbenchText else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }

        // Split Ratio Cycle Pill (visible whenever IDE is open)
        if (isIdePaneOpen) {
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
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ViewAgenda,
                        contentDescription = stringResource(R.string.cd_cycle_split_ratio),
                        tint = tactile.cobaltFace,
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = splitRatio.badgeLabel,
                        style = KovaMonoBadgeTextStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun LandscapeLeftProgressRail(
    progress: Float,
    isCompleted: Boolean,
    isIdePaneOpen: Boolean,
    splitRatio: LessonSplitRatio,
    onToggleIdePane: () -> Unit,
    onOpenAiFromLesson: () -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
            shape = CircleShape,
            color = tactile.cobaltSoft,
            border = BorderStroke(tactile.cardBorderWidthDp.dp, tactile.cobaltFace.copy(alpha = 0.55f)),
        ) {
            IconButton(
                onClick = onOpenAiFromLesson,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("lesson_open_ai_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = stringResource(R.string.nav_ai),
                    tint = tactile.cobaltFace,
                    modifier = Modifier.size(19.dp),
                )
            }
        }

        Surface(
            onClick = onToggleIdePane,
            shape = RoundedCornerShape(16.dp),
            color = if (isIdePaneOpen) tactile.codeWorkbenchChrome else tactile.cardFace,
            border = BorderStroke(
                tactile.cardBorderWidthDp.dp,
                if (isIdePaneOpen) tactile.emeraldFace else tactile.cardOutline,
            ),
            modifier = Modifier
                .width(56.dp)
                .testTag("lesson_ide_toggle_button"),
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Code,
                    contentDescription = stringResource(R.string.cd_toggle_ide_pane),
                    tint = if (isIdePaneOpen) tactile.emeraldFace else tactile.cobaltFace,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.ide_toggle_label),
                    style = KovaMonoBadgeTextStyle,
                    color = if (isIdePaneOpen) tactile.codeWorkbenchText else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
        }

        if (isIdePaneOpen) {
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
                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
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
                        style = KovaMonoBadgeTextStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
