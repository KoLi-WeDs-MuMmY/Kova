package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ConceptRule
import com.example.model.HighlightCategory
import com.example.model.HighlightNote
import com.example.model.Lesson
import com.example.model.LessonInteractiveContent
import com.example.model.LessonStatus
import com.example.model.PracticeTaskSpec
import com.example.model.WordBreakdown
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaScallopedSealBadge
import com.example.ui.components.KovaTactileButton
import com.example.ui.components.KovaTactileCard
import com.example.ui.feedback.LocalKovaFeedback
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonReaderPane(
    lesson: Lesson,
    content: LessonInteractiveContent,
    savedHighlights: List<HighlightNote>,
    dailyRemaining: Int,
    onSaveHighlight: (blockId: String, snippet: String, category: HighlightCategory, learnerNote: String) -> Unit,
    onAskAiHighlight: (blockId: String, snippet: String, learnerNote: String) -> Unit,
    onStartIntegratedTest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current
    val isCompleted = lesson.status == LessonStatus.COMPLETED

    var activeHighlightBlockId by rememberSaveable(lesson.id) { mutableStateOf<String?>(null) }
    var selectedTag by rememberSaveable(lesson.id) { mutableStateOf(HighlightCategory.KEY_IDEA) }
    var noteDraft by rememberSaveable(lesson.id) { mutableStateOf("") }
    var isSolutionVisible by rememberSaveable(lesson.id) { mutableStateOf(false) }

    val savedById = remember(savedHighlights) {
        savedHighlights.associateBy { it.id }
    }

    fun toggleBlockSelection(blockId: String) {
        feedback.tap(view)
        if (activeHighlightBlockId == blockId) {
            activeHighlightBlockId = null
        } else {
            val existing = savedById["hl_${lesson.id}_$blockId"]
            activeHighlightBlockId = blockId
            selectedTag = existing?.category ?: HighlightCategory.KEY_IDEA
            noteDraft = existing?.learnerNote.orEmpty()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 14.dp)
            .testTag("lesson_reader_pane"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Focused Lesson Header
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                KovaPillBadge(
                    text = "LESSON ${lesson.code} · ${lesson.estimatedMinutes}M",
                    containerColor = tactile.cobaltSoft,
                    contentColor = tactile.onCobaltSoft,
                    borderColor = Color.Transparent,
                )
                KovaPillBadge(
                    text = "$dailyRemaining/3 DAILY LEFT",
                    containerColor = tactile.cardFace,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    borderColor = tactile.cardOutline,
                )
                if (isCompleted && lesson.passScorePercent != null) {
                    KovaPillBadge(
                        text = "${lesson.passScorePercent}% PASSED",
                        containerColor = tactile.emeraldSoft,
                        contentColor = tactile.onEmeraldSoft,
                        borderColor = Color.Transparent,
                    )
                }
            }
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkAdd,
                    contentDescription = null,
                    tint = tactile.amberFace,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = stringResource(R.string.lesson_highlight_tap_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // 2. Plain-English Word Breakdown Card (before code)
        WordBreakdownSectionCard(
            lessonId = lesson.id,
            breakdowns = content.wordBreakdowns,
            savedById = savedById,
            activeBlockId = activeHighlightBlockId,
            selectedTag = selectedTag,
            noteDraft = noteDraft,
            onToggleBlock = ::toggleBlockSelection,
            onSelectTag = { tag ->
                feedback.tap(view)
                selectedTag = tag
            },
            onNoteDraftChange = { noteDraft = it },
            onSave = { blockId, snippet ->
                feedback.gotItChime(view)
                onSaveHighlight(blockId, snippet, selectedTag, noteDraft)
                activeHighlightBlockId = null
                noteDraft = ""
            },
            onSendGotchaToAi = { blockId, snippet ->
                feedback.gotItChime(view)
                onAskAiHighlight(blockId, snippet, noteDraft)
                activeHighlightBlockId = null
                noteDraft = ""
            },
        )

        // 3. Core Concept & Syntax Rules Cards
        SectionHeadingRow(
            icon = Icons.Outlined.Code,
            iconTint = tactile.emeraldFace,
            title = stringResource(R.string.lesson_section_concept_rules),
            badgeText = "${content.conceptRules.size} RULES",
        )

        content.conceptRules.forEach { rule ->
            val existingNote = savedById["hl_${lesson.id}_${rule.id}"]
            ConceptRuleCard(
                rule = rule,
                existingHighlight = existingNote,
                isTagBarExpanded = activeHighlightBlockId == rule.id,
                selectedTag = selectedTag,
                noteDraft = noteDraft,
                onTapBlock = { toggleBlockSelection(rule.id) },
                onSelectTag = { tag ->
                    feedback.tap(view)
                    selectedTag = tag
                },
                onNoteDraftChange = { noteDraft = it },
                onSave = {
                    feedback.gotItChime(view)
                    onSaveHighlight(
                        rule.id,
                        "${rule.title}: ${rule.explanation}",
                        selectedTag,
                        noteDraft,
                    )
                    activeHighlightBlockId = null
                    noteDraft = ""
                },
                onSendGotchaToAi = {
                    feedback.gotItChime(view)
                    onAskAiHighlight(
                        rule.id,
                        "${rule.title}: ${rule.explanation}",
                        noteDraft,
                    )
                    activeHighlightBlockId = null
                    noteDraft = ""
                },
            )
        }

        // 4. Desired Output & Collapsible "Show Solution" Card
        PracticeDesiredOutputCard(
            practiceTask = content.practiceTask,
            isSolutionVisible = isSolutionVisible,
            onToggleSolution = {
                feedback.tap(view)
                isSolutionVisible = !isSolutionVisible
            },
        )

        // 5. Real Integrated 5-Question Lesson Test Card
        IntegratedLessonTestCard(
            lesson = lesson,
            questionCount = content.testQuestions.size,
            onStartTest = {
                feedback.pedestalPress(view)
                onStartIntegratedTest()
            },
        )
    }
}

@Composable
private fun SectionHeadingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    badgeText: String,
) {
    val tactile = LocalKovaTactileColors.current
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
                tint = iconTint,
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
            text = badgeText,
            containerColor = tactile.cardFace,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            borderColor = tactile.cardOutline,
        )
    }
}

@Composable
private fun WordBreakdownSectionCard(
    lessonId: String,
    breakdowns: List<WordBreakdown>,
    savedById: Map<String, HighlightNote>,
    activeBlockId: String?,
    selectedTag: HighlightCategory,
    noteDraft: String,
    onToggleBlock: (String) -> Unit,
    onSelectTag: (HighlightCategory) -> Unit,
    onNoteDraftChange: (String) -> Unit,
    onSave: (String, String) -> Unit,
    onSendGotchaToAi: (String, String) -> Unit,
) {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.amberLip,
        borderColor = tactile.amberFace.copy(alpha = 0.6f),
        lipHeight = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_word_breakdown_card"),
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = null,
                    tint = tactile.amberFace,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.lesson_section_word_breakdown),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.lesson_word_breakdown_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            breakdowns.forEach { item ->
                val existing = savedById["hl_${lessonId}_${item.id}"]
                val highlightTint = existing?.category?.softSurfaceColor()
                val highlightBorder = existing?.category?.accentColor() ?: tactile.cardOutline

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = highlightTint ?: MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(if (existing != null) 1.5.dp else 1.dp, highlightBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onToggleBlock(item.id) }
                        .testTag("highlightable_word_${item.id}"),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            KovaPillBadge(
                                text = item.term,
                                containerColor = tactile.cobaltSoft,
                                contentColor = tactile.onCobaltSoft,
                                borderColor = Color.Transparent,
                                useMonospace = false,
                            )
                            HighlightStatusPill(existing = existing)
                        }

                        Text(
                            text = "Literal meaning: ${item.literalMeaning}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = tactile.amberFace,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(15.dp),
                            )
                            Text(
                                text = item.tenYearOldAnalogy,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        InlineHighlightTagBar(
                            visible = activeBlockId == item.id,
                            selectedTag = selectedTag,
                            noteDraft = noteDraft,
                            onSelectTag = onSelectTag,
                            onNoteDraftChange = onNoteDraftChange,
                            onSave = { onSave(item.id, item.highlightSnippet) },
                            onSendGotchaToAi = { onSendGotchaToAi(item.id, item.highlightSnippet) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConceptRuleCard(
    rule: ConceptRule,
    existingHighlight: HighlightNote?,
    isTagBarExpanded: Boolean,
    selectedTag: HighlightCategory,
    noteDraft: String,
    onTapBlock: () -> Unit,
    onSelectTag: (HighlightCategory) -> Unit,
    onNoteDraftChange: (String) -> Unit,
    onSave: () -> Unit,
    onSendGotchaToAi: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val borderColor = existingHighlight?.category?.accentColor() ?: tactile.cardOutline
    val cardFace = existingHighlight?.category?.softSurfaceColor() ?: tactile.cardFace

    KovaTactileCard(
        shape = KovaExpressiveShapes.StatAndAssignmentCard,
        faceColor = cardFace,
        lipColor = tactile.cardLip,
        borderColor = borderColor,
        lipHeight = 4.dp,
        onClick = onTapBlock,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("highlightable_rule_${rule.id}"),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = rule.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                HighlightStatusPill(existing = existingHighlight)
            }

            Text(
                text = rule.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, tactile.cardOutline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = rule.codeSnippet,
                    style = KovaCodeTextStyle,
                    color = tactile.cobaltFace,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                )
            }

            Text(
                text = "Why: ${rule.whyItMatters}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            InlineHighlightTagBar(
                visible = isTagBarExpanded,
                selectedTag = selectedTag,
                noteDraft = noteDraft,
                onSelectTag = onSelectTag,
                onNoteDraftChange = onNoteDraftChange,
                onSave = onSave,
                onSendGotchaToAi = onSendGotchaToAi,
            )
        }
    }
}

@Composable
private fun HighlightStatusPill(existing: HighlightNote?) {
    val tactile = LocalKovaTactileColors.current
    if (existing != null) {
        val accent = existing.category.accentColor()
        KovaPillBadge(
            text = existing.category.displayLabel().uppercase(),
            leadingIcon = Icons.Outlined.BookmarkAdded,
            containerColor = accent.copy(alpha = 0.16f),
            contentColor = accent,
            borderColor = accent.copy(alpha = 0.5f),
        )
    } else {
        Icon(
            imageVector = Icons.Outlined.BookmarkAdd,
            contentDescription = stringResource(R.string.cd_highlight_block),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InlineHighlightTagBar(
    visible: Boolean,
    selectedTag: HighlightCategory,
    noteDraft: String,
    onSelectTag: (HighlightCategory) -> Unit,
    onNoteDraftChange: (String) -> Unit,
    onSave: () -> Unit,
    onSendGotchaToAi: () -> Unit,
) {
    val motion = LocalKovaMotion.current
    val tactile = LocalKovaTactileColors.current

    AnimatedVisibility(
        visible = visible,
        enter = if (motion.enabled) {
            expandVertically(spring(dampingRatio = 0.76f, stiffness = 360f)) + fadeIn()
        } else {
            fadeIn(snap())
        },
        exit = if (motion.enabled) {
            shrinkVertically(spring(dampingRatio = 0.82f, stiffness = 380f)) + fadeOut()
        } else {
            fadeOut(snap())
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .testTag("inline_highlight_tag_bar"),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            HorizontalDivider(thickness = 1.dp, color = tactile.cardOutline.copy(alpha = 0.7f))

            Text(
                text = "TAG & SYNC TO REVISION BINDER",
                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val tagOptions = listOf(
                    Triple(HighlightCategory.KEY_IDEA, "Key Idea", tactile.amberFace),
                    Triple(HighlightCategory.CODE_RULE, "Code Rule", tactile.emeraldFace),
                    Triple(HighlightCategory.GOTCHA, "Gotcha / Ask AI", tactile.coralFace),
                )
                tagOptions.forEach { (category, label, accent) ->
                    val isPicked = selectedTag == category
                    val onSelected = when (category) {
                        HighlightCategory.KEY_IDEA -> tactile.onAmberFace
                        HighlightCategory.CODE_RULE -> tactile.onEmeraldFace
                        HighlightCategory.GOTCHA -> tactile.onCoralFace
                    }
                    Surface(
                        onClick = { onSelectTag(category) },
                        shape = KovaExpressiveShapes.Pill,
                        color = if (isPicked) accent else tactile.cardFace,
                        border = BorderStroke(1.25.dp, if (isPicked) accent else tactile.cardOutline),
                        modifier = Modifier.testTag("highlight_tag_${category.name.lowercase()}"),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isPicked) onSelected else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            OutlinedTextField(
                value = noteDraft,
                onValueChange = onNoteDraftChange,
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(R.string.lesson_highlight_note_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                shape = RoundedCornerShape(14.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = selectedTag.accentColor(),
                    unfocusedBorderColor = tactile.cardOutline,
                    focusedContainerColor = tactile.cardFace,
                    unfocusedContainerColor = tactile.cardFace,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("highlight_note_input"),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val saveFace = when (selectedTag) {
                    HighlightCategory.KEY_IDEA -> tactile.amberFace
                    HighlightCategory.CODE_RULE -> tactile.emeraldFace
                    HighlightCategory.GOTCHA -> tactile.coralFace
                }
                val saveLip = when (selectedTag) {
                    HighlightCategory.KEY_IDEA -> tactile.amberLip
                    HighlightCategory.CODE_RULE -> tactile.emeraldLip
                    HighlightCategory.GOTCHA -> tactile.coralLip
                }
                val saveContent = when (selectedTag) {
                    HighlightCategory.KEY_IDEA -> tactile.onAmberFace
                    HighlightCategory.CODE_RULE -> tactile.onEmeraldFace
                    HighlightCategory.GOTCHA -> tactile.onCoralFace
                }

                KovaTactileButton(
                    onClick = onSave,
                    faceColor = saveFace,
                    lipColor = saveLip,
                    contentColor = saveContent,
                    minHeight = 44.dp,
                    lipHeight = 4.dp,
                    playTapSound = false,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_highlight_button"),
                ) {
                    Text(
                        text = stringResource(R.string.lesson_highlight_save_cta),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = saveContent,
                        maxLines = 1,
                        softWrap = false,
                    )
                }

                if (selectedTag == HighlightCategory.GOTCHA) {
                    KovaTactileButton(
                        onClick = onSendGotchaToAi,
                        faceColor = tactile.cobaltFace,
                        lipColor = tactile.cobaltLip,
                        contentColor = tactile.onCobaltFace,
                        minHeight = 44.dp,
                        lipHeight = 4.dp,
                        playTapSound = false,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("send_gotcha_to_ai_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = tactile.onCobaltFace,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.lesson_highlight_send_ai_cta),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = tactile.onCobaltFace,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeDesiredOutputCard(
    practiceTask: PracticeTaskSpec,
    isSolutionVisible: Boolean,
    onToggleSolution: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val motion = LocalKovaMotion.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.cobaltLip,
        borderColor = tactile.cobaltFace.copy(alpha = 0.55f),
        lipHeight = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("practice_desired_output_card"),
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Terminal,
                    contentDescription = null,
                    tint = tactile.cobaltFace,
                    modifier = Modifier.size(19.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Practice Goal · ${practiceTask.title}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = practiceTask.prompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Desired Output Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.25.dp, tactile.emeraldFace.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "DESIRED OUTPUT",
                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                        color = tactile.emeraldFace,
                    )
                    Text(
                        text = practiceTask.desiredOutput,
                        style = KovaCodeTextStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // Hidden-by-default "Show Solution" toggle
            KovaTactileButton(
                onClick = onToggleSolution,
                faceColor = if (isSolutionVisible) tactile.cardElevatedFace else tactile.cobaltSoft,
                lipColor = if (isSolutionVisible) tactile.cardLip else tactile.cobaltFace.copy(alpha = 0.45f),
                contentColor = if (isSolutionVisible) MaterialTheme.colorScheme.onSurface else tactile.onCobaltSoft,
                minHeight = 46.dp,
                lipHeight = 4.dp,
                playTapSound = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_show_solution_button"),
            ) {
                Icon(
                    imageVector = if (isSolutionVisible) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = if (isSolutionVisible) MaterialTheme.colorScheme.onSurface else tactile.onCobaltSoft,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSolutionVisible) {
                        stringResource(R.string.lesson_hide_solution_cta)
                    } else {
                        stringResource(R.string.lesson_show_solution_cta)
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isSolutionVisible) MaterialTheme.colorScheme.onSurface else tactile.onCobaltSoft,
                )
            }

            AnimatedVisibility(
                visible = isSolutionVisible,
                enter = if (motion.enabled) {
                    expandVertically(spring(dampingRatio = 0.76f, stiffness = 340f)) + fadeIn()
                } else {
                    fadeIn(snap())
                },
                exit = if (motion.enabled) {
                    shrinkVertically(spring(dampingRatio = 0.82f, stiffness = 380f)) + fadeOut()
                } else {
                    fadeOut(snap())
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("solution_revealed_panel"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, tactile.cobaltFace.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                text = "CLEAN KOTLIN SOLUTION",
                                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                color = tactile.cobaltFace,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            practiceTask.solutionCode.lines().forEachIndexed { idx, codeLine ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Text(
                                        text = "${idx + 1}",
                                        style = KovaCodeTextStyle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.width(16.dp),
                                    )
                                    Text(
                                        text = codeLine,
                                        style = KovaCodeTextStyle,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "LINE-BY-LINE NOTES",
                            style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        practiceTask.lineNotes.forEach { note ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(tactile.cobaltSoft),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "L${note.lineNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = tactile.onCobaltSoft,
                                    )
                                }
                                Text(
                                    text = note.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntegratedLessonTestCard(
    lesson: Lesson,
    questionCount: Int,
    onStartTest: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val isCompleted = lesson.status == LessonStatus.COMPLETED
    val score = lesson.passScorePercent

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.cardFace,
        lipColor = if (isCompleted) tactile.emeraldLip else tactile.coralLip,
        borderColor = if (isCompleted) tactile.emeraldFace else tactile.coralFace,
        lipHeight = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("integrated_lesson_test_card"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                KovaScallopedSealBadge(
                    icon = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Quiz,
                    contentDescription = null,
                    containerColor = if (isCompleted) tactile.emeraldFace else tactile.coralFace,
                    lipColor = if (isCompleted) tactile.emeraldLip else tactile.coralLip,
                    contentColor = if (isCompleted) tactile.onEmeraldFace else tactile.onCoralFace,
                    size = 44.dp,
                    iconSize = 21.dp,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.lesson_test_section_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "$questionCount closed-book questions · Pass >= 80% (4/$questionCount) to unlock the next node.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (isCompleted) {
                Surface(
                    shape = KovaExpressiveShapes.Pill,
                    color = tactile.emeraldSoft,
                    border = BorderStroke(1.25.dp, tactile.emeraldFace),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lesson_passed_banner"),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = tactile.emeraldFace,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MASTERED · ${score ?: 80}% PASSED",
                            style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                            color = tactile.onEmeraldSoft,
                        )
                    }
                }
            }

            KovaTactileButton(
                onClick = onStartTest,
                faceColor = if (isCompleted) tactile.cobaltFace else tactile.emeraldFace,
                lipColor = if (isCompleted) tactile.cobaltLip else tactile.emeraldLip,
                contentColor = if (isCompleted) tactile.onCobaltFace else tactile.onEmeraldFace,
                minHeight = 50.dp,
                lipHeight = 5.dp,
                playTapSound = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_lesson_test_button"),
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = if (isCompleted) tactile.onCobaltFace else tactile.onEmeraldFace,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCompleted) {
                        stringResource(R.string.lesson_test_retake_cta)
                    } else {
                        stringResource(R.string.lesson_test_start_cta)
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isCompleted) tactile.onCobaltFace else tactile.onEmeraldFace,
                )
            }
        }
    }
}

@Composable
private fun HighlightCategory.softSurfaceColor(): Color {
    val tactile = LocalKovaTactileColors.current
    return when (this) {
        HighlightCategory.KEY_IDEA -> tactile.amberSoft.copy(alpha = 0.55f)
        HighlightCategory.CODE_RULE -> tactile.emeraldSoft.copy(alpha = 0.55f)
        HighlightCategory.GOTCHA -> tactile.coralSoft.copy(alpha = 0.55f)
    }
}
