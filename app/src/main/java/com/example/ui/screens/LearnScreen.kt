package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Chapter
import com.example.model.ChapterAssignment
import com.example.model.DetailDestination
import com.example.model.KovaUiState
import com.example.model.Lesson
import com.example.model.LessonStatus
import com.example.ui.components.KovaElevatedCard
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaProgressRing
import com.example.ui.components.KovaScallopedSealBadge
import com.example.ui.components.shakeOnTrigger
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LearnScreen(
    uiState: KovaUiState,
    isWideTabletLayout: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onLessonClick: (String) -> Unit,
    onAssignmentClick: (String) -> Unit,
    onRevisionSheetClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryChapter = uiState.chapters.firstOrNull() ?: return
    val secondaryChapter = uiState.chapters.getOrNull(1)

    if (isWideTabletLayout) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LearnPathColumn(
                uiState = uiState,
                primaryChapter = primaryChapter,
                secondaryChapter = secondaryChapter,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onLessonClick = onLessonClick,
                onAssignmentClick = onAssignmentClick,
                onRevisionSheetClick = onRevisionSheetClick,
                modifier = Modifier.weight(1.35f),
            )
            TabletLearnSidePane(
                uiState = uiState,
                primaryChapter = primaryChapter,
                onLessonClick = onLessonClick,
                onAssignmentClick = onAssignmentClick,
                onRevisionSheetClick = onRevisionSheetClick,
                modifier = Modifier
                    .weight(0.85f)
                    .fillMaxHeight()
                    .padding(top = 8.dp, bottom = 20.dp),
            )
        }
    } else {
        LearnPathColumn(
            uiState = uiState,
            primaryChapter = primaryChapter,
            secondaryChapter = secondaryChapter,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onLessonClick = onLessonClick,
            onAssignmentClick = onAssignmentClick,
            onRevisionSheetClick = onRevisionSheetClick,
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun LearnPathColumn(
    uiState: KovaUiState,
    primaryChapter: Chapter,
    secondaryChapter: Chapter?,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onLessonClick: (String) -> Unit,
    onAssignmentClick: (String) -> Unit,
    onRevisionSheetClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag("learn_path_list"),
        contentPadding = PaddingValues(top = 8.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Hero Chapter 1 Card
        item(key = "hero_${primaryChapter.id}") {
            HeroChapterCard(
                chapter = primaryChapter,
                dailyRemaining = uiState.quota.dailyLessonsRemaining,
                dailyMax = uiState.quota.dailyLessonsMax,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onRevisionSheetClick = { onRevisionSheetClick(primaryChapter.id) },
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Chapter 1 Winding S-Curve Lesson Nodes
        item(key = "winding_path_${primaryChapter.id}") {
            WindingChapterPath(
                chapter = primaryChapter,
                uiState = uiState,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onLessonClick = onLessonClick,
            )
        }

        // Chapter 1 Assignment Trophy Card
        item(key = "assignment_${primaryChapter.assignment.id}") {
            Spacer(modifier = Modifier.height(6.dp))
            ChapterAssignmentPathCard(
                assignment = primaryChapter.assignment,
                uiState = uiState,
                originPrefix = "learn",
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onClick = { onAssignmentClick(primaryChapter.assignment.id) },
            )
        }

        // Chapter 2 Section (Locked until Chapter 1 Assignment is completed)
        if (secondaryChapter != null) {
            item(key = "chapter_2_section") {
                Spacer(modifier = Modifier.height(18.dp))
                ChapterGateSection(
                    chapter = secondaryChapter,
                    uiState = uiState,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onLessonClick = onLessonClick,
                    onRevisionSheetClick = { onRevisionSheetClick(secondaryChapter.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun HeroChapterCard(
    chapter: Chapter,
    dailyRemaining: Int,
    dailyMax: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onRevisionSheetClick: () -> Unit,
) {
    val motion = LocalKovaMotion.current
    val animatedBar by animateFloatAsState(
        targetValue = chapter.progressFraction,
        animationSpec = if (motion.enabled) {
            spring(dampingRatio = 0.8f, stiffness = 240f)
        } else {
            snap()
        },
        label = "chapter_progress_bar",
    )

    KovaElevatedCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        accentGlowColor = MaterialTheme.colorScheme.primary,
        elevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_chapter_card_${chapter.id}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    KovaPillBadge(
                        text = "CHAPTER ${chapter.number} · ${chapter.completedLessonCount}/${chapter.lessons.size} DONE",
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    )
                    Text(
                        text = "Chapter ${chapter.number}: ${chapter.title}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = chapter.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                KovaProgressRing(
                    progress = chapter.progressFraction,
                    size = 74.dp,
                    strokeWidth = 7.dp,
                    activeColor = MaterialTheme.colorScheme.primary,
                )
            }

            LinearProgressIndicator(
                progress = { animatedBar },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                strokeCap = StrokeCap.Round,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KovaPillBadge(
                    text = "$dailyRemaining/$dailyMax Daily Lessons Left",
                    leadingIcon = Icons.Filled.Bolt,
                    containerColor = if (dailyRemaining > 0) {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    contentColor = if (dailyRemaining > 0) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f),
                    modifier = Modifier.testTag("daily_lessons_pill"),
                )

                Spacer(modifier = Modifier.weight(1f))

                with(sharedTransitionScope) {
                    val revKey = DetailDestination.RevisionSheetDetail(chapter.id).sharedBoundsKey
                    Surface(
                        onClick = onRevisionSheetClick,
                        shape = KovaExpressiveShapes.Pill,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                        ),
                        modifier = Modifier
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = revKey),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ -> motion.containerBoundsSpec },
                            )
                            .testTag("revision_sheet_button_${chapter.id}"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoStories,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = stringResource(R.string.revision_sheet_cta),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun WindingChapterPath(
    chapter: Chapter,
    uiState: KovaUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onLessonClick: (String) -> Unit,
) {
    // Gentle S-curve horizontal biases (-1f = left, 0f = center, +1f = right)
    val curvePattern = listOf(-0.42f, 0.36f, -0.34f, 0.38f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        chapter.lessons.forEachIndexed { index, lesson ->
            val currentBias = curvePattern[index % curvePattern.size]
            val shakeNonce = if (uiState.lockedShakeEvent?.nodeId == lesson.id) {
                uiState.lockedShakeEvent.nonce
            } else {
                null
            }

            WindingLessonNodeRow(
                lesson = lesson,
                horizontalBias = currentBias,
                isChapterUnlocked = chapter.isUnlocked || uiState.devUnlockAllOverride,
                shakeNonce = shakeNonce,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onClick = { onLessonClick(lesson.id) },
            )

            if (index < chapter.lessons.lastIndex) {
                val nextBias = curvePattern[(index + 1) % curvePattern.size]
                val connectorActive = lesson.status == LessonStatus.COMPLETED
                WindingBezierConnector(
                    startBias = currentBias,
                    endBias = nextBias,
                    isCompleted = connectorActive,
                )
            }
        }
    }
}

@Composable
private fun WindingBezierConnector(
    startBias: Float,
    endBias: Float,
    isCompleted: Boolean,
) {
    val motion = LocalKovaMotion.current
    val activeColor = MaterialTheme.colorScheme.primary
    val mutedColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)

    val lineColor by animateColorAsState(
        targetValue = if (isCompleted) activeColor else mutedColor,
        animationSpec = if (motion.enabled) tween(450) else snap(),
        label = "connector_color",
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
    ) {
        val centerX = size.width / 2f
        val maxShift = size.width * 0.24f
        val startX = centerX + startBias * maxShift
        val endX = centerX + endBias * maxShift
        val midY = size.height / 2f

        val path = Path().apply {
            moveTo(startX, 0f)
            cubicTo(
                startX,
                midY,
                endX,
                midY,
                endX,
                size.height,
            )
        }

        if (isCompleted) {
            drawPath(
                path = path,
                color = activeColor.copy(alpha = 0.24f),
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round),
            )
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = if (isCompleted) 4.5.dp.toPx() else 3.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = if (isCompleted) {
                    null
                } else {
                    PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                },
            ),
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun WindingLessonNodeRow(
    lesson: Lesson,
    horizontalBias: Float,
    isChapterUnlocked: Boolean,
    shakeNonce: Long?,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
) {
    val motion = LocalKovaMotion.current
    val effectiveStatus = if (!isChapterUnlocked) LessonStatus.LOCKED else lesson.status
    val isActiveUnlocked = effectiveStatus == LessonStatus.UNLOCKED
    val isCompleted = effectiveStatus == LessonStatus.COMPLETED

    val infiniteTransition = rememberInfiniteTransition(label = "active_node_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.48f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    val alignment = when {
        horizontalBias < -0.15f -> Alignment.CenterStart
        horizontalBias > 0.15f -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    val horizontalStartPadding = if (horizontalBias < -0.15f) 10.dp else 36.dp
    val horizontalEndPadding = if (horizontalBias > 0.15f) 10.dp else 36.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = horizontalStartPadding, end = horizontalEndPadding)
            .shakeOnTrigger(triggerNonce = shakeNonce, enabled = motion.enabled),
        contentAlignment = alignment,
    ) {
        val sharedKey = DetailDestination.LessonDetail(lesson.id).sharedBoundsKey
        val cardContainerColor = when (effectiveStatus) {
            LessonStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceContainerHigh
            LessonStatus.UNLOCKED -> MaterialTheme.colorScheme.surfaceContainerHigh
            LessonStatus.LOCKED -> MaterialTheme.colorScheme.surfaceContainerLow
        }

        with(sharedTransitionScope) {
            KovaElevatedCard(
                shape = KovaExpressiveShapes.LessonSquircleNode,
                containerColor = cardContainerColor,
                accentGlowColor = when (effectiveStatus) {
                    LessonStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                    LessonStatus.UNLOCKED -> MaterialTheme.colorScheme.primary
                    LessonStatus.LOCKED -> null
                },
                elevation = if (effectiveStatus == LessonStatus.LOCKED) 2.dp else 10.dp,
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = sharedKey),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> motion.containerBoundsSpec },
                    )
                    .testTag("lesson_node_${lesson.id}"),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Squircle Status Node Badge with Pulsing Emerald Halo when Active
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isActiveUnlocked) {
                            val ringScale = if (motion.enabled) pulseScale else 1.07f
                            val ringAlpha = if (motion.enabled) pulseAlpha else 0.34f
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .graphicsLayer {
                                        scaleX = ringScale
                                        scaleY = ringScale
                                    }
                                    .clip(KovaExpressiveShapes.LessonSquircleNode)
                                    .border(
                                        width = 2.5.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = ringAlpha),
                                        shape = KovaExpressiveShapes.LessonSquircleNode,
                                    ),
                            )
                        }

                        val squircleBg = when (effectiveStatus) {
                            LessonStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                            LessonStatus.UNLOCKED -> MaterialTheme.colorScheme.surfaceContainerHighest
                            LessonStatus.LOCKED -> MaterialTheme.colorScheme.surfaceContainerLowest
                        }
                        val squircleBorder = when (effectiveStatus) {
                            LessonStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                            LessonStatus.UNLOCKED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            LessonStatus.LOCKED -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        }

                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(KovaExpressiveShapes.LessonSquircleNode)
                                .background(squircleBg)
                                .border(1.5.dp, squircleBorder, KovaExpressiveShapes.LessonSquircleNode),
                            contentAlignment = Alignment.Center,
                        ) {
                            when (effectiveStatus) {
                                LessonStatus.COMPLETED -> Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Lesson completed",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(26.dp),
                                )
                                LessonStatus.UNLOCKED -> Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Start active lesson",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                )
                                LessonStatus.LOCKED -> Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Lesson locked",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }

                    // Lesson Text & Pills
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = lesson.code,
                                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                color = when (effectiveStatus) {
                                    LessonStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                                    LessonStatus.UNLOCKED -> MaterialTheme.colorScheme.primary
                                    LessonStatus.LOCKED -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                            Text(
                                text = "· ${lesson.estimatedMinutes}m",
                                style = KovaCodeTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            if (isActiveUnlocked) {
                                KovaPillBadge(
                                    text = stringResource(R.string.start_pill),
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    borderColor = MaterialTheme.colorScheme.primary,
                                )
                            } else if (isCompleted) {
                                KovaPillBadge(
                                    text = "${lesson.passScorePercent ?: 85}%",
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                )
                            }
                        }

                        Text(
                            text = lesson.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (effectiveStatus == LessonStatus.LOCKED) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = lesson.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (effectiveStatus == LessonStatus.LOCKED) 0.72f else 0.95f,
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChapterAssignmentPathCard(
    assignment: ChapterAssignment,
    uiState: KovaUiState,
    originPrefix: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = LocalKovaMotion.current
    val shakeNonce = if (uiState.lockedShakeEvent?.nodeId == assignment.id) {
        uiState.lockedShakeEvent.nonce
    } else {
        null
    }
    val sharedKey = DetailDestination.AssignmentDetail(assignment.id, originPrefix).sharedBoundsKey
    val isReady = assignment.isUnlocked || assignment.isCompleted || uiState.devUnlockAllOverride

    with(sharedTransitionScope) {
        KovaElevatedCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            containerColor = if (isReady) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
            accentGlowColor = if (assignment.isCompleted) {
                MaterialTheme.colorScheme.primary
            } else if (isReady) {
                MaterialTheme.colorScheme.secondary
            } else {
                null
            },
            elevation = if (isReady) 10.dp else 3.dp,
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .shakeOnTrigger(triggerNonce = shakeNonce, enabled = motion.enabled)
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = sharedKey),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ -> motion.containerBoundsSpec },
                )
                .testTag("${originPrefix}_assignment_card_${assignment.id}"),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                KovaScallopedSealBadge(
                    icon = when {
                        assignment.isCompleted -> Icons.Outlined.CheckCircle
                        isReady -> Icons.Filled.EmojiEvents
                        else -> Icons.Filled.Lock
                    },
                    contentDescription = "Chapter assignment status",
                    containerColor = when {
                        assignment.isCompleted -> MaterialTheme.colorScheme.primary
                        isReady -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    contentColor = when {
                        assignment.isCompleted -> MaterialTheme.colorScheme.onPrimary
                        isReady -> MaterialTheme.colorScheme.onSecondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    size = 52.dp,
                    iconSize = 24.dp,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        KovaPillBadge(
                            text = if (assignment.isCompleted) "COMPLETED" else assignment.chapterLabel,
                            containerColor = if (assignment.isCompleted) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                            contentColor = if (assignment.isCompleted) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            borderColor = if (assignment.isCompleted) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            },
                        )
                    }
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (assignment.isCompleted) {
                            "Completed · Chapter 2 unlocked across Learn & Lab"
                        } else if (isReady) {
                            "Ready in Learn or Lab · Complete anytime before Chapter 2"
                        } else {
                            stringResource(R.string.assignment_optional_hint)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = if (isReady) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ChapterGateSection(
    chapter: Chapter,
    uiState: KovaUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onLessonClick: (String) -> Unit,
    onRevisionSheetClick: () -> Unit,
) {
    val isUnlocked = chapter.isUnlocked || uiState.devUnlockAllOverride

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KovaElevatedCard(
            shape = KovaExpressiveShapes.HeroChapterCard,
            containerColor = if (isUnlocked) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
            accentGlowColor = if (isUnlocked) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondary
            },
            elevation = if (isUnlocked) 10.dp else 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("chapter_gate_card_${chapter.id}"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    KovaPillBadge(
                        text = if (isUnlocked) "CHAPTER ${chapter.number} · UNLOCKED" else "CHAPTER ${chapter.number} · LOCKED",
                        leadingIcon = if (isUnlocked) Icons.Filled.Bolt else Icons.Filled.Lock,
                        containerColor = if (isUnlocked) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        contentColor = if (isUnlocked) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                    if (isUnlocked) {
                        Surface(
                            onClick = onRevisionSheetClick,
                            shape = KovaExpressiveShapes.Pill,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ) {
                            Text(
                                text = stringResource(R.string.revision_sheet_cta),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
                }

                Text(
                    text = "Chapter ${chapter.number}: ${chapter.title}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    },
                )
                Text(
                    text = chapter.tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chapter_2_locked_banner"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LockClock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = chapter.lockReason ?: stringResource(R.string.chapter_locked_banner),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }
        }

        // Render Chapter 2 preview/unlocked winding path below its banner
        WindingChapterPath(
            chapter = chapter,
            uiState = uiState,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onLessonClick = onLessonClick,
        )
    }
}

@Composable
private fun TabletLearnSidePane(
    uiState: KovaUiState,
    primaryChapter: Chapter,
    onLessonClick: (String) -> Unit,
    onAssignmentClick: (String) -> Unit,
    onRevisionSheetClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeLesson = uiState.activeLesson
    val totalLessons = uiState.chapters.sumOf { it.lessons.size }
    val completedLessons = uiState.chapters.sumOf { it.completedLessonCount }

    Column(
        modifier = modifier.testTag("tablet_learn_side_pane"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        KovaElevatedCard(
            shape = KovaExpressiveShapes.HeroChapterCard,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            accentGlowColor = MaterialTheme.colorScheme.primary,
            elevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.tablet_side_progress_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    KovaProgressRing(
                        progress = if (totalLessons == 0) 0f else completedLessons.toFloat() / totalLessons,
                        size = 76.dp,
                        strokeWidth = 7.dp,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "$completedLessons of $totalLessons Lessons",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${uiState.quota.dailyLessonsRemaining}/${uiState.quota.dailyLessonsMax} daily slots open",
                            style = KovaCodeTextStyle,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Text(
                            text = "Pass >=80% per lesson",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (activeLesson != null) {
            KovaElevatedCard(
                shape = KovaExpressiveShapes.StatAndAssignmentCard,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                onClick = { onLessonClick(activeLesson.id) },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        KovaPillBadge(
                            text = "UP NEXT · ${activeLesson.code}",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "${activeLesson.estimatedMinutes}m",
                                style = KovaCodeTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = activeLesson.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = activeLesson.analogyTeaser,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        KovaElevatedCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            onClick = { onAssignmentClick(primaryChapter.assignment.id) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Chapter 1 Gate",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = primaryChapter.assignment.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = when {
                        primaryChapter.assignment.isCompleted -> "Completed · Chapter 2 Open"
                        primaryChapter.assignment.isUnlocked || uiState.devUnlockAllOverride -> "Unlocked · Tap to review"
                        else -> "Unlocks after Lesson 1.4"
                    },
                    style = KovaCodeTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Surface(
            onClick = { onRevisionSheetClick(primaryChapter.id) },
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoStories,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Open Chapter 1 Revision Sheet",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
