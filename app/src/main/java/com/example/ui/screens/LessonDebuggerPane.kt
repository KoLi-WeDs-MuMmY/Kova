package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.CodeLineRole
import com.example.model.DebuggerCodeLine
import com.example.model.DebuggerWalkthrough
import com.example.model.LessonSplitRatio
import com.example.model.PracticeTaskSpec
import com.example.model.VariableWatchEntry
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaTactileCard
import com.example.ui.feedback.LocalKovaFeedback
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.KovaMonoBadgeTextStyle
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors
import kotlinx.coroutines.delay

/**
 * Phase 2.1 Kotlin Code & Practice IDE Workspace + "Unlock Debugger" Gate:
 * - Defaults to a real Kotlin IDE editor (Main.kt tab, line numbers gutter, syntax-tinted surface,
 *   editable practice code box, Run / Check evaluator + console output).
 * - Does NOT open directly into line-by-line debugging.
 * - Tapping the prominent "Unlock Debugger" toggle plays KovaFeedbackController.chapterSwoosh,
 *   marks isDebuggerUnlocked = true in ViewModel, auto-expands 75:25 -> 50:50 if tight, and reveals
 *   the Step Back / Step Forward / Auto-Play / Reset controls + Variable & State Watch drawer.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonDebuggerPane(
    lessonId: String,
    practiceTask: PracticeTaskSpec,
    walkthrough: DebuggerWalkthrough,
    splitRatio: LessonSplitRatio,
    isDebuggerUnlocked: Boolean,
    onUnlockDebugger: () -> Unit,
    onAutoExpandToHalf: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val motion = LocalKovaMotion.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current

    val initialEditorCode = remember(lessonId, practiceTask.starterCode, walkthrough.lines) {
        practiceTask.starterCode.ifBlank {
            walkthrough.lines.joinToString("\n") { it.code }
        }
    }
    var editorCode by rememberSaveable(lessonId) { mutableStateOf(initialEditorCode) }
    var runOutputStatus by rememberSaveable(lessonId) { mutableStateOf<Boolean?>(null) }
    var runOutputText by rememberSaveable(lessonId) { mutableStateOf("") }
    var runHintText by rememberSaveable(lessonId) { mutableStateOf("") }

    // Always opens in standard Practice/Editor mode by default (NOT debug mode),
    // even when debugger was previously unlocked for this lesson.
    var isShowingDebugger by rememberSaveable(lessonId) { mutableStateOf(false) }

    LaunchedEffect(isDebuggerUnlocked) {
        if (!isDebuggerUnlocked) {
            isShowingDebugger = false
        }
    }

    val totalSteps = walkthrough.steps.size.coerceAtLeast(1)
    var currentStepIndex by rememberSaveable(lessonId) { mutableIntStateOf(0) }
    var isAutoPlaying by rememberSaveable(lessonId) { mutableStateOf(false) }

    val safeIndex = currentStepIndex.coerceIn(0, totalSteps - 1)
    val currentStep = walkthrough.steps.getOrNull(safeIndex)

    // Auto-play stepping loop with crisp audio/haptic ticks
    LaunchedEffect(isAutoPlaying, isShowingDebugger, safeIndex, totalSteps) {
        if (!isAutoPlaying || !isShowingDebugger) return@LaunchedEffect
        if (safeIndex >= totalSteps - 1) {
            isAutoPlaying = false
            return@LaunchedEffect
        }
        delay(1350L)
        val next = (safeIndex + 1).coerceAtMost(totalSteps - 1)
        currentStepIndex = next
        feedback.splitTick(view)
        if (next >= totalSteps - 1) {
            isAutoPlaying = false
        }
    }

    fun handleUnlockOrToggleDebugger() {
        if (!isDebuggerUnlocked) {
            feedback.chapterSwoosh(view)
            onUnlockDebugger()
            isShowingDebugger = true
            if (splitRatio == LessonSplitRatio.RATIO_75_25) {
                onAutoExpandToHalf()
            }
        } else {
            feedback.tap(view)
            isAutoPlaying = false
            isShowingDebugger = !isShowingDebugger
            if (isShowingDebugger && splitRatio == LessonSplitRatio.RATIO_75_25) {
                onAutoExpandToHalf()
            }
        }
    }

    fun handleRunCheckCode() {
        if (splitRatio == LessonSplitRatio.RATIO_75_25) {
            onAutoExpandToHalf()
        }
        val normalized = editorCode.trim()
        val missingTokens = practiceTask.expectedKeywords.filterNot { keyword ->
            normalized.contains(keyword, ignoreCase = false)
        }
        if (normalized.isNotEmpty() && missingTokens.isEmpty()) {
            feedback.success(view)
            runOutputStatus = true
            runOutputText = practiceTask.desiredOutput
            runHintText = ""
        } else {
            feedback.softThud(view)
            runOutputStatus = false
            runOutputText = extractPreviewConsoleOutput(normalized)
            runHintText = if (practiceTask.editorHint.isNotBlank()) {
                practiceTask.editorHint
            } else {
                "Almost there! Check for: ${missingTokens.joinToString(", ")}"
            }
        }
    }

    fun stepForward() {
        isAutoPlaying = false
        if (splitRatio == LessonSplitRatio.RATIO_75_25) {
            onAutoExpandToHalf()
        }
        val next = if (safeIndex < totalSteps - 1) safeIndex + 1 else 0
        currentStepIndex = next
        feedback.splitTick(view)
    }

    fun stepBack() {
        isAutoPlaying = false
        if (safeIndex > 0) {
            currentStepIndex = safeIndex - 1
            feedback.splitTick(view)
        } else {
            feedback.tap(view)
        }
    }

    fun toggleAutoPlay() {
        if (isAutoPlaying) {
            isAutoPlaying = false
            feedback.tap(view)
        } else {
            if (splitRatio == LessonSplitRatio.RATIO_75_25) {
                onAutoExpandToHalf()
            }
            if (safeIndex >= totalSteps - 1) {
                currentStepIndex = 0
            }
            isAutoPlaying = true
            feedback.splitTick(view)
        }
    }

    fun resetTrace() {
        isAutoPlaying = false
        currentStepIndex = 0
        feedback.tap(view)
    }

    // Syntax-tinted IDE surface adapting cleanly to Daylight, Warm Dark, and True AMOLED
    val ideChromeFace = if (tactile.isAmoled) {
        Color(0xFF0A0C10)
    } else if (tactile.isDarkSurface) {
        Color(0xFF191D24)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val ideEditorWell = if (tactile.isAmoled) {
        Color(0xFF05070A)
    } else if (tactile.isDarkSurface) {
        Color(0xFF11151B)
    } else {
        tactile.cardFace
    }

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = ideChromeFace,
        lipColor = tactile.cardLip,
        borderColor = if (isShowingDebugger) tactile.amberFace.copy(alpha = 0.65f) else tactile.cobaltFace.copy(alpha = 0.55f),
        lipHeight = 5.dp,
        modifier = modifier.testTag("lesson_debugger_pane"),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            val isTightHeight = maxHeight < 185.dp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(if (isTightHeight) 7.dp else 10.dp),
            ) {
                // IDE Top Toolbar: Main.kt File Tab + Run / Check + Unlock Debugger Gate
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ide_workspace_toolbar"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    // Active Editor Filename Tab (Main.kt)
                    Surface(
                        shape = RoundedCornerShape(11.dp),
                        color = ideEditorWell,
                        border = BorderStroke(1.dp, tactile.cobaltFace.copy(alpha = 0.45f)),
                        modifier = Modifier.testTag("ide_file_tab_main_kt"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Code,
                                contentDescription = null,
                                tint = tactile.cobaltFace,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "Main.kt",
                                style = KovaMonoBadgeTextStyle,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Run / Check Action in Code & Practice Mode
                    if (!isShowingDebugger) {
                        Surface(
                            onClick = ::handleRunCheckCode,
                            shape = KovaExpressiveShapes.Pill,
                            color = tactile.emeraldFace,
                            border = BorderStroke(1.25.dp, tactile.emeraldLip),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("ide_run_check_button"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = tactile.onEmeraldFace,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = stringResource(R.string.ide_run_check_cta),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = tactile.onEmeraldFace,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }

                    // Prominent "Unlock Debugger" Toggle Button (or Code/Debugger Switcher once unlocked)
                    Surface(
                        onClick = ::handleUnlockOrToggleDebugger,
                        shape = KovaExpressiveShapes.Pill,
                        color = when {
                            isShowingDebugger -> tactile.amberFace
                            isDebuggerUnlocked -> tactile.cobaltSoft
                            else -> tactile.amberSoft
                        },
                        border = BorderStroke(
                            1.25.dp,
                            when {
                                isShowingDebugger -> tactile.amberLip
                                isDebuggerUnlocked -> tactile.cobaltFace
                                else -> tactile.amberFace
                            },
                        ),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("ide_unlock_debugger_button"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = if (!isDebuggerUnlocked) {
                                    Icons.Outlined.LockOpen
                                } else if (isShowingDebugger) {
                                    Icons.Outlined.Code
                                } else {
                                    Icons.Outlined.BugReport
                                },
                                contentDescription = null,
                                tint = when {
                                    isShowingDebugger -> tactile.onAmberFace
                                    isDebuggerUnlocked -> tactile.onCobaltSoft
                                    else -> tactile.onAmberSoft
                                },
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = when {
                                    !isDebuggerUnlocked -> stringResource(R.string.ide_unlock_debugger_cta)
                                    isShowingDebugger -> stringResource(R.string.ide_practice_mode_cta)
                                    else -> "Debugger"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = when {
                                    isShowingDebugger -> tactile.onAmberFace
                                    isDebuggerUnlocked -> tactile.onCobaltSoft
                                    else -> tactile.onAmberSoft
                                },
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }

                // Body: Default Code & Practice Mode vs. Unlocked Step-by-Step Debugger Mode
                if (!isShowingDebugger) {
                    CodePracticeWorkspaceBody(
                        editorCode = editorCode,
                        desiredOutput = practiceTask.desiredOutput,
                        ideEditorWell = ideEditorWell,
                        runOutputStatus = runOutputStatus,
                        runOutputText = runOutputText,
                        runHintText = runHintText,
                        onCodeChange = { updated ->
                            editorCode = updated
                            if (runOutputStatus != null) {
                                runOutputStatus = null
                                runHintText = ""
                            }
                        },
                        onResetStarterCode = {
                            feedback.tap(view)
                            editorCode = initialEditorCode
                            runOutputStatus = null
                            runOutputText = ""
                            runHintText = ""
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                } else {
                    AnimatedVisibility(
                        visible = isShowingDebugger,
                        enter = if (motion.enabled) {
                            expandVertically(spring(dampingRatio = 0.78f, stiffness = 340f)) + fadeIn()
                        } else {
                            fadeIn(snap())
                        },
                        exit = if (motion.enabled) {
                            shrinkVertically(spring(dampingRatio = 0.82f, stiffness = 360f)) + fadeOut()
                        } else {
                            fadeOut(snap())
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(if (isTightHeight) 6.dp else 9.dp),
                        ) {
                            // Debugger Step Counter + Transport Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = walkthrough.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                KovaPillBadge(
                                    text = "STEP ${safeIndex + 1}/$totalSteps",
                                    containerColor = tactile.cobaltSoft,
                                    contentColor = tactile.onCobaltSoft,
                                    borderColor = Color.Transparent,
                                )
                            }

                            DebuggerTransportBar(
                                canStepBack = safeIndex > 0,
                                isAutoPlaying = isAutoPlaying,
                                isAtEnd = safeIndex >= totalSteps - 1,
                                onReset = ::resetTrace,
                                onStepBack = ::stepBack,
                                onTogglePlay = ::toggleAutoPlay,
                                onStepForward = ::stepForward,
                            )

                            // Scrollable Animated Code Lines + Caption + Live Variable & State Watch
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(9.dp),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(15.dp),
                                    color = ideEditorWell,
                                    border = BorderStroke(1.dp, tactile.cardOutline),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("debugger_code_surface"),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        walkthrough.lines.forEach { codeLine ->
                                            DebuggerCodeLineRow(
                                                line = codeLine,
                                                isActive = currentStep?.activeLineNumber == codeLine.lineNumber,
                                            )
                                        }
                                    }
                                }

                                if (currentStep != null) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = tactile.cobaltSoft.copy(alpha = 0.72f),
                                        border = BorderStroke(1.dp, tactile.cobaltFace.copy(alpha = 0.45f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("debugger_step_caption"),
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp),
                                        ) {
                                            Text(
                                                text = "LINE ${currentStep.activeLineNumber} · ${currentStep.headline}",
                                                style = KovaMonoBadgeTextStyle,
                                                color = tactile.onCobaltSoft,
                                            )
                                            Text(
                                                text = currentStep.explanation,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = tactile.onCobaltSoft,
                                            )
                                        }
                                    }

                                    VariableWatchPanel(
                                        variables = currentStep.variables,
                                        consoleOutput = currentStep.consoleOutput,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CodePracticeWorkspaceBody(
    editorCode: String,
    desiredOutput: String,
    ideEditorWell: Color,
    runOutputStatus: Boolean?,
    runOutputText: String,
    runHintText: String,
    onCodeChange: (String) -> Unit,
    onResetStarterCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val lineCount = remember(editorCode) { editorCode.lines().size.coerceAtLeast(4) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .testTag("ide_code_practice_body"),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        // Real IDE Code Editor Well with Line-Numbers Gutter
        Surface(
            shape = RoundedCornerShape(15.dp),
            color = ideEditorWell,
            border = BorderStroke(1.dp, tactile.cardOutline),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Subtle editor status strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "KOTLIN PRACTICE EDITOR · EDITABLE",
                        style = KovaMonoBadgeTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        onClick = onResetStarterCode,
                        shape = KovaExpressiveShapes.Pill,
                        color = tactile.cardFace,
                        border = BorderStroke(1.dp, tactile.cardOutline),
                        modifier = Modifier.testTag("ide_reset_starter_button"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Replay,
                                contentDescription = "Reset starter code",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = stringResource(R.string.ide_reset_code_cta),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = tactile.cardOutline.copy(alpha = 0.6f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    // Line Numbers Gutter (JetBrains Mono)
                    Column(
                        modifier = Modifier
                            .width(34.dp)
                            .padding(start = 8.dp, end = 6.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        for (lineNum in 1..lineCount) {
                            Text(
                                text = "$lineNum",
                                style = KovaCodeTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            )
                        }
                    }

                    // Subtle vertical gutter divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(tactile.cardOutline.copy(alpha = 0.65f)),
                    )

                    // Editable Kotlin Code Area
                    BasicTextField(
                        value = editorCode,
                        onValueChange = onCodeChange,
                        textStyle = KovaCodeTextStyle.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(tactile.cobaltFace),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                            .testTag("ide_code_editor"),
                    )
                }
            }
        }

        // Responsive Desired vs Actual Output Comparison Console
        val statusBadgeText = when (runOutputStatus) {
            true -> "MATCHED"
            false -> "DIFF DETECTED"
            null -> "READY"
        }
        val statusBg = when (runOutputStatus) {
            true -> tactile.emeraldSoft
            false -> tactile.amberSoft
            null -> tactile.cardFace
        }
        val statusBorder = when (runOutputStatus) {
            true -> tactile.emeraldFace
            false -> tactile.amberFace
            null -> tactile.cardOutline
        }
        val statusTint = when (runOutputStatus) {
            true -> tactile.onEmeraldSoft
            false -> tactile.onAmberSoft
            null -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = statusBg,
            border = BorderStroke(1.25.dp, statusBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ide_practice_console"),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = if (runOutputStatus == true) Icons.Outlined.CheckCircle else Icons.Outlined.Terminal,
                            contentDescription = null,
                            tint = statusTint,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "OUTPUT COMPARISON",
                            style = KovaMonoBadgeTextStyle,
                            color = statusTint,
                        )
                    }
                    KovaPillBadge(
                        text = statusBadgeText,
                        containerColor = ideEditorWell,
                        contentColor = statusTint,
                        borderColor = statusBorder.copy(alpha = 0.6f),
                    )
                }

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val useSideBySide = maxWidth >= 420.dp
                    val actualDisplay = when {
                        runOutputText.isNotBlank() -> runOutputText
                        else -> "Tap Run / Check to evaluate Main.kt"
                    }

                    if (useSideBySide) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ide_output_comparison_side_by_side"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            IdeOutputComparisonTile(
                                title = stringResource(R.string.ide_desired_output_label),
                                body = desiredOutput,
                                accentColor = tactile.emeraldFace,
                                wellColor = ideEditorWell,
                                useCodeFont = true,
                                modifier = Modifier.weight(1f),
                            )
                            IdeOutputComparisonTile(
                                title = stringResource(R.string.ide_actual_output_label),
                                body = actualDisplay,
                                accentColor = when (runOutputStatus) {
                                    true -> tactile.emeraldFace
                                    false -> tactile.amberFace
                                    null -> tactile.cobaltFace
                                },
                                wellColor = ideEditorWell,
                                useCodeFont = runOutputStatus != null,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ide_output_comparison_stacked"),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            IdeOutputComparisonTile(
                                title = stringResource(R.string.ide_desired_output_label),
                                body = desiredOutput,
                                accentColor = tactile.emeraldFace,
                                wellColor = ideEditorWell,
                                useCodeFont = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            IdeOutputComparisonTile(
                                title = stringResource(R.string.ide_actual_output_label),
                                body = actualDisplay,
                                accentColor = when (runOutputStatus) {
                                    true -> tactile.emeraldFace
                                    false -> tactile.amberFace
                                    null -> tactile.cobaltFace
                                },
                                wellColor = ideEditorWell,
                                useCodeFont = runOutputStatus != null,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                if (runOutputStatus == false && runHintText.isNotBlank()) {
                    Text(
                        text = "Hint: $runHintText",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = statusTint,
                    )
                }
            }
        }
    }
}

@Composable
private fun IdeOutputComparisonTile(
    title: String,
    body: String,
    accentColor: Color,
    wellColor: Color,
    useCodeFont: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(11.dp),
        color = wellColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = KovaMonoBadgeTextStyle,
                color = accentColor,
                maxLines = 1,
                softWrap = false,
            )
            Text(
                text = body,
                style = if (useCodeFont) KovaCodeTextStyle else MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun extractPreviewConsoleOutput(code: String): String {
    val printlnRegex = Regex("""println\s*\(([^)]*)\)""")
    val matches = printlnRegex.findAll(code)
        .map { it.groupValues[1].trim().removeSurrounding("\"") }
        .filter { it.isNotEmpty() }
        .toList()
    return if (matches.isNotEmpty()) {
        matches.joinToString("\n")
    } else {
        "(No println output detected yet)"
    }
}

@Composable
private fun DebuggerTransportBar(
    canStepBack: Boolean,
    isAutoPlaying: Boolean,
    isAtEnd: Boolean,
    onReset: () -> Unit,
    onStepBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onStepForward: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("debugger_transport_bar"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Reset
        Surface(
            shape = CircleShape,
            color = tactile.cardFace,
            border = BorderStroke(1.dp, tactile.cardOutline),
        ) {
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("debugger_reset_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Replay,
                    contentDescription = stringResource(R.string.cd_debugger_reset),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        // Step Back
        Surface(
            shape = CircleShape,
            color = tactile.cardFace,
            border = BorderStroke(1.dp, tactile.cardOutline),
        ) {
            IconButton(
                onClick = onStepBack,
                enabled = canStepBack,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("debugger_step_back_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.SkipPrevious,
                    contentDescription = stringResource(R.string.cd_debugger_step_back),
                    tint = if (canStepBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Auto-Play / Pause Pill
        Surface(
            onClick = onTogglePlay,
            shape = KovaExpressiveShapes.Pill,
            color = if (isAutoPlaying) tactile.amberFace else tactile.emeraldFace,
            border = BorderStroke(1.25.dp, if (isAutoPlaying) tactile.amberLip else tactile.emeraldLip),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .testTag("debugger_play_pause_button"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = if (isAutoPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = if (isAutoPlaying) tactile.onAmberFace else tactile.onEmeraldFace,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isAutoPlaying) "Pause" else "Play",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isAutoPlaying) tactile.onAmberFace else tactile.onEmeraldFace,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }

        // Step Forward Pill
        Surface(
            onClick = onStepForward,
            shape = KovaExpressiveShapes.Pill,
            color = tactile.cobaltFace,
            border = BorderStroke(1.25.dp, tactile.cobaltLip),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .testTag("debugger_step_forward_button"),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (isAtEnd) "Replay" else "Step",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = tactile.onCobaltFace,
                    maxLines = 1,
                    softWrap = false,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.SkipNext,
                    contentDescription = stringResource(R.string.cd_debugger_step_forward),
                    tint = tactile.onCobaltFace,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun DebuggerCodeLineRow(
    line: DebuggerCodeLine,
    isActive: Boolean,
) {
    val motion = LocalKovaMotion.current
    val roleAccent = line.role.accentColor()
    val roleSoft = line.role.softColor()

    val rowBg by animateColorAsState(
        targetValue = if (isActive) roleSoft else Color.Transparent,
        animationSpec = if (motion.enabled) spring(dampingRatio = 0.8f, stiffness = 360f) else snap(),
        label = "debugger_line_bg_${line.lineNumber}",
    )
    val rowBorder by animateColorAsState(
        targetValue = if (isActive) roleAccent else Color.Transparent,
        animationSpec = if (motion.enabled) spring(dampingRatio = 0.8f, stiffness = 360f) else snap(),
        label = "debugger_line_border_${line.lineNumber}",
    )

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = rowBg,
        border = if (isActive) BorderStroke(1.5.dp, rowBorder) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("debugger_line_${line.lineNumber}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(
                modifier = Modifier.width(24.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (isActive) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Active line",
                        tint = roleAccent,
                        modifier = Modifier.size(16.dp),
                    )
                } else {
                    Text(
                        text = "${line.lineNumber}",
                        style = KovaCodeTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    )
                }
            }

            Text(
                text = line.code,
                style = KovaCodeTextStyle.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
                modifier = Modifier.weight(1f),
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(roleAccent.copy(alpha = if (isActive) 0.22f else 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = line.role.badgeLabel,
                    style = KovaMonoBadgeTextStyle,
                    color = roleAccent,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VariableWatchPanel(
    variables: List<VariableWatchEntry>,
    consoleOutput: List<String>,
) {
    val tactile = LocalKovaTactileColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("debugger_variable_watch_panel"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = tactile.cardFace,
            border = BorderStroke(1.dp, tactile.cardOutline),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Memory,
                        contentDescription = null,
                        tint = tactile.amberFace,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.debugger_variable_watch_title),
                        style = KovaMonoBadgeTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (variables.isEmpty()) {
                    Text(
                        text = "No variables allocated yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        variables.forEach { entry ->
                            val chipBg = if (entry.isChanged) tactile.amberSoft else MaterialTheme.colorScheme.surfaceContainerLow
                            val chipBorder = if (entry.isChanged) tactile.amberFace else tactile.cardOutline
                            val valueDisplay = if (entry.previousValue != null) {
                                "${entry.previousValue} -> ${entry.value}"
                            } else {
                                entry.value
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = chipBg,
                                border = BorderStroke(if (entry.isChanged) 1.25.dp else 1.dp, chipBorder),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    Text(
                                        text = "${entry.name}:",
                                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                        color = if (entry.isChanged) tactile.onAmberSoft else MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = valueDisplay,
                                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                        color = if (entry.isChanged) tactile.amberFace else tactile.cobaltFace,
                                    )
                                    Text(
                                        text = "(${entry.typeName})",
                                        style = KovaMonoBadgeTextStyle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        val hasOutput = consoleOutput.isNotEmpty()
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (hasOutput) tactile.emeraldSoft else tactile.cardFace,
            border = BorderStroke(1.25.dp, if (hasOutput) tactile.emeraldFace else tactile.cardOutline),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("debugger_console_panel"),
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Terminal,
                        contentDescription = null,
                        tint = if (hasOutput) tactile.emeraldFace else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.debugger_console_output_title),
                        style = KovaMonoBadgeTextStyle,
                        color = if (hasOutput) tactile.onEmeraldSoft else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (!hasOutput) {
                    Text(
                        text = "Waiting for println(...) step…",
                        style = KovaCodeTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    )
                } else {
                    consoleOutput.forEach { outLine ->
                        Text(
                            text = "> $outLine",
                            style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                            color = tactile.onEmeraldSoft,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeLineRole.accentColor(): Color {
    val tactile = LocalKovaTactileColors.current
    return when (this) {
        CodeLineRole.DECLARATION -> tactile.cobaltFace
        CodeLineRole.EXECUTION -> tactile.amberFace
        CodeLineRole.DECISION -> tactile.coralFace
        CodeLineRole.OUTPUT -> tactile.emeraldFace
    }
}

@Composable
private fun CodeLineRole.softColor(): Color {
    val tactile = LocalKovaTactileColors.current
    return when (this) {
        CodeLineRole.DECLARATION -> tactile.cobaltSoft
        CodeLineRole.EXECUTION -> tactile.amberSoft
        CodeLineRole.DECISION -> tactile.coralSoft
        CodeLineRole.OUTPUT -> tactile.emeraldSoft
    }
}
