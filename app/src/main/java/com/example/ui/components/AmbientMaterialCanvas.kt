package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Subtle ambient background layer of 5 low-opacity expressive M3 geometric shapes
 * (rounded squircle, pill capsule, hollow ring, soft 4-point star, and diamond).
 * Drifts and rotates gently when Tactile Spring Motion is ON; pauses statically as calm
 * background art when Tactile Spring Motion is switched OFF.
 */
@Composable
fun AmbientMaterialCanvas(
    modifier: Modifier = Modifier,
) {
    val motion = LocalKovaMotion.current
    val tactile = LocalKovaTactileColors.current

    val driftPhase: Float
    val slowAngle: Float
    if (motion.enabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "ambient_material_shapes")
        val animatedDrift by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (Math.PI * 2.0).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 24000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "ambient_drift_phase",
        )
        val animatedAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 42000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "ambient_rotation_angle",
        )
        driftPhase = animatedDrift
        slowAngle = animatedAngle
    } else {
        // Static paused coordinates when Tactile Spring Motion is OFF
        driftPhase = 0.6f
        slowAngle = 18f
    }

    val shapeAlpha = when {
        tactile.isAmoled -> 0.055f
        tactile.isDarkSurface -> 0.065f
        else -> 0.072f
    }

    val emeraldTint = tactile.emeraldFace.copy(alpha = shapeAlpha)
    val cobaltTint = tactile.cobaltFace.copy(alpha = shapeAlpha)
    val amberTint = tactile.amberFace.copy(alpha = shapeAlpha * 1.1f)
    val ringTint = tactile.cobaltFace.copy(alpha = shapeAlpha * 0.95f)
    val starTint = tactile.emeraldFace.copy(alpha = shapeAlpha * 0.9f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val driftX = cos(driftPhase) * 18.dp.toPx()
        val driftY = sin(driftPhase) * 14.dp.toPx()

        // 1. Top-right rounded squircle (Cobalt)
        val squircleSize = (w * 0.24f).coerceIn(76.dp.toPx(), 132.dp.toPx())
        val sqCenter = Offset(w * 0.86f + driftX * 0.7f, h * 0.14f - driftY * 0.6f)
        rotate(degrees = slowAngle * 0.25f, pivot = sqCenter) {
            drawRoundRect(
                color = cobaltTint,
                topLeft = Offset(sqCenter.x - squircleSize / 2f, sqCenter.y - squircleSize / 2f),
                size = Size(squircleSize, squircleSize),
                cornerRadius = CornerRadius(squircleSize * 0.34f, squircleSize * 0.34f),
            )
        }

        // 2. Upper-left hollow Material ring (Emerald)
        val ringRadius = (w * 0.13f).coerceIn(42.dp.toPx(), 78.dp.toPx())
        val ringCenter = Offset(w * 0.11f - driftX * 0.6f, h * 0.26f + driftY * 0.8f)
        drawCircle(
            color = ringTint,
            radius = ringRadius,
            center = ringCenter,
            style = Stroke(width = 14.dp.toPx()),
        )

        // 3. Mid-right tilted pill capsule (Amber)
        val pillW = (w * 0.28f).coerceIn(88.dp.toPx(), 150.dp.toPx())
        val pillH = pillW * 0.42f
        val pillCenter = Offset(w * 0.88f - driftX * 0.5f, h * 0.54f + driftY * 0.7f)
        rotate(degrees = -22f - slowAngle * 0.18f, pivot = pillCenter) {
            drawRoundRect(
                color = amberTint,
                topLeft = Offset(pillCenter.x - pillW / 2f, pillCenter.y - pillH / 2f),
                size = Size(pillW, pillH),
                cornerRadius = CornerRadius(pillH / 2f, pillH / 2f),
            )
        }

        // 4. Lower-left soft 4-point M3 expressive star (Emerald)
        val starRadius = (w * 0.14f).coerceIn(44.dp.toPx(), 84.dp.toPx())
        val starCenter = Offset(w * 0.15f + driftX * 0.8f, h * 0.78f - driftY * 0.65f)
        rotate(degrees = slowAngle * 0.35f, pivot = starCenter) {
            val starPath = Path().apply {
                val points = 8
                for (i in 0 until points) {
                    val angle = (i * Math.PI / 4.0)
                    val r = if (i % 2 == 0) starRadius else starRadius * 0.48f
                    val px = starCenter.x + (cos(angle) * r).toFloat()
                    val py = starCenter.y + (sin(angle) * r).toFloat()
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(path = starPath, color = starTint)
        }

        // 5. Bottom-right rounded diamond (Emerald/Amber blend)
        val diamondSide = (w * 0.17f).coerceIn(54.dp.toPx(), 96.dp.toPx())
        val diamondCenter = Offset(w * 0.76f + driftX * 0.5f, h * 0.88f + driftY * 0.5f)
        rotate(degrees = 45f - slowAngle * 0.22f, pivot = diamondCenter) {
            drawRoundRect(
                color = emeraldTint,
                topLeft = Offset(diamondCenter.x - diamondSide / 2f, diamondCenter.y - diamondSide / 2f),
                size = Size(diamondSide, diamondSide),
                cornerRadius = CornerRadius(diamondSide * 0.28f, diamondSide * 0.28f),
            )
        }
    }
}
