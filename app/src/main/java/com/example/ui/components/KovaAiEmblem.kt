package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.LocalKovaTactileColors

/**
 * Custom geometric Kova AI emblem badge with a physical 3D bottom lip, orbital ring,
 * and 4-point neural spark. Adapts crisply across Warm Daylight, Warm Dark, and True AMOLED.
 */
@Composable
fun KovaAiEmblem(
    size: Dp = 40.dp,
    lipHeight: Dp = 3.5.dp,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val cornerRadius = size * 0.34f
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .size(width = size, height = size + lipHeight)
            .semantics { contentDescription = "Kova AI emblem" },
        contentAlignment = Alignment.TopCenter,
    ) {
        // 3D bottom lip
        Box(
            modifier = Modifier
                .size(size)
                .padding(top = lipHeight)
                .clip(shape)
                .background(tactile.cobaltLip),
        )
        // Raised face
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(tactile.cobaltFace),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.16f)) {
                val cx = this.size.width / 2f
                val cy = this.size.height / 2f
                val r = minOf(cx, cy)

                // Crisp outer orbital ring
                drawCircle(
                    color = tactile.onCobaltFace.copy(alpha = 0.28f),
                    radius = r * 0.92f,
                    center = Offset(cx, cy),
                    style = Stroke(width = (r * 0.14f).coerceAtLeast(1.8f), cap = StrokeCap.Round),
                )

                // Center 4-point geometric spark
                val outer = r * 0.78f
                val inner = r * 0.24f
                val spark = Path().apply {
                    moveTo(cx, cy - outer)
                    quadraticTo(cx + inner * 0.35f, cy - inner * 0.35f, cx + outer, cy)
                    quadraticTo(cx + inner * 0.35f, cy + inner * 0.35f, cx, cy + outer)
                    quadraticTo(cx - inner * 0.35f, cy + inner * 0.35f, cx - outer, cy)
                    quadraticTo(cx - inner * 0.35f, cy - inner * 0.35f, cx, cy - outer)
                    close()
                }
                drawPath(path = spark, color = tactile.onCobaltFace)

                // Subtle emerald & amber companion nodes on the orbit
                drawCircle(
                    color = tactile.emeraldFace,
                    radius = (r * 0.17f).coerceAtLeast(2.2f),
                    center = Offset(cx + r * 0.64f, cy - r * 0.60f),
                )
                drawCircle(
                    color = tactile.amberFace,
                    radius = (r * 0.12f).coerceAtLeast(1.8f),
                    center = Offset(cx - r * 0.64f, cy + r * 0.58f),
                )
            }
        }
    }
}

/**
 * Centered brand welcome stage displayed at the top of fresh / empty AI threads.
 */
@Composable
fun KovaAiWelcomeEmblemCard(
    scopedLabel: String,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, tactile.cardOutline),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KovaAiEmblem(size = 56.dp, lipHeight = 5.dp)
            Text(
                text = "Kova AI Companion",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Scoped strictly to $scopedLabel. Ask a concept doubt, paste an error, or pick a prompt chip below—every message saves locally.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = tactile.emeraldSoft,
                border = BorderStroke(1.dp, tactile.emeraldFace.copy(alpha = 0.45f)),
            ) {
                Text(
                    text = "LOCAL HISTORY ON · LIVE GEMINI IN PHASE 3",
                    style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                    color = tactile.onEmeraldSoft,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

/**
 * Clean, distraction-free centered AI emblem centerpiece shown when a chat thread is empty
 * or starting fresh. Avoids heavy box clutter and offers an optional quick return pill when
 * entered from inside a specific lesson.
 */
@Composable
fun KovaAiCenteredEmptyState(
    isLessonScoped: Boolean,
    scopedLessonLabel: String?,
    returnLessonCode: String?,
    onReturnToLesson: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KovaAiEmblem(size = 64.dp, lipHeight = 5.dp)

        Text(
            text = if (isLessonScoped) "Kova Lesson Tutor" else "Ask Kova AI",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Text(
            text = if (isLessonScoped && !scopedLessonLabel.isNullOrBlank()) {
                "Focused on $scopedLessonLabel. Ask a doubt or tap a quick prompt below."
            } else {
                "Fresh chat ready. Ask about Kotlin syntax, analogies, or errors."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (returnLessonCode != null) {
            Surface(
                onClick = onReturnToLesson,
                shape = RoundedCornerShape(999.dp),
                color = tactile.emeraldSoft,
                border = BorderStroke(1.25.dp, tactile.emeraldFace.copy(alpha = 0.6f)),
            ) {
                Text(
                    text = "← Return to Lesson $returnLessonCode",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = tactile.onEmeraldSoft,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

