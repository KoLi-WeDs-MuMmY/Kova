package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DetailDestination
import com.example.model.HighlightCategory
import com.example.model.HighlightNote
import com.example.model.KovaUiState
import com.example.model.SpacedRepetitionItem
import com.example.ui.components.KovaElevatedCard
import com.example.ui.components.KovaPillBadge
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaSemanticColors

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RevisionScreen(
    uiState: KovaUiState,
    isWideTabletLayout: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onSelectFilter: (HighlightCategory?) -> Unit,
    onNoteClick: (String) -> Unit,
    onOpenChapterRevisionSheet: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val highlights = uiState.filteredHighlights

    if (isWideTabletLayout) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1.15f)
                    .testTag("revision_notes_list"),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    NotesSectionHeader(
                        selectedFilter = uiState.selectedHighlightFilter,
                        onSelectFilter = onSelectFilter,
                    )
                }
                items(
                    count = highlights.size,
                    key = { highlights[it].id },
                ) { idx ->
                    HighlightNoteCard(
                        note = highlights[idx],
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onClick = { onNoteClick(highlights[idx].id) },
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(0.85f)
                    .testTag("revision_sr_pane"),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    RevisionQuotaSummaryCard(
                        uiState = uiState,
                        onOpenChapter1Sheet = {
                            uiState.chapters.firstOrNull()?.let { onOpenChapterRevisionSheet(it.id) }
                        },
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.revision_sr_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                items(
                    count = uiState.spacedRepetitionItems.size,
                    key = { uiState.spacedRepetitionItems[it].id },
                ) { idx ->
                    SpacedRepetitionTimerCard(item = uiState.spacedRepetitionItems[idx])
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("revision_screen_list"),
            contentPadding = PaddingValues(top = 8.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                RevisionQuotaSummaryCard(
                    uiState = uiState,
                    onOpenChapter1Sheet = {
                        uiState.chapters.firstOrNull()?.let { onOpenChapterRevisionSheet(it.id) }
                    },
                )
            }
            item {
                NotesSectionHeader(
                    selectedFilter = uiState.selectedHighlightFilter,
                    onSelectFilter = onSelectFilter,
                )
            }
            items(
                count = highlights.size,
                key = { highlights[it].id },
            ) { idx ->
                HighlightNoteCard(
                    note = highlights[idx],
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onClick = { onNoteClick(highlights[idx].id) },
                )
            }
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.revision_sr_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            items(
                count = uiState.spacedRepetitionItems.size,
                key = { uiState.spacedRepetitionItems[it].id },
            ) { idx ->
                SpacedRepetitionTimerCard(item = uiState.spacedRepetitionItems[idx])
            }
        }
    }
}

@Composable
private fun NotesSectionHeader(
    selectedFilter: HighlightCategory?,
    onSelectFilter: (HighlightCategory?) -> Unit,
) {
    val semantic = LocalKovaSemanticColors.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.revision_notes_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                HighlightFilterChip(
                    label = stringResource(R.string.filter_all),
                    dotColor = MaterialTheme.colorScheme.primary,
                    selected = selectedFilter == null,
                    onClick = { onSelectFilter(null) },
                    testTag = "filter_notes_all",
                )
            }
            item {
                HighlightFilterChip(
                    label = stringResource(R.string.filter_key_idea),
                    dotColor = semantic.keyIdea,
                    selected = selectedFilter == HighlightCategory.KEY_IDEA,
                    onClick = { onSelectFilter(HighlightCategory.KEY_IDEA) },
                    testTag = "filter_notes_key_idea",
                )
            }
            item {
                HighlightFilterChip(
                    label = stringResource(R.string.filter_code_rule),
                    dotColor = semantic.codeRule,
                    selected = selectedFilter == HighlightCategory.CODE_RULE,
                    onClick = { onSelectFilter(HighlightCategory.CODE_RULE) },
                    testTag = "filter_notes_code_rule",
                )
            }
            item {
                HighlightFilterChip(
                    label = stringResource(R.string.filter_ask_ai),
                    dotColor = semantic.askAi,
                    selected = selectedFilter == HighlightCategory.ASK_AI,
                    onClick = { onSelectFilter(HighlightCategory.ASK_AI) },
                    testTag = "filter_notes_ask_ai",
                )
            }
        }
    }
}

@Composable
private fun HighlightFilterChip(
    label: String,
    dotColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        shape = KovaExpressiveShapes.Pill,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = dotColor.copy(alpha = 0.7f),
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
        modifier = Modifier.testTag(testTag),
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun HighlightNoteCard(
    note: HighlightNote,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
) {
    val motion = LocalKovaMotion.current
    val accent = note.category.accentColor()
    val categoryLabel = note.category.displayLabel()
    val sharedKey = DetailDestination.NoteDetail(note.id).sharedBoundsKey

    with(sharedTransitionScope) {
        KovaElevatedCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            accentGlowColor = accent,
            borderColor = accent.copy(alpha = 0.55f),
            elevation = 6.dp,
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = sharedKey),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ -> motion.containerBoundsSpec },
                )
                .testTag("revision_note_card_${note.id}"),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(74.dp)
                        .clip(CircleShape)
                        .background(accent),
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        KovaPillBadge(
                            text = categoryLabel.uppercase(),
                            containerColor = accent.copy(alpha = 0.16f),
                            contentColor = accent,
                            borderColor = accent.copy(alpha = 0.45f),
                        )
                        Text(
                            text = "Lesson ${note.lessonCode} · ${note.savedAtLabel}",
                            style = KovaCodeTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Text(
                        text = "\"${note.highlightedSnippet}\"",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = note.learnerNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SpacedRepetitionTimerCard(item: SpacedRepetitionItem) {
    KovaElevatedCard(
        shape = KovaExpressiveShapes.StatAndAssignmentCard,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sr_timer_card_${item.id}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = KovaExpressiveShapes.LessonSquircleNode,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Alarm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Lesson ${item.lessonCode} · ${item.intervalTag} · ${item.retentionPercent}% recall",
                    style = KovaCodeTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            KovaPillBadge(
                text = item.countdownLabel,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun RevisionQuotaSummaryCard(
    uiState: KovaUiState,
    onOpenChapter1Sheet: () -> Unit,
) {
    KovaElevatedCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        accentGlowColor = MaterialTheme.colorScheme.secondary,
        elevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Revision & Quota Pulse",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                KovaPillBadge(
                    text = "SR RESERVE: ${uiState.quota.spacedRepetitionReserve}",
                    leadingIcon = Icons.Outlined.Bolt,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Text(
                text = "Daily Lessons: ${uiState.quota.dailyLessonsRemaining}/${uiState.quota.dailyLessonsMax} left · Morning Digest: ${uiState.quota.morningDigestStatus}",
                style = KovaCodeTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                onClick = onOpenChapter1Sheet,
                shape = KovaExpressiveShapes.Pill,
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoStories,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Open Chapter 1 Revision Sheet",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
fun HighlightCategory.accentColor(): Color {
    val semantic = LocalKovaSemanticColors.current
    return when (this) {
        HighlightCategory.KEY_IDEA -> semantic.keyIdea
        HighlightCategory.CODE_RULE -> semantic.codeRule
        HighlightCategory.ASK_AI -> semantic.askAi
    }
}

fun HighlightCategory.displayLabel(): String = when (this) {
    HighlightCategory.KEY_IDEA -> "Key Idea"
    HighlightCategory.CODE_RULE -> "Code Rule"
    HighlightCategory.ASK_AI -> "Ask AI"
}
