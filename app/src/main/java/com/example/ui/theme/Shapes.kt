package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val KovaMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * Material 3 Extended Expressive shape tokens for Kova.
 */
object KovaExpressiveShapes {
    val HeroChapterCard: Shape = RoundedCornerShape(28.dp)
    val StatAndAssignmentCard: Shape = RoundedCornerShape(20.dp)
    val LessonSquircleNode: Shape = RoundedCornerShape(26.dp)
    val FloatingCompanionWindow: Shape = RoundedCornerShape(28.dp)
    val Pill: Shape = CircleShape
    val AsymmetricPreviewCard: Shape = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 16.dp,
        bottomEnd = 28.dp,
        bottomStart = 16.dp,
    )

    /**
     * Expressive M3 10-lobe soft scalloped seal used for mastery trophies and active badges.
     */
    val ScallopedSeal: Shape = createScallopShape(lobes = 10, innerRadiusRatio = 0.86f)

    /**
     * Expressive M3 6-lobe soft clover seal for chapter milestones.
     */
    val SoftCloverBadge: Shape = createScallopShape(lobes = 6, innerRadiusRatio = 0.84f)
}

private fun createScallopShape(lobes: Int, innerRadiusRatio: Float): Shape =
    GenericShape { size, _ ->
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = minOf(cx, cy)
        val innerR = outerR * innerRadiusRatio
        val steps = lobes * 12
        for (i in 0..steps) {
            val theta = (2.0 * PI * i) / steps
            val wave = (cos(lobes * theta) + 1.0) / 2.0
            val r = innerR + (outerR - innerR) * wave.toFloat()
            val x = cx + r * cos(theta).toFloat()
            val y = cy + r * sin(theta).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
