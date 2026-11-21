package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Chapter
import com.example.model.ChapterAssignment
import com.example.model.DetailDestination
import com.example.model.KovaUiState
import com.example.model.Lesson
import com.example.model.LessonReturnEvent
import com.example.model.LessonStatus
import com.example.ui.components.Kova3DChapterRing
import com.example.ui.components.Kova3DGoldenTrophyPedestal
import com.example.ui.components.Kova3DQuestPedestal
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaScallopedSealBadge
import com.example.ui.components.KovaTactileButton
import com.example.ui.components.KovaTactileCard
import com.example.ui.components.settleOrCelebrateOnReturn
import com.example.ui.components.shakeOnTrigger
import com.example.ui.feedback.LocalKovaFeedback
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors
import kotlinx.coroutines.delay

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
    val motion = LocalKovaMotion.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current

    // Trigger celebratory chime on returning from a newly completed lesson
    LaunchedEffect(uiState.lessonReturnEvent?.nonce) {
        val event = uiState.lessonReturnEvent
        if (event != null && event.wasNewlyCompleted) {
            delay(110L)
            feedback.success(view)
        }
    }

    // Staggered spring entrance animation for surrounding chrome / telemetry on exit-from-lesson return
    val returnReveal = remember { Animatable(1f) }
    LaunchedEffect(uiState.lessonReturnEvent?.nonce, motion.enabled) {
        if (uiState.lessonReturnEvent != null && motion.enabled) {
            returnReveal.snapTo(0.86f)
            returnReveal.animateTo(1f, spring(dampingRatio = 0.76f, stiffness = 340f))
        }
    }

    if (isWideTabletLayout) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LearnChaptersColumn(
                uiState = uiState,
                returnRevealProgress = returnReveal.value,
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
                returnRevealProgress = returnReveal.value,
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
            returnRevealProgress = returnReveal.value,
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
    returnRevealProgress: Float,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onToggleChapterExpanded: (String) -> Unit,
    onLessonClick: (String) -> Unit,
    onAssignmentClick: (String) -> Unit,
    onRevisionSheetClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val returnEvent = uiState.lessonReturnEvent

    LazyColumn(
        modifier = modifier.testTag("learn_path_list"),
        contentPadding = PaddingValues(top = 6.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "daily_quota_strip") {
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = returnRevealProgress
                    translationY = (1f - returnRevealProgress) * -28f
                },
            ) {
                DailyQuotaHeaderStrip(
                    dailyRemaining = uiState.quota.dailyLessonsRemaining,
                    dailyMax = uiState.quota.dailyLessonsMax,
                    completedCount = uiState.chapters.sumOf { it.completedLessonCount },
                    totalCount = uiState.chapters.sumOf { it.lessons.size },
                )
            }
        }

        items(
            count = uiState.chapters.size,
            key = { uiState.chapters[it].id },
        ) { index ->
            val chapter = uiState.chapters[index]
            val isExpanded = chapter.id in uiState.expandedChapterIds
            val isChapterUnlocked = chapter.isUnlocked || uiState.devUnlockAllOverride
            val chapterContainsReturnedLesson = returnEvent != null &&
                chapter.lessons.any { it.id == returnEvent.lessonId }
            val ringCelebrationNonce = if (chapterContainsReturnedLesson && returnEvent?.wasNewlyCompleted == true) {
                returnEvent.nonce
            } else {
                null
            }

            Column(
                modifier = Modifier.graphicsLayer {
                    val staggerShift = (1f - returnRevealProgress) * (24f + index * 18f)
                    translationY = staggerShift
                },
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                Tactile3DChapterCard(
                    chapter = chapter,
                    isUnlocked = isChapterUnlocked,
                    isExpanded = isExpanded,
                    celebrationNonce = ringCelebrationNonce,
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
                                dampingRatio = 0.76f,
                                stiffness = 340f,
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
                    ChapterWindingQuestTrail(
                        chapter = chapter,
                        isChapterUnlocked = isChapterUnlocked,
                        uiState = uiState,
                        returnEvent = returnEvent,
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

@OptIn(ExperimentalLayoutApi::class)
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
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KovaPillBadge(
                text = "$dailyRemaining/$dailyMax Daily Left",
                leadingIcon = Icons.Filled.Bolt,
                containerColor = if (dailyRemaining > 0) tactile.coralSoft else tactile.stoneFace,
                contentColor = if (dailyRemaining > 0) tactile.onCoralSoft else tactile.stoneLabel,
                borderColor = if (dailyRemaining > 0) tactile.coralFace.copy(alpha = 0.45f) else tactile.stoneLip,
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
    celebrationNonce: Long?,
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
            spring(dampingRatio = 0.76f, stiffness = 340f)
        } else {
            snap()
        },
        label = "chapter_chevron_rotation",
    )

    val ringArcColor = if (!isUnlocked) tactile.stoneLabel else tactile.emeraldFace
    val ringArcLip = if (!isUnlocked) tactile.stoneLip else tactile.emeraldLip
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
    val emblemTint = when {
        !isUnlocked -> tactile.stoneLabel
        isChapterMastered -> tactile.onEmeraldFace
        else -> tactile.onCobaltFace
    }
    val emblemIcon = when {
        !isUnlocked -> Icons.Filled.Lock
        chapter.number == 1 -> Icons.Filled.Code
        else -> Icons.Filled.Functions
    }

    val cardBorder = when {
        !isUnlocked -> tactile.cardOutline
        isExpanded -> tactile.cobaltFace.copy(alpha = 0.65f)
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
        borderWidth = if (isExpanded && isUnlocked) 1.75.dp else tactile.cardBorderWidthDp.dp,
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
                    celebrationNonce = celebrationNonce,
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
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else tactile.stoneLabel,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = if (isExpanded) {
                        "Tap card to fold quest trail"
                    } else if (isUnlocked) {
                        "Tap to open ${chapter.lessons.size} lessons"
                    } else {
                        "Tap to peek at locked trail"
                    },
                    style = KovaCodeTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
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
private fun ChapterWindingQuestTrail(
    chapter: Chapter,
    isChapterUnlocked: Boolean,
    uiState: KovaUiState,
    returnEvent: LessonReturnEvent?,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onLessonClick: (String) -> Unit,
    onAssignmentClick: (String) -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val curvePattern = listOf(-0.48f, 0.48f, -0.44f, 0.44f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 10.dp)
            .testTag("chapter_dropdown_${chapter.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!isChapterUnlocked) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tactile.amberSoft,
                border = BorderStroke(1.25.dp, tactile.amberFace),
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
                        tint = tactile.amberFace,
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
            val isReturnedLesson = returnEvent?.lessonId == lesson.id

            WindingQuestPedestalNode(
                lesson = lesson,
                horizontalBias = currentBias,
                isChapterUnlocked = isChapterUnlocked,
                shakeNonce = shakeNonce,
                returnNonce = if (isReturnedLesson) returnEvent?.nonce else null,
                returnCelebration = isReturnedLesson && (returnEvent?.wasNewlyCompleted == true),
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onClick = { onLessonClick(lesson.id) },
            )

            if (index < chapter.lessons.lastIndex) {
                val nextBias = curvePattern[(index + 1) % curvePattern.size]
                val connectorCompleted = isChapterUnlocked && lesson.status == LessonStatus.COMPLETED
                SinusoidalSteppingTrailConnector(
                    startBias = currentBias,
                    endBias = nextBias,
                    isCompleted = connectorCompleted,
                    celebrationNonce = if (isReturnedLesson && returnEvent?.wasNewlyCompleted == true) {
                        returnEvent.nonce
                    } else {
                        null
                    },
                )
            }
        }

        SinusoidalSteppingTrailConnector(
            startBias = curvePattern[(chapter.lessons.size - 1).coerceAtLeast(0) % curvePattern.size],
            endBias = 0f,
            isCompleted = isChapterUnlocked && chapter.assignment.isUnlocked,
            celebrationNonce = null,
        )

        GoldenTrophyAssignmentNode(
            assignment = chapter.assignment,
            uiState = uiState,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            onClick = { onAssignmentClick(chapter.assignment.id) },
        )
    }
}

@Composable
private fun SinusoidalSteppingTrailConnector(
    startBias: Float,
    endBias: Float,
    isCompleted: Boolean,
    celebrationNonce: Long?,
) {
    val motion = LocalKovaMotion.current
    val tactile = LocalKovaTactileColors.current

    val lineColor by animateColorAsState(
        targetValue = if (isCompleted) tactile.emeraldFace else tactile.pathInactive,
        animationSpec = if (motion.enabled) tween(360) else snap(),
        label = "sinusoidal_connector_color",
    )

    val strokeBoost = remember { Animatable(1f) }
    LaunchedEffect(celebrationNonce, motion.enabled) {
        if (celebrationNonce != null && motion.enabled) {
            strokeBoost.snapTo(1f)
            strokeBoost.animateTo(1.45f, spring(dampingRatio = 0.55f, stiffness = 420f))
            strokeBoost.animateTo(1f, spring(dampingRatio = 0.76f, stiffness = 340f))
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
    ) {
        val centerX = size.width / 2f
        val maxShift = size.width * 0.28f
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
                color = tactile.emeraldLip.copy(alpha = 0.35f),
                style = Stroke(width = (11.dp * strokeBoost.value).toPx(), cap = StrokeCap.Round),
            )
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = (6.dp * strokeBoost.value).toPx(), cap = StrokeCap.Round),
            )
        } else {
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 5.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 22f), 0f),
                ),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun WindingQuestPedestalNode(
    lesson: Lesson,
    horizontalBias: Float,
    isChapterUnlocked: Boolean,
    shakeNonce: Long?,
    returnNonce: Long?,
    returnCelebration: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
) {
    val motion = LocalKovaMotion.current
    val tactile = LocalKovaTactileColors.current
    val effectiveStatus = if (!isChapterUnlocked) LessonStatus.LOCKED else lesson.status
    val isActiveUnlocked = effectiveStatus == LessonStatus.UNLOCKED

    val pedestalFace = when (effectiveStatus) {
        LessonStatus.COMPLETED -> tactile.emeraldFace
        LessonStatus.UNLOCKED -> tactile.cobaltFace
        LessonStatus.LOCKED -> tactile.stoneFace
    }
    val pedestalLip = when (effectiveStatus) {
        LessonStatus.COMPLETED -> tactile.emeraldLip
        LessonStatus.UNLOCKED -> tactile.cobaltLip
        LessonStatus.LOCKED -> tactile.stoneLip
    }
    val pedestalIcon = when (effectiveStatus) {
        LessonStatus.COMPLETED -> Icons.Filled.Check
        LessonStatus.UNLOCKED -> Icons.Filled.PlayArrow
        LessonStatus.LOCKED -> Icons.Filled.Lock
    }
    val pedestalTint = when (effectiveStatus) {
        LessonStatus.COMPLETED -> tactile.onEmeraldFace
        LessonStatus.UNLOCKED -> tactile.onCobaltFace
        LessonStatus.LOCKED -> tactile.stoneLabel
    }

    val pedestalOnLeft = horizontalBias < 0f
    val sharedKey = DetailDestination.LessonDetail(lesson.id).sharedBoundsKey

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .shakeOnTrigger(triggerNonce = shakeNonce, enabled = motion.enabled)
            .settleOrCelebrateOnReturn(
                returnNonce = returnNonce,
                isCelebration = returnCelebration,
                enabled = motion.enabled,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (pedestalOnLeft) Arrangement.Start else Arrangement.End,
    ) {
        if (!pedestalOnLeft) {
            FloatingLessonInfoPill(
                lesson = lesson,
                effectiveStatus = effectiveStatus,
                alignEnd = true,
                onClick = onClick,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = 228.dp)
                    .padding(end = 10.dp),
            )
        }

        with(sharedTransitionScope) {
            Kova3DQuestPedestal(
                icon = pedestalIcon,
                contentDescription = when (effectiveStatus) {
                    LessonStatus.COMPLETED -> "Lesson ${lesson.code} completed"
                    LessonStatus.UNLOCKED -> "Start active lesson ${lesson.code}"
                    LessonStatus.LOCKED -> "Lesson ${lesson.code} locked"
                },
                faceColor = pedestalFace,
                lipColor = pedestalLip,
                iconTint = pedestalTint,
                isActiveQuest = isActiveUnlocked,
                onClick = onClick,
                pedestalSize = 80.dp,
                lipHeight = 6.dp,
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = sharedKey),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> motion.containerBoundsSpec },
                    )
                    .testTag("lesson_node_${lesson.id}"),
            )
        }

        if (pedestalOnLeft) {
            FloatingLessonInfoPill(
                lesson = lesson,
                effectiveStatus = effectiveStatus,
                alignEnd = false,
                onClick = onClick,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(max = 228.dp)
                    .padding(start = 10.dp),
            )
        }
    }
}

@Composable
private fun FloatingLessonInfoPill(
    lesson: Lesson,
    effectiveStatus: LessonStatus,
    alignEnd: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val isLocked = effectiveStatus == LessonStatus.LOCKED
    val isActive = effectiveStatus == LessonStatus.UNLOCKED
    val isCompleted = effectiveStatus == LessonStatus.COMPLETED

    KovaTactileCard(
        shape = RoundedCornerShape(18.dp),
        faceColor = if (isLocked) tactile.stoneFace.copy(alpha = 0.72f) else tactile.cardFace,
        lipColor = when (effectiveStatus) {
            LessonStatus.COMPLETED -> tactile.emeraldLip
            LessonStatus.UNLOCKED -> tactile.cobaltLip
            LessonStatus.LOCKED -> tactile.stoneLip
        },
        borderColor = when (effectiveStatus) {
            LessonStatus.COMPLETED -> tactile.emeraldFace.copy(alpha = 0.55f)
            LessonStatus.UNLOCKED -> tactile.cobaltFace
            LessonStatus.LOCKED -> tactile.cardOutline
        },
        lipHeight = 4.dp,
        borderWidth = if (isActive) 1.5.dp else tactile.cardBorderWidthDp.dp,
        onClick = onClick,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
            horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp, if (alignEnd) Alignment.End else Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "${lesson.code} · ${lesson.estimatedMinutes}m",
                    style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                    color = when (effectiveStatus) {
                        LessonStatus.COMPLETED -> tactile.emeraldFace
                        LessonStatus.UNLOCKED -> tactile.cobaltFace
                        LessonStatus.LOCKED -> tactile.stoneLabel
                    },
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                if (isActive) {
                    KovaPillBadge(
                        text = stringResource(R.string.start_pill),
                        containerColor = tactile.coralFace,
                        contentColor = tactile.onCoralFace,
                        borderColor = tactile.coralLip,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                } else if (isCompleted) {
                    KovaPillBadge(
                        text = "${lesson.passScorePercent ?: 85}%",
                        containerColor = tactile.emeraldSoft,
                        contentColor = tactile.onEmeraldSoft,
                        borderColor = Color.Transparent,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
            }
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isLocked) tactile.stoneLabel else MaterialTheme.colorScheme.onSurface,
                textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GoldenTrophyAssignmentNode(
    assignment: ChapterAssignment,
    uiState: KovaUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
) {
    val motion = LocalKovaMotion.current
    val tactile = LocalKovaTactileColors.current
    val shakeNonce = if (uiState.lockedShakeEvent?.nodeId == assignment.id) {
        uiState.lockedShakeEvent.nonce
    } else {
        null
    }
    val sharedKey = DetailDestination.AssignmentDetail(assignment.id, "learn").sharedBoundsKey
    val isReady = assignment.isUnlocked || assignment.isCompleted || uiState.devUnlockAllOverride

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shakeOnTrigger(triggerNonce = shakeNonce, enabled = motion.enabled),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        with(sharedTransitionScope) {
            Kova3DGoldenTrophyPedestal(
                isUnlocked = isReady,
                isCompleted = assignment.isCompleted,
                onClick = onClick,
                modifier = Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = sharedKey),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> motion.containerBoundsSpec },
                    )
                    .testTag("learn_assignment_card_${assignment.id}"),
            )
        }

        KovaTactileCard(
            shape = RoundedCornerShape(18.dp),
            faceColor = if (isReady) tactile.cardFace else tactile.stoneFace.copy(alpha = 0.72f),
            lipColor = when {
                assignment.isCompleted -> tactile.emeraldLip
                isReady -> tactile.amberLip
                else -> tactile.stoneLip
            },
            borderColor = when {
                assignment.isCompleted -> tactile.emeraldFace
                isReady -> tactile.amberFace
                else -> tactile.cardOutline
            },
            lipHeight = 4.dp,
            onClick = onClick,
            modifier = Modifier.widthIn(max = 310.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                KovaPillBadge(
                    text = if (assignment.isCompleted) "TROPHY CLAIMED" else assignment.chapterLabel,
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
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = if (assignment.isCompleted) {
                        "Completed · Next chapter unlocked"
                    } else if (isReady) {
                        "Tap Trophy · Synced with Lab Hub"
                    } else {
                        stringResource(R.string.assignment_optional_hint)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isReady) MaterialTheme.colorScheme.onSurfaceVariant else tactile.stoneLabel,
                    textAlign = TextAlign.Center,
                )
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

    val faceColor = if (isReady) tactile.cardFace else tactile.stoneFace.copy(alpha = 0.72f)
    val lipColor = when {
        assignment.isCompleted -> tactile.emeraldLip
        isReady -> tactile.amberLip
        else -> tactile.stoneLip
    }
    val borderColor = when {
        assignment.isCompleted -> tactile.emeraldFace
        isReady -> tactile.amberFace
        else -> tactile.cardOutline
    }

    with(sharedTransitionScope) {
        KovaTactileCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            faceColor = faceColor,
            lipColor = lipColor,
            borderColor = borderColor,
            lipHeight = 5.dp,
            borderWidth = if (isReady) 1.75.dp else tactile.cardBorderWidthDp.dp,
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
                    contentColor = when {
                        assignment.isCompleted -> tactile.onEmeraldFace
                        isReady -> tactile.onAmberFace
                        else -> tactile.stoneLabel
                    },
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
                        isReady -> tactile.amberFace
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
    returnRevealProgress: Float,
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
        modifier = modifier
            .graphicsLayer {
                alpha = returnRevealProgress
                translationX = (1f - returnRevealProgress) * 36f
            }
            .testTag("tablet_learn_side_pane"),
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
                        emblemIconTint = tactile.onAmberFace,
                        size = 78.dp,
                        strokeWidth = 8.dp,
                        celebrationNonce = if (uiState.lessonReturnEvent?.wasNewlyCompleted == true) {
                            uiState.lessonReturnEvent.nonce
                        } else {
                            null
                        },
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = "$completedLessons of $totalLessons Lessons",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${uiState.quota.dailyLessonsRemaining}/${uiState.quota.dailyLessonsMax} daily slots remaining",
                            style = KovaCodeTextStyle,
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
                borderColor = tactile.cobaltFace.copy(alpha = 0.55f),
                lipHeight = 5.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KovaPillBadge(
                        text = "ACTIVE QUEST · ${activeLesson.code}",
                        containerColor = tactile.cobaltSoft,
                        contentColor = tactile.onCobaltSoft,
                        borderColor = Color.Transparent,
                    )
                    Text(
                        text = activeLesson.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = activeLesson.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    KovaTactileButton(
                        onClick = { onLessonClick(activeLesson.id) },
                        faceColor = tactile.cobaltFace,
                        lipColor = tactile.cobaltLip,
                        contentColor = tactile.onCobaltFace,
                        minHeight = 46.dp,
                        lipHeight = 4.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Open Lesson ${activeLesson.code}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = tactile.onCobaltFace,
                        )
                    }
                }
            }
        }

        KovaTactileCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.cardLip,
            borderColor = tactile.cardOutline,
            lipHeight = 4.dp,
            onClick = { onRevisionSheetClick(primaryChapter.id) },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chapter ${primaryChapter.number} Cheat Sheet",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${primaryChapter.revisionBulletPoints.size} rapid-recall points",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = tactile.emeraldFace,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}
