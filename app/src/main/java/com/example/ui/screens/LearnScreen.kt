package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.components.Kova3DChapterRing
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaScallopedSealBadge
import com.example.ui.components.KovaTactileCard
import com.example.ui.components.KovaTactileNodeCap
import com.example.ui.components.shakeOnTrigger
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LearnScreen(
    uiState: KovaUiState,
    isWideTabletLayout: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onToggleChapterExpanded: (String) -> Unit,
    onLessonClick: (String) -> Unit,
    onAssignmentClick: (String) -> Unit,
    onRevisionSheetClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryChapter = uiState.chapters.firstOrNull() ?: return

    if (isWideTabletLayout) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LearnChaptersColumn(
                uiState = uiState,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onToggleChapterExpanded = onToggleChapterExpanded,
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
        LearnChaptersColumn(
            uiState = uiState,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onToggleChapterExpanded = onToggleChapterExpanded,
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
private fun LearnChaptersColumn(
    uiState: KovaUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onToggleChapterExpanded: (String) -> Unit,
    onLessonClick: (String) -> Unit,
    onAssignmentClick: (String) -> Unit,
    onRevisionSheetClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag("learn_path_list"),
        contentPadding = PaddingValues(top = 6.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Daily Quota & Quick Mastery Strip
        item(key = "daily_quota_strip") {
            DailyQuotaHeaderStrip(
                dailyRemaining = uiState.quota.dailyLessonsRemaining,
                dailyMax = uiState.quota.dailyLessonsMax,
                completedCount = uiState.chapters.sumOf { it.completedLessonCount },
                totalCount = uiState.chapters.sumOf { it.lessons.size },
            )
        }

        // Tactile 3D Chapter Cards with 3D Circular Progress Rings & Drop-Down Trails
        items(
            count = uiState.chapters.size,
            key = { uiState.chapters[it].id },
        ) { index ->
            val chapter = uiState.chapters[index]
            val isExpanded = chapter.id in uiState.expandedChapterIds
            val isChapterUnlocked = chapter.isUnlocked || uiState.devUnlockAllOverride

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Tactile3DChapterCard(
                    chapter = chapter,
                    isUnlocked = isChapterUnlocked,
                    isExpanded = isExpanded,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onToggleExpand = { onToggleChapterExpanded(chapter.id) },
                    onRevisionSheetClick = { onRevisionSheetClick(chapter.id) },
                )

                val motion = LocalKovaMotion.current
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = if (motion.enabled) {
                        expandVertically(
                            animationSpec = spring(
                                dampingRatio = 0.72f,
                                stiffness = 320f,
                            ),
                        ) + fadeIn(tween(200))
                    } else {
                        expandVertically(snap()) + fadeIn(snap())
                    },
                    exit = if (motion.enabled) {
                        shrinkVertically(
                            animationSpec = spring(
                                dampingRatio = 0.82f,
                                stiffness = 360f,
                            ),
                        ) + fadeOut(tween(150))
                    } else {
                        shrinkVertically(snap()) + fadeOut(snap())
                    },
                ) {
                    ChapterDropDownTrail(
                        chapter = chapter,
                        isChapterUnlocked = isChapterUnlocked,
                        uiState = uiState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onLessonClick = onLessonClick,
                        onAssignmentClick = onAssignmentClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyQuotaHeaderStrip(
    dailyRemaining: Int,
    dailyMax: Int,
    completedCount: Int,
    totalCount: Int,
) {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.StatAndAssignmentCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.cardLip,
        borderColor = tactile.cardOutline,
        lipHeight = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_lessons_strip"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            KovaPillBadge(
                text = "$dailyRemaining/$dailyMax Daily Lessons Left",
                leadingIcon = Icons.Filled.Bolt,
                containerColor = if (dailyRemaining > 0) tactile.amberSoft else tactile.stoneFace,
                contentColor = if (dailyRemaining > 0) tactile.onAmberSoft else tactile.stoneLabel,
                borderColor = if (dailyRemaining > 0) tactile.amberFace.copy(alpha = 0.45f) else tactile.stoneLip,
                modifier = Modifier.testTag("daily_lessons_pill"),
            )

            KovaPillBadge(
                text = "$completedCount/$totalCount MASTERED",
                containerColor = tactile.emeraldSoft,
                contentColor = tactile.onEmeraldSoft,
                borderColor = tactile.emeraldFace.copy(alpha = 0.35f),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Tactile3DChapterCard(
    chapter: Chapter,
    isUnlocked: Boolean,
    isExpanded: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onToggleExpand: () -> Unit,
    onRevisionSheetClick: () -> Unit,
) {
    val motion = LocalKovaMotion.current
    val tactile = LocalKovaTactileColors.current
    val isChapterMastered = chapter.progressFraction >= 1f && chapter.assignment.isCompleted

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = if (motion.enabled) {
            spring(dampingRatio = 0.72f, stiffness = 340f)
        } else {
            snap()
        },
        label = "chapter_chevron_rotation",
    )

    val ringArcColor = when {
        !isUnlocked -> tactile.stoneLabel
        chapter.progressFraction >= 1f -> tactile.emeraldFace
        else -> tactile.emeraldFace
    }
    val ringArcLip = when {
        !isUnlocked -> tactile.stoneLip
        else -> tactile.emeraldLip
    }
    val emblemFace = when {
        !isUnlocked -> tactile.stoneFace
        isChapterMastered -> tactile.emeraldFace
        else -> tactile.cobaltFace
    }
    val emblemLip = when {
        !isUnlocked -> tactile.stoneLip
        isChapterMastered -> tactile.emeraldLip
        else -> tactile.cobaltLip
    }
    val emblemTint = if (isUnlocked) Color.White else tactile.stoneLabel
    val emblemIcon = when {
        !isUnlocked -> Icons.Filled.Lock
        chapter.number == 1 -> Icons.Filled.Code
        else -> Icons.Filled.Functions
    }

    val cardBorder = when {
        !isUnlocked -> tactile.cardOutline
        isExpanded -> tactile.cobaltFace.copy(alpha = 0.55f)
        else -> tactile.cardOutline
    }
    val cardLip = when {
        !isUnlocked -> tactile.stoneLip
        isExpanded -> tactile.cobaltLip
        else -> tactile.cardLip
    }

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.cardFace,
        lipColor = cardLip,
        borderColor = cardBorder,
        lipHeight = 6.dp,
        borderWidth = if (isExpanded && isUnlocked) 2.dp else 1.5.dp,
        onClick = onToggleExpand,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chapter_card_${chapter.id}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Thick 3D Circular Progress Ring framing Chapter Emblem
                Kova3DChapterRing(
                    progress = chapter.progressFraction,
                    emblemIcon = emblemIcon,
                    emblemContentDescription = "Chapter ${chapter.number} progress ${(chapter.progressFraction * 100).toInt()} percent",
                    arcColor = ringArcColor,
                    arcLipColor = ringArcLip,
                    emblemFaceColor = emblemFace,
                    emblemLipColor = emblemLip,
                    emblemIconTint = emblemTint,
                    size = 84.dp,
                    strokeWidth = 9.dp,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        KovaPillBadge(
                            text = if (isUnlocked) {
                                "CH ${chapter.number} · ${(chapter.progressFraction * 100).toInt()}%"
                            } else {
                                "CH ${chapter.number} · LOCKED"
                            },
                            containerColor = when {
                                !isUnlocked -> tactile.stoneFace
                                chapter.progressFraction >= 1f -> tactile.emeraldSoft
                                else -> tactile.cobaltSoft
                            },
                            contentColor = when {
                                !isUnlocked -> tactile.stoneLabel
                                chapter.progressFraction >= 1f -> tactile.onEmeraldSoft
                                else -> tactile.onCobaltSoft
                            },
                            borderColor = Color.Transparent,
                        )
                        Text(
                            text = "${chapter.completedLessonCount}/${chapter.lessons.size}",
                            style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Text(
                        text = "Chapter ${chapter.number}: ${chapter.title}",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isUnlocked) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            tactile.stoneLabel
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = chapter.tagline,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Tactile Drop-Down Chevron Pill
                Surface(
                    shape = CircleShape,
                    color = if (isExpanded && isUnlocked) tactile.cobaltSoft else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, tactile.cardOutline),
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse chapter" else "Expand chapter",
                            tint = if (isExpanded && isUnlocked) tactile.cobaltFace else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(22.dp)
                                .graphicsLayer { rotationZ = chevronRotation },
                        )
                    }
                }
            }

            // Bottom Action Row inside Chapter Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (isExpanded) {
                        "Tap card to fold trail"
                    } else if (isUnlocked) {
                        "Tap card to drop down ${chapter.lessons.size} lessons"
                    } else {
                        "Tap to peek at locked trail"
                    },
                    style = KovaCodeTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                with(sharedTransitionScope) {
                    val revKey = DetailDestination.RevisionSheetDetail(chapter.id).sharedBoundsKey
                    Surface(
                        onClick = onRevisionSheetClick,
                        shape = KovaExpressiveShapes.Pill,
                        color = tactile.emeraldSoft,
                        border = BorderStroke(1.dp, tactile.emeraldFace.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = revKey),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ -> motion.containerBoundsSpec },
                            )
                            .testTag("revision_sheet_button_${chapter.id}"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoStories,
                                contentDescription = null,
                                tint = tactile.onEmeraldSoft,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = stringResource(R.string.revision_sheet_cta),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = tactile.onEmeraldSoft,
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
private fun ChapterDropDownTrail(
    chapter: Chapter,
    isChapterUnlocked: Boolean,
    uiState: KovaUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onLessonClick: (String) -> Unit,
    onAssignmentClick: (String) -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val curvePattern = listOf(-0.36f, 0.34f, -0.32f, 0.34f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 6.dp)
            .testTag("chapter_dropdown_${chapter.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!isChapterUnlocked) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tactile.amberSoft,
                border = BorderStroke(1.5.dp, tactile.amberFace),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .testTag("chapter_2_locked_banner"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LockClock,
                        contentDescription = null,
                        tint = tactile.amberLip,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = chapter.lockReason ?: stringResource(R.string.chapter_locked_banner),
                        style = MaterialTheme.typography.labelLarge,
                        color = tactile.onAmberSoft,
                    )
                }
            }
        }

        chapter.lessons.forEachIndexed { index, lesson ->
            val currentBias = curvePattern[index % curvePattern.size]
            val shakeNonce = if (uiState.lockedShakeEvent?.nodeId == lesson.id) {
                uiState.lockedShakeEvent.nonce
            } else {
                null
            }

            TactileLessonNodeRow(
                lesson = lesson,
                horizontalBias = currentBias,
                isChapterUnlocked = isChapterUnlocked,
                shakeNonce = shakeNonce,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onClick = { onLessonClick(lesson.id) },
            )

            if (index < chapter.lessons.lastIndex) {
                val nextBias = curvePattern[(index + 1) % curvePattern.size]
                val connectorCompleted = isChapterUnlocked && lesson.status == LessonStatus.COMPLETED
                TactilePathConnector(
                    startBias = currentBias,
                    endBias = nextBias,
                    isCompleted = connectorCompleted,
                )
            }
        }

        // Clean vertical connector down to the Chapter Assignment Trophy Card
        TactilePathConnector(
            startBias = curvePattern[(chapter.lessons.size - 1).coerceAtLeast(0) % curvePattern.size],
            endBias = 0f,
            isCompleted = isChapterUnlocked && chapter.assignment.isUnlocked,
        )

        ChapterAssignmentPathCard(
            assignment = chapter.assignment,
            uiState = uiState,
            originPrefix = "learn",
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onClick = { onAssignmentClick(chapter.assignment.id) },
        )
    }
}

@Composable
private fun TactilePathConnector(
    startBias: Float,
    endBias: Float,
    isCompleted: Boolean,
) {
    val motion = LocalKovaMotion.current
    val tactile = LocalKovaTactileColors.current

    val lineColor by animateColorAsState(
        targetValue = if (isCompleted) tactile.emeraldFace else tactile.pathInactive,
        animationSpec = if (motion.enabled) tween(380) else snap(),
        label = "tactile_connector_color",
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
    ) {
        val centerX = size.width / 2f
        val maxShift = size.width * 0.22f
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

        // Crisp solid track or clean dashed stone path — zero neon glow blur
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = if (isCompleted) 5.dp.toPx() else 3.5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = if (isCompleted) {
                    null
                } else {
                    PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f)
                },
            ),
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TactileLessonNodeRow(
    lesson: Lesson,
    horizontalBias: Float,
    isChapterUnlocked: Boolean,
    shakeNonce: Long?,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
) {
    val motion = LocalKovaMotion.current
    val tactile = LocalKovaTactileColors.current
    val effectiveStatus = if (!isChapterUnlocked) LessonStatus.LOCKED else lesson.status
    val isActiveUnlocked = effectiveStatus == LessonStatus.UNLOCKED
    val isCompleted = effectiveStatus == LessonStatus.COMPLETED

    val alignment = when {
        horizontalBias < -0.15f -> Alignment.CenterStart
        horizontalBias > 0.15f -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    val startPadding = if (horizontalBias < -0.15f) 6.dp else 28.dp
    val endPadding = if (horizontalBias > 0.15f) 6.dp else 28.dp

    val cardFace = when (effectiveStatus) {
        LessonStatus.COMPLETED -> tactile.cardFace
        LessonStatus.UNLOCKED -> tactile.cardFace
        LessonStatus.LOCKED -> tactile.stoneFace.copy(alpha = if (tactile.isDarkSurface) 0.9f else 0.72f)
    }
    val cardLip = when (effectiveStatus) {
        LessonStatus.COMPLETED -> tactile.emeraldLip
        LessonStatus.UNLOCKED -> tactile.cobaltLip
        LessonStatus.LOCKED -> tactile.stoneLip
    }
    val cardBorder = when (effectiveStatus) {
        LessonStatus.COMPLETED -> tactile.emeraldFace.copy(alpha = 0.65f)
        LessonStatus.UNLOCKED -> tactile.cobaltFace
        LessonStatus.LOCKED -> tactile.stoneLip
    }

    val capFace = when (effectiveStatus) {
        LessonStatus.COMPLETED -> tactile.emeraldFace
        LessonStatus.UNLOCKED -> tactile.cobaltFace
        LessonStatus.LOCKED -> tactile.stoneFace
    }
    val capLip = when (effectiveStatus) {
        LessonStatus.COMPLETED -> tactile.emeraldLip
        LessonStatus.UNLOCKED -> tactile.cobaltLip
        LessonStatus.LOCKED -> tactile.stoneLip
    }
    val capIcon = when (effectiveStatus) {
        LessonStatus.COMPLETED -> Icons.Filled.Check
        LessonStatus.UNLOCKED -> Icons.Filled.PlayArrow
        LessonStatus.LOCKED -> Icons.Filled.Lock
    }
    val capTint = if (effectiveStatus == LessonStatus.LOCKED) tactile.stoneLabel else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding)
            .shakeOnTrigger(triggerNonce = shakeNonce, enabled = motion.enabled),
        contentAlignment = alignment,
    ) {
        val sharedKey = DetailDestination.LessonDetail(lesson.id).sharedBoundsKey

        with(sharedTransitionScope) {
            KovaTactileCard(
                shape = KovaExpressiveShapes.LessonSquircleNode,
                faceColor = cardFace,
                lipColor = cardLip,
                borderColor = cardBorder,
                lipHeight = if (effectiveStatus == LessonStatus.LOCKED) 4.dp else 5.dp,
                borderWidth = if (isActiveUnlocked) 2.dp else 1.5.dp,
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth(0.94f)
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
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // 3D Tactile Game Node Keycap
                    KovaTactileNodeCap(
                        icon = capIcon,
                        contentDescription = when (effectiveStatus) {
                            LessonStatus.COMPLETED -> "Lesson completed"
                            LessonStatus.UNLOCKED -> "Start active lesson"
                            LessonStatus.LOCKED -> "Lesson locked"
                        },
                        faceColor = capFace,
                        lipColor = capLip,
                        iconTint = capTint,
                        size = 54.dp,
                        lipHeight = 5.dp,
                    )

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
                                    LessonStatus.COMPLETED -> tactile.emeraldFace
                                    LessonStatus.UNLOCKED -> tactile.cobaltFace
                                    LessonStatus.LOCKED -> tactile.stoneLabel
                                },
                            )
                            Text(
                                text = "· ${lesson.estimatedMinutes}m",
                                style = KovaCodeTextStyle,
                                color = if (effectiveStatus == LessonStatus.LOCKED) {
                                    tactile.stoneLabel
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            if (isActiveUnlocked) {
                                KovaPillBadge(
                                    text = stringResource(R.string.start_pill),
                                    containerColor = tactile.cobaltFace,
                                    contentColor = Color.White,
                                    borderColor = tactile.cobaltLip,
                                )
                            } else if (isCompleted) {
                                KovaPillBadge(
                                    text = "${lesson.passScorePercent ?: 85}%",
                                    containerColor = tactile.emeraldSoft,
                                    contentColor = tactile.onEmeraldSoft,
                                    borderColor = tactile.emeraldFace.copy(alpha = 0.4f),
                                )
                            }
                        }

                        Text(
                            text = lesson.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (effectiveStatus == LessonStatus.LOCKED) {
                                tactile.stoneLabel
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = lesson.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (effectiveStatus == LessonStatus.LOCKED) {
                                tactile.stoneLabel.copy(alpha = 0.85f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
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
    val tactile = LocalKovaTactileColors.current
    val shakeNonce = if (uiState.lockedShakeEvent?.nodeId == assignment.id) {
        uiState.lockedShakeEvent.nonce
    } else {
        null
    }
    val sharedKey = DetailDestination.AssignmentDetail(assignment.id, originPrefix).sharedBoundsKey
    val isReady = assignment.isUnlocked || assignment.isCompleted || uiState.devUnlockAllOverride

    val faceColor = when {
        assignment.isCompleted -> tactile.cardFace
        isReady -> tactile.cardFace
        else -> tactile.stoneFace.copy(alpha = if (tactile.isDarkSurface) 0.9f else 0.72f)
    }
    val lipColor = when {
        assignment.isCompleted -> tactile.emeraldLip
        isReady -> tactile.amberLip
        else -> tactile.stoneLip
    }
    val borderColor = when {
        assignment.isCompleted -> tactile.emeraldFace
        isReady -> tactile.amberFace
        else -> tactile.stoneLip
    }

    with(sharedTransitionScope) {
        KovaTactileCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            faceColor = faceColor,
            lipColor = lipColor,
            borderColor = borderColor,
            lipHeight = 5.dp,
            borderWidth = if (isReady) 2.dp else 1.5.dp,
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
                        assignment.isCompleted -> tactile.emeraldFace
                        isReady -> tactile.amberFace
                        else -> tactile.stoneFace
                    },
                    lipColor = when {
                        assignment.isCompleted -> tactile.emeraldLip
                        isReady -> tactile.amberLip
                        else -> tactile.stoneLip
                    },
                    contentColor = if (isReady) Color.White else tactile.stoneLabel,
                    size = 50.dp,
                    iconSize = 24.dp,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    KovaPillBadge(
                        text = if (assignment.isCompleted) "COMPLETED" else assignment.chapterLabel,
                        containerColor = when {
                            assignment.isCompleted -> tactile.emeraldSoft
                            isReady -> tactile.amberSoft
                            else -> tactile.stoneFace
                        },
                        contentColor = when {
                            assignment.isCompleted -> tactile.onEmeraldSoft
                            isReady -> tactile.onAmberSoft
                            else -> tactile.stoneLabel
                        },
                        borderColor = Color.Transparent,
                    )
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isReady) MaterialTheme.colorScheme.onSurface else tactile.stoneLabel,
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
                        color = if (isReady) MaterialTheme.colorScheme.onSurfaceVariant else tactile.stoneLabel,
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = when {
                        assignment.isCompleted -> tactile.emeraldFace
                        isReady -> tactile.amberLip
                        else -> tactile.stoneLabel
                    },
                )
            }
        }
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
    val tactile = LocalKovaTactileColors.current
    val activeLesson = uiState.activeLesson
    val totalLessons = uiState.chapters.sumOf { it.lessons.size }
    val completedLessons = uiState.chapters.sumOf { it.completedLessonCount }
    val overallProgress = if (totalLessons == 0) 0f else completedLessons.toFloat() / totalLessons

    Column(
        modifier = modifier.testTag("tablet_learn_side_pane"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        KovaTactileCard(
            shape = KovaExpressiveShapes.HeroChapterCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.cardLip,
            borderColor = tactile.cardOutline,
            lipHeight = 5.dp,
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
                    Kova3DChapterRing(
                        progress = overallProgress,
                        emblemIcon = Icons.Filled.EmojiEvents,
                        emblemContentDescription = "Overall mastery progress",
                        arcColor = tactile.emeraldFace,
                        arcLipColor = tactile.emeraldLip,
                        emblemFaceColor = tactile.amberFace,
                        emblemLipColor = tactile.amberLip,
                        emblemIconTint = Color.White,
                        size = 78.dp,
                        strokeWidth = 8.dp,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = "$completedLessons of $totalLessons Lessons",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${uiState.quota.dailyLessonsRemaining}/${uiState.quota.dailyLessonsMax} daily slots open",
                            style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                            color = tactile.amberLip,
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
            KovaTactileCard(
                shape = KovaExpressiveShapes.StatAndAssignmentCard,
                faceColor = tactile.cardFace,
                lipColor = tactile.cobaltLip,
                borderColor = tactile.cobaltFace,
                lipHeight = 5.dp,
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
                            containerColor = tactile.cobaltSoft,
                            contentColor = tactile.onCobaltSoft,
                            borderColor = Color.Transparent,
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

        KovaTactileCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.cardLip,
            borderColor = tactile.cardOutline,
            lipHeight = 4.dp,
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
                    color = tactile.amberLip,
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

        KovaTactileCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            faceColor = tactile.emeraldSoft,
            lipColor = tactile.emeraldLip,
            borderColor = tactile.emeraldFace,
            lipHeight = 4.dp,
            onClick = { onRevisionSheetClick(primaryChapter.id) },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
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
                        tint = tactile.onEmeraldSoft,
                    )
                    Text(
                        text = "Open Chapter 1 Revision Sheet",
                        style = MaterialTheme.typography.labelLarge,
                        color = tactile.onEmeraldSoft,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = tactile.onEmeraldSoft,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
