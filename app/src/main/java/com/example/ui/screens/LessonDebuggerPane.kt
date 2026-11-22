package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Terminal
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.model.VariableWatchEntry
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaTactileCard
import com.example.ui.feedback.LocalKovaFeedback
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors
import kotlinx.coroutines.delay

/**
 * Interactive Guided Code Viewer + Step-by-Step Debugger Walkthrough inside the Code/IDE split pane.
 * - Color-coded Kotlin lines with role badges (Declaration, Execution, Branch, Output)
 * - Step Back / Step Forward / Auto-Play / Reset controls with animated active line & instant-restart audio/haptic ticks
 * - Auto-expands split ratio from 75:25 -> 50:50 whenever Step Forward or Auto-Play is tapped
 * - Live Variable & State Watch panel showing value diffs (e.g., dailyXp = 40 -> 55) + live Console Output
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonDebuggerPane(
    lessonId: String,
    walkthrough: DebuggerWalkthrough,
    splitRatio: LessonSplitRatio,
    onAutoExpandToHalf: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current

    val totalSteps = walkthrough.steps.size.coerceAtLeast(1)
    var currentStepIndex by rememberSaveable(lessonId) { mutableIntStateOf(0) }
    var isAutoPlaying by rememberSaveable(lessonId) { mutableStateOf(false) }

    val safeIndex = currentStepIndex.coerceIn(0, totalSteps - 1)
    val currentStep = walkthrough.steps.getOrNull(safeIndex)

    // Auto-play stepping loop with crisp audio/haptic ticks
    LaunchedEffect(isAutoPlaying, safeIndex, totalSteps) {
        if (!isAutoPlaying) return@LaunchedEffect
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

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = MaterialTheme.colorScheme.surfaceContainerLow,
        lipColor = tactile.cardLip,
        borderColor = tactile.cobaltFace.copy(alpha = 0.5f),
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
                verticalArrangement = Arrangement.spacedBy(if (isTightHeight) 6.dp else 10.dp),
            ) {
                // Header + Step Counter + Transport Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BugReport,
                            contentDescription = null,
                            tint = tactile.cobaltFace,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = walkthrough.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    KovaPillBadge(
                        text = "STEP ${safeIndex + 1}/$totalSteps",
                        containerColor = tactile.cobaltSoft,
                        contentColor = tactile.onCobaltSoft,
                        borderColor = Color.Transparent,
                    )
                }

                // Transport Bar: Reset / Back / Auto-Play / Forward
                DebuggerTransportBar(
                    canStepBack = safeIndex > 0,
                    isAutoPlaying = isAutoPlaying,
                    isAtEnd = safeIndex >= totalSteps - 1,
                    onReset = ::resetTrace,
                    onStepBack = ::stepBack,
                    onTogglePlay = ::toggleAutoPlay,
                    onStepForward = ::stepForward,
                )

                // Scrollable Code + Step Explanation + Variable Watch + Console
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    // Color-coded Kotlin Code Surface
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = tactile.cardFace,
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

                    // Active Step Plain-English Micro-Caption
                    if (currentStep != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = tactile.cobaltSoft.copy(alpha = 0.7f),
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
                                    style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                    color = tactile.onCobaltSoft,
                                )
                                Text(
                                    text = currentStep.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tactile.onCobaltSoft,
                                )
                            }
                        }

                        // Live Variable & State Watch Panel
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
                    text = if (isAutoPlaying) "Pause" else "Auto-Play",
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
    val tactile = LocalKovaTactileColors.current
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
            // Active indicator arrow + line number
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

            // Code text
            Text(
                text = line.code,
                style = KovaCodeTextStyle.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
                modifier = Modifier.weight(1f),
            )

            // Role Badge Pill (DECL / EXEC / BRANCH / OUTPUT)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(roleAccent.copy(alpha = if (isActive) 0.22f else 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = line.role.badgeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
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
        // Variable & State Watch
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
                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
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
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Console Output Strip (lights up on println step)
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
                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
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
