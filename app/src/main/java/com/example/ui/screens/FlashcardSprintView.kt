package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Flip
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
 * Offline 60-Second 3D Flashcard Flip Sprint Mode.
 * Uses real Y-axis rotation (`graphicsLayer { rotationY = ...; cameraDistance = 14f * density }`)
 * with Pause/Resume/Restart controls, tactile 3D rating buttons, and an end-of-sprint summary.
 */
@Composable
fun FlashcardSprintView(
    deck: List<SpacedRepetitionItem>,
    sprint: FlashcardSprintState,
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
    val density = LocalDensity.current.density

    // Celebratory rising chime when the 60s sprint completes (deck finished or timer hits 0)
    LaunchedEffect(sprint.isSummaryVisible) {
        if (sprint.isSummaryVisible) {
            feedback.success(view)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("flashcard_sprint_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Live 60s Countdown Header Card
        KovaTactileCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.cardLip,
            borderColor = tactile.cardOutline,
            lipHeight = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    @OptIn(ExperimentalLayoutApi::class)
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
                            text = "Got It: ${sprint.gotItCount} · Again: ${sprint.reviewAgainCount}",
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
                                .size(38.dp)
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
                                .size(38.dp)
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
                        .height(8.dp)
                        .clip(KovaExpressiveShapes.Pill),
                )
            }
        }

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
                    modifier = Modifier.padding(20.dp),
                )
            }
            return
        }

        if (sprint.isSummaryVisible) {
            SprintSummaryCard(
                sprint = sprint,
                onRestart = onRestart,
            )
            return
        }

        val currentCard = deck[sprint.currentIndex.coerceIn(0, deck.lastIndex)]
        val targetRotation = if (sprint.isFlipped) 180f else 0f
        val animatedRotation by animateFloatAsState(
            targetValue = targetRotation,
            animationSpec = if (motion.enabled) {
                spring(dampingRatio = 0.76f, stiffness = 340f)
            } else {
                snap()
            },
            label = "flashcard_3d_flip_rotation",
        )
        val showingBack = animatedRotation > 90f

        // Real 3D Y-Axis Flip Flashcard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    rotationY = animatedRotation
                    cameraDistance = 14f * density
                }
                .clickable {
                    feedback.tap(view)
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

        // Tactile 3D Rating Controls
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
                minHeight = 52.dp,
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
                )
            }

            KovaTactileButton(
                onClick = {
                    feedback.success(view)
                    onRateGotIt()
                },
                faceColor = tactile.emeraldFace,
                lipColor = tactile.emeraldLip,
                contentColor = tactile.onEmeraldFace,
                minHeight = 52.dp,
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
                )
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
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.cobaltLip,
        borderColor = tactile.cobaltFace,
        lipHeight = 6.dp,
        borderWidth = 1.75.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
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
                Text(
                    text = "$cardNumber / $totalCards",
                    style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = card.title,
                style = MaterialTheme.typography.labelLarge,
                color = tactile.cobaltFace,
            )

            Text(
                text = card.frontPrompt,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flip,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.flashcard_tap_to_flip),
                    style = KovaCodeTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FlashcardBackFace(
    card: SpacedRepetitionItem,
) {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.emeraldLip,
        borderColor = tactile.emeraldFace,
        lipHeight = 6.dp,
        borderWidth = 1.75.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KovaPillBadge(
                    text = "10-YEAR-OLD RULE",
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, tactile.cardOutline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = card.codeExample,
                    style = KovaCodeTextStyle,
                    color = tactile.emeraldFace,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}

@Composable
private fun SprintSummaryCard(
    sprint: FlashcardSprintState,
    onRestart: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val totalReviewed = sprint.gotItCount + sprint.reviewAgainCount
    val accuracy = if (totalReviewed == 0) 0 else (sprint.gotItCount * 100) / totalReviewed

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
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
                .padding(22.dp),
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
                text = "Reviewed $totalReviewed cards · $accuracy% Got It accuracy · ${sprint.secondsRemaining}s left on clock",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            @OptIn(ExperimentalLayoutApi::class)
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
            KovaTactileButton(
                onClick = onRestart,
                faceColor = tactile.cobaltFace,
                lipColor = tactile.cobaltLip,
                contentColor = tactile.onCobaltFace,
                minHeight = 50.dp,
                lipHeight = 5.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("flashcard_summary_restart_button"),
            ) {
                Text(
                    text = "Run Another 60s Sprint",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = tactile.onCobaltFace,
                )
            }
        }
    }
}
