package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lightbulb
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
import com.example.model.DebuggerStep
import com.example.model.DebuggerWalkthrough
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
 * Phase 2.2 Blurred-Background Adaptive 70:30 Debugger Stage Overlay:
 * - Renders over the blurred LessonFocusWorkspace with a frosted dark scrim.
 * - Intercepts System Back cleanly via BackHandler to close the stage.
 * - Adaptive 70:30 split via BoxWithConstraints:
 *   * Portrait (maxWidth <= maxHeight): 70% top Debugger Workbench + 30% bottom Step Explanation.
 *   * Landscape / Tablet (maxWidth > maxHeight): 70% left Debugger Workbench + 30% right Step Explanation.
 */
@Composable
fun DebuggerFocusStageOverlay(
    lessonId: String,
    lessonCode: String,
    walkthrough: DebuggerWalkthrough,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current

    BackHandler(onBack = {
        feedback.tap(view)
        onClose()
    })

    val totalSteps = walkthrough.steps.size.coerceAtLeast(1)
    var currentStepIndex by rememberSaveable("stage_$lessonId") { mutableIntStateOf(0) }
    var isAutoPlaying by rememberSaveable("stage_play_$lessonId") { mutableStateOf(false) }

    val safeIndex = currentStepIndex.coerceIn(0, totalSteps - 1)
    val currentStep = walkthrough.steps.getOrNull(safeIndex)

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f))
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("debugger_focus_stage_overlay"),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Stage Top Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KovaPillBadge(
                    text = stringResource(R.string.debugger_stage_badge),
                    leadingIcon = Icons.Outlined.BugReport,
                    containerColor = tactile.amberFace,
                    contentColor = tactile.onAmberFace,
                    borderColor = tactile.amberLip,
                )

                KovaPillBadge(
                    text = "L$lessonCode · STEP ${safeIndex + 1}/$totalSteps",
                    containerColor = tactile.codeWorkbenchChrome,
                    contentColor = tactile.codeWorkbenchText,
                    borderColor = tactile.codeWorkbenchBorder,
                )

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = CircleShape,
                    color = tactile.cardFace,
                    border = BorderStroke(1.25.dp, tactile.cardOutline),
                ) {
                    IconButton(
                        onClick = {
                            isAutoPlaying = false
                            feedback.tap(view)
                            onClose()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("debugger_stage_close_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.cd_close_detail),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            // Adaptive 70:30 Debugger Layout (Portrait: Vertical 70/30, Landscape: Side-by-Side 70/30)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                val isLandscape = maxWidth > maxHeight

                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("debugger_stage_landscape_70_30"),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        DebuggerWorkbenchSection(
                            walkthrough = walkthrough,
                            currentStep = currentStep,
                            canStepBack = safeIndex > 0,
                            isAutoPlaying = isAutoPlaying,
                            isAtEnd = safeIndex >= totalSteps - 1,
                            onReset = ::resetTrace,
                            onStepBack = ::stepBack,
                            onTogglePlay = ::toggleAutoPlay,
                            onStepForward = ::stepForward,
                            modifier = Modifier
                                .weight(0.70f)
                                .fillMaxHeight(),
                        )
                        DebuggerStepExplanationCard(
                            step = currentStep,
                            stepNumber = safeIndex + 1,
                            totalSteps = totalSteps,
                            modifier = Modifier
                                .weight(0.30f)
                                .fillMaxHeight(),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("debugger_stage_portrait_70_30"),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        DebuggerWorkbenchSection(
                            walkthrough = walkthrough,
                            currentStep = currentStep,
                            canStepBack = safeIndex > 0,
                            isAutoPlaying = isAutoPlaying,
                            isAtEnd = safeIndex >= totalSteps - 1,
                            onReset = ::resetTrace,
                            onStepBack = ::stepBack,
                            onTogglePlay = ::toggleAutoPlay,
                            onStepForward = ::stepForward,
                            modifier = Modifier
                                .weight(0.70f)
                                .fillMaxWidth(),
                        )
                        DebuggerStepExplanationCard(
                            step = currentStep,
                            stepNumber = safeIndex + 1,
                            totalSteps = totalSteps,
                            modifier = Modifier
                                .weight(0.30f)
                                .fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DebuggerWorkbenchSection(
    walkthrough: DebuggerWalkthrough,
    currentStep: DebuggerStep?,
    canStepBack: Boolean,
    isAutoPlaying: Boolean,
    isAtEnd: Boolean,
    onReset: () -> Unit,
    onStepBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onStepForward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.codeWorkbenchChrome,
        lipColor = tactile.amberLip,
        borderColor = tactile.amberFace.copy(alpha = 0.7f),
        lipHeight = 5.dp,
        modifier = modifier.testTag("debugger_stage_workbench_70"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Walkthrough Title + Transport Controls
            Text(
                text = walkthrough.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = tactile.codeWorkbenchText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            StageDebuggerTransportBar(
                canStepBack = canStepBack,
                isAutoPlaying = isAutoPlaying,
                isAtEnd = isAtEnd,
                onReset = onReset,
                onStepBack = onStepBack,
                onTogglePlay = onTogglePlay,
                onStepForward = onStepForward,
            )

            // Scrollable Dark Code Trace + Live Variable Watch + Console Output
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = tactile.codeWorkbenchWell,
                    border = BorderStroke(1.dp, tactile.codeWorkbenchBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debugger_stage_code_surface"),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        walkthrough.lines.forEach { codeLine ->
                            StageDebuggerCodeLineRow(
                                line = codeLine,
                                isActive = currentStep?.activeLineNumber == codeLine.lineNumber,
                            )
                        }
                    }
                }

                if (currentStep != null) {
                    StageVariableAndConsolePanel(
                        variables = currentStep.variables,
                        consoleOutput = currentStep.consoleOutput,
                    )
                }
            }
        }
    }
}

@Composable
private fun DebuggerStepExplanationCard(
    step: DebuggerStep?,
    stepNumber: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.cobaltLip,
        borderColor = tactile.cobaltFace.copy(alpha = 0.7f),
        lipHeight = 5.dp,
        modifier = modifier.testTag("debugger_stage_explanation_30"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = tactile.amberFace,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.debugger_step_explanation_title),
                    style = KovaMonoBadgeTextStyle,
                    color = tactile.cobaltFace,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (step != null) {
                    KovaPillBadge(
                        text = "LINE ${step.activeLineNumber}",
                        containerColor = tactile.cobaltSoft,
                        contentColor = tactile.onCobaltSoft,
                        borderColor = Color.Transparent,
                    )
                }
            }

            if (step != null) {
                Text(
                    text = step.headline,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = step.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val changedVars = step.variables.filter { it.isChanged }
                if (changedVars.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(11.dp),
                        color = tactile.amberSoft,
                        border = BorderStroke(1.dp, tactile.amberFace.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = "CHANGED ON STEP $stepNumber/$totalSteps",
                                style = KovaMonoBadgeTextStyle,
                                color = tactile.onAmberSoft,
                            )
                            changedVars.forEach { entry ->
                                val delta = if (entry.previousValue != null) {
                                    "${entry.name}: ${entry.previousValue} -> ${entry.value}"
                                } else {
                                    "${entry.name} = ${entry.value} (${entry.typeName})"
                                }
                                Text(
                                    text = delta,
                                    style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                    color = tactile.onAmberSoft,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StageDebuggerTransportBar(
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
            .testTag("debugger_stage_transport_bar"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = tactile.codeWorkbenchWell,
            border = BorderStroke(1.dp, tactile.codeWorkbenchBorder),
        ) {
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("debugger_stage_reset_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Replay,
                    contentDescription = stringResource(R.string.cd_debugger_reset),
                    tint = tactile.codeWorkbenchText,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = tactile.codeWorkbenchWell,
            border = BorderStroke(1.dp, tactile.codeWorkbenchBorder),
        ) {
            IconButton(
                onClick = onStepBack,
                enabled = canStepBack,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("debugger_stage_step_back_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.SkipPrevious,
                    contentDescription = stringResource(R.string.cd_debugger_step_back),
                    tint = if (canStepBack) tactile.codeWorkbenchText else tactile.codeWorkbenchGutterText.copy(alpha = 0.45f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Surface(
            onClick = onTogglePlay,
            shape = KovaExpressiveShapes.Pill,
            color = if (isAutoPlaying) tactile.amberFace else tactile.emeraldFace,
            border = BorderStroke(1.25.dp, if (isAutoPlaying) tactile.amberLip else tactile.emeraldLip),
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("debugger_stage_play_pause_button"),
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

        Surface(
            onClick = onStepForward,
            shape = KovaExpressiveShapes.Pill,
            color = tactile.cobaltFace,
            border = BorderStroke(1.25.dp, tactile.cobaltLip),
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("debugger_stage_step_forward_button"),
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
private fun StageDebuggerCodeLineRow(
    line: DebuggerCodeLine,
    isActive: Boolean,
) {
    val tactile = LocalKovaTactileColors.current
    val motion = LocalKovaMotion.current
    val roleAccent = when (line.role) {
        CodeLineRole.DECLARATION -> tactile.cobaltFace
        CodeLineRole.EXECUTION -> tactile.amberFace
        CodeLineRole.DECISION -> tactile.coralFace
        CodeLineRole.OUTPUT -> tactile.emeraldFace
    }

    val rowBg by animateColorAsState(
        targetValue = if (isActive) roleAccent.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = if (motion.enabled) spring(dampingRatio = 0.8f, stiffness = 360f) else snap(),
        label = "stage_line_bg_${line.lineNumber}",
    )
    val rowBorder by animateColorAsState(
        targetValue = if (isActive) roleAccent else Color.Transparent,
        animationSpec = if (motion.enabled) spring(dampingRatio = 0.8f, stiffness = 360f) else snap(),
        label = "stage_line_border_${line.lineNumber}",
    )

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = rowBg,
        border = if (isActive) BorderStroke(1.5.dp, rowBorder) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("debugger_stage_line_${line.lineNumber}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        color = tactile.codeWorkbenchGutterText,
                    )
                }
            }

            Text(
                text = line.code,
                style = KovaCodeTextStyle.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                ),
                color = if (isActive) tactile.codeWorkbenchText else tactile.codeWorkbenchText.copy(alpha = 0.82f),
                modifier = Modifier.weight(1f),
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(roleAccent.copy(alpha = if (isActive) 0.28f else 0.14f))
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
private fun StageVariableAndConsolePanel(
    variables: List<VariableWatchEntry>,
    consoleOutput: List<String>,
) {
    val tactile = LocalKovaTactileColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("debugger_stage_variable_watch"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = tactile.codeWorkbenchWell,
            border = BorderStroke(1.dp, tactile.codeWorkbenchBorder),
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
                        color = tactile.codeWorkbenchGutterText,
                    )
                }

                if (variables.isEmpty()) {
                    Text(
                        text = "No variables allocated yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = tactile.codeWorkbenchGutterText,
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        variables.forEach { entry ->
                            val chipBg = if (entry.isChanged) {
                                tactile.amberFace.copy(alpha = 0.20f)
                            } else {
                                tactile.codeWorkbenchChrome
                            }
                            val chipBorder = if (entry.isChanged) tactile.amberFace else tactile.codeWorkbenchBorder
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
                                        color = tactile.codeWorkbenchText,
                                    )
                                    Text(
                                        text = valueDisplay,
                                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                        color = if (entry.isChanged) tactile.amberFace else tactile.cobaltFace,
                                    )
                                    Text(
                                        text = "(${entry.typeName})",
                                        style = KovaMonoBadgeTextStyle,
                                        color = tactile.codeWorkbenchGutterText,
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
            color = if (hasOutput) tactile.emeraldFace.copy(alpha = 0.14f) else tactile.codeWorkbenchWell,
            border = BorderStroke(1.25.dp, if (hasOutput) tactile.emeraldFace else tactile.codeWorkbenchBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("debugger_stage_console_panel"),
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
                        tint = if (hasOutput) tactile.emeraldFace else tactile.codeWorkbenchGutterText,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.debugger_console_output_title),
                        style = KovaMonoBadgeTextStyle,
                        color = if (hasOutput) tactile.emeraldFace else tactile.codeWorkbenchGutterText,
                    )
                }

                if (!hasOutput) {
                    Text(
                        text = "Waiting for println(...) step…",
                        style = KovaCodeTextStyle,
                        color = tactile.codeWorkbenchGutterText,
                    )
                } else {
                    consoleOutput.forEach { outLine ->
                        Text(
                            text = "> $outLine",
                            style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                            color = tactile.emeraldFace,
                        )
                    }
                }
            }
        }
    }
}
