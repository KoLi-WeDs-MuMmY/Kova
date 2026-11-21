package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.VerticalSplit
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Lesson
import com.example.model.LessonSplitRatio
import com.example.model.LessonStatus
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaScallopedSealBadge
import com.example.ui.components.KovaTactileButton
import com.example.ui.components.KovaTactileCard
import com.example.ui.feedback.LocalKovaFeedback
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors

/**
 * Distraction-free Lesson Focus Canvas:
 * - Portrait (height >= width): Horizontal Lesson Progress Bar along the TOP + Top/Bottom split.
 * - Landscape (width > height): Vertical Lesson Progress Bar along the LEFT edge rail + Left/Right split.
 * - Smooth spring-animated Split Ratio Toggle cycling 75:25 -> 50:50 -> 25:75 -> 75:25.
 * - Clean styled empty/placeholder Code & IDE Space canvas ready for Phase 2/3.
 */
@Composable
fun LessonFocusWorkspace(
    lesson: Lesson,
    chapterProgress: Float,
    dailyRemaining: Int,
    splitRatio: LessonSplitRatio,
    onCycleSplitRatio: () -> Unit,
    onClose: () -> Unit,
    onPassMockTest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = LocalKovaMotion.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current
    val isCompleted = lesson.status == LessonStatus.COMPLETED

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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .testTag("lesson_focus_workspace"),
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
                        feedback.tap(view)
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
                    LessonContentPane(
                        lesson = lesson,
                        dailyRemaining = dailyRemaining,
                        onPassMockTest = {
                            feedback.success(view)
                            onPassMockTest()
                        },
                        modifier = Modifier
                            .weight(animatedLessonWeight)
                            .fillMaxHeight(),
                    )
                    EmptyIdePlaceholderPane(
                        splitRatio = splitRatio,
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
                        feedback.tap(view)
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
                    LessonContentPane(
                        lesson = lesson,
                        dailyRemaining = dailyRemaining,
                        onPassMockTest = {
                            feedback.success(view)
                            onPassMockTest()
                        },
                        modifier = Modifier
                            .weight(animatedLessonWeight)
                            .fillMaxWidth(),
                    )
                    EmptyIdePlaceholderPane(
                        splitRatio = splitRatio,
                        modifier = Modifier
                            .weight(animatedIdeWeight)
                            .fillMaxWidth(),
                    )
                }
            }
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

@Composable
private fun LessonContentPane(
    lesson: Lesson,
    dailyRemaining: Int,
    onPassMockTest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val isCompleted = lesson.status == LessonStatus.COMPLETED

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Focused Lesson Header (no global top bar chrome)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                KovaPillBadge(
                    text = "LESSON ${lesson.code} · ${lesson.estimatedMinutes}M",
                    containerColor = tactile.cobaltSoft,
                    contentColor = tactile.onCobaltSoft,
                    borderColor = Color.Transparent,
                )
                KovaPillBadge(
                    text = "$dailyRemaining/3 DAILY LEFT",
                    containerColor = tactile.cardFace,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    borderColor = tactile.cardOutline,
                )
            }
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Analogy & Literal Word Breakdown Card
        KovaTactileCard(
            shape = KovaExpressiveShapes.HeroChapterCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.cobaltLip,
            borderColor = tactile.cobaltFace.copy(alpha = 0.55f),
            lipHeight = 5.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = tactile.amberFace,
                        modifier = Modifier.size(19.dp),
                    )
                    Text(
                        text = "10-Year-Old Analogy & Concept Focus",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = lesson.analogyTeaser,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = lesson.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Compact Code Reference Snippet Card
        KovaTactileCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.cardLip,
            borderColor = tactile.cardOutline,
            lipHeight = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BugReport,
                        contentDescription = null,
                        tint = tactile.emeraldFace,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Reference Pattern",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, tactile.cardOutline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = lesson.codePreview,
                        style = KovaCodeTextStyle,
                        color = tactile.cobaltFace,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }

        // Integrated Lesson Test Action Card
        KovaTactileCard(
            shape = KovaExpressiveShapes.HeroChapterCard,
            faceColor = tactile.cardFace,
            lipColor = if (isCompleted) tactile.emeraldLip else tactile.coralLip,
            borderColor = if (isCompleted) tactile.emeraldFace else tactile.coralFace,
            lipHeight = 5.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KovaScallopedSealBadge(
                        icon = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.TaskAlt,
                        contentDescription = null,
                        containerColor = if (isCompleted) tactile.emeraldFace else tactile.coralFace,
                        lipColor = if (isCompleted) tactile.emeraldLip else tactile.coralLip,
                        contentColor = if (isCompleted) tactile.onEmeraldFace else tactile.onCoralFace,
                        size = 42.dp,
                        iconSize = 20.dp,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.lesson_test_section_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.lesson_test_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (isCompleted) {
                    Surface(
                        shape = KovaExpressiveShapes.Pill,
                        color = tactile.emeraldSoft,
                        border = BorderStroke(1.25.dp, tactile.emeraldFace),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lesson_passed_banner"),
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 11.dp, horizontal = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = tactile.emeraldFace,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.lesson_test_passed_badge),
                                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                color = tactile.onEmeraldSoft,
                            )
                        }
                    }
                } else {
                    KovaTactileButton(
                        onClick = onPassMockTest,
                        faceColor = tactile.emeraldFace,
                        lipColor = tactile.emeraldLip,
                        contentColor = tactile.onEmeraldFace,
                        minHeight = 50.dp,
                        lipHeight = 5.dp,
                        playTapSound = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pass_mock_test_button"),
                    ) {
                        Text(
                            text = stringResource(R.string.lesson_test_pass_cta),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = tactile.onEmeraldFace,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean, styled empty/placeholder Code & IDE workspace canvas with zero fake editor content.
 */
@Composable
private fun EmptyIdePlaceholderPane(
    splitRatio: LessonSplitRatio,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = MaterialTheme.colorScheme.surfaceContainerLow,
        lipColor = tactile.cardLip,
        borderColor = tactile.cardOutline,
        lipHeight = 5.dp,
        modifier = modifier.testTag("lesson_ide_placeholder_pane"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = null,
                        tint = tactile.cobaltFace,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.lesson_ide_header),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                KovaPillBadge(
                    text = stringResource(R.string.lesson_ide_ready_badge),
                    containerColor = tactile.coralSoft,
                    contentColor = tactile.onCoralSoft,
                    borderColor = Color.Transparent,
                )
            }

            // Clean dashed empty workspace canvas (intentionally no fake editor lines)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                val isCompactHeight = maxHeight < 92.dp
                val dashColor = tactile.cardOutline
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRoundRect(
                        color = dashColor,
                        topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                        size = Size(size.width - 4.dp.toPx(), size.height - 4.dp.toPx()),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                        ),
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "IDE Pane · ${splitRatio.badgeLabel}",
                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!isCompactHeight) {
                        Text(
                            text = stringResource(R.string.lesson_ide_empty_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
