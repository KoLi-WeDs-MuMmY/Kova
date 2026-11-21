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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Rich, low-opacity Material 3 Expressive ambient background layer with 7 varied geometric
 * objects (rounded squircle, hollow ring, pill capsule, 4-point expressive star, curved arc/wave
 * ribbon, 6-lobe scalloped polygon, and rounded diamond).
 * Gently drifts & rotates when Tactile Spring Motion is ON; pauses statically as subtle
 * background art when motion is OFF. Never intercepts touches or obscures text.
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
        val infiniteTransition = rememberInfiniteTransition(label = "ambient_m3_expressive_objects")
        val animatedDrift by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (PI * 2.0).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 26000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "ambient_drift_phase",
        )
        val animatedAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 46000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "ambient_rotation_angle",
        )
        driftPhase = animatedDrift
        slowAngle = animatedAngle
    } else {
        driftPhase = 0.72f
        slowAngle = 22f
    }

    val baseAlpha = when {
        tactile.isAmoled -> 0.028f
        tactile.isDarkSurface -> 0.058f
        else -> 0.068f
    }

    val emeraldTint = tactile.emeraldFace.copy(alpha = baseAlpha)
    val cobaltTint = tactile.cobaltFace.copy(alpha = baseAlpha)
    val amberTint = tactile.amberFace.copy(alpha = baseAlpha * 1.08f)
    val coralTint = tactile.coralFace.copy(alpha = baseAlpha * 1.05f)
    val waveTint = tactile.cobaltFace.copy(alpha = baseAlpha * 0.85f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val driftX = cos(driftPhase) * 18.dp.toPx()
        val driftY = sin(driftPhase) * 15.dp.toPx()

        // 1. Top-right rounded squircle (Balanced Cobalt)
        val squircleSize = (w * 0.23f).coerceIn(74.dp.toPx(), 130.dp.toPx())
        val sqCenter = Offset(w * 0.87f + driftX * 0.7f, h * 0.13f - driftY * 0.6f)
        rotate(degrees = slowAngle * 0.25f, pivot = sqCenter) {
            drawRoundRect(
                color = cobaltTint,
                topLeft = Offset(sqCenter.x - squircleSize / 2f, sqCenter.y - squircleSize / 2f),
                size = Size(squircleSize, squircleSize),
                cornerRadius = CornerRadius(squircleSize * 0.34f, squircleSize * 0.34f),
            )
        }

        // 2. Upper-left hollow expressive ring (Emerald)
        val ringRadius = (w * 0.12f).coerceIn(40.dp.toPx(), 74.dp.toPx())
        val ringCenter = Offset(w * 0.10f - driftX * 0.55f, h * 0.23f + driftY * 0.75f)
        drawCircle(
            color = emeraldTint,
            radius = ringRadius,
            center = ringCenter,
            style = Stroke(width = 13.dp.toPx()),
        )

        // 3. Curved sinusoidal M3 Expressive arc/wave ribbon sweeping mid-upper background
        val wavePath = Path().apply {
            val startY = h * 0.38f + driftY * 0.45f
            moveTo(-20.dp.toPx(), startY)
            cubicTo(
                w * 0.28f,
                startY - 38.dp.toPx() + driftY * 0.3f,
                w * 0.68f,
                startY + 42.dp.toPx() - driftY * 0.3f,
                w + 20.dp.toPx(),
                startY - 12.dp.toPx(),
            )
        }
        drawPath(
            path = wavePath,
            color = waveTint,
            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
        )

        // 4. Mid-right tilted pill capsule (Amber)
        val pillW = (w * 0.27f).coerceIn(86.dp.toPx(), 148.dp.toPx())
        val pillH = pillW * 0.42f
        val pillCenter = Offset(w * 0.88f - driftX * 0.5f, h * 0.55f + driftY * 0.68f)
        rotate(degrees = -24f - slowAngle * 0.18f, pivot = pillCenter) {
            drawRoundRect(
                color = amberTint,
                topLeft = Offset(pillCenter.x - pillW / 2f, pillCenter.y - pillH / 2f),
                size = Size(pillW, pillH),
                cornerRadius = CornerRadius(pillH / 2f, pillH / 2f),
            )
        }

        // 5. Mid-left 6-lobe scalloped expressive polygon (Coral)
        val scallopRadius = (w * 0.11f).coerceIn(36.dp.toPx(), 68.dp.toPx())
        val scallopCenter = Offset(w * 0.14f + driftX * 0.5f, h * 0.52f - driftY * 0.55f)
        rotate(degrees = -slowAngle * 0.28f, pivot = scallopCenter) {
            val scallopPath = Path().apply {
                val steps = 48
                for (i in 0..steps) {
                    val theta = (i.toDouble() / steps) * PI * 2.0
                    val r = scallopRadius * (1.0 + 0.16 * cos(6.0 * theta))
                    val px = scallopCenter.x + (cos(theta) * r).toFloat()
                    val py = scallopCenter.y + (sin(theta) * r).toFloat()
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(path = scallopPath, color = coralTint)
        }

        // 6. Lower-left soft 4-point M3 expressive star (Emerald)
        val starRadius = (w * 0.14f).coerceIn(44.dp.toPx(), 82.dp.toPx())
        val starCenter = Offset(w * 0.16f + driftX * 0.75f, h * 0.80f - driftY * 0.6f)
        rotate(degrees = slowAngle * 0.34f, pivot = starCenter) {
            val starPath = Path().apply {
                val steps = 36
                for (i in 0..steps) {
                    val theta = (i.toDouble() / steps) * PI * 2.0
                    val r = starRadius * (0.74 + 0.26 * cos(4.0 * theta))
                    val px = starCenter.x + (cos(theta) * r).toFloat()
                    val py = starCenter.y + (sin(theta) * r).toFloat()
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(path = starPath, color = emeraldTint)
        }

        // 7. Bottom-right rounded diamond & subtle curved arc companion (Coral / Cobalt)
        val diamondSide = (w * 0.16f).coerceIn(52.dp.toPx(), 94.dp.toPx())
        val diamondCenter = Offset(w * 0.79f + driftX * 0.48f, h * 0.88f + driftY * 0.48f)
        rotate(degrees = 45f - slowAngle * 0.22f, pivot = diamondCenter) {
            drawRoundRect(
                color = coralTint,
                topLeft = Offset(diamondCenter.x - diamondSide / 2f, diamondCenter.y - diamondSide / 2f),
                size = Size(diamondSide, diamondSide),
                cornerRadius = CornerRadius(diamondSide * 0.28f, diamondSide * 0.28f),
            )
        }
    }
}
