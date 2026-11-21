package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    WARM_DAYLIGHT,
    WARM_DARK,
    AMOLED_BLACK,
}

@Immutable
data class KovaMotionConfig(
    val enabled: Boolean = true,
) {
    val containerBoundsSpec: FiniteAnimationSpec<Rect>
        get() = if (enabled) {
            spring(dampingRatio = 0.75f, stiffness = 340f)
        } else {
            snap()
        }

    fun <T> expressiveSpring(): FiniteAnimationSpec<T> =
        if (enabled) {
            spring(dampingRatio = 0.75f, stiffness = 340f)
        } else {
            snap()
        }
}

/**
 * Tactile 3D game-board palette tokens with solid face + darker 3D lip pairs.
 * Zero neon or glowing accents across Daylight, Warm Charcoal Dark, and True AMOLED.
 */
@Immutable
data class KovaTactileColors(
    val cardFace: Color,
    val cardElevatedFace: Color,
    val cardOutline: Color,
    val cardLip: Color,
    val cardBorderWidthDp: Float,
    val emeraldFace: Color,
    val emeraldLip: Color,
    val onEmeraldFace: Color,
    val emeraldSoft: Color,
    val onEmeraldSoft: Color,
    val cobaltFace: Color,
    val cobaltLip: Color,
    val onCobaltFace: Color,
    val cobaltSoft: Color,
    val onCobaltSoft: Color,
    val amberFace: Color,
    val amberLip: Color,
    val onAmberFace: Color,
    val amberSoft: Color,
    val onAmberSoft: Color,
    val stoneFace: Color,
    val stoneLip: Color,
    val stoneLabel: Color,
    val ringTrack: Color,
    val ringTrackBevel: Color,
    val pathInactive: Color,
    val keyIdea: Color,
    val codeRule: Color,
    val gotcha: Color,
    val isDarkSurface: Boolean,
    val isAmoled: Boolean,
)

val LocalKovaMotion = staticCompositionLocalOf { KovaMotionConfig(enabled = true) }

val LocalKovaTactileColors = staticCompositionLocalOf { WarmDaylightTactileColors }

private val WarmDaylightTactileColors = KovaTactileColors(
    cardFace = WarmCardSurface,
    cardElevatedFace = WarmSurfaceHigh,
    cardOutline = WarmOutline,
    cardLip = WarmCardLip,
    cardBorderWidthDp = 1.5f,
    emeraldFace = TactileEmerald,
    emeraldLip = TactileEmeraldLip,
    onEmeraldFace = Color.White,
    emeraldSoft = TactileEmeraldSoft,
    onEmeraldSoft = TactileOnEmeraldSoft,
    cobaltFace = TactileCobalt,
    cobaltLip = TactileCobaltLip,
    onCobaltFace = Color.White,
    cobaltSoft = TactileCobaltSoft,
    onCobaltSoft = TactileOnCobaltSoft,
    amberFace = TactileAmber,
    amberLip = TactileAmberLip,
    onAmberFace = Color.White,
    amberSoft = TactileAmberSoft,
    onAmberSoft = TactileOnAmberSoft,
    stoneFace = TactileStone,
    stoneLip = TactileStoneLip,
    stoneLabel = TactileStoneLabel,
    ringTrack = Color(0xFFEDE6D9),
    ringTrackBevel = Color(0xFFD8CEBC),
    pathInactive = Color(0xFFD7CEBF),
    keyIdea = HighlightKeyIdeaDay,
    codeRule = HighlightCodeRuleDay,
    gotcha = HighlightGotchaDay,
    isDarkSurface = false,
    isAmoled = false,
)

private val WarmCharcoalDarkTactileColors = KovaTactileColors(
    cardFace = WarmCharcoalSurface,
    cardElevatedFace = WarmCharcoalSurfaceHigh,
    cardOutline = WarmCharcoalBorder,
    cardLip = WarmCharcoalCardLip,
    cardBorderWidthDp = 1.25f,
    emeraldFace = WarmDarkEmerald,
    emeraldLip = WarmDarkEmeraldLip,
    onEmeraldFace = Color(0xFF042F1E),
    emeraldSoft = WarmDarkEmeraldSoft,
    onEmeraldSoft = WarmDarkOnEmeraldSoft,
    cobaltFace = WarmDarkCobalt,
    cobaltLip = WarmDarkCobaltLip,
    onCobaltFace = Color(0xFF091B3A),
    cobaltSoft = WarmDarkCobaltSoft,
    onCobaltSoft = WarmDarkOnCobaltSoft,
    amberFace = WarmDarkAmber,
    amberLip = WarmDarkAmberLip,
    onAmberFace = Color(0xFF331B00),
    amberSoft = WarmDarkAmberSoft,
    onAmberSoft = WarmDarkOnAmberSoft,
    stoneFace = WarmCharcoalStone,
    stoneLip = WarmCharcoalStoneLip,
    stoneLabel = WarmCharcoalStoneLabel,
    ringTrack = WarmCharcoalSurfaceHigh,
    ringTrackBevel = WarmCharcoalSurfaceLow,
    pathInactive = Color(0xFF474139),
    keyIdea = HighlightKeyIdeaDark,
    codeRule = HighlightCodeRuleDark,
    gotcha = HighlightGotchaDark,
    isDarkSurface = true,
    isAmoled = false,
)

private val TrueAmoledTactileColors = WarmCharcoalDarkTactileColors.copy(
    cardFace = AmoledCardSurface,
    cardElevatedFace = AmoledSurfaceHigh,
    cardOutline = AmoledOutline,
    cardLip = AmoledCardLip,
    cardBorderWidthDp = 1f,
    stoneFace = AmoledStone,
    stoneLip = AmoledStoneLip,
    ringTrack = AmoledSurfaceHigh,
    ringTrackBevel = AmoledSurfaceLow,
    pathInactive = Color(0xFF333333),
    isAmoled = true,
)

private val WarmDaylightColorScheme = lightColorScheme(
    primary = TactileEmerald,
    onPrimary = Color.White,
    primaryContainer = TactileEmeraldSoft,
    onPrimaryContainer = TactileOnEmeraldSoft,
    secondary = TactileAmber,
    onSecondary = Color(0xFF3B1F00),
    secondaryContainer = TactileAmberSoft,
    onSecondaryContainer = TactileOnAmberSoft,
    tertiary = TactileCobalt,
    onTertiary = Color.White,
    tertiaryContainer = TactileCobaltSoft,
    onTertiaryContainer = TactileOnCobaltSoft,
    background = WarmIvoryBackground,
    onBackground = WarmInkPrimary,
    surface = WarmCardSurface,
    onSurface = WarmInkPrimary,
    surfaceVariant = WarmSurfaceHigh,
    onSurfaceVariant = WarmInkSecondary,
    surfaceContainerLowest = WarmCardSurface,
    surfaceContainerLow = WarmSurfaceLow,
    surfaceContainer = WarmSurfaceHigh,
    surfaceContainerHigh = WarmCardSurface,
    surfaceContainerHighest = WarmSurfaceHighest,
    outline = WarmCardLip,
    outlineVariant = WarmOutline,
)

private val WarmCharcoalDarkColorScheme = darkColorScheme(
    primary = WarmDarkEmerald,
    onPrimary = Color(0xFF042F1E),
    primaryContainer = WarmDarkEmeraldSoft,
    onPrimaryContainer = WarmDarkOnEmeraldSoft,
    secondary = WarmDarkAmber,
    onSecondary = Color(0xFF331B00),
    secondaryContainer = WarmDarkAmberSoft,
    onSecondaryContainer = WarmDarkOnAmberSoft,
    tertiary = WarmDarkCobalt,
    onTertiary = Color(0xFF091B3A),
    tertiaryContainer = WarmDarkCobaltSoft,
    onTertiaryContainer = WarmDarkOnCobaltSoft,
    background = WarmCharcoalBackground,
    onBackground = WarmCharcoalTextPrimary,
    surface = WarmCharcoalSurface,
    onSurface = WarmCharcoalTextPrimary,
    surfaceVariant = WarmCharcoalSurfaceHigh,
    onSurfaceVariant = WarmCharcoalTextSecondary,
    surfaceContainerLowest = WarmCharcoalBackground,
    surfaceContainerLow = WarmCharcoalSurfaceLow,
    surfaceContainer = WarmCharcoalSurface,
    surfaceContainerHigh = WarmCharcoalSurfaceHigh,
    surfaceContainerHighest = WarmCharcoalSurfaceHighest,
    outline = WarmCharcoalBorder,
    outlineVariant = WarmCharcoalBorder,
)

private val TrueAmoledColorScheme = WarmCharcoalDarkColorScheme.copy(
    background = AmoledTrueBlackBackground,
    surface = AmoledCardSurface,
    surfaceVariant = AmoledSurfaceHigh,
    surfaceContainerLowest = AmoledTrueBlackBackground,
    surfaceContainerLow = AmoledSurfaceLow,
    surfaceContainer = AmoledCardSurface,
    surfaceContainerHigh = AmoledSurfaceHigh,
    surfaceContainerHighest = AmoledSurfaceHighest,
    outline = AmoledOutline,
    outlineVariant = AmoledOutline,
)

@Composable
fun KovaTheme(
    themeMode: ThemeMode = ThemeMode.WARM_DAYLIGHT,
    dynamicColor: Boolean = false,
    expressiveMotionEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val isDark = themeMode != ThemeMode.WARM_DAYLIGHT

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            when (themeMode) {
                ThemeMode.WARM_DAYLIGHT -> dynamicLightColorScheme(context)
                ThemeMode.WARM_DARK -> dynamicDarkColorScheme(context)
                ThemeMode.AMOLED_BLACK -> dynamicDarkColorScheme(context).copy(
                    background = AmoledTrueBlackBackground,
                    surface = AmoledCardSurface,
                    surfaceContainerLowest = AmoledTrueBlackBackground,
                    surfaceContainerLow = AmoledSurfaceLow,
                    surfaceContainer = AmoledCardSurface,
                    surfaceContainerHigh = AmoledSurfaceHigh,
                    surfaceContainerHighest = AmoledSurfaceHighest,
                    outline = AmoledOutline,
                    outlineVariant = AmoledOutline,
                )
            }
        }
        themeMode == ThemeMode.AMOLED_BLACK -> TrueAmoledColorScheme
        themeMode == ThemeMode.WARM_DARK -> WarmCharcoalDarkColorScheme
        else -> WarmDaylightColorScheme
    }

    val baseTactile = when (themeMode) {
        ThemeMode.WARM_DAYLIGHT -> WarmDaylightTactileColors
        ThemeMode.WARM_DARK -> WarmCharcoalDarkTactileColors
        ThemeMode.AMOLED_BLACK -> TrueAmoledTactileColors
    }

    val tactileColors = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        baseTactile.copy(
            cardFace = if (themeMode == ThemeMode.AMOLED_BLACK) AmoledCardSurface else colorScheme.surfaceContainer,
            cardElevatedFace = if (themeMode == ThemeMode.AMOLED_BLACK) AmoledSurfaceHigh else colorScheme.surfaceContainerHigh,
            cardOutline = colorScheme.outlineVariant,
            emeraldFace = colorScheme.primary,
            emeraldLip = darkenColor(colorScheme.primary, 0.24f),
            onEmeraldFace = colorScheme.onPrimary,
            emeraldSoft = colorScheme.primaryContainer,
            onEmeraldSoft = colorScheme.onPrimaryContainer,
            cobaltFace = colorScheme.tertiary,
            cobaltLip = darkenColor(colorScheme.tertiary, 0.24f),
            onCobaltFace = colorScheme.onTertiary,
            cobaltSoft = colorScheme.tertiaryContainer,
            onCobaltSoft = colorScheme.onTertiaryContainer,
            amberFace = colorScheme.secondary,
            amberLip = darkenColor(colorScheme.secondary, 0.24f),
            onAmberFace = colorScheme.onSecondary,
            amberSoft = colorScheme.secondaryContainer,
            onAmberSoft = colorScheme.onSecondaryContainer,
        )
    } else {
        baseTactile
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalKovaMotion provides KovaMotionConfig(enabled = expressiveMotionEnabled),
        LocalKovaTactileColors provides tactileColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KovaTypography,
            shapes = KovaMaterialShapes,
            content = content,
        )
    }
}

private fun darkenColor(color: Color, factor: Float): Color {
    val scale = (1f - factor).coerceIn(0f, 1f)
    return Color(
        red = color.red * scale,
        green = color.green * scale,
        blue = color.blue * scale,
        alpha = color.alpha,
    )
}
