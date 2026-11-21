package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors

/**
 * Tactile 3D game-board card with a real solid bottom lip that physically compresses
 * downward on press with spring physics and a subtle haptic tick. Zero neon glow or blur shadows.
 */
@Composable
fun KovaTactileCard(
    modifier: Modifier = Modifier,
    shape: Shape = KovaExpressiveShapes.StatAndAssignmentCard,
    faceColor: Color = LocalKovaTactileColors.current.cardFace,
    lipColor: Color = LocalKovaTactileColors.current.cardLip,
    borderColor: Color = LocalKovaTactileColors.current.cardOutline,
    lipHeight: Dp = 5.dp,
    borderWidth: Dp = 1.5.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val motion = LocalKovaMotion.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed && onClick != null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
        // Grounded solid 3D bottom lip
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = lipHeight)
                .clip(shape)
                .background(lipColor),
        )

        // Elevated tactile face that physically presses down into the lip
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
 * Physical 3D keycap button with darker accent bottom lip, downward spring press, and haptic tick.
 */
@Composable
fun KovaTactileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    faceColor: Color = LocalKovaTactileColors.current.emeraldFace,
    lipColor: Color = LocalKovaTactileColors.current.emeraldLip,
    contentColor: Color = Color.White,
    borderColor: Color = Color.Transparent,
    shape: Shape = KovaExpressiveShapes.Pill,
    lipHeight: Dp = 5.dp,
    minHeight: Dp = 52.dp,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val motion = LocalKovaMotion.current
    val haptic = LocalHapticFeedback.current
    val tactile = LocalKovaTactileColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                        Modifier.border(BorderStroke(1.5.dp, borderColor), shape)
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
 * Thick 3D Circular Progress Ring framing a chapter emblem.
 * Draws a recessed 3D track bevel + thick beveled progress arc that animates smoothly
 * as lessons complete (0% -> 25% -> 50% -> 75% -> 100%).
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
) {
    val motion = LocalKovaMotion.current
    val tactile = LocalKovaTactileColors.current

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (motion.enabled) {
            spring(dampingRatio = 0.72f, stiffness = 210f)
        } else {
            snap()
        },
        label = "chapter_3d_ring_progress",
    )

    Box(
        modifier = modifier.size(size),
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

            // Recessed 3D track lip underneath
            drawArc(
                color = tactile.ringTrackBevel,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(topLeft.x, topLeft.y + bevelDropPx),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // Top circular track face
            drawArc(
                color = tactile.ringTrack,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // 3D beveled progress arc
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

        // Tactile 3D circular emblem seated inside the ring
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

/**
 * Tactile 3D squircle/circular keycap badge used for Lesson Nodes inside chapter drop-downs.
 */
@Composable
fun KovaTactileNodeCap(
    icon: ImageVector,
    contentDescription: String?,
    faceColor: Color,
    lipColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    lipHeight: Dp = 5.dp,
    shape: Shape = KovaExpressiveShapes.LessonSquircleNode,
) {
    Box(
        modifier = modifier.size(width = size, height = size + lipHeight),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .offset(y = lipHeight)
                .clip(shape)
                .background(lipColor),
        )
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(faceColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(26.dp),
            )
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
