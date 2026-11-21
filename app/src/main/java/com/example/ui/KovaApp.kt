package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.model.KovaTab
import com.example.model.KovaUiState
import com.example.ui.components.DeveloperSettingsSheet
import com.example.ui.components.FloatingCompanionHost
import com.example.ui.screens.AiScreen
import com.example.ui.screens.DetailSheetHost
import com.example.ui.screens.LabScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.RevisionScreen
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.KovaTheme
import com.example.ui.theme.LocalKovaMotion
import com.example.viewmodel.KovaViewModel

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
    viewModel: KovaViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
                onTabSelected = viewModel::selectTab,
                onToggleFloating = viewModel::toggleFloatingPreview,
                onOpenSettings = { viewModel.setSettingsOpen(true) },
                onLessonClick = viewModel::openLesson,
                onAssignmentClick = viewModel::openAssignment,
                onNoteClick = viewModel::openNote,
                onRevisionSheetClick = viewModel::openRevisionSheet,
                onCloseDetail = viewModel::closeDetail,
                onPassLessonMockTest = viewModel::passLessonMockTest,
                onCompleteAssignment = viewModel::completeAssignment,
                onSelectHighlightFilter = viewModel::selectHighlightFilter,
            )
        }

        if (uiState.isSettingsOpen) {
            DeveloperSettingsSheet(
                uiState = uiState,
                onDismiss = { viewModel.setSettingsOpen(false) },
                onThemeModeSelected = viewModel::setThemeMode,
                onDynamicColorChanged = viewModel::setDynamicColorEnabled,
                onExpressiveMotionChanged = viewModel::setExpressiveMotionEnabled,
                onDevOverrideChanged = viewModel::setDevUnlockAllOverride,
                onResetProgress = viewModel::resetDemoProgress,
            )
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
    onLessonClick: (String) -> Unit,
    onAssignmentClick: (String, String) -> Unit,
    onNoteClick: (String) -> Unit,
    onRevisionSheetClick: (String) -> Unit,
    onCloseDetail: () -> Unit,
    onPassLessonMockTest: (String) -> Unit,
    onCompleteAssignment: (String) -> Unit,
    onSelectHighlightFilter: (com.example.model.HighlightCategory?) -> Unit,
) {
    val motion = LocalKovaMotion.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val useTabletRail = maxWidth >= 600.dp && !isCompactFloating
        val useTwoPane = maxWidth >= 700.dp && !isCompactFloating

        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = uiState.activeDetail,
                transitionSpec = {
                    val duration = if (motion.enabled) 240 else 0
                    fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
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
                            onPassLessonMockTest = onPassLessonMockTest,
                            onCompleteAssignment = onCompleteAssignment,
                        )
                    }
                } else {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (useTabletRail) {
                            KovaNavigationRail(
                                selectedTab = uiState.selectedTab,
                                onTabSelected = onTabSelected,
                            )
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                KovaTopBar(
                                    isCompactFloating = isCompactFloating,
                                    isFloatingPreview = uiState.isFloatingPreview,
                                    devOverrideEnabled = uiState.devUnlockAllOverride,
                                    onToggleFloating = onToggleFloating,
                                    onOpenSettings = onOpenSettings,
                                )

                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    when (uiState.selectedTab) {
                                        KovaTab.LEARN -> LearnScreen(
                                            uiState = uiState,
                                            isWideTabletLayout = useTwoPane,
                                            sharedTransitionScope = this@SharedTransitionLayout,
                                            animatedVisibilityScope = this@AnimatedContent,
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
                                        )
                                        KovaTab.REVISION -> RevisionScreen(
                                            uiState = uiState,
                                            isWideTabletLayout = useTwoPane,
                                            sharedTransitionScope = this@SharedTransitionLayout,
                                            animatedVisibilityScope = this@AnimatedContent,
                                            onSelectFilter = onSelectHighlightFilter,
                                            onNoteClick = onNoteClick,
                                            onOpenChapterRevisionSheet = onRevisionSheetClick,
                                        )
                                    }
                                }
                            }

                            if (!useTabletRail) {
                                KovaFloatingBottomNavBar(
                                    selectedTab = uiState.selectedTab,
                                    isCompactFloating = isCompactFloating,
                                    onTabSelected = onTabSelected,
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                )
                            }
                        }
                    }
                }
            }

            // Floating Snackbar Host
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
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
        // Brand emblem & title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompactFloating) 32.dp else 38.dp)
                    .clip(KovaExpressiveShapes.LessonSquircleNode)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "K",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (devOverrideEnabled) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = "DEV",
                                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
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

        // Right controls: Full View / Floating Pill Toggle + Settings Gear
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onToggleFloating,
                shape = KovaExpressiveShapes.Pill,
                color = if (isFloatingPreview) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                border = BorderStroke(
                    1.dp,
                    if (isFloatingPreview) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                    },
                ),
                modifier = Modifier
                    .height(40.dp)
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
                        tint = if (isFloatingPreview) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = if (isFloatingPreview) {
                            stringResource(R.string.mode_full_view)
                        } else {
                            stringResource(R.string.mode_floating)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isFloatingPreview) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                ),
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
private fun KovaFloatingBottomNavBar(
    selectedTab: KovaTab,
    isCompactFloating: Boolean,
    onTabSelected: (KovaTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navShape = RoundedCornerShape(28.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (!isCompactFloating) Modifier.navigationBarsPadding() else Modifier)
            .padding(
                horizontal = if (isCompactFloating) 10.dp else 18.dp,
                vertical = if (isCompactFloating) 8.dp else 12.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = navShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = navShape,
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.45f),
                )
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
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
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
                                    MaterialTheme.colorScheme.onPrimaryContainer
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
                                        MaterialTheme.colorScheme.onPrimaryContainer
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
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxHeight()
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
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier.testTag("rail_tab_${dest.tab.name.lowercase()}"),
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
