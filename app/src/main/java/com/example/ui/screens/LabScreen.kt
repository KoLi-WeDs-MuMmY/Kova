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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.KovaUiState
import com.example.ui.components.KovaPillBadge
import com.example.ui.components.KovaScallopedSealBadge
import com.example.ui.components.KovaTactileCard
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaTactileColors

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LabScreen(
    uiState: KovaUiState,
    isWideTabletLayout: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onAssignmentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isWideTabletLayout) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1.1f)
                    .testTag("lab_assignments_list"),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { AssignmentsHubHeader() }
                items(
                    count = uiState.chapters.size,
                    key = { uiState.chapters[it].assignment.id },
                ) { index ->
                    val assignment = uiState.chapters[index].assignment
                    ChapterAssignmentPathCard(
                        assignment = assignment,
                        uiState = uiState,
                        originPrefix = "lab",
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onClick = { onAssignmentClick(assignment.id) },
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(0.95f)
                    .testTag("lab_prompt_builder_pane"),
                contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { VirtualPromptBuilderPreviewCard() }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("lab_screen_list"),
            contentPadding = PaddingValues(top = 8.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { AssignmentsHubHeader() }
            items(
                count = uiState.chapters.size,
                key = { uiState.chapters[it].assignment.id },
            ) { index ->
                val assignment = uiState.chapters[index].assignment
                ChapterAssignmentPathCard(
                    assignment = assignment,
                    uiState = uiState,
                    originPrefix = "lab",
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onClick = { onAssignmentClick(assignment.id) },
                )
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
                VirtualPromptBuilderPreviewCard()
            }
        }
    }
}

@Composable
private fun AssignmentsHubHeader() {
    val tactile = LocalKovaTactileColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.lab_assignments_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.lab_assignments_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        KovaPillBadge(
            text = "SYNCED",
            leadingIcon = Icons.Outlined.Sync,
            containerColor = tactile.emeraldSoft,
            contentColor = tactile.onEmeraldSoft,
            borderColor = tactile.emeraldFace.copy(alpha = 0.35f),
        )
    }
}

@Composable
private fun VirtualPromptBuilderPreviewCard() {
    val tactile = LocalKovaTactileColors.current

    KovaTactileCard(
        shape = KovaExpressiveShapes.AsymmetricPreviewCard,
        faceColor = tactile.cardFace,
        lipColor = tactile.cobaltLip,
        borderColor = tactile.cobaltFace.copy(alpha = 0.55f),
        lipHeight = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("virtual_prompt_builder_preview"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KovaScallopedSealBadge(
                    icon = Icons.Outlined.Science,
                    contentDescription = null,
                    containerColor = tactile.cobaltFace,
                    lipColor = tactile.cobaltLip,
                    contentColor = Color.White,
                    size = 46.dp,
                    iconSize = 22.dp,
                    shape = KovaExpressiveShapes.SoftCloverBadge,
                )
                Column(modifier = Modifier.weight(1f)) {
                    KovaPillBadge(
                        text = stringResource(R.string.lab_prompt_builder_badge),
                        containerColor = tactile.amberSoft,
                        contentColor = tactile.onAmberSoft,
                        borderColor = tactile.amberFace.copy(alpha = 0.45f),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.lab_prompt_builder_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Text(
                text = stringResource(R.string.lab_prompt_builder_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.5.dp, tactile.cardOutline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "VIRTUAL BUNDLE PREVIEW",
                            style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Bold),
                            color = tactile.cobaltFace,
                        )
                        KovaPillBadge(
                            text = "2 FILES · 0 UPLOADS",
                            containerColor = tactile.cardFace,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            borderColor = tactile.cardOutline,
                        )
                    }

                    VirtualTreeLine(
                        indent = 0,
                        icon = Icons.Outlined.FolderOpen,
                        tag = "<Folder name=\"chapter1-lab\">",
                        isFolder = true,
                    )
                    VirtualTreeLine(
                        indent = 1,
                        icon = Icons.Outlined.Code,
                        tag = "<File name=\"StreakEvaluator.kt\" />",
                        isFolder = false,
                    )
                    VirtualTreeLine(
                        indent = 1,
                        icon = Icons.Outlined.Description,
                        tag = "<File name=\"ExpectedOutput.txt\" />",
                        isFolder = false,
                    )
                    VirtualTreeLine(
                        indent = 0,
                        icon = Icons.Outlined.Folder,
                        tag = "</Folder>",
                        isFolder = true,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KovaExpressiveShapes.Pill)
                    .background(tactile.stoneFace)
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+ Add Virtual Folder (Phase 3)",
                    style = MaterialTheme.typography.labelLarge,
                    color = tactile.stoneLabel,
                )
            }
        }
    }
}

@Composable
private fun VirtualTreeLine(
    indent: Int,
    icon: ImageVector,
    tag: String,
    isFolder: Boolean,
) {
    val tactile = LocalKovaTactileColors.current

    Row(
        modifier = Modifier.padding(start = (indent * 18).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isFolder) tactile.amberLip else tactile.emeraldFace,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = tag,
            style = KovaCodeTextStyle,
            color = if (isFolder) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
