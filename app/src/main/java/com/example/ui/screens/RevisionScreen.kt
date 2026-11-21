package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Chapter
import com.example.model.DetailDestination
import com.example.model.HighlightCategory
import com.example.model.HighlightNote
import com.example.model.KovaUiState
import com.example.model.MasteryRadarSummary
import com.example.model.NoteSource
import com.example.model.RevisionBinderTab
import com.example.model.SpacedRepetitionItem
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaScallopedSealBadge
import com.example.ui.components.KovaTactileButton
import com.example.ui.components.KovaTactileCard
import com.example.ui.feedback.LocalKovaFeedback
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
    onSelectBinderTab: (RevisionBinderTab) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onSelectFilter: (HighlightCategory?) -> Unit,
    onFocusNextFromRadar: () -> Unit,
    onLaunchFlashcardFocusStage: () -> Unit,
    onStartQuickReview: () -> Unit,
    onNoteClick: (String) -> Unit,
    onOpenChapterRevisionSheet: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = LocalKovaMotion.current
    val horizontalPad = if (isWideTabletLayout) 22.dp else 16.dp
    val activeChapter = uiState.chapters.firstOrNull { it.isUnlocked } ?: uiState.chapters.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPad)
            .testTag("revision_screen_list"),
        contentPadding = PaddingValues(top = 6.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Structured Compact Top Header: Mastery Telemetry + Quota Pills + Active Chapter Revision Sheet
        item(key = "revision_mastery_quota_header") {
            RevisionMasteryAndQuotaHeaderCard(
                uiState = uiState,
                activeChapter = activeChapter,
                onOpenChapterRevisionSheet = {
                    activeChapter?.let { onOpenChapterRevisionSheet(it.id) }
                },
            )
        }

        // Clean 3-Way Segmented Binder Switcher (Overview & Notes | Flashcards | SR Queue)
        item(key = "binder_segment_bar") {
            RevisionBinderSegmentedBar(
                selectedTab = uiState.selectedBinderTab,
                highlightsCount = uiState.highlights.size,
                dueNowCount = uiState.dueNowSrItems.size,
                onSelectTab = onSelectBinderTab,
            )
        }

        // Spring-damped Binder Section Content
        item(key = "binder_active_section") {
            AnimatedContent(
                targetState = uiState.selectedBinderTab,
                transitionSpec = {
                    if (motion.enabled) {
                        (fadeIn(tween(200)) + slideInVertically(spring(dampingRatio = 0.76f, stiffness = 340f)) { it / 16 })
                            .togetherWith(fadeOut(tween(140)))
                    } else {
                        fadeIn(snap()) togetherWith fadeOut(snap())
                    }
                },
                label = "revision_binder_tab_switch",
            ) { activeTab ->
                when (activeTab) {
                    RevisionBinderTab.HIGHLIGHTS -> OverviewAndNotesBinderSection(
                        uiState = uiState,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onFocusNextFromRadar = onFocusNextFromRadar,
                        onUpdateSearchQuery = onUpdateSearchQuery,
                        onSelectFilter = onSelectFilter,
                        onNoteClick = onNoteClick,
                        onOpenChapterRevisionSheet = onOpenChapterRevisionSheet,
                    )
                    RevisionBinderTab.FLASHCARDS -> FlashcardDeckPreviewCard(
                        deck = uiState.flashcardDeck,
                        sprint = uiState.flashcardSprint,
                        onLaunchFocusStage = onLaunchFlashcardFocusStage,
                    )
                    RevisionBinderTab.SR_QUEUE -> SpacedRepetitionQueueSection(
                        dueNowItems = uiState.dueNowSrItems,
                        upcomingItems = uiState.upcomingSrItems,
                        onStartQuickReview = onStartQuickReview,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RevisionMasteryAndQuotaHeaderCard(
    uiState: KovaUiState,
    activeChapter: Chapter?,
    onOpenChapterRevisionSheet: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val allLessons = uiState.chapters.flatMap { it.lessons }
    val completedCount = allLessons.count { it.status == com.example.model.LessonStatus.COMPLETED }
    val avgRetention = if (uiState.spacedRepetitionItems.isEmpty()) {
        0
    } else {
        uiState.spacedRepetitionItems.map { it.retentionPercent }.average().toInt()
    }

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.cobaltLip,
        borderColor = tactile.cobaltFace.copy(alpha = 0.55f),
        lipHeight = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("revision_top_header_card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KovaScallopedSealBadge(
                    icon = Icons.Outlined.AutoStories,
                    contentDescription = null,
                    containerColor = tactile.cobaltFace,
                    lipColor = tactile.cobaltLip,
                    contentColor = tactile.onCobaltFace,
                    size = 44.dp,
                    iconSize = 22.dp,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Study Binder",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "$avgRetention% avg recall · $completedCount/${allLessons.size} lessons mastered",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Responsive Quota & Retention Telemetry Row (zero overlap on compact widths)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                KovaPillBadge(
                    text = "DAILY ${uiState.quota.dailyLessonsRemaining}/${uiState.quota.dailyLessonsMax}",
                    containerColor = tactile.emeraldSoft,
                    contentColor = tactile.onEmeraldSoft,
                    borderColor = Color.Transparent,
                )
                KovaPillBadge(
                    text = "DUE NOW ${uiState.dueNowSrItems.size}",
                    containerColor = if (uiState.dueNowSrItems.isNotEmpty()) tactile.coralSoft else tactile.emeraldSoft,
                    contentColor = if (uiState.dueNowSrItems.isNotEmpty()) tactile.onCoralSoft else tactile.onEmeraldSoft,
                    borderColor = Color.Transparent,
                )
                KovaPillBadge(
                    text = "SR RESERVE ${uiState.quota.spacedRepetitionReserve}",
                    containerColor = tactile.amberSoft,
                    contentColor = tactile.onAmberSoft,
                    borderColor = Color.Transparent,
                )
            }

            // Active Chapter Revision Sheet Quick Entry
            if (activeChapter != null) {
                HorizontalDivider(thickness = 1.dp, color = tactile.cardOutline.copy(alpha = 0.6f))
                Surface(
                    onClick = onOpenChapterRevisionSheet,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, tactile.cardOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("revision_header_active_sheet_button"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 13.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoStories,
                            contentDescription = null,
                            tint = tactile.emeraldFace,
                            modifier = Modifier.size(18.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chapter ${activeChapter.number} Revision Sheet",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "${activeChapter.title} · ${activeChapter.revisionBulletPoints.size} rapid-recall points",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Open revision sheet",
                            tint = tactile.emeraldFace,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RevisionBinderSegmentedBar(
    selectedTab: RevisionBinderTab,
    highlightsCount: Int,
    dueNowCount: Int,
    onSelectTab: (RevisionBinderTab) -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.Pill,
        faceColor = tactile.cardFace,
        lipColor = tactile.cardLip,
        borderColor = tactile.cardOutline,
        lipHeight = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("revision_binder_segment_bar"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val segments = listOf(
                Triple(
                    RevisionBinderTab.HIGHLIGHTS,
                    "Notes ($highlightsCount)",
                    "binder_tab_highlights",
                ),
                Triple(
                    RevisionBinderTab.FLASHCARDS,
                    stringResource(R.string.binder_flashcards),
                    "binder_tab_flashcards",
                ),
                Triple(
                    RevisionBinderTab.SR_QUEUE,
                    "SR ($dueNowCount)",
                    "binder_tab_sr_queue",
                ),
            )

            segments.forEach { (tab, label, tag) ->
                val isSelected = selectedTab == tab
                Surface(
                    onClick = {
                        feedback.tap(view)
                        onSelectTab(tab)
                    },
                    shape = KovaExpressiveShapes.Pill,
                    color = if (isSelected) tactile.cobaltFace else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag(tag),
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            ),
                            color = if (isSelected) tactile.onCobaltFace else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun OverviewAndNotesBinderSection(
    uiState: KovaUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onFocusNextFromRadar: () -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onSelectFilter: (HighlightCategory?) -> Unit,
    onNoteClick: (String) -> Unit,
    onOpenChapterRevisionSheet: (String) -> Unit,
) {
    val tactile = LocalKovaTactileColors.current

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Concept Mastery & Mistake Radar Card
        ConceptMasteryRadarCard(
            radar = uiState.masteryRadar,
            onFocusNext = onFocusNextFromRadar,
        )

        // Search Input
        OutlinedTextField(
            value = uiState.revisionSearchQuery,
            onValueChange = onUpdateSearchQuery,
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.revision_search_notes_hint),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = tactile.cobaltFace,
                unfocusedBorderColor = tactile.cardOutline,
                focusedContainerColor = tactile.cardFace,
                unfocusedContainerColor = tactile.cardFace,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("revision_notes_search_input"),
        )

        // Category Filter Pills
        HighlightFilterPillsRow(
            selected = uiState.selectedHighlightFilter,
            onSelect = onSelectFilter,
        )

        val notes = uiState.filteredHighlights
        BinderSectionHeader(
            title = "Highlighted Study Notes",
            count = notes.size,
            icon = Icons.Outlined.BookmarkBorder,
            badgeContainerColor = tactile.cobaltSoft,
            badgeContentColor = tactile.onCobaltSoft,
        )

        if (notes.isEmpty()) {
            KovaTactileCard(
                shape = KovaExpressiveShapes.StatAndAssignmentCard,
                faceColor = tactile.cardFace,
                lipColor = tactile.cardLip,
                borderColor = tactile.cardOutline,
                lipHeight = 4.dp,
            ) {
                Text(
                    text = "No saved highlights match your current filter. Clear search or save an AI Tutor answer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(18.dp),
                )
            }
        } else {
            notes.forEach { note ->
                HighlightNoteCard(
                    note = note,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onClick = { onNoteClick(note.id) },
                )
            }
        }

        // All Chapter Revision Sheets
        HorizontalDivider(thickness = 1.dp, color = tactile.cardOutline.copy(alpha = 0.65f))
        BinderSectionHeader(
            title = "Chapter Revision Sheets",
            count = uiState.chapters.size,
            icon = Icons.Outlined.AutoStories,
            badgeContainerColor = tactile.emeraldSoft,
            badgeContentColor = tactile.onEmeraldSoft,
        )
        uiState.chapters.forEach { chapter ->
            KovaTactileCard(
                shape = KovaExpressiveShapes.StatAndAssignmentCard,
                faceColor = tactile.cardFace,
                lipColor = tactile.cardLip,
                borderColor = tactile.cardOutline,
                lipHeight = 4.dp,
                onClick = { onOpenChapterRevisionSheet(chapter.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("revision_chapter_sheet_${chapter.id}"),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KovaScallopedSealBadge(
                        icon = Icons.Outlined.AutoStories,
                        contentDescription = null,
                        containerColor = tactile.emeraldSoft,
                        lipColor = tactile.emeraldFace.copy(alpha = 0.3f),
                        contentColor = tactile.onEmeraldSoft,
                        size = 40.dp,
                        iconSize = 19.dp,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chapter ${chapter.number}: ${chapter.title}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${chapter.revisionBulletPoints.size} rapid-recall points · ${(chapter.progressFraction * 100).toInt()}% mastered",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = tactile.emeraldFace,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConceptMasteryRadarCard(
    radar: MasteryRadarSummary,
    onFocusNext: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.StatAndAssignmentCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.cardLip,
        borderColor = tactile.cardOutline,
        lipHeight = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mastery_radar_card"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Radar,
                    contentDescription = null,
                    tint = tactile.cobaltFace,
                    modifier = Modifier.size(22.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.revision_radar_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = radar.focusReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = tactile.emeraldSoft,
                    border = BorderStroke(1.dp, tactile.emeraldFace.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(
                        modifier = Modifier.padding(11.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TrendingUp,
                                contentDescription = null,
                                tint = tactile.onEmeraldSoft,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = "STRONGEST",
                                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                color = tactile.onEmeraldSoft,
                                maxLines = 1,
                            )
                        }
                        radar.strongest.forEach { metric ->
                            Text(
                                text = "• ${metric.label} (${metric.scorePercent}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = tactile.onEmeraldSoft,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = tactile.coralSoft,
                    border = BorderStroke(1.dp, tactile.coralFace.copy(alpha = 0.45f)),
                    modifier = Modifier.weight(1f),
                ) {
                    Column(
                        modifier = Modifier.padding(11.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TrendingDown,
                                contentDescription = null,
                                tint = tactile.onCoralSoft,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = "WEAK SPOTS",
                                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                color = tactile.onCoralSoft,
                                maxLines = 1,
                            )
                        }
                        radar.weakSpots.forEach { metric ->
                            Text(
                                text = "• ${metric.label} (${metric.scorePercent}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = tactile.onCoralSoft,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            KovaTactileButton(
                onClick = onFocusNext,
                faceColor = tactile.emeraldFace,
                lipColor = tactile.emeraldLip,
                contentColor = tactile.onEmeraldFace,
                minHeight = 46.dp,
                lipHeight = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("radar_focus_next_button"),
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = tactile.onEmeraldFace,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${stringResource(R.string.revision_focus_next)}: ${radar.focusTitle}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = tactile.onEmeraldFace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpacedRepetitionQueueSection(
    dueNowItems: List<SpacedRepetitionItem>,
    upcomingItems: List<SpacedRepetitionItem>,
    onStartQuickReview: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current

    Column(
        modifier = Modifier.testTag("sr_queue_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterVertically),
            ) {
                Text(
                    text = stringResource(R.string.revision_due_now),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                KovaPillBadge(
                    text = "${dueNowItems.size}",
                    containerColor = tactile.coralSoft,
                    contentColor = tactile.onCoralSoft,
                    borderColor = Color.Transparent,
                )
            }

            if (dueNowItems.isNotEmpty()) {
                KovaTactileButton(
                    onClick = onStartQuickReview,
                    faceColor = tactile.amberFace,
                    lipColor = tactile.amberLip,
                    contentColor = tactile.onAmberFace,
                    minHeight = 40.dp,
                    lipHeight = 4.dp,
                    modifier = Modifier.testTag("sr_start_quick_review_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = tactile.onAmberFace,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.revision_start_quick_review),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = tactile.onAmberFace,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }

        if (dueNowItems.isEmpty()) {
            KovaTactileCard(
                shape = KovaExpressiveShapes.StatAndAssignmentCard,
                faceColor = tactile.emeraldSoft,
                lipColor = tactile.emeraldLip,
                borderColor = tactile.emeraldFace,
                lipHeight = 4.dp,
            ) {
                Text(
                    text = "All Due Now reviews cleared! Your retention timers are healthy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tactile.onEmeraldSoft,
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            dueNowItems.forEach { item ->
                SpacedRepetitionQueueCard(
                    item = item,
                    onClick = onStartQuickReview,
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.revision_upcoming),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            KovaPillBadge(
                text = "${upcomingItems.size}",
                containerColor = tactile.cobaltSoft,
                contentColor = tactile.onCobaltSoft,
                borderColor = Color.Transparent,
            )
        }

        upcomingItems.forEach { item ->
            SpacedRepetitionQueueCard(
                item = item,
                onClick = null,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpacedRepetitionQueueCard(
    item: SpacedRepetitionItem,
    onClick: (() -> Unit)?,
) {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.StatAndAssignmentCard,
        faceColor = tactile.cardFace,
        lipColor = if (item.isDueNow) tactile.amberLip else tactile.cardLip,
        borderColor = if (item.isDueNow) tactile.amberFace else tactile.cardOutline,
        lipHeight = 4.dp,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sr_item_${item.id}"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                KovaPillBadge(
                    text = "L${item.lessonCode} · ${item.intervalTag}",
                    containerColor = if (item.isDueNow) tactile.amberSoft else tactile.cobaltSoft,
                    contentColor = if (item.isDueNow) tactile.onAmberSoft else tactile.onCobaltSoft,
                    borderColor = Color.Transparent,
                )
                KovaPillBadge(
                    text = item.countdownLabel,
                    leadingIcon = Icons.Outlined.Schedule,
                    containerColor = tactile.cardElevatedFace,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    borderColor = tactile.cardOutline,
                    useMonospace = false,
                )
            }

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LinearProgressIndicator(
                    progress = { (item.retentionPercent / 100f).coerceIn(0f, 1f) },
                    color = if (item.retentionPercent >= 75) tactile.emeraldFace else tactile.amberFace,
                    trackColor = tactile.ringTrack,
                    modifier = Modifier
                        .weight(1f)
                        .height(7.dp)
                        .clip(KovaExpressiveShapes.Pill),
                )
                Text(
                    text = "${item.retentionPercent}%",
                    style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                    color = if (item.retentionPercent >= 75) tactile.emeraldFace else tactile.amberFace,
                )
            }
        }
    }
}

@Composable
private fun BinderSectionHeader(
    title: String,
    count: Int,
    icon: ImageVector,
    badgeContainerColor: Color,
    badgeContentColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = badgeContentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        KovaPillBadge(
            text = "$count",
            containerColor = badgeContainerColor,
            contentColor = badgeContentColor,
            borderColor = Color.Transparent,
        )
    }
}

@Composable
private fun HighlightFilterPillsRow(
    selected: HighlightCategory?,
    onSelect: (HighlightCategory?) -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current
    val options: List<Pair<HighlightCategory?, String>> = listOf(
        null to stringResource(R.string.filter_all),
        HighlightCategory.KEY_IDEA to stringResource(R.string.filter_key_idea),
        HighlightCategory.CODE_RULE to stringResource(R.string.filter_code_rule),
        HighlightCategory.GOTCHA to stringResource(R.string.filter_gotcha),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (category, label) ->
            val isSelected = selected == category
            val accent = category?.accentColor() ?: tactile.cobaltFace
            val selectedContentColor = when (category) {
                null -> tactile.onCobaltFace
                HighlightCategory.KEY_IDEA -> tactile.onAmberFace
                HighlightCategory.CODE_RULE -> tactile.onEmeraldFace
                HighlightCategory.GOTCHA -> tactile.onCoralFace
            }
            Surface(
                onClick = {
                    feedback.tap(view)
                    onSelect(category)
                },
                shape = KovaExpressiveShapes.Pill,
                color = if (isSelected) accent else tactile.cardFace,
                border = BorderStroke(1.25.dp, if (isSelected) accent else tactile.cardOutline),
                modifier = Modifier.testTag("filter_chip_${category?.name?.lowercase() ?: "all"}"),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) selectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
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
    val sharedKey = DetailDestination.NoteDetail(note.id).sharedBoundsKey

    with(sharedTransitionScope) {
        KovaTactileCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.cardLip,
            borderColor = accent.copy(alpha = 0.5f),
            lipHeight = 4.dp,
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = sharedKey),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ -> motion.containerBoundsSpec },
                )
                .testTag("highlight_note_${note.id}"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    KovaPillBadge(
                        text = note.category.displayLabel(),
                        containerColor = accent.copy(alpha = 0.14f),
                        contentColor = accent,
                        borderColor = accent.copy(alpha = 0.45f),
                        useMonospace = false,
                    )
                    KovaPillBadge(
                        text = if (note.source == NoteSource.AI_TUTOR) "AI TUTOR" else "LESSON",
                        leadingIcon = if (note.source == NoteSource.AI_TUTOR) {
                            Icons.Outlined.AutoAwesome
                        } else {
                            Icons.Outlined.BookmarkBorder
                        },
                        containerColor = tactile.cardElevatedFace,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        borderColor = tactile.cardOutline,
                    )
                    KovaPillBadge(
                        text = "L${note.lessonCode}",
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        borderColor = tactile.cardOutline,
                    )
                }

                Text(
                    text = "\"${note.highlightedSnippet}\"",
                    style = MaterialTheme.typography.titleMedium,
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

@Composable
fun HighlightCategory.accentColor(): Color {
    val tactile = LocalKovaTactileColors.current
    return when (this) {
        HighlightCategory.KEY_IDEA -> tactile.keyIdea
        HighlightCategory.CODE_RULE -> tactile.codeRule
        HighlightCategory.GOTCHA -> tactile.gotcha
    }
}

fun HighlightCategory.displayLabel(): String = when (this) {
    HighlightCategory.KEY_IDEA -> "Key Idea"
    HighlightCategory.CODE_RULE -> "Code Rule"
    HighlightCategory.GOTCHA -> "Gotcha"
}
