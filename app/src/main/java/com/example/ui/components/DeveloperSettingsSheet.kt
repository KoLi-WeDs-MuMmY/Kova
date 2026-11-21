package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.KovaUiState
import com.example.ui.theme.KovaCodeTextStyle
import com.example.ui.theme.KovaExpressiveShapes
import com.example.ui.theme.LocalKovaTactileColors
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperSettingsSheet(
    uiState: KovaUiState,
    onDismiss: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onExpressiveMotionChanged: (Boolean) -> Unit,
    onDevOverrideChanged: (Boolean) -> Unit,
    onResetProgress: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tactile = LocalKovaTactileColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = KovaExpressiveShapes.HeroChapterCard,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Phase 2 Tactile Theme, Motion & Testing Controls",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                KovaPillBadge(
                    text = "PHASE 2",
                    containerColor = tactile.cobaltSoft,
                    contentColor = tactile.onCobaltSoft,
                    borderColor = tactile.cobaltFace.copy(alpha = 0.35f),
                )
            }

            // 3-Way Surface Theme Selector (Daylight / Warm Dark / AMOLED)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.settings_theme_mode),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ThemeModeChip(
                        label = stringResource(R.string.settings_theme_daylight),
                        hexTag = "#FBF8F3",
                        icon = Icons.Outlined.LightMode,
                        selected = uiState.themeMode == ThemeMode.WARM_DAYLIGHT,
                        onClick = { onThemeModeSelected(ThemeMode.WARM_DAYLIGHT) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("theme_mode_daylight"),
                    )
                    ThemeModeChip(
                        label = stringResource(R.string.settings_theme_warm_dark),
                        hexTag = "#141311",
                        icon = Icons.Outlined.DarkMode,
                        selected = uiState.themeMode == ThemeMode.WARM_DARK,
                        onClick = { onThemeModeSelected(ThemeMode.WARM_DARK) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("theme_mode_warm_dark"),
                    )
                    ThemeModeChip(
                        label = stringResource(R.string.settings_theme_amoled),
                        hexTag = "#000000",
                        icon = Icons.Outlined.NightsStay,
                        selected = uiState.themeMode == ThemeMode.AMOLED_BLACK,
                        onClick = { onThemeModeSelected(ThemeMode.AMOLED_BLACK) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("theme_mode_amoled"),
                    )
                }
            }

            HorizontalDivider(color = tactile.cardOutline)

            val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            SettingsToggleRow(
                icon = Icons.Outlined.Palette,
                title = stringResource(R.string.settings_dynamic_color),
                subtitle = if (supportsDynamic) {
                    stringResource(R.string.settings_dynamic_color_desc)
                } else {
                    "Requires Android 12+ (currently using Warm Tactile theme)"
                },
                checked = uiState.dynamicColorEnabled,
                enabled = supportsDynamic,
                onCheckedChange = onDynamicColorChanged,
                testTag = "toggle_dynamic_color",
            )

            SettingsToggleRow(
                icon = Icons.Outlined.Animation,
                title = stringResource(R.string.settings_motion_title),
                subtitle = stringResource(R.string.settings_motion_desc),
                checked = uiState.expressiveMotionEnabled,
                enabled = true,
                onCheckedChange = onExpressiveMotionChanged,
                testTag = "toggle_expressive_motion",
            )

            HorizontalDivider(color = tactile.cardOutline)

            // Dev Override Tactile 3D Card
            KovaTactileCard(
                shape = KovaExpressiveShapes.StatAndAssignmentCard,
                faceColor = if (uiState.devUnlockAllOverride) tactile.amberSoft else tactile.cardFace,
                lipColor = if (uiState.devUnlockAllOverride) tactile.amberLip else tactile.cardLip,
                borderColor = if (uiState.devUnlockAllOverride) tactile.amberFace else tactile.cardOutline,
                lipHeight = 4.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    KovaScallopedSealBadge(
                        icon = Icons.Outlined.DeveloperMode,
                        contentDescription = null,
                        containerColor = tactile.amberFace,
                        lipColor = tactile.amberLip,
                        contentColor = Color.White,
                        size = 42.dp,
                        iconSize = 20.dp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_dev_override),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.settings_dev_override_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = uiState.devUnlockAllOverride,
                        onCheckedChange = onDevOverrideChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = tactile.amberFace,
                        ),
                        modifier = Modifier.testTag("toggle_dev_override"),
                    )
                }
            }

            KovaTactileButton(
                onClick = onResetProgress,
                faceColor = tactile.cardFace,
                lipColor = tactile.cardLip,
                borderColor = tactile.cardOutline,
                contentColor = MaterialTheme.colorScheme.onSurface,
                minHeight = 48.dp,
                lipHeight = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_mock_progress_button"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_reset_demo),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ThemeModeChip(
    label: String,
    hexTag: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactile = LocalKovaTactileColors.current
    val face = if (selected) tactile.cobaltFace else tactile.cardFace
    val lip = if (selected) tactile.cobaltLip else tactile.cardLip
    val border = if (selected) tactile.cobaltLip else tactile.cardOutline
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface

    KovaTactileCard(
        shape = KovaExpressiveShapes.StatAndAssignmentCard,
        faceColor = face,
        lipColor = lip,
        borderColor = border,
        lipHeight = 4.dp,
        onClick = onClick,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1,
            )
            Text(
                text = hexTag,
                style = KovaCodeTextStyle.copy(fontWeight = FontWeight.Normal),
                color = contentColor.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
) {
    val tactile = LocalKovaTactileColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = KovaExpressiveShapes.LessonSquircleNode,
            color = tactile.cobaltSoft,
            modifier = Modifier.size(40.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tactile.cobaltFace,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = tactile.emeraldFace,
            ),
            modifier = Modifier.testTag(testTag),
        )
    }
}
