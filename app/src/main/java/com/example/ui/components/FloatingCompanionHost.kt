package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import kotlin.math.roundToInt

private enum class SnapCorner(val label: String) {
    TOP_LEFT("TL"),
    TOP_RIGHT("TR"),
    CENTER("Center"),
    BOTTOM_LEFT("BL"),
    BOTTOM_RIGHT("BR"),
}

/**
 * Smoothly transitions between Full View and a draggable Floating Companion Window preview
 * over a dimmed simulated CodeAssist/Termux workspace backdrop with corner-snap controls.
 */
@Composable
fun FloatingCompanionHost(
    isFloatingPreview: Boolean,
    onExitFloating: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (isCompactFloating: Boolean) -> Unit,
) {
    val motion = LocalKovaMotion.current
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }

        val targetWidthDp = if (isFloatingPreview) {
            (maxWidth * 0.86f).coerceIn(290.dp, 390.dp)
        } else {
            maxWidth
        }
        val targetHeightDp = if (isFloatingPreview) {
            (maxHeight * 0.74f).coerceIn(430.dp, 640.dp)
        } else {
            maxHeight
        }

        val animatedWidth by animateDpAsState(
            targetValue = targetWidthDp,
            animationSpec = if (motion.enabled) {
                spring(dampingRatio = 0.78f, stiffness = 360f)
            } else {
                snap()
            },
            label = "floating_width",
        )
        val animatedHeight by animateDpAsState(
            targetValue = targetHeightDp,
            animationSpec = if (motion.enabled) {
                spring(dampingRatio = 0.78f, stiffness = 360f)
            } else {
                snap()
            },
            label = "floating_height",
        )
        val animatedCornerRadius by animateDpAsState(
            targetValue = if (isFloatingPreview) 28.dp else 0.dp,
            animationSpec = if (motion.enabled) {
                spring(dampingRatio = 0.82f, stiffness = 380f)
            } else {
                snap()
            },
            label = "floating_corner",
        )

        val windowWidthPx = with(density) { targetWidthDp.toPx() }
        val windowHeightPx = with(density) { targetHeightDp.toPx() }
        val edgeMarginPx = with(density) { 12.dp.toPx() }
        val topSafeMarginPx = with(density) { 44.dp.toPx() }
        val bottomControlMarginPx = with(density) { 86.dp.toPx() }

        val maxOffsetX = ((containerWidthPx - windowWidthPx) / 2f - edgeMarginPx).coerceAtLeast(0f)
        val maxOffsetYUp = ((containerHeightPx - windowHeightPx) / 2f - topSafeMarginPx).coerceAtLeast(0f)
        val maxOffsetYDown = ((containerHeightPx - windowHeightPx) / 2f - bottomControlMarginPx).coerceAtLeast(0f)

        var rawOffsetX by remember { mutableFloatStateOf(0f) }
        var rawOffsetY by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(isFloatingPreview, maxWidth, maxHeight) {
            if (!isFloatingPreview) {
                rawOffsetX = 0f
                rawOffsetY = 0f
            } else {
                rawOffsetX = rawOffsetX.coerceIn(-maxOffsetX, maxOffsetX)
                rawOffsetY = rawOffsetY.coerceIn(-maxOffsetYUp, maxOffsetYDown)
            }
        }

        val animatedOffsetX by animateFloatAsState(
            targetValue = if (isFloatingPreview) rawOffsetX else 0f,
            animationSpec = if (motion.enabled) {
                spring(dampingRatio = 0.80f, stiffness = 420f)
            } else {
                snap()
            },
            label = "floating_offset_x",
        )
        val animatedOffsetY by animateFloatAsState(
            targetValue = if (isFloatingPreview) rawOffsetY else 0f,
            animationSpec = if (motion.enabled) {
                spring(dampingRatio = 0.80f, stiffness = 420f)
            } else {
                snap()
            },
            label = "floating_offset_y",
        )

        // Dimmed simulated CodeAssist / Termux workspace backdrop behind floating window
        AnimatedVisibility(
            visible = isFloatingPreview,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            SimulatedWorkspaceBackdrop(
                onSnap = { corner ->
                    when (corner) {
                        SnapCorner.TOP_LEFT -> {
                            rawOffsetX = -maxOffsetX
                            rawOffsetY = -maxOffsetYUp
                        }
                        SnapCorner.TOP_RIGHT -> {
                            rawOffsetX = maxOffsetX
                            rawOffsetY = -maxOffsetYUp
                        }
                        SnapCorner.CENTER -> {
                            rawOffsetX = 0f
                            rawOffsetY = 0f
                        }
                        SnapCorner.BOTTOM_LEFT -> {
                            rawOffsetX = -maxOffsetX
                            rawOffsetY = maxOffsetYDown
                        }
                        SnapCorner.BOTTOM_RIGHT -> {
                            rawOffsetX = maxOffsetX
                            rawOffsetY = maxOffsetYDown
                        }
                    }
                },
                onExitFloating = onExitFloating,
            )
        }

        // Main App Frame (Full Screen or Draggable Floating Companion Window)
        val frameShape = RoundedCornerShape(animatedCornerRadius)
        val borderModifier = if (isFloatingPreview) {
            Modifier
                .shadow(
                    elevation = 24.dp,
                    shape = frameShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                    spotColor = Color.Black.copy(alpha = 0.65f),
                )
                .border(
                    BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f),
                            ),
                        ),
                    ),
                    frameShape,
                )
        } else {
            Modifier
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset {
                    IntOffset(
                        x = animatedOffsetX.roundToInt(),
                        y = animatedOffsetY.roundToInt(),
                    )
                }
                .size(width = animatedWidth, height = animatedHeight)
                .then(borderModifier)
                .clip(frameShape)
                .background(MaterialTheme.colorScheme.background),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (isFloatingPreview) {
                    FloatingWindowDragHeader(
                        onDrag = { dx, dy ->
                            rawOffsetX = (rawOffsetX + dx).coerceIn(-maxOffsetX, maxOffsetX)
                            rawOffsetY = (rawOffsetY + dy).coerceIn(-maxOffsetYUp, maxOffsetYDown)
                        },
                        onExpand = onExitFloating,
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    content(isFloatingPreview)
                }
            }
        }
    }
}

@Composable
private fun FloatingWindowDragHeader(
    onDrag: (Float, Float) -> Unit,
    onExpand: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("floating_drag_handle"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.floating_preview_label),
                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Icon(
            imageVector = Icons.Outlined.DragHandle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )

        IconButton(
            onClick = onExpand,
            modifier = Modifier
                .size(32.dp)
                .testTag("floating_expand_button"),
        ) {
            Icon(
                imageVector = Icons.Outlined.OpenInFull,
                contentDescription = stringResource(R.string.cd_toggle_floating_mode),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SimulatedWorkspaceBackdrop(
    onSnap: (SnapCorner) -> Unit,
    onExitFloating: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF04060A))
            .clickable(onClick = onExitFloating),
    ) {
        // Subtle simulated terminal/IDE grid lines to evoke CodeAssist / Termux behind the overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Terminal,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "CodeAssist · MainActivity.kt (Simulated Backdrop)",
                    style = KovaCodeTextStyle,
                    color = Color(0xFF64748B),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            val mockCodeLines = listOf(
                "01  package dev.learner.kova",
                "02  fun main() {",
                "03      val dailyGoal = 3",
                "04      var completed = 1",
                "05      println(\"Remaining: \${dailyGoal - completed}\")",
                "06  }",
            )
            mockCodeLines.forEach { line ->
                Text(
                    text = line,
                    style = KovaCodeTextStyle,
                    color = Color(0xFF334155),
                )
            }
        }

        // Bottom corner-snap bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clickable(enabled = false) {},
            shape = KovaExpressiveShapes.Pill,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            tonalElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Snap:",
                    style = KovaCodeTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, end = 2.dp),
                )
                SnapCorner.entries.forEach { corner ->
                    Surface(
                        onClick = { onSnap(corner) },
                        shape = KovaExpressiveShapes.Pill,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.testTag("snap_corner_${corner.name.lowercase()}"),
                    ) {
                        Text(
                            text = corner.label,
                            style = KovaCodeTextStyle.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}
