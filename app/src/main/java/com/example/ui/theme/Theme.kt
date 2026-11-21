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
    OBSIDIAN_DARK,
    AMOLED_BLACK,
    LIGHT,
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

@Immutable
data class KovaSemanticColors(
    val keyIdea: Color,
    val codeRule: Color,
    val askAi: Color,
    val isDarkSurface: Boolean,
    val isAmoled: Boolean,
)

val LocalKovaMotion = staticCompositionLocalOf { KovaMotionConfig(enabled = true) }
val LocalKovaSemanticColors = staticCompositionLocalOf {
    KovaSemanticColors(
        keyIdea = HighlightKeyIdeaDark,
        codeRule = HighlightCodeRuleDark,
        askAi = HighlightAskAiDark,
        isDarkSurface = true,
        isAmoled = false,
    )
}

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = ElectricMint,
    onPrimary = OnElectricMint,
    primaryContainer = ElectricMintContainer,
    onPrimaryContainer = OnElectricMintContainer,
    secondary = WarmAmber,
    onSecondary = OnWarmAmber,
    secondaryContainer = WarmAmberContainer,
    onSecondaryContainer = OnWarmAmberContainer,
    tertiary = SoftCoralTertiary,
    onTertiary = OnSoftCoralTertiary,
    tertiaryContainer = SoftCoralContainer,
    onTertiaryContainer = OnSoftCoralContainer,
    background = ObsidianBackground,
    onBackground = ObsidianTextPrimary,
    surface = ObsidianSurface,
    onSurface = ObsidianTextPrimary,
    surfaceVariant = ObsidianSurfaceHigh,
    onSurfaceVariant = ObsidianTextSecondary,
    surfaceContainerLowest = ObsidianSurfaceLowest,
    surfaceContainerLow = ObsidianSurfaceLow,
    surfaceContainer = ObsidianSurface,
    surfaceContainerHigh = ObsidianSurfaceHigh,
    surfaceContainerHighest = ObsidianSurfaceHighest,
    outline = ObsidianOutline,
    outlineVariant = ObsidianOutlineVariant,
)

private val AmoledTrueBlackColorScheme = ObsidianDarkColorScheme.copy(
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurfaceHigh,
    surfaceContainerLowest = AmoledSurfaceLowest,
    surfaceContainerLow = AmoledSurfaceLow,
    surfaceContainer = AmoledSurface,
    surfaceContainerHigh = AmoledSurfaceHigh,
    surfaceContainerHighest = AmoledSurfaceHighest,
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1F2A3A),
)

private val DaylightLightColorScheme = lightColorScheme(
    primary = LightEmeraldPrimary,
    onPrimary = OnLightEmerald,
    primaryContainer = LightEmeraldContainer,
    onPrimaryContainer = OnLightEmeraldContainer,
    secondary = LightAmberSecondary,
    onSecondary = OnLightAmber,
    secondaryContainer = LightAmberContainer,
    onSecondaryContainer = OnLightAmberContainer,
    tertiary = LightCoralTertiary,
    onTertiary = OnLightCoral,
    tertiaryContainer = LightCoralContainer,
    onTertiaryContainer = OnLightCoralContainer,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurfaceLowest,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceHigh,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLowest = LightSurfaceLowest,
    surfaceContainerLow = LightSurfaceLow,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceHigh,
    surfaceContainerHighest = LightSurfaceHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
)

@Composable
fun KovaTheme(
    themeMode: ThemeMode = ThemeMode.OBSIDIAN_DARK,
    dynamicColor: Boolean = false,
    expressiveMotionEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val isDark = themeMode != ThemeMode.LIGHT
    val isAmoled = themeMode == ThemeMode.AMOLED_BLACK

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            when (themeMode) {
                ThemeMode.LIGHT -> dynamicLightColorScheme(context)
                ThemeMode.OBSIDIAN_DARK -> dynamicDarkColorScheme(context)
                ThemeMode.AMOLED_BLACK -> dynamicDarkColorScheme(context).copy(
                    background = AmoledBackground,
                    surface = AmoledSurface,
                    surfaceContainerLowest = AmoledSurfaceLowest,
                    surfaceContainerLow = AmoledSurfaceLow,
                    surfaceContainer = AmoledSurface,
                    surfaceContainerHigh = AmoledSurfaceHigh,
                    surfaceContainerHighest = AmoledSurfaceHighest,
                )
            }
        }
        themeMode == ThemeMode.AMOLED_BLACK -> AmoledTrueBlackColorScheme
        themeMode == ThemeMode.OBSIDIAN_DARK -> ObsidianDarkColorScheme
        else -> DaylightLightColorScheme
    }

    val semanticColors = if (isDark) {
        KovaSemanticColors(
            keyIdea = HighlightKeyIdeaDark,
            codeRule = HighlightCodeRuleDark,
            askAi = HighlightAskAiDark,
            isDarkSurface = true,
            isAmoled = isAmoled,
        )
    } else {
        KovaSemanticColors(
            keyIdea = HighlightKeyIdeaLight,
            codeRule = HighlightCodeRuleLight,
            askAi = HighlightAskAiLight,
            isDarkSurface = false,
            isAmoled = false,
        )
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
        LocalKovaSemanticColors provides semanticColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KovaTypography,
            shapes = KovaMaterialShapes,
            content = content,
        )
    }
}
