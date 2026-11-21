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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.model.HighlightCategory
import com.example.model.KovaUiState
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaTactileButton
import com.example.ui.components.KovaTactileCard
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors

private val QuickPromptTemplates = listOf(
    "Explain like I'm 10",
    "Break down word meanings",
    "Why did this fail?",
    "Give a mini drill",
)

@Composable
fun AiScreen(
    uiState: KovaUiState,
    isWideTabletLayout: Boolean,
    onSelectThread: (String) -> Unit,
    onCreateNewThread: () -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onTogglePinThread: (String) -> Unit,
    onDeleteThread: (String) -> Unit,
    onUpdateComposerText: (String) -> Unit,
    onInsertQuickPrompt: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSaveMessageToNotes: (String, HighlightCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isHistoryDrawerOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_screen_root"),
    ) {
        if (isWideTabletLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ChatHistoryPanel(
                    threads = uiState.filteredChatThreads,
                    activeThreadId = uiState.activeThread?.id,
                    searchQuery = uiState.chatSearchQuery,
                    showCloseButton = false,
                    onSearchQueryChange = onUpdateSearchQuery,
                    onCreateNewThread = onCreateNewThread,
                    onSelectThread = onSelectThread,
                    onTogglePin = onTogglePinThread,
                    onDeleteThread = onDeleteThread,
                    onClose = {},
                    modifier = Modifier
                        .weight(0.36f)
                        .fillMaxHeight()
                        .padding(bottom = 16.dp),
                )

                AiChatMainPane(
                    uiState = uiState,
                    isWideTabletLayout = true,
                    onOpenHistoryDrawer = {},
                    onUpdateComposerText = onUpdateComposerText,
                    onInsertQuickPrompt = onInsertQuickPrompt,
                    onSendMessage = onSendMessage,
                    onSaveMessageToNotes = onSaveMessageToNotes,
                    modifier = Modifier
                        .weight(0.64f)
                        .fillMaxHeight(),
                )
            }
        } else {
            AiChatMainPane(
                uiState = uiState,
                isWideTabletLayout = false,
                onOpenHistoryDrawer = { isHistoryDrawerOpen = true },
                onUpdateComposerText = onUpdateComposerText,
                onInsertQuickPrompt = onInsertQuickPrompt,
                onSendMessage = onSendMessage,
                onSaveMessageToNotes = onSaveMessageToNotes,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
            )

            // Slide-over Persistent Chat History Drawer on compact screens
            if (isHistoryDrawerOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { isHistoryDrawerOpen = false },
                ) {
                    ChatHistoryPanel(
                        threads = uiState.filteredChatThreads,
                        activeThreadId = uiState.activeThread?.id,
                        searchQuery = uiState.chatSearchQuery,
                        showCloseButton = true,
                        onSearchQueryChange = onUpdateSearchQuery,
                        onCreateNewThread = {
                            onCreateNewThread()
                            isHistoryDrawerOpen = false
                        },
                        onSelectThread = { id ->
                            onSelectThread(id)
                            isHistoryDrawerOpen = false
                        },
                        onTogglePin = onTogglePinThread,
                        onDeleteThread = onDeleteThread,
                        onClose = { isHistoryDrawerOpen = false },
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.86f)
                            .widthIn(max = 360.dp)
                            .padding(start = 12.dp, top = 6.dp, bottom = 96.dp)
                            .clickable(enabled = false) {},
                    )
                }
            }
        }
    }
}

@Composable
private fun AiChatMainPane(
    uiState: KovaUiState,
    isWideTabletLayout: Boolean,
    onOpenHistoryDrawer: () -> Unit,
    onUpdateComposerText: (String) -> Unit,
    onInsertQuickPrompt: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSaveMessageToNotes: (String, HighlightCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val listState = rememberLazyListState()
    val messages = uiState.activeThreadMessages
    val activeThread = uiState.activeThread
    val scopedLabel = if (activeThread != null) {
        "Lesson ${activeThread.lessonCode} · ${activeThread.lessonTitle}"
    } else {
        val activeLesson = uiState.activeLesson
        "Lesson ${activeLesson?.code ?: "1.1"} · ${activeLesson?.title ?: "Variables & Boxes"}"
    }

    LaunchedEffect(messages.size, activeThread?.id) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = modifier.padding(bottom = if (isWideTabletLayout) 14.dp else 94.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Compact Top Context & Quota Strip
        KovaTactileCard(
            shape = KovaExpressiveShapes.StatAndAssignmentCard,
            faceColor = tactile.cardFace,
            lipColor = tactile.cobaltLip,
            borderColor = tactile.cobaltFace.copy(alpha = 0.5f),
            lipHeight = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_context_quota_strip"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (!isWideTabletLayout) {
                        Surface(
                            onClick = onOpenHistoryDrawer,
                            shape = CircleShape,
                            color = tactile.cobaltSoft,
                            border = BorderStroke(1.dp, tactile.cobaltFace.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("open_chat_history_button"),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = stringResource(R.string.cd_open_history),
                                    tint = tactile.onCobaltSoft,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }

                    KovaPillBadge(
                        text = scopedLabel,
                        leadingIcon = Icons.Outlined.Lock,
                        containerColor = tactile.cobaltSoft,
                        contentColor = tactile.onCobaltSoft,
                        borderColor = Color.Transparent,
                        useMonospace = false,
                        modifier = Modifier.weight(1f),
                    )
                }

                // Quota Strip: Morning Digest · Active Pool · SR Reserve
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    KovaPillBadge(
                        text = "Morning Digest: ${uiState.quota.morningDigestStatus}",
                        containerColor = tactile.emeraldSoft,
                        contentColor = tactile.onEmeraldSoft,
                        borderColor = Color.Transparent,
                    )
                    KovaPillBadge(
                        text = "Active Pool: ${uiState.quota.activeAiPoolRemaining}/${uiState.quota.activeAiPoolMax}",
                        containerColor = tactile.cobaltSoft,
                        contentColor = tactile.onCobaltSoft,
                        borderColor = Color.Transparent,
                    )
                    KovaPillBadge(
                        text = "SR Reserve: ${uiState.quota.spacedRepetitionReserve}",
                        containerColor = tactile.amberSoft,
                        contentColor = tactile.onAmberSoft,
                        borderColor = Color.Transparent,
                    )
                }
            }
        }

        // Scrollable Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("ai_message_list"),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items = messages, key = { it.id }) { msg ->
                AiChatBubble(
                    message = msg,
                    onSaveToNotes = { category -> onSaveMessageToNotes(msg.id, category) },
                )
            }
        }

        // Quick-Prompt Chips above Composer (fills input box for editing)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("ai_quick_prompts_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickPromptTemplates.forEachIndexed { index, chipText ->
                Surface(
                    onClick = { onInsertQuickPrompt(chipText) },
                    shape = KovaExpressiveShapes.Pill,
                    color = tactile.cardFace,
                    border = BorderStroke(1.dp, tactile.cardOutline),
                    modifier = Modifier.testTag("quick_prompt_chip_$index"),
                ) {
                    Text(
                        text = chipText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }

        // Tactile Editable Composer Bar
        KovaTactileCard(
            shape = RoundedCornerShape(22.dp),
            faceColor = tactile.cardFace,
            lipColor = tactile.cardLip,
            borderColor = tactile.cardOutline,
            lipHeight = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = uiState.chatComposerText,
                    onValueChange = onUpdateComposerText,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.ai_input_placeholder),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_composer_input"),
                )

                val canSend = uiState.chatComposerText.isNotBlank()
                Surface(
                    onClick = onSendMessage,
                    enabled = canSend,
                    shape = CircleShape,
                    color = if (canSend) tactile.emeraldFace else tactile.stoneFace,
                    border = BorderStroke(
                        1.5.dp,
                        if (canSend) tactile.emeraldLip else tactile.stoneLip,
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("ai_send_button"),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = stringResource(R.string.cd_send_message),
                            tint = if (canSend) tactile.onEmeraldFace else tactile.stoneLabel,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiChatBubble(
    message: ChatMessage,
    onSaveToNotes: (HighlightCategory) -> Unit,
) {
    val tactile = LocalKovaTactileColors.current
    val motion = LocalKovaMotion.current
    var showCategoryPicker by remember(message.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start,
    ) {
        if (message.isFromUser) {
            Surface(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 6.dp),
                color = tactile.cobaltFace,
                border = BorderStroke(1.25.dp, tactile.cobaltLip),
                modifier = Modifier.widthIn(max = 320.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = tactile.onCobaltFace,
                    )
                    Text(
                        text = message.timestampLabel,
                        style = KovaCodeTextStyle,
                        color = tactile.onCobaltFace.copy(alpha = 0.78f),
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            }
        } else {
            KovaTactileCard(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 20.dp),
                faceColor = tactile.cardFace,
                lipColor = tactile.cardLip,
                borderColor = tactile.cardOutline,
                lipHeight = 4.dp,
                modifier = Modifier.fillMaxWidth(0.95f),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = tactile.emeraldFace,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "Kova Scoped Tutor",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = tactile.emeraldFace,
                            )
                        }
                        Text(
                            text = message.timestampLabel,
                            style = KovaCodeTextStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    // Feature #2: 1-Tap "Save AI Answer to Revision Notes"
                    val isSaved = message.savedNoteId != null
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            onClick = {
                                if (!isSaved) {
                                    showCategoryPicker = !showCategoryPicker
                                }
                            },
                            shape = KovaExpressiveShapes.Pill,
                            color = if (isSaved) tactile.emeraldSoft else tactile.amberSoft,
                            border = BorderStroke(
                                1.dp,
                                if (isSaved) tactile.emeraldFace else tactile.amberFace.copy(alpha = 0.6f),
                            ),
                            modifier = Modifier.testTag("save_to_notes_button_${message.id}"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.BookmarkAdded else Icons.Outlined.BookmarkAdd,
                                    contentDescription = null,
                                    tint = if (isSaved) tactile.onEmeraldSoft else tactile.onAmberSoft,
                                    modifier = Modifier.size(15.dp),
                                )
                                Text(
                                    text = if (isSaved) {
                                        stringResource(R.string.ai_saved_to_notes)
                                    } else {
                                        stringResource(R.string.ai_save_to_notes)
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSaved) tactile.onEmeraldSoft else tactile.onAmberSoft,
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = showCategoryPicker && !isSaved,
                        enter = if (motion.enabled) {
                            expandVertically(spring(dampingRatio = 0.75f, stiffness = 340f)) + fadeIn()
                        } else {
                            expandVertically(snap()) + fadeIn(snap())
                        },
                        exit = if (motion.enabled) {
                            shrinkVertically(spring(dampingRatio = 0.8f, stiffness = 360f)) + fadeOut()
                        } else {
                            shrinkVertically(snap()) + fadeOut(snap())
                        },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            HighlightCategory.entries.forEach { category ->
                                val tagColor = category.accentColor()
                                Surface(
                                    onClick = {
                                        showCategoryPicker = false
                                        onSaveToNotes(category)
                                    },
                                    shape = KovaExpressiveShapes.Pill,
                                    color = tagColor.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, tagColor.copy(alpha = 0.65f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_tag_${category.name.lowercase()}_${message.id}"),
                                ) {
                                    Text(
                                        text = category.displayLabel(),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = tagColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatHistoryPanel(
    threads: List<ChatThread>,
    activeThreadId: String?,
    searchQuery: String,
    showCloseButton: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onCreateNewThread: () -> Unit,
    onSelectThread: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onDeleteThread: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.HeroChapterCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.cardLip,
        borderColor = tactile.cardOutline,
        lipHeight = 5.dp,
        modifier = modifier.testTag("ai_chat_history_panel"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.ai_history_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (showCloseButton) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.cd_close_detail),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            KovaTactileButton(
                onClick = onCreateNewThread,
                faceColor = tactile.cobaltFace,
                lipColor = tactile.cobaltLip,
                contentColor = tactile.onCobaltFace,
                minHeight = 44.dp,
                lipHeight = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_new_thread_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = tactile.onCobaltFace,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.ai_new_thread_cta),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = tactile.onCobaltFace,
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
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
                        text = stringResource(R.string.ai_search_threads_hint),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                shape = RoundedCornerShape(14.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = tactile.cobaltFace,
                    unfocusedBorderColor = tactile.cardOutline,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_history_search_input"),
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = threads, key = { it.id }) { thread ->
                    val isSelected = thread.id == activeThreadId
                    Surface(
                        onClick = { onSelectThread(thread.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) tactile.cobaltSoft else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) tactile.cobaltFace else tactile.cardOutline,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_thread_item_${thread.id}"),
                    ) {
                        Column(
                            modifier = Modifier.padding(11.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                KovaPillBadge(
                                    text = "L${thread.lessonCode}",
                                    containerColor = tactile.cardFace,
                                    contentColor = tactile.cobaltFace,
                                    borderColor = tactile.cardOutline,
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = thread.updatedAtLabel,
                                        style = KovaCodeTextStyle,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    IconButton(
                                        onClick = { onTogglePin(thread.id) },
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Icon(
                                            imageVector = if (thread.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                            contentDescription = stringResource(R.string.cd_pin_thread),
                                            tint = if (thread.isPinned) tactile.amberFace else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteThread(thread.id) },
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = stringResource(R.string.cd_delete_thread),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                            Text(
                                text = thread.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = thread.previewSnippet,
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
    }
}
