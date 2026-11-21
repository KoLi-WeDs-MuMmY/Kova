package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TabletAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.HighlightCategory
import com.example.model.KovaTab
import com.example.model.KovaUiState
import com.example.model.RevisionBinderTab
import com.example.ui.components.AmbientMaterialCanvas
import com.example.ui.components.DeveloperSettingsSheet
import com.example.ui.components.FloatingCompanionHost
import com.example.ui.components.KovaTactileCard
import com.example.ui.feedback.KovaFeedbackController
import com.example.ui.feedback.LocalKovaFeedback
import com.example.ui.screens.AiScreen
import com.example.ui.screens.DetailSheetHost
import com.example.ui.screens.LabScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.RevisionScreen
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.KovaTheme
import com.example.ui.theme.LocalKovaMotion
import com.example.ui.theme.LocalKovaTactileColors
import com.example.viewmodel.KovaViewModel
import com.example.viewmodel.KovaViewModelFactory

private data class NavDestinationSpec(
    val tab: KovaTab,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val KovaNavDestinations = listOf(
    NavDestinationSpec(KovaTab.LEARN, R.string.nav_learn, Icons.Filled.Route, Icons.Outlined.Route),
    NavDestinationSpec(KovaTab.LAB, R.string.nav_lab, Icons.Filled.Science, Icons.Outlined.Science),
    NavDestinationSpec(KovaTab.AI, R.string.nav_ai, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    NavDestinationSpec(KovaTab.REVISION, R.string.nav_revision, Icons.Filled.Bookmarks, Icons.Outlined.Bookmarks),
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun KovaApp(
    viewModel: KovaViewModel = viewModel(
        factory = KovaViewModelFactory(LocalContext.current),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val feedbackController = remember(uiState.soundAndHapticsEnabled) {
        KovaFeedbackController(enabled = uiState.soundAndHapticsEnabled)
    }
    val view = LocalView.current

    // Gentle low note + REJECT haptic whenever a locked node shakes
    LaunchedEffect(uiState.lockedShakeEvent?.nonce) {
        if (uiState.lockedShakeEvent != null) {
            feedbackController.errorLock(view)
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
            viewModel.consumeSnackbar()
        }
    }

    CompositionLocalProvider(LocalKovaFeedback provides feedbackController) {
        KovaTheme(
            themeMode = uiState.themeMode,
            dynamicColor = uiState.dynamicColorEnabled,
            expressiveMotionEnabled = uiState.expressiveMotionEnabled,
        ) {
            FloatingCompanionHost(
                isFloatingPreview = uiState.isFloatingPreview,
                onExitFloating = { viewModel.setFloatingPreview(false) },
            ) { isCompactFloating ->
                KovaShellContent(
                    uiState = uiState,
                    isCompactFloating = isCompactFloating,
                    snackbarHostState = snackbarHostState,
                    onTabSelected = { tab ->
                        feedbackController.tap(view)
                        viewModel.selectTab(tab)
                    },
                    onToggleFloating = {
                        feedbackController.tap(view)
                        viewModel.toggleFloatingPreview()
                    },
                    onOpenSettings = {
                        feedbackController.tap(view)
                        viewModel.setSettingsOpen(true)
                    },
                    onToggleChapterExpanded = viewModel::toggleChapterExpanded,
                    onLessonClick = viewModel::openLesson,
                    onAssignmentClick = viewModel::openAssignment,
                    onNoteClick = viewModel::openNote,
                    onRevisionSheetClick = viewModel::openRevisionSheet,
                    onCloseDetail = viewModel::closeDetail,
                    onCycleSplitRatio = viewModel::cycleLessonSplitRatio,
                    onPassLessonMockTest = viewModel::passLessonMockTest,
                    onCompleteAssignment = viewModel::completeAssignment,
                    onSelectThread = viewModel::selectChatThread,
                    onCreateNewThread = viewModel::createNewLessonThread,
                    onUpdateChatSearch = viewModel::updateChatSearchQuery,
                    onTogglePinThread = viewModel::togglePinThread,
                    onDeleteThread = viewModel::deleteChatThread,
                    onUpdateChatComposer = viewModel::updateChatComposerText,
                    onInsertQuickPrompt = viewModel::insertQuickPrompt,
                    onSendChatMessage = viewModel::sendChatMessage,
                    onSaveMessageToNotes = viewModel::saveAiMessageToNotes,
                    onSelectBinderTab = viewModel::selectBinderTab,
                    onUpdateRevisionSearch = viewModel::updateRevisionSearchQuery,
                    onSelectHighlightFilter = viewModel::selectHighlightFilter,
                    onFocusNextFromRadar = viewModel::focusNextFromRadar,
                    onStartQuickReview = viewModel::startQuickReviewFromQueue,
                    onTogglePauseSprint = viewModel::togglePauseFlashcardSprint,
                    onRestartSprint = { viewModel.restartFlashcardSprint(autoStart = true) },
                    onFlipFlashcard = viewModel::flipFlashcard,
                    onRateFlashcardReviewAgain = viewModel::rateFlashcardReviewAgain,
                    onRateFlashcardGotIt = viewModel::rateFlashcardGotIt,
                )
            }

            if (uiState.isSettingsOpen) {
                DeveloperSettingsSheet(
                    uiState = uiState,
                    onDismiss = { viewModel.setSettingsOpen(false) },
                    onThemeModeSelected = viewModel::setThemeMode,
                    onDynamicColorChanged = viewModel::setDynamicColorEnabled,
                    onExpressiveMotionChanged = viewModel::setExpressiveMotionEnabled,
                    onSoundAndHapticsChanged = viewModel::setSoundAndHapticsEnabled,
                    onDevOverrideChanged = viewModel::setDevUnlockAllOverride,
                    onResetProgress = viewModel::resetDemoProgress,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun KovaShellContent(
    uiState: KovaUiState,
    isCompactFloating: Boolean,
    snackbarHostState: SnackbarHostState,
    onTabSelected: (KovaTab) -> Unit,
    onToggleFloating: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleChapterExpanded: (String) -> Unit,
    onLessonClick: (String) -> Unit,
    onAssignmentClick: (String, String) -> Unit,
    onNoteClick: (String) -> Unit,
    onRevisionSheetClick: (String) -> Unit,
    onCloseDetail: () -> Unit,
    onCycleSplitRatio: () -> Unit,
    onPassLessonMockTest: (String) -> Unit,
    onCompleteAssignment: (String) -> Unit,
    onSelectThread: (String) -> Unit,
    onCreateNewThread: () -> Unit,
    onUpdateChatSearch: (String) -> Unit,
    onTogglePinThread: (String) -> Unit,
    onDeleteThread: (String) -> Unit,
    onUpdateChatComposer: (String) -> Unit,
    onInsertQuickPrompt: (String) -> Unit,
    onSendChatMessage: () -> Unit,
    onSaveMessageToNotes: (String, HighlightCategory) -> Unit,
    onSelectBinderTab: (RevisionBinderTab) -> Unit,
    onUpdateRevisionSearch: (String) -> Unit,
    onSelectHighlightFilter: (HighlightCategory?) -> Unit,
    onFocusNextFromRadar: () -> Unit,
    onStartQuickReview: () -> Unit,
    onTogglePauseSprint: () -> Unit,
    onRestartSprint: () -> Unit,
    onFlipFlashcard: () -> Unit,
    onRateFlashcardReviewAgain: () -> Unit,
    onRateFlashcardGotIt: () -> Unit,
) {
    val motion = LocalKovaMotion.current
    val tactile = LocalKovaTactileColors.current

    // Smooth Focus Fade for app chrome when entering a lesson, and spring reveal on return
    val chromeAlpha by animateFloatAsState(
        targetValue = if (uiState.isLessonFocusMode) 0f else 1f,
        animationSpec = if (motion.enabled) {
            spring(dampingRatio = 0.76f, stiffness = 340f)
        } else {
            snap()
        },
        label = "lesson_focus_chrome_alpha",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val useTabletRail = maxWidth >= 600.dp && !isCompactFloating
        val useTwoPane = maxWidth >= 700.dp && !isCompactFloating

        AmbientMaterialCanvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (uiState.isLessonFocusMode) 0.45f else 1f },
        )

        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = uiState.activeDetail,
                transitionSpec = {
                    val enterDur = if (motion.enabled) 260 else 0
                    val exitDur = if (motion.enabled) 210 else 0
                    (fadeIn(tween(enterDur)) + scaleIn(
                        initialScale = 0.97f,
                        animationSpec = if (motion.enabled) {
                            spring(dampingRatio = 0.76f, stiffness = 340f)
                        } else {
                            snap()
                        },
                    )).togetherWith(fadeOut(tween(exitDur)))
                },
                label = "kova_detail_container_transform",
            ) { activeDetail ->
                if (activeDetail != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (!isCompactFloating) Modifier.statusBarsPadding() else Modifier),
                    ) {
                        DetailSheetHost(
                            destination = activeDetail,
                            uiState = uiState,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this@AnimatedContent,
                            onClose = onCloseDetail,
                            onCycleSplitRatio = onCycleSplitRatio,
                            onPassLessonMockTest = onPassLessonMockTest,
                            onCompleteAssignment = onCompleteAssignment,
                        )
                    }
                } else {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (useTabletRail) {
                            Box(
                                modifier = Modifier.graphicsLayer {
                                    alpha = chromeAlpha
                                    translationX = (1f - chromeAlpha) * -40f
                                },
                            ) {
                                KovaNavigationRail(
                                    selectedTab = uiState.selectedTab,
                                    onTabSelected = onTabSelected,
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        alpha = chromeAlpha
                                        translationY = (1f - chromeAlpha) * -24f
                                    },
                                ) {
                                    KovaTopBar(
                                        isCompactFloating = isCompactFloating,
                                        isFloatingPreview = uiState.isFloatingPreview,
                                        devOverrideEnabled = uiState.devUnlockAllOverride,
                                        onToggleFloating = onToggleFloating,
                                        onOpenSettings = onOpenSettings,
                                    )
                                }

                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    AnimatedContent(
                                        targetState = uiState.selectedTab,
                                        transitionSpec = {
                                            if (motion.enabled) {
                                                (fadeIn(tween(210)) + scaleIn(
                                                    initialScale = 0.985f,
                                                    animationSpec = spring(dampingRatio = 0.76f, stiffness = 340f),
                                                )).togetherWith(fadeOut(tween(150)))
                                            } else {
                                                fadeIn(snap()) togetherWith fadeOut(snap())
                                            }
                                        },
                                        label = "kova_tab_spring_switch",
                                    ) { tab ->
                                        when (tab) {
                                            KovaTab.LEARN -> LearnScreen(
                                                uiState = uiState,
                                                isWideTabletLayout = useTwoPane,
                                                sharedTransitionScope = this@SharedTransitionLayout,
                                                animatedVisibilityScope = this@AnimatedContent,
                                                onToggleChapterExpanded = onToggleChapterExpanded,
                                                onLessonClick = onLessonClick,
                                                onAssignmentClick = { id -> onAssignmentClick(id, "learn") },
                                                onRevisionSheetClick = onRevisionSheetClick,
                                            )
                                            KovaTab.LAB -> LabScreen(
                                                uiState = uiState,
                                                isWideTabletLayout = useTwoPane,
                                                sharedTransitionScope = this@SharedTransitionLayout,
                                                animatedVisibilityScope = this@AnimatedContent,
                                                onAssignmentClick = { id -> onAssignmentClick(id, "lab") },
                                            )
                                            KovaTab.AI -> AiScreen(
                                                uiState = uiState,
                                                isWideTabletLayout = useTwoPane,
                                                onSelectThread = onSelectThread,
                                                onCreateNewThread = onCreateNewThread,
                                                onUpdateSearchQuery = onUpdateChatSearch,
                                                onTogglePinThread = onTogglePinThread,
                                                onDeleteThread = onDeleteThread,
                                                onUpdateComposerText = onUpdateChatComposer,
                                                onInsertQuickPrompt = onInsertQuickPrompt,
                                                onSendMessage = onSendChatMessage,
                                                onSaveMessageToNotes = onSaveMessageToNotes,
                                            )
                                            KovaTab.REVISION -> RevisionScreen(
                                                uiState = uiState,
                                                isWideTabletLayout = useTwoPane,
                                                sharedTransitionScope = this@SharedTransitionLayout,
                                                animatedVisibilityScope = this@AnimatedContent,
                                                onSelectBinderTab = onSelectBinderTab,
                                                onUpdateSearchQuery = onUpdateRevisionSearch,
                                                onSelectFilter = onSelectHighlightFilter,
                                                onFocusNextFromRadar = onFocusNextFromRadar,
                                                onStartQuickReview = onStartQuickReview,
                                                onTogglePauseSprint = onTogglePauseSprint,
                                                onRestartSprint = onRestartSprint,
                                                onFlipFlashcard = onFlipFlashcard,
                                                onRateFlashcardReviewAgain = onRateFlashcardReviewAgain,
                                                onRateFlashcardGotIt = onRateFlashcardGotIt,
                                                onNoteClick = onNoteClick,
                                                onOpenChapterRevisionSheet = onRevisionSheetClick,
                                            )
                                        }
                                    }
                                }
                            }

                            if (!useTabletRail) {
                                KovaTactileBottomNavBar(
                                    selectedTab = uiState.selectedTab,
                                    isCompactFloating = isCompactFloating,
                                    onTabSelected = onTabSelected,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .graphicsLayer {
                                            alpha = chromeAlpha
                                            translationY = (1f - chromeAlpha) * 36f
                                        },
                                )
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .then(if (!isCompactFloating) Modifier.navigationBarsPadding() else Modifier)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (useTabletRail || uiState.activeDetail != null) 18.dp else 94.dp,
                    ),
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = KovaExpressiveShapes.StatAndAssignmentCard,
                    containerColor = if (tactile.isDarkSurface) tactile.cardElevatedFace else Color(0xFF1E293B),
                    contentColor = Color.White,
                )
            }
        }
    }
}

@Composable
private fun KovaTopBar(
    isCompactFloating: Boolean,
    isFloatingPreview: Boolean,
    devOverrideEnabled: Boolean,
    onToggleFloating: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val tactile = LocalKovaTactileColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isCompactFloating) Modifier.statusBarsPadding() else Modifier)
            .padding(
                horizontal = if (isCompactFloating) 12.dp else 16.dp,
                vertical = if (isCompactFloating) 8.dp else 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val badgeSize = if (isCompactFloating) 34.dp else 40.dp
            Box(
                modifier = Modifier.size(width = badgeSize, height = badgeSize + 4.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier = Modifier
                        .size(badgeSize)
                        .padding(top = 4.dp)
                        .clip(KovaExpressiveShapes.LessonSquircleNode)
                        .background(tactile.emeraldLip),
                )
                Box(
                    modifier = Modifier
                        .size(badgeSize)
                        .clip(KovaExpressiveShapes.LessonSquircleNode)
                        .background(tactile.emeraldFace),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "K",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = tactile.onEmeraldFace,
                    )
                }
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    if (devOverrideEnabled) {
                        Surface(
                            shape = CircleShape,
                            color = tactile.coralSoft,
                            border = BorderStroke(1.dp, tactile.coralFace),
                        ) {
                            Text(
                                text = "DEV",
                                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                color = tactile.onCoralSoft,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
                if (!isCompactFloating) {
                    Text(
                        text = stringResource(R.string.brand_subtitle),
                        style = KovaCodeTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onToggleFloating,
                shape = KovaExpressiveShapes.Pill,
                color = if (isFloatingPreview) tactile.cobaltFace else tactile.cardFace,
                border = BorderStroke(
                    tactile.cardBorderWidthDp.dp,
                    if (isFloatingPreview) tactile.cobaltLip else tactile.cardOutline,
                ),
                modifier = Modifier
                    .height(42.dp)
                    .testTag("toggle_floating_preview_pill"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = if (isFloatingPreview) {
                            Icons.Outlined.TabletAndroid
                        } else {
                            Icons.Outlined.PictureInPictureAlt
                        },
                        contentDescription = stringResource(R.string.cd_toggle_floating_mode),
                        tint = if (isFloatingPreview) tactile.onCobaltFace else tactile.cobaltFace,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = if (isFloatingPreview) {
                            stringResource(R.string.mode_full_view)
                        } else {
                            stringResource(R.string.mode_floating)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isFloatingPreview) tactile.onCobaltFace else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = tactile.cardFace,
                border = BorderStroke(tactile.cardBorderWidthDp.dp, tactile.cardOutline),
            ) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("open_settings_button"),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.cd_open_settings),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(21.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun KovaTactileBottomNavBar(
    selectedTab: KovaTab,
    isCompactFloating: Boolean,
    onTabSelected: (KovaTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val navShape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (!isCompactFloating) Modifier.navigationBarsPadding() else Modifier)
            .padding(
                horizontal = if (isCompactFloating) 10.dp else 16.dp,
                vertical = if (isCompactFloating) 8.dp else 12.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        KovaTactileCard(
            shape = navShape,
            faceColor = tactile.cardFace,
            lipColor = tactile.cardLip,
            borderColor = tactile.cardOutline,
            lipHeight = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("kova_floating_bottom_nav"),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KovaNavDestinations.forEach { dest ->
                    val selected = dest.tab == selectedTab
                    val label = stringResource(dest.labelRes)
                    Surface(
                        onClick = { onTabSelected(dest.tab) },
                        shape = KovaExpressiveShapes.Pill,
                        color = if (selected) tactile.cobaltSoft else Color.Transparent,
                        border = if (selected) {
                            BorderStroke(1.dp, tactile.cobaltFace.copy(alpha = 0.45f))
                        } else {
                            null
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("nav_tab_${dest.tab.name.lowercase()}"),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = if (selected) 14.dp else 10.dp,
                                vertical = 8.dp,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = label,
                                tint = if (selected) {
                                    tactile.cobaltFace
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(21.dp),
                            )
                            if (selected || !isCompactFloating) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                    color = if (selected) {
                                        tactile.onCobaltSoft
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
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
private fun KovaNavigationRail(
    selectedTab: KovaTab,
    onTabSelected: (KovaTab) -> Unit,
) {
    val tactile = LocalKovaTactileColors.current

    NavigationRail(
        containerColor = tactile.cardFace,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxHeight()
            .border(BorderStroke(1.dp, tactile.cardOutline))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 8.dp)
            .testTag("kova_tablet_nav_rail"),
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        KovaNavDestinations.forEach { dest ->
            val selected = dest.tab == selectedTab
            val label = stringResource(dest.labelRes)
            NavigationRailItem(
                selected = selected,
                onClick = { onTabSelected(dest.tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                        contentDescription = label,
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = tactile.cobaltFace,
                    selectedTextColor = tactile.cobaltFace,
                    indicatorColor = tactile.cobaltSoft,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier.testTag("rail_tab_${dest.tab.name.lowercase()}"),
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
