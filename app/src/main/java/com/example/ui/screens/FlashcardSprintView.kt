package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Flip
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.FlashcardSprintState
import com.example.model.SpacedRepetitionItem
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
 * Inline Flashcard Deck Preview card shown inside Revision -> Flashcards.
 * Features deck stats, active topic preview, and a tactile "Launch Focus Stage" CTA
 * that opens the blurred-backdrop 3D Flashcard Focus Stage.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlashcardDeckPreviewCard(
    deck: List<SpacedRepetitionItem>,
    sprint: FlashcardSprintState,
    onLaunchFocusStage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val dueCount = deck.count { it.isDueNow }
    val previewCard = deck.getOrNull(sprint.currentIndex.coerceIn(0, (deck.size - 1).coerceAtLeast(0)))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("flashcard_preview_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KovaTactileCard(
            shape = KovaExpressiveShapes.HeroChapterCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.cobaltLip,
            borderColor = tactile.cobaltFace.copy(alpha = 0.65f),
            lipHeight = 6.dp,
            borderWidth = 1.5.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KovaScallopedSealBadge(
                        icon = Icons.Outlined.Style,
                        contentDescription = null,
                        containerColor = tactile.cobaltFace,
                        lipColor = tactile.cobaltLip,
                        contentColor = tactile.onCobaltFace,
                        size = 48.dp,
                        iconSize = 24.dp,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Focused 3D Flashcard Stage",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Distraction-free study cards with blurred backdrop, 3D flip & formatted Kotlin code blocks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    KovaPillBadge(
                        text = "${deck.size} CARDS IN DECK",
                        containerColor = tactile.cobaltSoft,
                        contentColor = tactile.onCobaltSoft,
                        borderColor = Color.Transparent,
                    )
                    KovaPillBadge(
                        text = "$dueCount DUE NOW",
                        containerColor = if (dueCount > 0) tactile.coralSoft else tactile.emeraldSoft,
                        contentColor = if (dueCount > 0) tactile.onCoralSoft else tactile.onEmeraldSoft,
                        borderColor = Color.Transparent,
                    )
                    KovaPillBadge(
                        text = "GOT IT ${sprint.gotItCount} · AGAIN ${sprint.reviewAgainCount}",
                        containerColor = tactile.cardElevatedFace,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        borderColor = tactile.cardOutline,
                    )
                }

                if (previewCard != null) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, tactile.cardOutline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "UP NEXT · L${previewCard.lessonCode} ${previewCard.title}",
                                    style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                    color = tactile.cobaltFace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                if (previewCard.codeExample.isNotBlank()) {
                                    KovaPillBadge(
                                        text = "CODE",
                                        leadingIcon = Icons.Outlined.Code,
                                        containerColor = tactile.emeraldSoft,
                                        contentColor = tactile.onEmeraldSoft,
                                        borderColor = Color.Transparent,
                                    )
                                }
                            }
                            Text(
                                text = previewCard.frontPrompt,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                KovaTactileButton(
                    onClick = onLaunchFocusStage,
                    faceColor = tactile.cobaltFace,
                    lipColor = tactile.cobaltLip,
                    contentColor = tactile.onCobaltFace,
                    minHeight = 52.dp,
                    lipHeight = 5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("launch_flashcard_focus_stage_button"),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Fullscreen,
                        contentDescription = null,
                        tint = tactile.onCobaltFace,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Launch Focus Stage (60s Sprint)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = tactile.onCobaltFace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Compact scannable deck queue list beneath the preview card
        deck.forEachIndexed { idx, card ->
            Surface(
                onClick = onLaunchFocusStage,
                shape = RoundedCornerShape(16.dp),
                color = tactile.cardFace,
                border = BorderStroke(1.dp, tactile.cardOutline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    KovaPillBadge(
                        text = "#${idx + 1}",
                        containerColor = if (card.isDueNow) tactile.coralSoft else tactile.emeraldSoft,
                        contentColor = if (card.isDueNow) tactile.onCoralSoft else tactile.onEmeraldSoft,
                        borderColor = Color.Transparent,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "L${card.lessonCode} · ${card.retentionPercent}% retention",
                            style = KovaCodeTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (card.codeExample.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Outlined.Code,
                            contentDescription = "Includes code block",
                            tint = tactile.cobaltFace,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full-screen Focused Flashcard Stage overlaid atop the blurred Revision background.
 * Keeps attention locked onto a physical 3D-flipping study card with formatted monospace
 * Kotlin code blocks, 60s sprint telemetry, and tactile "Review Again" / "Got It" controls.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlashcardFocusStageOverlay(
    deck: List<SpacedRepetitionItem>,
    sprint: FlashcardSprintState,
    onClose: () -> Unit,
    onTogglePause: () -> Unit,
    onRestart: () -> Unit,
    onFlipCard: () -> Unit,
    onRateReviewAgain: () -> Unit,
    onRateGotIt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val motion = LocalKovaMotion.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current

    BackHandler(onBack = onClose)

    LaunchedEffect(sprint.isSummaryVisible) {
        if (sprint.isSummaryVisible) {
            feedback.success(view)
        }
    }

    val scrimColor = if (tactile.isAmoled) {
        Color.Black.copy(alpha = 0.72f)
    } else if (tactile.isDarkSurface) {
        Color(0xFF0C0A08).copy(alpha = 0.68f)
    } else {
        Color(0xFF1A1612).copy(alpha = 0.42f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scrimColor)
            .clickable(enabled = false) {}
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("flashcard_focus_stage_overlay"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Focus Stage Header Bar: Close + Timer + Controls
            KovaTactileCard(
                shape = KovaExpressiveShapes.StatAndAssignmentCard,
                faceColor = tactile.cardFace,
                lipColor = tactile.cardLip,
                borderColor = tactile.cardOutline,
                lipHeight = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = tactile.cardElevatedFace,
                            border = BorderStroke(1.dp, tactile.cardOutline),
                        ) {
                            IconButton(
                                onClick = {
                                    feedback.tap(view)
                                    onClose()
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("flashcard_stage_close_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Exit flashcard focus mode",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        FlowRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            KovaPillBadge(
                                text = "${sprint.secondsRemaining}s",
                                leadingIcon = Icons.Outlined.Timer,
                                containerColor = if (sprint.secondsRemaining <= 15) tactile.coralSoft else tactile.cobaltSoft,
                                contentColor = if (sprint.secondsRemaining <= 15) tactile.onCoralSoft else tactile.onCobaltSoft,
                                borderColor = Color.Transparent,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .testTag("flashcard_timer_badge"),
                            )
                            Text(
                                text = "Got It ${sprint.gotItCount} · Again ${sprint.reviewAgainCount}",
                                style = KovaCodeTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = Modifier.align(Alignment.CenterVertically),
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                onClick = {
                                    feedback.tap(view)
                                    onTogglePause()
                                },
                                shape = CircleShape,
                                color = tactile.cobaltSoft,
                                border = BorderStroke(1.dp, tactile.cobaltFace.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("flashcard_pause_resume_button"),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (sprint.isRunning && !sprint.isPaused) {
                                            Icons.Filled.Pause
                                        } else {
                                            Icons.Filled.PlayArrow
                                        },
                                        contentDescription = if (sprint.isRunning) "Pause sprint timer" else "Start or resume sprint timer",
                                        tint = tactile.onCobaltSoft,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                            Surface(
                                onClick = {
                                    feedback.tap(view)
                                    onRestart()
                                },
                                shape = CircleShape,
                                color = tactile.cardElevatedFace,
                                border = BorderStroke(1.dp, tactile.cardOutline),
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("flashcard_restart_button"),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Restart 60 second sprint",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }

                    val progress = (sprint.secondsRemaining.toFloat() / sprint.totalSeconds.coerceAtLeast(1).toFloat())
                        .coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        color = if (sprint.secondsRemaining <= 15) tactile.coralFace else tactile.emeraldFace,
                        trackColor = tactile.ringTrack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(KovaExpressiveShapes.Pill),
                    )
                }
            }

            // Center Stage: Physical 3D Study Card or Summary
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                if (deck.isEmpty()) {
                    KovaTactileCard(
                        shape = KovaExpressiveShapes.HeroChapterCard,
                        faceColor = tactile.cardFace,
                        lipColor = tactile.cardLip,
                        borderColor = tactile.cardOutline,
                    ) {
                        Text(
                            text = "No flashcards scheduled yet. Complete a lesson or save AI notes to build your deck.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(22.dp),
                        )
                    }
                } else if (sprint.isSummaryVisible) {
                    SprintSummaryCard(
                        sprint = sprint,
                        onRestart = onRestart,
                        onDone = onClose,
                    )
                } else {
                    val currentCard = deck[sprint.currentIndex.coerceIn(0, deck.lastIndex)]
                    val targetRotation = if (sprint.isFlipped) 180f else 0f
                    val animatedRotation by animateFloatAsState(
                        targetValue = targetRotation,
                        animationSpec = if (motion.enabled) {
                            spring(dampingRatio = 0.74f, stiffness = 320f)
                        } else {
                            snap()
                        },
                        label = "flashcard_3d_flip_rotation",
                    )
                    val showingBack = animatedRotation > 90f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                rotationY = animatedRotation
                                cameraDistance = 15f * density
                            }
                            .clickable {
                                feedback.cardFlip(view)
                                onFlipCard()
                            }
                            .testTag("flashcard_3d_card"),
                    ) {
                        if (!showingBack) {
                            FlashcardFrontFace(
                                card = currentCard,
                                cardNumber = sprint.currentIndex + 1,
                                totalCards = deck.size,
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer { rotationY = 180f },
                            ) {
                                FlashcardBackFace(card = currentCard)
                            }
                        }
                    }
                }
            }

            // Tactile Rating Action Footer
            if (deck.isNotEmpty() && !sprint.isSummaryVisible) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KovaTactileButton(
                        onClick = {
                            feedback.softThud(view)
                            onRateReviewAgain()
                        },
                        faceColor = tactile.amberFace,
                        lipColor = tactile.amberLip,
                        contentColor = tactile.onAmberFace,
                        minHeight = 54.dp,
                        lipHeight = 5.dp,
                        playTapSound = false,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("flashcard_review_again_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = tactile.onAmberFace,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.flashcard_review_again),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = tactile.onAmberFace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    KovaTactileButton(
                        onClick = {
                            feedback.gotItChime(view)
                            onRateGotIt()
                        },
                        faceColor = tactile.emeraldFace,
                        lipColor = tactile.emeraldLip,
                        contentColor = tactile.onEmeraldFace,
                        minHeight = 54.dp,
                        lipHeight = 5.dp,
                        playTapSound = false,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("flashcard_got_it_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = tactile.onEmeraldFace,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.flashcard_got_it),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = tactile.onEmeraldFace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardFrontFace(
    card: SpacedRepetitionItem,
    cardNumber: Int,
    totalCards: Int,
) {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = RoundedCornerShape(30.dp),
        faceColor = tactile.cardFace,
        lipColor = tactile.cobaltLip,
        borderColor = tactile.cobaltFace,
        lipHeight = 7.dp,
        borderWidth = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 310.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KovaPillBadge(
                    text = "QUESTION · L${card.lessonCode}",
                    containerColor = tactile.cobaltSoft,
                    contentColor = tactile.onCobaltSoft,
                    borderColor = Color.Transparent,
                )
                KovaPillBadge(
                    text = "$cardNumber / $totalCards",
                    containerColor = tactile.cardElevatedFace,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    borderColor = tactile.cardOutline,
                )
            }

            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = tactile.cobaltFace,
            )

            Text(
                text = card.frontPrompt,
                style = MaterialTheme.typography.headlineSmall.copy(lineHeight = 32.sp),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = KovaExpressiveShapes.Pill,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, tactile.cardOutline),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Flip,
                        contentDescription = null,
                        tint = tactile.cobaltFace,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.flashcard_tap_to_flip),
                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlashcardBackFace(
    card: SpacedRepetitionItem,
) {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = RoundedCornerShape(30.dp),
        faceColor = tactile.cardFace,
        lipColor = tactile.emeraldLip,
        borderColor = tactile.emeraldFace,
        lipHeight = 7.dp,
        borderWidth = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 310.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                KovaPillBadge(
                    text = "ANSWER · 10-YEAR-OLD RULE",
                    containerColor = tactile.emeraldSoft,
                    contentColor = tactile.onEmeraldSoft,
                    borderColor = Color.Transparent,
                )
                KovaPillBadge(
                    text = "RETENTION ${card.retentionPercent}%",
                    containerColor = tactile.cardElevatedFace,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    borderColor = tactile.cardOutline,
                )
            }

            Text(
                text = card.backAnswer,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 25.sp),
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Dedicated Formatted Code Block whenever the flashcard involves code
            if (card.codeExample.isNotBlank()) {
                KovaFlashcardCodeBlock(
                    code = card.codeExample,
                    modifier = Modifier.testTag("flashcard_code_block"),
                )
            }
        }
    }
}

@Composable
private fun KovaFlashcardCodeBlock(
    code: String,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val terminalSurface = if (tactile.isAmoled) {
        Color(0xFF080808)
    } else if (tactile.isDarkSurface) {
        Color(0xFF141210)
    } else {
        Color(0xFF171A21)
    }
    val terminalHeader = if (tactile.isAmoled) {
        Color(0xFF121212)
    } else {
        Color(0xFF1F2430)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = terminalSurface,
        border = BorderStroke(1.25.dp, tactile.emeraldFace.copy(alpha = 0.45f)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Terminal-style header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(terminalHeader)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = null,
                        tint = tactile.emeraldFace,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "KOTLIN · CODE EXAMPLE",
                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFCBD5E1),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(tactile.coralFace))
                    Box(Modifier.size(7.dp).clip(CircleShape).background(tactile.amberFace))
                    Box(Modifier.size(7.dp).clip(CircleShape).background(tactile.emeraldFace))
                }
            }

            // Horizontally scrollable monospace code body
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(
                    text = code.trimIndent(),
                    style = KovaCodeTextStyle.copy(
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                    ),
                    color = Color(0xFF86EFAC),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SprintSummaryCard(
    sprint: FlashcardSprintState,
    onRestart: () -> Unit,
    onDone: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val totalReviewed = sprint.gotItCount + sprint.reviewAgainCount
    val accuracy = if (totalReviewed == 0) 0 else (sprint.gotItCount * 100) / totalReviewed

    KovaTactileCard(
        shape = RoundedCornerShape(30.dp),
        faceColor = tactile.cardFace,
        lipColor = tactile.emeraldLip,
        borderColor = tactile.emeraldFace,
        lipHeight = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("flashcard_sprint_summary"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KovaScallopedSealBadge(
                icon = Icons.Filled.EmojiEvents,
                contentDescription = null,
                containerColor = tactile.amberFace,
                lipColor = tactile.amberLip,
                contentColor = tactile.onAmberFace,
                size = 56.dp,
                iconSize = 28.dp,
            )
            Text(
                text = "60-Second Sprint Complete!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Reviewed $totalReviewed cards · $accuracy% Got It accuracy · ${sprint.secondsRemaining}s left",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KovaPillBadge(
                    text = "${sprint.gotItCount} GOT IT",
                    containerColor = tactile.emeraldSoft,
                    contentColor = tactile.onEmeraldSoft,
                    borderColor = Color.Transparent,
                )
                KovaPillBadge(
                    text = "${sprint.reviewAgainCount} REVIEW AGAIN",
                    containerColor = tactile.amberSoft,
                    contentColor = tactile.onAmberSoft,
                    borderColor = Color.Transparent,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KovaTactileButton(
                    onClick = onDone,
                    faceColor = tactile.cardElevatedFace,
                    lipColor = tactile.cardLip,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    minHeight = 50.dp,
                    lipHeight = 4.dp,
                    modifier = Modifier.weight(0.42f),
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                KovaTactileButton(
                    onClick = onRestart,
                    faceColor = tactile.cobaltFace,
                    lipColor = tactile.cobaltLip,
                    contentColor = tactile.onCobaltFace,
                    minHeight = 50.dp,
                    lipHeight = 4.dp,
                    modifier = Modifier
                        .weight(0.58f)
                        .testTag("flashcard_summary_restart_button"),
                ) {
                    Text(
                        text = "Run Again",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = tactile.onCobaltFace,
                    )
                }
            }
        }
    }
}
