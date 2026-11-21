package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.feedback.LocalKovaFeedback
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors

/**
 * Tactile 3D game-board card with a real solid bottom lip that physically compresses
 * downward on press with spring physics and synthesized tap sound + haptic tick.
 */
@Composable
fun KovaTactileCard(
    modifier: Modifier = Modifier,
    shape: Shape = KovaExpressiveShapes.StatAndAssignmentCard,
    faceColor: Color = LocalKovaTactileColors.current.cardFace,
    lipColor: Color = LocalKovaTactileColors.current.cardLip,
    borderColor: Color = LocalKovaTactileColors.current.cardOutline,
    lipHeight: Dp = 5.dp,
    borderWidth: Dp = LocalKovaTactileColors.current.cardBorderWidthDp.dp,
    onClick: (() -> Unit)? = null,
    playTapSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val motion = LocalKovaMotion.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed && onClick != null && playTapSound) {
            feedback.tap(view)
        }
    }

    val targetOffset = if (isPressed && onClick != null) {
        (lipHeight - 1.dp).coerceAtLeast(0.dp)
    } else {
        0.dp
    }

    val pressOffsetY by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = if (motion.enabled) {
            spring(dampingRatio = 0.65f, stiffness = 620f)
        } else {
            snap()
        },
        label = "tactile_card_press",
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier.then(clickableModifier),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = lipHeight)
                .clip(shape)
                .background(lipColor),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = lipHeight)
                .offset(y = pressOffsetY)
                .clip(shape)
                .background(faceColor)
                .border(BorderStroke(borderWidth, borderColor), shape),
            content = content,
        )
    }
}

/**
 * Physical 3D keycap button with darker accent bottom lip, downward spring press,
 * and synthesized sound + haptic tick.
 */
@Composable
fun KovaTactileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    faceColor: Color = LocalKovaTactileColors.current.emeraldFace,
    lipColor: Color = LocalKovaTactileColors.current.emeraldLip,
    contentColor: Color = LocalKovaTactileColors.current.onEmeraldFace,
    borderColor: Color = Color.Transparent,
    shape: Shape = KovaExpressiveShapes.Pill,
    lipHeight: Dp = 5.dp,
    minHeight: Dp = 50.dp,
    enabled: Boolean = true,
    playTapSound: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val motion = LocalKovaMotion.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current
    val tactile = LocalKovaTactileColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed && enabled && playTapSound) {
            feedback.tap(view)
        }
    }

    val effectiveFace = if (enabled) faceColor else tactile.stoneFace
    val effectiveLip = if (enabled) lipColor else tactile.stoneLip

    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed && enabled) (lipHeight - 1.dp).coerceAtLeast(0.dp) else 0.dp,
        animationSpec = if (motion.enabled) {
            spring(dampingRatio = 0.62f, stiffness = 680f)
        } else {
            snap()
        },
        label = "tactile_button_press",
    )

    Box(
        modifier = modifier
            .height(minHeight + lipHeight)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = lipHeight)
                .clip(shape)
                .background(effectiveLip),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight)
                .offset(y = pressOffsetY)
                .clip(shape)
                .background(effectiveFace)
                .then(
                    if (borderColor != Color.Transparent) {
                        Modifier.border(BorderStroke(1.25.dp, borderColor), shape)
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content,
        )
    }
}

/**
 * Tactile 3D circular/squircle Quest Pedestal (80.dp) for the winding S-curve lesson path.
 * Features a physical 6.dp bottom lip, downward spring press physics, synthesized keycap sound,
 * and an optional animated pulsing crown ring on the active quest node (zero neon glow).
 */
@Composable
fun Kova3DQuestPedestal(
    icon: ImageVector,
    contentDescription: String?,
    faceColor: Color,
    lipColor: Color,
    iconTint: Color,
    isActiveQuest: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pedestalSize: Dp = 80.dp,
    lipHeight: Dp = 6.dp,
    shape: Shape = KovaExpressiveShapes.LessonSquircleNode,
) {
    val motion = LocalKovaMotion.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current
    val tactile = LocalKovaTactileColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            feedback.pedestalPress(view)
        }
    }

    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed) (lipHeight - 1.dp) else 0.dp,
        animationSpec = if (motion.enabled) {
            spring(dampingRatio = 0.62f, stiffness = 680f)
        } else {
            snap()
        },
        label = "quest_pedestal_press",
    )

    val pulseScale: Float
    val crownBobY: Float
    if (isActiveQuest && motion.enabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "active_quest_ring")
        val animatedScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "quest_ring_pulse_scale",
        )
        val animatedBob by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "quest_crown_bob",
        )
        pulseScale = animatedScale
        crownBobY = animatedBob
    } else {
        pulseScale = 1.04f
        crownBobY = 0f
    }

    Box(
        modifier = modifier
            .size(width = pedestalSize + 18.dp, height = pedestalSize + lipHeight + 22.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isActiveQuest) {
            Box(
                modifier = Modifier
                    .size(pedestalSize + 12.dp)
                    .offset(y = 6.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .border(
                        BorderStroke(2.5.dp, faceColor.copy(alpha = 0.55f)),
                        shape,
                    ),
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .size(width = pedestalSize, height = pedestalSize + lipHeight),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .size(pedestalSize)
                    .offset(y = lipHeight)
                    .clip(shape)
                    .background(lipColor),
            )
            Box(
                modifier = Modifier
                    .size(pedestalSize)
                    .offset(y = pressOffsetY)
                    .clip(shape)
                    .background(faceColor)
                    .border(
                        BorderStroke(1.5.dp, Color.White.copy(alpha = if (tactile.isDarkSurface) 0.16f else 0.28f)),
                        shape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(34.dp),
                )
            }
        }

        if (isActiveQuest) {
            Surface(
                shape = CircleShape,
                color = tactile.coralFace,
                border = BorderStroke(1.5.dp, tactile.coralLip),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer { translationY = crownBobY }
                    .size(26.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = tactile.onCoralFace,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}

/**
 * 3D Golden Trophy Pedestal sitting at the climax of each dropped-down chapter quest path.
 */
@Composable
fun Kova3DGoldenTrophyPedestal(
    isUnlocked: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val face = when {
        isCompleted -> tactile.emeraldFace
        isUnlocked -> tactile.amberFace
        else -> tactile.stoneFace
    }
    val lip = when {
        isCompleted -> tactile.emeraldLip
        isUnlocked -> tactile.amberLip
        else -> tactile.stoneLip
    }
    val tint = when {
        isCompleted -> tactile.onEmeraldFace
        isUnlocked -> tactile.onAmberFace
        else -> tactile.stoneLabel
    }

    Kova3DQuestPedestal(
        icon = Icons.Filled.EmojiEvents,
        contentDescription = "Chapter Assignment Trophy Pedestal",
        faceColor = face,
        lipColor = lip,
        iconTint = tint,
        isActiveQuest = isUnlocked && !isCompleted,
        onClick = onClick,
        pedestalSize = 84.dp,
        lipHeight = 6.dp,
        shape = KovaExpressiveShapes.ScallopedSeal,
        modifier = modifier,
    )
}

/**
 * Thick 3D Circular Progress Ring framing a chapter emblem.
 */
@Composable
fun Kova3DChapterRing(
    progress: Float,
    emblemIcon: ImageVector,
    emblemContentDescription: String?,
    arcColor: Color,
    arcLipColor: Color,
    emblemFaceColor: Color,
    emblemLipColor: Color,
    emblemIconTint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp,
    strokeWidth: Dp = 9.dp,
    celebrationNonce: Long? = null,
) {
    val motion = LocalKovaMotion.current
    val tactile = LocalKovaTactileColors.current

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (motion.enabled) {
            spring(dampingRatio = 0.76f, stiffness = 340f)
        } else {
            snap()
        },
        label = "chapter_3d_ring_progress",
    )

    val ringPulse = remember { Animatable(1f) }
    LaunchedEffect(celebrationNonce, motion.enabled) {
        if (celebrationNonce != null && motion.enabled) {
            ringPulse.snapTo(1f)
            ringPulse.animateTo(1.12f, spring(dampingRatio = 0.55f, stiffness = 480f))
            ringPulse.animateTo(1f, spring(dampingRatio = 0.72f, stiffness = 340f))
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = ringPulse.value
                scaleY = ringPulse.value
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokePx = strokeWidth.toPx()
            val bevelDropPx = 3.dp.toPx()
            val inset = strokePx / 2f + 1.dp.toPx()
            val arcDiameter = this.size.minDimension - (inset * 2f) - bevelDropPx
            val topLeft = Offset(
                x = (this.size.width - arcDiameter) / 2f,
                y = (this.size.height - arcDiameter - bevelDropPx) / 2f,
            )
            val arcSize = Size(arcDiameter, arcDiameter)

            drawArc(
                color = tactile.ringTrackBevel,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(topLeft.x, topLeft.y + bevelDropPx),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            drawArc(
                color = tactile.ringTrack,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            if (animatedProgress > 0.005f) {
                val sweep = animatedProgress * 360f
                drawArc(
                    color = arcLipColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(topLeft.x, topLeft.y + bevelDropPx * 0.85f),
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
                drawArc(
                    color = arcColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }

        val innerDiameter = size - (strokeWidth * 2) - 14.dp
        val emblemLip = 4.dp
        Box(
            modifier = Modifier.size(innerDiameter),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = emblemLip)
                    .clip(CircleShape)
                    .background(emblemLipColor),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(bottom = emblemLip)
                    .clip(CircleShape)
                    .background(emblemFaceColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = emblemIcon,
                    contentDescription = emblemContentDescription,
                    tint = emblemIconTint,
                    modifier = Modifier.size(innerDiameter * 0.48f),
                )
            }
        }
    }
}

@Composable
fun KovaPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = LocalKovaTactileColors.current.cardOutline,
    leadingIcon: ImageVector? = null,
    useMonospace: Boolean = true,
) {
    Surface(
        modifier = modifier,
        shape = KovaExpressiveShapes.Pill,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = text,
                style = if (useMonospace) {
                    KovaCodeTextStyle.copy(fontWeight = FontWeight.SemiBold)
                } else {
                    MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                },
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun KovaScallopedSealBadge(
    icon: ImageVector,
    contentDescription: String?,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    lipColor: Color? = null,
    size: Dp = 46.dp,
    iconSize: Dp = 22.dp,
    shape: Shape = KovaExpressiveShapes.ScallopedSeal,
) {
    val lipDepth = if (lipColor != null) 4.dp else 0.dp
    Box(
        modifier = modifier.size(width = size, height = size + lipDepth),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (lipColor != null) {
            Box(
                modifier = Modifier
                    .size(size)
                    .offset(y = lipDepth)
                    .clip(shape)
                    .background(lipColor),
            )
        }
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(containerColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

fun Modifier.shakeOnTrigger(
    triggerNonce: Long?,
    enabled: Boolean,
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(triggerNonce, enabled) {
        if (triggerNonce != null && enabled) {
            offsetX.snapTo(0f)
            val amplitudes = listOf(-14f, 14f, -10f, 10f, -5f, 0f)
            for (target in amplitudes) {
                offsetX.animateTo(target, animationSpec = tween(durationMillis = 42))
            }
        }
    }

    graphicsLayer {
        translationX = offsetX.value
    }
}

/**
 * Exit-from-Lesson tactile return spring settle (or celebratory bounce when newly completed).
 */
fun Modifier.settleOrCelebrateOnReturn(
    returnNonce: Long?,
    isCelebration: Boolean,
    enabled: Boolean,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val bounceY = remember { Animatable(0f) }

    LaunchedEffect(returnNonce, isCelebration, enabled) {
        if (returnNonce != null && enabled) {
            if (isCelebration) {
                scale.snapTo(0.88f)
                bounceY.snapTo(-18f)
                scale.animateTo(1.14f, spring(dampingRatio = 0.45f, stiffness = 420f))
                bounceY.animateTo(0f, spring(dampingRatio = 0.52f, stiffness = 340f))
                scale.animateTo(1f, spring(dampingRatio = 0.72f, stiffness = 340f))
            } else {
                scale.snapTo(0.93f)
                scale.animateTo(1.04f, spring(dampingRatio = 0.65f, stiffness = 420f))
                scale.animateTo(1f, spring(dampingRatio = 0.76f, stiffness = 340f))
            }
        }
    }

    graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        translationY = bounceY.value
    }
}
