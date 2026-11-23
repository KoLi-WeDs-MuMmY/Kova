package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.BookmarkAdded
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.example.model.PreHighlightedTakeaway
import com.example.model.WordBreakdown
import com.example.ui.components.KovaFramedCodeBlock
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaScallopedSealBadge
import com.example.ui.components.KovaTactileButton
import com.example.ui.components.KovaTactileCard
import com.example.ui.feedback.LocalKovaFeedback
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.KovaMonoBadgeTextStyle
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Phase 2.2 Structured Lesson Reader Pane & Dedicated Focused Topic Pages:
 * 1. Lesson Header & Progress (zero top highlight banner clutter)
 * 2. Numbered Topics List (1.1 [Topic Name], 1.2 [Topic Name], etc.) -> tapping any topic
 *    collapses the lesson and opens its Dedicated Focused Topic Page (analogy -> Kotlin
 *    connection -> framed dark monospace code block -> plain-English symbol breakdown)
 * 3. Practice Task & Desired Output (framed code block, hands-on code-writing check +
 *    "Open IDE to Practice" button, and collapsed-by-default "Show Solution & Line Notes")
 * 4. Separator Bar -> End-of-Lesson Detailed Revision (Word Breakdowns, Topic Recap,
 *    and filterable Pre-Highlighted Takeaways + saved Highlights moved here exclusively)
 * 5. Integrated 5-Question Closed-Book Lesson Test launcher
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonReaderPane(
    lesson: Lesson,
    content: LessonInteractiveContent,
    savedHighlights: List<HighlightNote>,
    dailyRemaining: Int,
    savedScrollPosition: Int = 0,
    activeExplainRuleId: String? = null,
    onScrollPositionChanged: (Int) -> Unit = {},
    onActiveExplainRuleChanged: (String?) -> Unit = {},
    onOpenIdeForPractice: () -> Unit = {},
    onSaveHighlight: (blockId: String, snippet: String, category: HighlightCategory, learnerNote: String) -> Unit,
    onAskAiHighlight: (blockId: String, snippet: String, learnerNote: String) -> Unit,
    onStartIntegratedTest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val motion = LocalKovaMotion.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current
    val isCompleted = lesson.status == LessonStatus.COMPLETED
    val chapterPrefix = remember(lesson.code) {
        lesson.code.substringBefore('.').ifBlank { "1" }
    }

    var activeHighlightBlockId by rememberSaveable(lesson.id) { mutableStateOf<String?>(null) }
    var selectedTag by rememberSaveable(lesson.id) { mutableStateOf(HighlightCategory.KEY_IDEA) }
    var noteDraft by rememberSaveable(lesson.id) { mutableStateOf("") }
    var isSolutionVisible by rememberSaveable(lesson.id) { mutableStateOf(false) }
    var revisionHighlightFilter by rememberSaveable(lesson.id) { mutableStateOf<HighlightCategory?>(null) }

    val readerScrollState = rememberScrollState(initial = savedScrollPosition)

    LaunchedEffect(lesson.id) {
        if (savedScrollPosition > 0 && readerScrollState.value != savedScrollPosition) {
            readerScrollState.scrollTo(savedScrollPosition)
        }
    }

    LaunchedEffect(lesson.id, readerScrollState) {
        snapshotFlow { readerScrollState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it }
            .collect {
                onScrollPositionChanged(readerScrollState.value)
            }
    }

    DisposableEffect(lesson.id) {
        onDispose {
            onScrollPositionChanged(readerScrollState.value)
        }
    }

    val savedById = remember(savedHighlights) {
        savedHighlights.associateBy { it.id }
    }
    val lessonSavedHighlights = remember(savedHighlights, lesson.code) {
        savedHighlights.filter { it.lessonCode == lesson.code }
    }
    val activeTopicEntry = remember(activeExplainRuleId, content.conceptRules) {
        activeExplainRuleId?.let { id ->
            val index = content.conceptRules.indexOfFirst { it.id == id }
            if (index >= 0) index to content.conceptRules[index] else null
        }
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

    // Intercept system Back while inside a Focused Topic Page
    BackHandler(enabled = activeTopicEntry != null) {
        feedback.tap(view)
        onActiveExplainRuleChanged(null)
    }

    AnimatedContent(
        targetState = activeTopicEntry,
        transitionSpec = {
            if (motion.enabled) {
                (fadeIn(tween(220)) + scaleIn(
                    initialScale = 0.97f,
                    animationSpec = spring(dampingRatio = 0.78f, stiffness = 340f),
                )).togetherWith(fadeOut(tween(160)))
            } else {
                fadeIn(snap()) togetherWith fadeOut(snap())
            }
        },
        modifier = modifier.testTag("lesson_reader_pane"),
        label = "lesson_reader_topic_focus_transition",
    ) { topicPair ->
        if (topicPair != null) {
            val (topicIndex, topicRule) = topicPair
            val topicNumber = "$chapterPrefix.${topicIndex + 1}"
            val existingNote = savedById["hl_${lesson.id}_${topicRule.id}"]
            FocusedTopicPage(
                lessonCode = lesson.code,
                topicNumber = topicNumber,
                rule = topicRule,
                existingHighlight = existingNote,
                isTagBarExpanded = activeHighlightBlockId == topicRule.id,
                selectedTag = selectedTag,
                noteDraft = noteDraft,
                onToggleHighlightBar = { toggleBlockSelection(topicRule.id) },
                onSelectTag = { tag ->
                    feedback.tap(view)
                    selectedTag = tag
                },
                onNoteDraftChange = { noteDraft = it },
                onSaveHighlight = {
                    feedback.gotItChime(view)
                    onSaveHighlight(
                        topicRule.id,
                        "${topicRule.title}: ${topicRule.explanation}",
                        selectedTag,
                        noteDraft,
                    )
                    activeHighlightBlockId = null
                    noteDraft = ""
                },
                onSendGotchaToAi = {
                    feedback.gotItChime(view)
                    onAskAiHighlight(
                        topicRule.id,
                        "${topicRule.title}: ${topicRule.explanation}",
                        noteDraft,
                    )
                    activeHighlightBlockId = null
                    noteDraft = ""
                },
                onBackToLesson = {
                    feedback.tap(view)
                    activeHighlightBlockId = null
                    onActiveExplainRuleChanged(null)
                },
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(readerScrollState)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // 1. Clean Lesson Header & Progress (zero top highlight banner dump)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    Text(
                        text = lesson.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // 2. Numbered Topics List (1.1 [Topic Name], 1.2 [Topic Name], ...)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lesson_numbered_topics_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionHeadingRow(
                        icon = Icons.Outlined.Code,
                        iconTint = tactile.cobaltFace,
                        title = stringResource(R.string.lesson_section_topics),
                        badgeText = "${content.conceptRules.size} TOPICS",
                    )

                    content.conceptRules.forEachIndexed { idx, rule ->
                        val topicNumber = "$chapterPrefix.${idx + 1}"
                        val existingNote = savedById["hl_${lesson.id}_${rule.id}"]
                        NumberedTopicCard(
                            topicNumber = topicNumber,
                            rule = rule,
                            existingHighlight = existingNote,
                            onOpenTopic = {
                                feedback.chapterSwoosh(view)
                                onScrollPositionChanged(readerScrollState.value)
                                activeHighlightBlockId = null
                                onActiveExplainRuleChanged(rule.id)
                            },
                        )
                    }
                }

                // 3. Practice Task & Desired Output (with framed code, hands-on IDE check, collapsible Solution)
                PracticeDesiredOutputCard(
                    lessonId = lesson.id,
                    practiceTask = content.practiceTask,
                    isSolutionVisible = isSolutionVisible,
                    onOpenIdeForPractice = {
                        feedback.chapterSwoosh(view)
                        onOpenIdeForPractice()
                    },
                    onToggleSolution = {
                        feedback.tap(view)
                        isSolutionVisible = !isSolutionVisible
                    },
                )

                // 4. Separator Bar -> End-of-Lesson Detailed Revision (on the same lesson page)
                LessonRevisionSeparatorBar()

                EndOfLessonRevisionSection(
                    lessonId = lesson.id,
                    chapterPrefix = chapterPrefix,
                    content = content,
                    savedById = savedById,
                    lessonSavedHighlights = lessonSavedHighlights,
                    activeBlockId = activeHighlightBlockId,
                    selectedTag = selectedTag,
                    noteDraft = noteDraft,
                    selectedHighlightFilter = revisionHighlightFilter,
                    onSelectHighlightFilter = { filter ->
                        feedback.tap(view)
                        revisionHighlightFilter = filter
                    },
                    onToggleBlock = ::toggleBlockSelection,
                    onSelectTag = { tag ->
                        feedback.tap(view)
                        selectedTag = tag
                    },
                    onNoteDraftChange = { noteDraft = it },
                    onSaveHighlight = { blockId, snippet ->
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
                    onOpenTopicFromRevision = { ruleId ->
                        feedback.chapterSwoosh(view)
                        onScrollPositionChanged(readerScrollState.value)
                        activeHighlightBlockId = null
                        onActiveExplainRuleChanged(ruleId)
                    },
                )

                // 5. Integrated 5-Question Closed-Book Lesson Test Card
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
    }
}

@Composable
private fun NumberedTopicCard(
    topicNumber: String,
    rule: ConceptRule,
    existingHighlight: HighlightNote?,
    onOpenTopic: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val borderColor = existingHighlight?.category?.accentColor() ?: tactile.cardOutline

    KovaTactileCard(
        shape = KovaExpressiveShapes.StatAndAssignmentCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.cardLip,
        borderColor = borderColor,
        lipHeight = 4.dp,
        onClick = onOpenTopic,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_topic_card_${rule.id}"),
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = tactile.cobaltSoft,
                    border = BorderStroke(1.dp, tactile.cobaltFace.copy(alpha = 0.45f)),
                ) {
                    Text(
                        text = topicNumber,
                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                        color = tactile.onCobaltSoft,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    )
                }

                Text(
                    text = rule.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (existingHighlight != null) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkAdded,
                        contentDescription = "Highlighted",
                        tint = existingHighlight.category.accentColor(),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Text(
                text = rule.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = rule.whyItMatters,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = KovaExpressiveShapes.Pill,
                    color = tactile.cobaltSoft,
                    border = BorderStroke(1.dp, tactile.cobaltFace.copy(alpha = 0.45f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.lesson_open_topic_cta),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = tactile.onCobaltSoft,
                            maxLines = 1,
                            softWrap = false,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = tactile.onCobaltSoft,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Focused Topic Page opened when tapping any numbered topic (e.g. 1.1, 1.2, 1.3).
 * Hides the rest of the lesson and walks conversationally through:
 * 1) Everyday Real-Life Analogy
 * 2) Connection to Kotlin
 * 3) Dark-Framed Monospace Code Block
 * 4) Plain-English Breakdown of Every Symbol & Keyword
 * 5) One-Line Takeaway + unobtrusive Save Highlight / Ask AI trigger
 * 6) Clear "<- Back to Lesson" control
 */
@Composable
private fun FocusedTopicPage(
    lessonCode: String,
    topicNumber: String,
    rule: ConceptRule,
    existingHighlight: HighlightNote?,
    isTagBarExpanded: Boolean,
    selectedTag: HighlightCategory,
    noteDraft: String,
    onToggleHighlightBar: () -> Unit,
    onSelectTag: (HighlightCategory) -> Unit,
    onNoteDraftChange: (String) -> Unit,
    onSaveHighlight: () -> Unit,
    onSendGotchaToAi: () -> Unit,
    onBackToLesson: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val deepDive = rule.explainDeepDive

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("lesson_explain_focus_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Top Back-to-Lesson bar + Topic Number pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(
                onClick = onBackToLesson,
                shape = KovaExpressiveShapes.Pill,
                color = tactile.cardFace,
                border = BorderStroke(1.25.dp, tactile.cobaltFace.copy(alpha = 0.6f)),
                modifier = Modifier
                    .height(44.dp)
                    .testTag("explain_back_to_lesson_button"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = tactile.cobaltFace,
                        modifier = Modifier.size(17.dp),
                    )
                    Text(
                        text = "Back to Lesson",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }

            KovaPillBadge(
                text = "L$lessonCode · TOPIC $topicNumber",
                containerColor = tactile.cobaltSoft,
                contentColor = tactile.onCobaltSoft,
                borderColor = Color.Transparent,
            )
        }

        // Topic Headline (Space Grotesk)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.lesson_explain_badge),
                style = KovaMonoBadgeTextStyle,
                color = tactile.cobaltFace,
            )
            Text(
                text = "$topicNumber ${deepDive.headline}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Step 1: Real-life Everyday Analogy
        KovaTactileCard(
            shape = KovaExpressiveShapes.HeroChapterCard,
            faceColor = tactile.amberSoft.copy(alpha = 0.55f),
            lipColor = tactile.amberLip,
            borderColor = tactile.amberFace.copy(alpha = 0.65f),
            lipHeight = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = tactile.amberFace,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "1. Everyday Real-Life Analogy",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = deepDive.realLifeOpening,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Step 2: Connection to Kotlin
        KovaTactileCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.cardLip,
            borderColor = tactile.cardOutline,
            lipHeight = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "2. How Kotlin Connects",
                    style = MaterialTheme.typography.titleMedium,
                    color = tactile.cobaltFace,
                )
                Text(
                    text = deepDive.kotlinConnection,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Step 3: Distinct Dark-Framed Monospace Kotlin Code Block
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "3. KOTLIN CODE EXAMPLE",
                style = KovaMonoBadgeTextStyle,
                color = tactile.cobaltFace,
            )
            KovaFramedCodeBlock(
                code = deepDive.syntaxSnippet,
                languageLabel = "KOTLIN · TOPIC $topicNumber",
                accentColor = tactile.emeraldFace,
                showLineNumbers = true,
                modifier = Modifier.testTag("topic_framed_code_block"),
            )
        }

        // Step 4: Plain-English Breakdown of Every Keyword & Symbol
        KovaTactileCard(
            shape = KovaExpressiveShapes.HeroChapterCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.emeraldLip,
            borderColor = tactile.emeraldFace.copy(alpha = 0.55f),
            lipHeight = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                Text(
                    text = "4. Every Keyword & Symbol in Plain English",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                deepDive.symbolBreakdowns.forEach { piece ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = tactile.codeWorkbenchWell,
                            border = BorderStroke(1.dp, tactile.codeWorkbenchBorder),
                        ) {
                            Text(
                                text = piece.symbol,
                                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                color = tactile.emeraldFace,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            )
                        }
                        Text(
                            text = piece.plainWords,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // Step 5: One-Line Takeaway + Unobtrusive Highlight / Ask AI Action
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = tactile.emeraldSoft,
            border = BorderStroke(1.25.dp, tactile.emeraldFace.copy(alpha = 0.65f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "ONE-LINE TAKEAWAY",
                        style = KovaMonoBadgeTextStyle,
                        color = tactile.onEmeraldSoft,
                    )
                    Surface(
                        onClick = onToggleHighlightBar,
                        shape = KovaExpressiveShapes.Pill,
                        color = tactile.cardFace,
                        border = BorderStroke(1.dp, tactile.emeraldFace.copy(alpha = 0.55f)),
                        modifier = Modifier.testTag("topic_highlight_toggle_button"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = if (existingHighlight != null) Icons.Outlined.BookmarkAdded else Icons.Outlined.BookmarkAdd,
                                contentDescription = stringResource(R.string.cd_highlight_block),
                                tint = existingHighlight?.category?.accentColor() ?: tactile.cobaltFace,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = if (existingHighlight != null) "Saved" else "Highlight / Ask AI",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                Text(
                    text = deepDive.takeaway,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = tactile.onEmeraldSoft,
                )
                InlineHighlightTagBar(
                    visible = isTagBarExpanded,
                    selectedTag = selectedTag,
                    noteDraft = noteDraft,
                    onSelectTag = onSelectTag,
                    onNoteDraftChange = onNoteDraftChange,
                    onSave = onSaveHighlight,
                    onSendGotchaToAi = onSendGotchaToAi,
                )
            }
        }

        // Bottom Back-to-Lesson Button
        KovaTactileButton(
            onClick = onBackToLesson,
            faceColor = tactile.cobaltFace,
            lipColor = tactile.cobaltLip,
            contentColor = tactile.onCobaltFace,
            minHeight = 48.dp,
            lipHeight = 4.dp,
            playTapSound = false,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explain_bottom_back_button"),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                tint = tactile.onCobaltFace,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.lesson_back_to_lesson_cta),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = tactile.onCobaltFace,
            )
        }
    }
}

@Composable
private fun LessonRevisionSeparatorBar() {
    val tactile = LocalKovaTactileColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("lesson_revision_separator_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.5.dp,
            color = tactile.cardOutline,
        )
        KovaPillBadge(
            text = "END-OF-LESSON REVISION",
            leadingIcon = Icons.Outlined.AutoStories,
            containerColor = tactile.amberSoft,
            contentColor = tactile.onAmberSoft,
            borderColor = tactile.amberFace.copy(alpha = 0.5f),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.5.dp,
            color = tactile.cardOutline,
        )
    }
}

@Composable
private fun EndOfLessonRevisionSection(
    lessonId: String,
    chapterPrefix: String,
    content: LessonInteractiveContent,
    savedById: Map<String, HighlightNote>,
    lessonSavedHighlights: List<HighlightNote>,
    activeBlockId: String?,
    selectedTag: HighlightCategory,
    noteDraft: String,
    selectedHighlightFilter: HighlightCategory?,
    onSelectHighlightFilter: (HighlightCategory?) -> Unit,
    onToggleBlock: (String) -> Unit,
    onSelectTag: (HighlightCategory) -> Unit,
    onNoteDraftChange: (String) -> Unit,
    onSaveHighlight: (String, String) -> Unit,
    onSendGotchaToAi: (String, String) -> Unit,
    onOpenTopicFromRevision: (String) -> Unit,
) {
    val tactile = LocalKovaTactileColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_end_revision_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.lesson_section_end_revision),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.lesson_end_revision_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // A. Topic-by-Topic Recap
        KovaTactileCard(
            shape = KovaExpressiveShapes.HeroChapterCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.cardLip,
            borderColor = tactile.cardOutline,
            lipHeight = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "TOPIC-BY-TOPIC RECAP",
                    style = KovaMonoBadgeTextStyle,
                    color = tactile.cobaltFace,
                )
                content.conceptRules.forEachIndexed { idx, rule ->
                    val topicNumber = "$chapterPrefix.${idx + 1}"
                    Surface(
                        onClick = { onOpenTopicFromRevision(rule.id) },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, tactile.cardOutline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                KovaPillBadge(
                                    text = topicNumber,
                                    containerColor = tactile.cobaltSoft,
                                    contentColor = tactile.onCobaltSoft,
                                    borderColor = Color.Transparent,
                                )
                                Text(
                                    text = rule.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Text(
                                text = rule.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            KovaFramedCodeBlock(
                                code = rule.codeSnippet,
                                languageLabel = "TOPIC $topicNumber SNIPPET",
                                accentColor = tactile.cobaltFace,
                            )
                        }
                    }
                }
            }
        }

        // B. Plain-English Key Words Recap (tap-to-highlight preserved cleanly)
        if (content.wordBreakdowns.isNotEmpty()) {
            WordBreakdownSectionCard(
                lessonId = lessonId,
                breakdowns = content.wordBreakdowns,
                savedById = savedById,
                activeBlockId = activeBlockId,
                selectedTag = selectedTag,
                noteDraft = noteDraft,
                onToggleBlock = onToggleBlock,
                onSelectTag = onSelectTag,
                onNoteDraftChange = onNoteDraftChange,
                onSave = onSaveHighlight,
                onSendGotchaToAi = onSendGotchaToAi,
            )
        }

        // C. Pre-Highlighted Key Takeaways (moved exclusively into End-of-Lesson Revision with clean filters)
        if (content.preHighlightedTakeaways.isNotEmpty() || lessonSavedHighlights.isNotEmpty()) {
            RevisionHighlightsDeck(
                takeaways = content.preHighlightedTakeaways,
                savedHighlights = lessonSavedHighlights,
                selectedFilter = selectedHighlightFilter,
                onSelectFilter = onSelectHighlightFilter,
            )
        }
    }
}

@Composable
private fun RevisionHighlightsDeck(
    takeaways: List<PreHighlightedTakeaway>,
    savedHighlights: List<HighlightNote>,
    selectedFilter: HighlightCategory?,
    onSelectFilter: (HighlightCategory?) -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val filteredTakeaways = remember(takeaways, selectedFilter) {
        if (selectedFilter == null) takeaways else takeaways.filter { it.category == selectedFilter }
    }
    val filteredUserNotes = remember(savedHighlights, selectedFilter) {
        if (selectedFilter == null) savedHighlights else savedHighlights.filter { it.category == selectedFilter }
    }

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.amberLip,
        borderColor = tactile.amberFace.copy(alpha = 0.55f),
        lipHeight = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_prehighlighted_takeaways"),
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkAdded,
                        contentDescription = null,
                        tint = tactile.amberFace,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.lesson_section_key_takeaways),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                KovaPillBadge(
                    text = "${takeaways.size + savedHighlights.size} NOTES",
                    containerColor = tactile.amberSoft,
                    contentColor = tactile.onAmberSoft,
                    borderColor = Color.Transparent,
                )
            }

            // Compact filter chips: All | Key Idea | Code Rule | Gotcha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                val filters = listOf<Pair<HighlightCategory?, String>>(
                    null to stringResource(R.string.filter_all),
                    HighlightCategory.KEY_IDEA to stringResource(R.string.filter_key_idea),
                    HighlightCategory.CODE_RULE to stringResource(R.string.filter_code_rule),
                    HighlightCategory.GOTCHA to stringResource(R.string.filter_gotcha),
                )
                filters.forEach { (cat, label) ->
                    val isSelected = selectedFilter == cat
                    val accent = cat?.accentColor() ?: tactile.cobaltFace
                    Surface(
                        onClick = { onSelectFilter(cat) },
                        shape = KovaExpressiveShapes.Pill,
                        color = if (isSelected) accent.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, if (isSelected) accent else tactile.cardOutline),
                        modifier = Modifier.testTag("revision_highlight_filter_${cat?.name?.lowercase() ?: "all"}"),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            filteredTakeaways.forEach { item ->
                val accent = item.category.accentColor()
                val softBg = item.category.softSurfaceColor()
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = softBg,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pre_highlight_${item.id}"),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .width(4.dp)
                                .height(34.dp)
                                .clip(KovaExpressiveShapes.Pill)
                                .background(accent),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                text = item.category.displayLabel().uppercase(),
                                style = KovaMonoBadgeTextStyle,
                                color = accent,
                            )
                            Text(
                                text = item.headline,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = item.body,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            filteredUserNotes.forEach { saved ->
                val accent = saved.category.accentColor()
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = "YOUR HIGHLIGHT · ${saved.category.displayLabel().uppercase()}",
                            style = KovaMonoBadgeTextStyle,
                            color = accent,
                        )
                        Text(
                            text = saved.highlightedSnippet,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (saved.learnerNote.isNotBlank()) {
                            Text(
                                text = saved.learnerNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(19.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
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
        lipColor = tactile.cardLip,
        borderColor = tactile.cardOutline,
        lipHeight = 4.dp,
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
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.MenuBook,
                    contentDescription = null,
                    tint = tactile.amberFace,
                    modifier = Modifier.size(19.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.lesson_section_word_breakdown),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.lesson_highlight_tap_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            breakdowns.forEach { item ->
                val existing = savedById["hl_${lessonId}_${item.id}"]
                val highlightTint = existing?.category?.softSurfaceColor()
                val highlightBorder = existing?.category?.accentColor() ?: tactile.cardOutline

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = highlightTint ?: MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(if (existing != null) 1.5.dp else 1.dp, highlightBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onToggleBlock(item.id) }
                        .testTag("highlightable_word_${item.id}"),
                ) {
                    Column(
                        modifier = Modifier.padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
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
                            text = "Plain meaning: ${item.literalMeaning}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
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
                                style = MaterialTheme.typography.bodySmall,
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
private fun HighlightStatusPill(existing: HighlightNote?) {
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HorizontalDivider(thickness = 1.dp, color = tactile.cardOutline.copy(alpha = 0.7f))

            Text(
                text = "TAG & SYNC TO REVISION BINDER",
                style = KovaMonoBadgeTextStyle,
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
    lessonId: String,
    practiceTask: PracticeTaskSpec,
    isSolutionVisible: Boolean,
    onOpenIdeForPractice: () -> Unit,
    onToggleSolution: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val motion = LocalKovaMotion.current
    val feedback = LocalKovaFeedback.current
    val view = LocalView.current

    val checkedSteps = remember(lessonId) { mutableStateListOf<String>() }

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.cobaltLip,
        borderColor = tactile.cobaltFace.copy(alpha = 0.6f),
        lipHeight = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("practice_desired_output_card"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Terminal,
                    contentDescription = null,
                    tint = tactile.cobaltFace,
                    modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Practice Task · ${practiceTask.title}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = practiceTask.prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Distinct Dark-Framed Desired Output Box
            KovaFramedCodeBlock(
                code = practiceTask.desiredOutput,
                languageLabel = "DESIRED OUTPUT",
                accentColor = tactile.emeraldFace,
                modifier = Modifier.testTag("desired_output_box"),
            )

            // Hands-On Code-Writing Self-Check Items
            if (practiceTask.expectedKeywords.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, tactile.cardOutline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "HANDS-ON CODE CHECKLIST",
                            style = KovaMonoBadgeTextStyle,
                            color = tactile.cobaltFace,
                        )
                        practiceTask.expectedKeywords.forEach { token ->
                            val isChecked = token in checkedSteps
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        feedback.tap(view)
                                        if (isChecked) checkedSteps.remove(token) else checkedSteps.add(token)
                                    }
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = if (isChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                    contentDescription = "Mark $token written",
                                    tint = if (isChecked) tactile.emeraldFace else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Include `$token` in Main.kt",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }

            // Launch Split IDE directly at 50:50 to practice writing Kotlin
            KovaTactileButton(
                onClick = onOpenIdeForPractice,
                faceColor = tactile.emeraldFace,
                lipColor = tactile.emeraldLip,
                contentColor = tactile.onEmeraldFace,
                minHeight = 48.dp,
                lipHeight = 4.dp,
                playTapSound = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("practice_open_ide_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Code,
                    contentDescription = null,
                    tint = tactile.onEmeraldFace,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.lesson_open_ide_practice_cta),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = tactile.onEmeraldFace,
                )
            }

            // Collapsible "Show Solution" Card Trigger (collapsed by default)
            KovaTactileButton(
                onClick = onToggleSolution,
                faceColor = if (isSolutionVisible) tactile.cardElevatedFace else tactile.cobaltSoft,
                lipColor = if (isSolutionVisible) tactile.cardLip else tactile.cobaltFace.copy(alpha = 0.45f),
                contentColor = if (isSolutionVisible) MaterialTheme.colorScheme.onSurface else tactile.onCobaltSoft,
                minHeight = 48.dp,
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KovaFramedCodeBlock(
                        code = practiceTask.solutionCode,
                        languageLabel = "ANNOTATED KOTLIN SOLUTION",
                        accentColor = tactile.cobaltFace,
                        showLineNumbers = true,
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(
                            text = "LINE-BY-LINE PLAIN-ENGLISH NOTES",
                            style = KovaMonoBadgeTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        practiceTask.lineNotes.forEach { note ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(tactile.cobaltSoft),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "L${note.lineNumber}",
                                        style = KovaMonoBadgeTextStyle,
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
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "$questionCount closed-book questions · Score at least 80% (4 of $questionCount) to unlock the next node.",
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
                            style = KovaMonoBadgeTextStyle,
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
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
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
        HighlightCategory.KEY_IDEA -> tactile.amberSoft.copy(alpha = 0.6f)
        HighlightCategory.CODE_RULE -> tactile.emeraldSoft.copy(alpha = 0.6f)
        HighlightCategory.GOTCHA -> tactile.coralSoft.copy(alpha = 0.6f)
    }
}
