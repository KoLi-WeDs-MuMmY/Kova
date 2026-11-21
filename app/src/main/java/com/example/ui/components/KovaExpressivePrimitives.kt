package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaSemanticColors

/**
 * Layered tonal card with subtle 1.dp gradient border and ambient shadow.
 * Avoids flat uniform boxes across Obsidian, AMOLED True Black, and Light modes.
 */
@Composable
fun KovaElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = KovaExpressiveShapes.StatAndAssignmentCard,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    accentGlowColor: Color? = null,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    elevation: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val semantic = LocalKovaSemanticColors.current
    val effectiveBorderAlpha = if (semantic.isAmoled) 0.65f else 0.42f
    val borderBrush = if (accentGlowColor != null) {
        Brush.linearGradient(
            colors = listOf(
                accentGlowColor.copy(alpha = 0.55f),
                borderColor.copy(alpha = effectiveBorderAlpha),
                accentGlowColor.copy(alpha = 0.22f),
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                borderColor.copy(alpha = effectiveBorderAlpha + 0.16f),
                borderColor.copy(alpha = effectiveBorderAlpha * 0.65f),
            ),
        )
    }

    val shadowColor = if (semantic.isDarkSurface) {
        (accentGlowColor ?: Color.Black).copy(alpha = if (semantic.isAmoled) 0.18f else 0.35f)
    } else {
        Color.Black.copy(alpha = 0.10f)
    }

    val cardModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = shadowColor,
            spotColor = shadowColor,
        )
        .clip(shape)
        .background(containerColor)
        .then(
            if (accentGlowColor != null) {
                Modifier.background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentGlowColor.copy(alpha = if (semantic.isDarkSurface) 0.11f else 0.08f),
                            Color.Transparent,
                        ),
                        center = Offset(120f, 40f),
                        radius = 520f,
                    ),
                )
            } else {
                Modifier
            },
        )
        .border(BorderStroke(1.dp, borderBrush), shape)
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            },
        )

    Box(modifier = cardModifier, content = content)
}

@Composable
fun KovaPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
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
fun KovaProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 74.dp,
    strokeWidth: Dp = 7.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    centerLabel: String? = null,
) {
    val motion = LocalKovaMotion.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (motion.enabled) {
            spring(dampingRatio = 0.8f, stiffness = 240f)
        } else {
            snap()
        },
        label = "kova_progress_ring",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            if (animatedProgress > 0.005f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            activeColor.copy(alpha = 0.72f),
                            activeColor,
                            activeColor,
                        ),
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }

        val label = centerLabel ?: "${(animatedProgress * 100).toInt()}%"
        Text(
            text = label,
            style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun KovaScallopedSealBadge(
    icon: ImageVector,
    contentDescription: String?,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    shape: Shape = KovaExpressiveShapes.ScallopedSeal,
) {
    Box(
        modifier = modifier
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
