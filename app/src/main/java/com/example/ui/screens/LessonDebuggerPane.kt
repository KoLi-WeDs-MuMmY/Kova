package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DebuggerWalkthrough
import com.example.model.LessonSplitRatio
import com.example.model.PracticeTaskSpec
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaTactileCard
import com.example.ui.feedback.LocalKovaFeedback
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.KovaMonoBadgeTextStyle
import com.example.ui.theme.LocalKovaTactileColors

/**
 * Phase 2.2 Distinct Dark Kotlin IDE Workbench:
 * - Always opens in Practice / Code Editor mode (never drops directly into step-by-step debugger).
 * - Uses strict dark workbench surface tokens (codeWorkbenchChrome / codeWorkbenchWell / Border)
 *   so the IDE is visually distinct from the warm reading pane in Daylight, Warm Dark, and AMOLED.
 * - Measures wrapped visual lines via BasicTextField's onTextLayout (TextLayoutResult) so gutter
 *   line numbers stay strictly aligned with every logical Kotlin line even on wrapped narrow screens.
 * - Tapping "Unlock Debugger" / "Debugger 70:30" launches the blurred-background Adaptive 70:30
 *   DebuggerFocusStageOverlay.
 */
@Composable
fun LessonDebuggerPane(
    lessonId: String,
    practiceTask: PracticeTaskSpec,
    walkthrough: DebuggerWalkthrough,
    splitRatio: LessonSplitRatio,
    isDebuggerUnlocked: Boolean,
    onOpenDebuggerStage: () -> Unit,
    onAutoExpandToHalf: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
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

    fun handleLaunchDebuggerStage() {
        if (!isDebuggerUnlocked) {
            feedback.chapterSwoosh(view)
        } else {
            feedback.tap(view)
        }
        onOpenDebuggerStage()
    }

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.codeWorkbenchChrome,
        lipColor = tactile.codeWorkbenchBorder,
        borderColor = tactile.cobaltFace.copy(alpha = 0.65f),
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
                // IDE Top Toolbar: Main.kt File Tab + Run / Check + Unlock Debugger (70:30 Stage)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ide_workspace_toolbar"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(11.dp),
                        color = tactile.codeWorkbenchWell,
                        border = BorderStroke(1.dp, tactile.cobaltFace.copy(alpha = 0.55f)),
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
                                color = tactile.codeWorkbenchText,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Run / Check Action
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

                    // Prominent "Unlock Debugger" / "Debugger 70:30" Stage Trigger
                    Surface(
                        onClick = ::handleLaunchDebuggerStage,
                        shape = KovaExpressiveShapes.Pill,
                        color = if (isDebuggerUnlocked) tactile.amberFace else tactile.amberSoft,
                        border = BorderStroke(
                            1.25.dp,
                            if (isDebuggerUnlocked) tactile.amberLip else tactile.amberFace,
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
                                imageVector = if (isDebuggerUnlocked) Icons.Outlined.BugReport else Icons.Outlined.LockOpen,
                                contentDescription = null,
                                tint = if (isDebuggerUnlocked) tactile.onAmberFace else tactile.onAmberSoft,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = if (isDebuggerUnlocked) {
                                    stringResource(R.string.ide_open_debugger_stage_cta)
                                } else {
                                    stringResource(R.string.ide_unlock_debugger_cta)
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDebuggerUnlocked) tactile.onAmberFace else tactile.onAmberSoft,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }

                // Editable Kotlin Code Editor with TextLayoutResult-Aligned Line Gutter + Output Comparison
                CodePracticeWorkspaceBody(
                    editorCode = editorCode,
                    desiredOutput = practiceTask.desiredOutput,
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
            }
        }
    }
}

@Composable
private fun CodePracticeWorkspaceBody(
    editorCode: String,
    desiredOutput: String,
    runOutputStatus: Boolean?,
    runOutputText: String,
    runHintText: String,
    onCodeChange: (String) -> Unit,
    onResetStarterCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val density = LocalDensity.current
    var codeTextLayout by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Compute exact character start index of every logical line ('\n'-delimited)
    val logicalLineStarts = remember(editorCode) {
        buildList {
            add(0)
            editorCode.forEachIndexed { index, ch ->
                if (ch == '\n') {
                    add(index + 1)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .testTag("ide_code_practice_body"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Real Dark Code Editor Well with Strictly Aligned Gutter
        Surface(
            shape = RoundedCornerShape(15.dp),
            color = tactile.codeWorkbenchWell,
            border = BorderStroke(1.25.dp, tactile.codeWorkbenchBorder),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tactile.codeWorkbenchChrome)
                        .padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "KOTLIN WORKBENCH · ${logicalLineStarts.size} LINES",
                        style = KovaMonoBadgeTextStyle,
                        color = tactile.codeWorkbenchGutterText,
                    )
                    Surface(
                        onClick = onResetStarterCode,
                        shape = KovaExpressiveShapes.Pill,
                        color = tactile.codeWorkbenchWell,
                        border = BorderStroke(1.dp, tactile.codeWorkbenchBorder),
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
                                tint = tactile.codeWorkbenchGutterText,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = stringResource(R.string.ide_reset_code_cta),
                                style = MaterialTheme.typography.labelSmall,
                                color = tactile.codeWorkbenchGutterText,
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = tactile.codeWorkbenchBorder)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    // Measured Gutter: places each line number at the exact Y offset of its
                    // corresponding wrapped visual line from TextLayoutResult.
                    val layout = codeTextLayout
                    val measuredHeightDp = if (layout != null) {
                        with(density) { layout.size.height.toDp() }
                    } else {
                        (logicalLineStarts.size * 20).dp
                    }

                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .heightIn(min = measuredHeightDp)
                            .padding(start = 6.dp, end = 7.dp)
                            .testTag("ide_aligned_line_gutter"),
                    ) {
                        if (layout != null && layout.lineCount > 0) {
                            logicalLineStarts.forEachIndexed { idx, startOffset ->
                                val clampedOffset = startOffset.coerceIn(0, editorCode.length)
                                val visualLine = layout.getLineForOffset(clampedOffset)
                                    .coerceIn(0, layout.lineCount - 1)
                                val lineTopDp = with(density) { layout.getLineTop(visualLine).toDp() }
                                Text(
                                    text = "${idx + 1}",
                                    style = KovaCodeTextStyle,
                                    color = tactile.codeWorkbenchGutterText,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(y = lineTopDp),
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.End,
                            ) {
                                logicalLineStarts.forEachIndexed { idx, _ ->
                                    Text(
                                        text = "${idx + 1}",
                                        style = KovaCodeTextStyle,
                                        color = tactile.codeWorkbenchGutterText,
                                        textAlign = TextAlign.End,
                                    )
                                }
                            }
                        }
                    }

                    // Crisp vertical gutter divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(tactile.codeWorkbenchBorder),
                    )

                    // Editable Kotlin Code Area (JetBrains Mono)
                    BasicTextField(
                        value = editorCode,
                        onValueChange = onCodeChange,
                        textStyle = KovaCodeTextStyle.copy(
                            color = tactile.codeWorkbenchText,
                        ),
                        cursorBrush = SolidColor(tactile.emeraldFace),
                        onTextLayout = { layoutResult ->
                            codeTextLayout = layoutResult
                        },
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
        val statusBorder = when (runOutputStatus) {
            true -> tactile.emeraldFace
            false -> tactile.amberFace
            null -> tactile.codeWorkbenchBorder
        }
        val statusTint = when (runOutputStatus) {
            true -> tactile.emeraldFace
            false -> tactile.amberFace
            null -> tactile.codeWorkbenchGutterText
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = tactile.codeWorkbenchWell,
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
                        containerColor = tactile.codeWorkbenchChrome,
                        contentColor = statusTint,
                        borderColor = statusBorder.copy(alpha = 0.7f),
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
                        color = tactile.amberFace,
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
    useCodeFont: Boolean,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    Surface(
        shape = RoundedCornerShape(11.dp),
        color = tactile.codeWorkbenchChrome,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
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
                color = tactile.codeWorkbenchText,
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
