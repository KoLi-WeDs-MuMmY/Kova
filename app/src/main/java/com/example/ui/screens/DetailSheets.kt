package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Chapter
import com.example.model.ChapterAssignment
import com.example.model.DetailDestination
import com.example.model.HighlightNote
import com.example.model.KovaUiState
import com.example.model.Lesson
import com.example.model.LessonStatus
import com.example.model.NoteSource
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaScallopedSealBadge
import com.example.ui.components.KovaTactileButton
import com.example.ui.components.KovaTactileCard
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailSheetHost(
    destination: DetailDestination,
    uiState: KovaUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClose: () -> Unit,
    onPassLessonMockTest: (String) -> Unit,
    onCompleteAssignment: (String) -> Unit,
) {
    BackHandler(onBack = onClose)

    val motion = LocalKovaMotion.current
    with(sharedTransitionScope) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = destination.sharedBoundsKey),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ -> motion.containerBoundsSpec },
                ),
        ) {
            when (destination) {
                is DetailDestination.LessonDetail -> {
                    val lesson = uiState.findLesson(destination.lessonId)
                    if (lesson != null) {
                        LessonDetailContent(
                            lesson = lesson,
                            dailyRemaining = uiState.quota.dailyLessonsRemaining,
                            onClose = onClose,
                            onPassMockTest = { onPassLessonMockTest(lesson.id) },
                        )
                    }
                }
                is DetailDestination.AssignmentDetail -> {
                    val assignment = uiState.findAssignment(destination.assignmentId)
                    if (assignment != null) {
                        AssignmentDetailContent(
                            assignment = assignment,
                            onClose = onClose,
                            onCompleteAssignment = { onCompleteAssignment(assignment.id) },
                        )
                    }
                }
                is DetailDestination.NoteDetail -> {
                    val note = uiState.findNote(destination.noteId)
                    if (note != null) {
                        NoteDetailContent(note = note, onClose = onClose)
                    }
                }
                is DetailDestination.RevisionSheetDetail -> {
                    val chapter = uiState.findChapter(destination.chapterId)
                    if (chapter != null) {
                        RevisionSheetContent(chapter = chapter, onClose = onClose)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTopHeader(
    badgeText: String,
    title: String,
    onClose: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = tactile.cardFace,
            border = BorderStroke(tactile.cardBorderWidthDp.dp, tactile.cardOutline),
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("detail_back_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = badgeText,
                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                color = tactile.cobaltFace,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Surface(
            shape = CircleShape,
            color = tactile.cardFace,
            border = BorderStroke(tactile.cardBorderWidthDp.dp, tactile.cardOutline),
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("detail_close_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.cd_close_detail),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LessonDetailContent(
    lesson: Lesson,
    dailyRemaining: Int,
    onClose: () -> Unit,
    onPassMockTest: () -> Unit,
) {
    val isCompleted = lesson.status == LessonStatus.COMPLETED
    val tactile = LocalKovaTactileColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        DetailTopHeader(
            badgeText = "LESSON ${lesson.code} · ${lesson.estimatedMinutes} MIN",
            title = lesson.title,
            onClose = onClose,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KovaPillBadge(
                    text = stringResource(R.string.phase2_lesson_tag),
                    containerColor = tactile.amberSoft,
                    contentColor = tactile.onAmberSoft,
                    borderColor = tactile.amberFace.copy(alpha = 0.45f),
                )
                KovaPillBadge(
                    text = "$dailyRemaining/3 DAILY LEFT",
                    containerColor = tactile.cardFace,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    borderColor = tactile.cardOutline,
                )
            }

            KovaTactileCard(
                shape = KovaExpressiveShapes.HeroChapterCard,
                faceColor = tactile.cardFace,
                lipColor = tactile.cobaltLip,
                borderColor = tactile.cobaltFace.copy(alpha = 0.55f),
                lipHeight = 5.dp,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = tactile.amberFace,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "10-Year-Old Analogy & Word Breakdown",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = lesson.analogyTeaser,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = lesson.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            KovaTactileCard(
                shape = KovaExpressiveShapes.StatAndAssignmentCard,
                faceColor = tactile.cardFace,
                lipColor = tactile.cardLip,
                borderColor = tactile.cardOutline,
                lipHeight = 4.dp,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BugReport,
                                contentDescription = null,
                                tint = tactile.emeraldFace,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = "Debugger Line Animation & Watch Panel",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.PlayCircleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, tactile.cardOutline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = lesson.codePreview,
                            style = KovaCodeTextStyle,
                            color = tactile.cobaltFace,
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                    Text(
                        text = "Interactive step-by-step line stepping, live variable watch chips, Desired Output, and collapsible Show Solution arrive in Phase 3.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            KovaTactileCard(
                shape = KovaExpressiveShapes.HeroChapterCard,
                faceColor = tactile.cardFace,
                lipColor = if (isCompleted) tactile.emeraldLip else tactile.amberLip,
                borderColor = if (isCompleted) tactile.emeraldFace else tactile.amberFace,
                lipHeight = 5.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        KovaScallopedSealBadge(
                            icon = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.TaskAlt,
                            contentDescription = null,
                            containerColor = if (isCompleted) tactile.emeraldFace else tactile.amberFace,
                            lipColor = if (isCompleted) tactile.emeraldLip else tactile.amberLip,
                            contentColor = if (isCompleted) tactile.onEmeraldFace else tactile.onAmberFace,
                            size = 44.dp,
                            iconSize = 22.dp,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.lesson_test_section_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.lesson_test_subtitle),
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
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
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
                                    text = stringResource(R.string.lesson_test_passed_badge),
                                    style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                    color = tactile.onEmeraldSoft,
                                )
                            }
                        }
                    } else {
                        KovaTactileButton(
                            onClick = onPassMockTest,
                            faceColor = tactile.emeraldFace,
                            lipColor = tactile.emeraldLip,
                            contentColor = tactile.onEmeraldFace,
                            minHeight = 52.dp,
                            lipHeight = 5.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pass_mock_test_button"),
                        ) {
                            Text(
                                text = stringResource(R.string.lesson_test_pass_cta),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = tactile.onEmeraldFace,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentDetailContent(
    assignment: ChapterAssignment,
    onClose: () -> Unit,
    onCompleteAssignment: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        DetailTopHeader(
            badgeText = "${assignment.chapterLabel} · ${assignment.estimatedMinutes} MIN",
            title = assignment.title,
            onClose = onClose,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            KovaTactileCard(
                shape = KovaExpressiveShapes.HeroChapterCard,
                faceColor = tactile.cardFace,
                lipColor = if (assignment.isCompleted) tactile.emeraldLip else tactile.amberLip,
                borderColor = if (assignment.isCompleted) tactile.emeraldFace else tactile.amberFace,
                lipHeight = 5.dp,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        KovaScallopedSealBadge(
                            icon = if (assignment.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            containerColor = if (assignment.isCompleted) tactile.emeraldFace else tactile.amberFace,
                            lipColor = if (assignment.isCompleted) tactile.emeraldLip else tactile.amberLip,
                            contentColor = if (assignment.isCompleted) tactile.onEmeraldFace else tactile.onAmberFace,
                            size = 50.dp,
                            iconSize = 24.dp,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Learn <-> Lab Synchronized Gate",
                                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                color = tactile.amberFace,
                            )
                            Text(
                                text = assignment.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    Text(
                        text = "Deliverables Checklist",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    assignment.deliverables.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = if (assignment.isCompleted) tactile.emeraldFace else tactile.amberFace,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(18.dp),
                            )
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            if (assignment.isCompleted) {
                Surface(
                    shape = KovaExpressiveShapes.Pill,
                    color = tactile.emeraldSoft,
                    border = BorderStroke(1.25.dp, tactile.emeraldFace),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assignment_completed_banner"),
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
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
                            text = stringResource(R.string.assignment_completed_badge),
                            style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                            color = tactile.onEmeraldSoft,
                        )
                    }
                }
            } else {
                KovaTactileButton(
                    onClick = onCompleteAssignment,
                    faceColor = tactile.amberFace,
                    lipColor = tactile.amberLip,
                    contentColor = tactile.onAmberFace,
                    minHeight = 52.dp,
                    lipHeight = 5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("complete_assignment_button"),
                ) {
                    Text(
                        text = stringResource(R.string.assignment_mark_complete),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = tactile.onAmberFace,
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteDetailContent(
    note: HighlightNote,
    onClose: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val accent = note.category.accentColor()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        DetailTopHeader(
            badgeText = "${note.category.displayLabel().uppercase()} · LESSON ${note.lessonCode}",
            title = note.lessonTitle,
            onClose = onClose,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            KovaTactileCard(
                shape = KovaExpressiveShapes.HeroChapterCard,
                faceColor = tactile.cardFace,
                lipColor = tactile.cardLip,
                borderColor = accent,
                lipHeight = 5.dp,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        KovaPillBadge(
                            text = "SAVED ${note.savedAtLabel}",
                            containerColor = accent.copy(alpha = 0.14f),
                            contentColor = accent,
                            borderColor = accent.copy(alpha = 0.45f),
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
                    }
                    Text(
                        text = "\"${note.highlightedSnippet}\"",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            KovaTactileCard(
                shape = KovaExpressiveShapes.StatAndAssignmentCard,
                faceColor = tactile.cardFace,
                lipColor = tactile.cardLip,
                borderColor = tactile.cardOutline,
                lipHeight = 4.dp,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (note.source == NoteSource.AI_TUTOR) "SAVED AI EXPLANATION" else "YOUR PERSONAL NOTE",
                        style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = note.learnerNote,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun RevisionSheetContent(
    chapter: Chapter,
    onClose: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        DetailTopHeader(
            badgeText = "CHAPTER ${chapter.number} REVISION SHEET",
            title = chapter.title,
            onClose = onClose,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            KovaTactileCard(
                shape = KovaExpressiveShapes.HeroChapterCard,
                faceColor = tactile.cardFace,
                lipColor = tactile.emeraldLip,
                borderColor = tactile.emeraldFace,
                lipHeight = 5.dp,
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KovaScallopedSealBadge(
                        icon = Icons.Outlined.AutoStories,
                        contentDescription = null,
                        containerColor = tactile.emeraldFace,
                        lipColor = tactile.emeraldLip,
                        contentColor = tactile.onEmeraldFace,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rapid Recall Summary",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${chapter.completedLessonCount}/${chapter.lessons.size} lessons mastered in this chapter",
                            style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                            color = tactile.emeraldFace,
                        )
                    }
                }
            }

            chapter.revisionBulletPoints.forEachIndexed { index, point ->
                KovaTactileCard(
                    shape = KovaExpressiveShapes.StatAndAssignmentCard,
                    faceColor = tactile.cardFace,
                    lipColor = tactile.cardLip,
                    borderColor = tactile.cardOutline,
                    lipHeight = 4.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(tactile.cobaltSoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                color = tactile.onCobaltSoft,
                            )
                        }
                        Text(
                            text = point,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
