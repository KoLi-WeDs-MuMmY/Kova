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
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaTactileButton
import com.example.ui.components.KovaTactileCard
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors

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
                        color = MaterialTheme.colorScheme.onBackground,
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
                    color = MaterialTheme.colorScheme.onBackground,
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
    val tactile = LocalKovaTactileColors.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.revision_notes_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                HighlightFilterChip(
                    label = stringResource(R.string.filter_all),
                    dotColor = tactile.cobaltFace,
                    selected = selectedFilter == null,
                    onClick = { onSelectFilter(null) },
                    testTag = "filter_notes_all",
                )
            }
            item {
                HighlightFilterChip(
                    label = stringResource(R.string.filter_key_idea),
                    dotColor = tactile.keyIdea,
                    selected = selectedFilter == HighlightCategory.KEY_IDEA,
                    onClick = { onSelectFilter(HighlightCategory.KEY_IDEA) },
                    testTag = "filter_notes_key_idea",
                )
            }
            item {
                HighlightFilterChip(
                    label = stringResource(R.string.filter_code_rule),
                    dotColor = tactile.codeRule,
                    selected = selectedFilter == HighlightCategory.CODE_RULE,
                    onClick = { onSelectFilter(HighlightCategory.CODE_RULE) },
                    testTag = "filter_notes_code_rule",
                )
            }
            item {
                HighlightFilterChip(
                    label = stringResource(R.string.filter_ask_ai),
                    dotColor = tactile.askAi,
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
    val tactile = LocalKovaTactileColors.current

    FilterChip(
        selected = selected,
        onClick = onClick,
        shape = KovaExpressiveShapes.Pill,
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(9.dp)
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
            containerColor = tactile.cardFace,
            selectedContainerColor = dotColor.copy(alpha = 0.14f),
            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = dotColor,
            borderColor = tactile.cardOutline,
            selectedBorderWidth = 1.5.dp,
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
    val tactile = LocalKovaTactileColors.current
    val accent = note.category.accentColor()
    val categoryLabel = note.category.displayLabel()
    val sharedKey = DetailDestination.NoteDetail(note.id).sharedBoundsKey

    with(sharedTransitionScope) {
        KovaTactileCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.cardLip,
            borderColor = tactile.cardOutline,
            lipHeight = 4.dp,
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
                        .width(5.dp)
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
                            containerColor = accent.copy(alpha = 0.14f),
                            contentColor = accent,
                            borderColor = accent.copy(alpha = 0.4f),
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
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.StatAndAssignmentCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.cardLip,
        borderColor = tactile.cardOutline,
        lipHeight = 4.dp,
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
                color = tactile.amberSoft,
                border = BorderStroke(1.dp, tactile.amberFace.copy(alpha = 0.4f)),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Alarm,
                        contentDescription = null,
                        tint = tactile.amberLip,
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
                containerColor = tactile.amberSoft,
                contentColor = tactile.onAmberSoft,
                borderColor = tactile.amberFace.copy(alpha = 0.45f),
            )
        }
    }
}

@Composable
private fun RevisionQuotaSummaryCard(
    uiState: KovaUiState,
    onOpenChapter1Sheet: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.amberLip,
        borderColor = tactile.amberFace.copy(alpha = 0.6f),
        lipHeight = 5.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
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
                    containerColor = tactile.amberSoft,
                    contentColor = tactile.onAmberSoft,
                    borderColor = tactile.amberFace.copy(alpha = 0.45f),
                )
            }
            Text(
                text = "Daily Lessons: ${uiState.quota.dailyLessonsRemaining}/${uiState.quota.dailyLessonsMax} left · Morning Digest: ${uiState.quota.morningDigestStatus}",
                style = KovaCodeTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            KovaTactileButton(
                onClick = onOpenChapter1Sheet,
                faceColor = tactile.emeraldFace,
                lipColor = tactile.emeraldLip,
                contentColor = Color.White,
                minHeight = 46.dp,
                lipHeight = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoStories,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Chapter 1 Revision Sheet",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
fun HighlightCategory.accentColor(): Color {
    val tactile = LocalKovaTactileColors.current
    return when (this) {
        HighlightCategory.KEY_IDEA -> tactile.keyIdea
        HighlightCategory.CODE_RULE -> tactile.codeRule
        HighlightCategory.ASK_AI -> tactile.askAi
    }
}

fun HighlightCategory.displayLabel(): String = when (this) {
    HighlightCategory.KEY_IDEA -> "Key Idea"
    HighlightCategory.CODE_RULE -> "Code Rule"
    HighlightCategory.ASK_AI -> "Ask AI"
}
