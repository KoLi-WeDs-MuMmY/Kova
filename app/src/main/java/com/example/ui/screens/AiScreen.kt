package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.KovaUiState
import com.example.ui.components.KovaElevatedCard
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaScallopedSealBadge
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiScreen(
    uiState: KovaUiState,
    isWideTabletLayout: Boolean,
    modifier: Modifier = Modifier,
) {
    val activeLesson = uiState.activeLesson
    val quota = uiState.quota

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = if (isWideTabletLayout) 20.dp else 16.dp)
            .testTag("ai_screen_root"),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                KovaElevatedCard(
                    shape = KovaExpressiveShapes.HeroChapterCard,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    accentGlowColor = MaterialTheme.colorScheme.primary,
                    elevation = 10.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            KovaScallopedSealBadge(
                                icon = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                size = 48.dp,
                                iconSize = 22.dp,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                KovaPillBadge(
                                    text = stringResource(R.string.ai_tutor_badge),
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.ai_tutor_title),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        // Active Lesson Scope Context Badge
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_active_lesson_scope_badge"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Column {
                                    Text(
                                        text = stringResource(R.string.ai_context_locked_label),
                                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = if (activeLesson != null) {
                                            "Lesson ${activeLesson.code}: ${activeLesson.title}"
                                        } else {
                                            "Chapter 1: Kotlin Foundations"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }

                        // Quota Budget Pills
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_quota_pills_row"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            KovaPillBadge(
                                text = "Morning Digest: ${quota.morningDigestStatus}",
                                leadingIcon = Icons.Outlined.WbSunny,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f),
                            )
                            KovaPillBadge(
                                text = "Active Pool: ${quota.activeAiPoolRemaining} left",
                                leadingIcon = Icons.Outlined.Bolt,
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            )
                            KovaPillBadge(
                                text = "Spaced-Repetition Reserve: ${quota.spacedRepetitionReserve}",
                                leadingIcon = Icons.Outlined.NotificationsActive,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            // Local Learner Behavior Profile Preview Card
            item {
                KovaElevatedCard(
                    shape = KovaExpressiveShapes.StatAndAssignmentCard,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    elevation = 4.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(26.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Local Learner Behavior Profile (Preview)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Tracks pace, weak syntax spots, and analogy style locally to fuel your Morning Digest without overspending daily AI quota.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // Styled Mock Scoped Conversation Bubble Preview
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SCOPED THREAD PREVIEW",
                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = 6.dp,
                            bottomEnd = 20.dp,
                        ),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "Kova Tutor · Locked to ${activeLesson?.code ?: "1.1"}",
                                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Phase 3 will activate live Gemini responses strictly scoped to your active lesson notes and Lab virtual files. Off-topic drift stays blocked so every quota token builds mastery.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }

        // Disabled Mock Input Composer (Ready for Phase 3)
        Surface(
            shape = KovaExpressiveShapes.Pill,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isWideTabletLayout) 20.dp else 104.dp)
                .testTag("ai_disabled_input_bar"),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.ai_input_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.size(36.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send disabled in Phase 1",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}
