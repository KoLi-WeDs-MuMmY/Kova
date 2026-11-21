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
    MATTE_DARK,
    AMOLED_BLACK,
}

@Immutable
data class KovaMotionConfig(
    val enabled: Boolean = true,
) {
    val containerBoundsSpec: FiniteAnimationSpec<Rect>
        get() = if (enabled) {
            spring(dampingRatio = 0.78f, stiffness = 360f)
        } else {
            snap()
        }

    fun <T> expressiveSpring(): FiniteAnimationSpec<T> =
        if (enabled) {
            spring(dampingRatio = 0.78f, stiffness = 360f)
        } else {
            snap()
        }
}

/**
 * Tactile 3D game-board palette tokens with solid face + darker 3D lip pairs.
 * Zero neon or glowing accents.
 */
@Immutable
data class KovaTactileColors(
    val cardFace: Color,
    val cardOutline: Color,
    val cardLip: Color,
    val emeraldFace: Color,
    val emeraldLip: Color,
    val emeraldSoft: Color,
    val onEmeraldSoft: Color,
    val cobaltFace: Color,
    val cobaltLip: Color,
    val cobaltSoft: Color,
    val onCobaltSoft: Color,
    val amberFace: Color,
    val amberLip: Color,
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
    val askAi: Color,
    val isDarkSurface: Boolean,
    val isAmoled: Boolean,
)

val LocalKovaMotion = staticCompositionLocalOf { KovaMotionConfig(enabled = true) }

val LocalKovaTactileColors = staticCompositionLocalOf {
    WarmDaylightTactileColors
}

private val WarmDaylightTactileColors = KovaTactileColors(
    cardFace = WarmCardSurface,
    cardOutline = WarmOutline,
    cardLip = WarmCardLip,
    emeraldFace = TactileEmerald,
    emeraldLip = TactileEmeraldLip,
    emeraldSoft = TactileEmeraldSoft,
    onEmeraldSoft = TactileOnEmeraldSoft,
    cobaltFace = TactileCobalt,
    cobaltLip = TactileCobaltLip,
    cobaltSoft = TactileCobaltSoft,
    onCobaltSoft = TactileOnCobaltSoft,
    amberFace = TactileAmber,
    amberLip = TactileAmberLip,
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
    askAi = HighlightAskAiDay,
    isDarkSurface = false,
    isAmoled = false,
)

private val MatteDarkTactileColors = KovaTactileColors(
    cardFace = MatteDarkCardSurface,
    cardOutline = MatteDarkOutline,
    cardLip = MatteDarkCardLip,
    emeraldFace = TactileEmerald,
    emeraldLip = TactileEmeraldLip,
    emeraldSoft = MatteDarkEmeraldSoft,
    onEmeraldSoft = MatteDarkOnEmeraldSoft,
    cobaltFace = TactileCobalt,
    cobaltLip = TactileCobaltLip,
    cobaltSoft = MatteDarkCobaltSoft,
    onCobaltSoft = MatteDarkOnCobaltSoft,
    amberFace = TactileAmber,
    amberLip = TactileAmberLip,
    amberSoft = MatteDarkAmberSoft,
    onAmberSoft = MatteDarkOnAmberSoft,
    stoneFace = MatteDarkStone,
    stoneLip = MatteDarkStoneLip,
    stoneLabel = MatteDarkStoneLabel,
    ringTrack = Color(0xFF332F2A),
    ringTrackBevel = Color(0xFF1A1815),
    pathInactive = Color(0xFF423D36),
    keyIdea = HighlightKeyIdeaDark,
    codeRule = HighlightCodeRuleDark,
    askAi = HighlightAskAiDark,
    isDarkSurface = true,
    isAmoled = false,
)

private val AmoledTactileColors = MatteDarkTactileColors.copy(
    cardFace = AmoledCardSurface,
    cardOutline = AmoledOutline,
    cardLip = AmoledCardLip,
    stoneFace = Color(0xFF1C1C1C),
    stoneLip = Color(0xFF0E0E0E),
    ringTrack = Color(0xFF262626),
    ringTrackBevel = Color(0xFF121212),
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

private val MatteDarkColorScheme = darkColorScheme(
    primary = TactileEmerald,
    onPrimary = Color.White,
    primaryContainer = MatteDarkEmeraldSoft,
    onPrimaryContainer = MatteDarkOnEmeraldSoft,
    secondary = TactileAmber,
    onSecondary = Color(0xFF2D1600),
    secondaryContainer = MatteDarkAmberSoft,
    onSecondaryContainer = MatteDarkOnAmberSoft,
    tertiary = TactileCobalt,
    onTertiary = Color.White,
    tertiaryContainer = MatteDarkCobaltSoft,
    onTertiaryContainer = MatteDarkOnCobaltSoft,
    background = MatteDarkBackground,
    onBackground = MatteDarkInkPrimary,
    surface = MatteDarkCardSurface,
    onSurface = MatteDarkInkPrimary,
    surfaceVariant = MatteDarkSurfaceHigh,
    onSurfaceVariant = MatteDarkInkSecondary,
    surfaceContainerLowest = MatteDarkSurfaceLow,
    surfaceContainerLow = MatteDarkSurfaceLow,
    surfaceContainer = MatteDarkCardSurface,
    surfaceContainerHigh = MatteDarkSurfaceHigh,
    surfaceContainerHighest = MatteDarkSurfaceHighest,
    outline = Color(0xFF4F483E),
    outlineVariant = MatteDarkOutline,
)

private val AmoledColorScheme = MatteDarkColorScheme.copy(
    background = AmoledTrueBlackBackground,
    surface = AmoledCardSurface,
    surfaceVariant = AmoledSurfaceHigh,
    surfaceContainerLowest = AmoledTrueBlackBackground,
    surfaceContainerLow = AmoledSurfaceLow,
    surfaceContainer = AmoledCardSurface,
    surfaceContainerHigh = AmoledSurfaceHigh,
    surfaceContainerHighest = AmoledSurfaceHighest,
    outline = Color(0xFF3D3D3D),
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
                ThemeMode.MATTE_DARK -> dynamicDarkColorScheme(context)
                ThemeMode.AMOLED_BLACK -> dynamicDarkColorScheme(context).copy(
                    background = AmoledTrueBlackBackground,
                    surface = AmoledCardSurface,
                    surfaceContainerLowest = AmoledTrueBlackBackground,
                    surfaceContainerLow = AmoledSurfaceLow,
                    surfaceContainer = AmoledCardSurface,
                    surfaceContainerHigh = AmoledSurfaceHigh,
                    surfaceContainerHighest = AmoledSurfaceHighest,
                )
            }
        }
        themeMode == ThemeMode.AMOLED_BLACK -> AmoledColorScheme
        themeMode == ThemeMode.MATTE_DARK -> MatteDarkColorScheme
        else -> WarmDaylightColorScheme
    }

    val baseTactile = when (themeMode) {
        ThemeMode.WARM_DAYLIGHT -> WarmDaylightTactileColors
        ThemeMode.MATTE_DARK -> MatteDarkTactileColors
        ThemeMode.AMOLED_BLACK -> AmoledTactileColors
    }

    val tactileColors = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        baseTactile.copy(
            cardFace = if (themeMode == ThemeMode.AMOLED_BLACK) AmoledCardSurface else colorScheme.surfaceContainerLowest,
            cardOutline = colorScheme.outlineVariant,
            emeraldFace = colorScheme.primary,
            emeraldLip = darkenColor(colorScheme.primary, 0.22f),
            emeraldSoft = colorScheme.primaryContainer,
            onEmeraldSoft = colorScheme.onPrimaryContainer,
            cobaltFace = colorScheme.tertiary,
            cobaltLip = darkenColor(colorScheme.tertiary, 0.22f),
            cobaltSoft = colorScheme.tertiaryContainer,
            onCobaltSoft = colorScheme.onTertiaryContainer,
            amberFace = colorScheme.secondary,
            amberLip = darkenColor(colorScheme.secondary, 0.24f),
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
