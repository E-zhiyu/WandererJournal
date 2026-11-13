package com.wanderer.journal.ui.theme

import android.app.Activity
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.core.view.WindowCompat
import com.wanderer.journal.R
import com.wanderer.journal.auxiliary.enums.settings.ThemeMode
import com.wanderer.journal.data.save.preference.AppSettingsPreference

/**
 * 统一加载静态色彩方案
 * 直接读取你现有的 R.color.md_theme_*，Android 系统会自动根据当前是否为 night 模式
 * 从 values/colors.xml 或 values-night/colors.xml 中加载对应的色值，无需重复定义！
 */
@Composable
private fun rememberStaticColorScheme(darkTheme: Boolean): ColorScheme {
    val primary = colorResource(id = R.color.md_theme_primary)
    val onPrimary = colorResource(id = R.color.md_theme_onPrimary)
    val primaryContainer = colorResource(id = R.color.md_theme_primaryContainer)
    val onPrimaryContainer = colorResource(id = R.color.md_theme_onPrimaryContainer)
    val secondary = colorResource(id = R.color.md_theme_secondary)
    val onSecondary = colorResource(id = R.color.md_theme_onSecondary)
    val secondaryContainer = colorResource(id = R.color.md_theme_secondaryContainer)
    val onSecondaryContainer = colorResource(id = R.color.md_theme_onSecondaryContainer)
    val tertiary = colorResource(id = R.color.md_theme_tertiary)
    val onTertiary = colorResource(id = R.color.md_theme_onTertiary)
    val tertiaryContainer = colorResource(id = R.color.md_theme_tertiaryContainer)
    val onTertiaryContainer = colorResource(id = R.color.md_theme_onTertiaryContainer)
    val error = colorResource(id = R.color.md_theme_error)
    val onError = colorResource(id = R.color.md_theme_onError)
    val errorContainer = colorResource(id = R.color.md_theme_errorContainer)
    val onErrorContainer = colorResource(id = R.color.md_theme_onErrorContainer)
    val background = colorResource(id = R.color.md_theme_background)
    val onBackground = colorResource(id = R.color.md_theme_onBackground)
    val surface = colorResource(id = R.color.md_theme_surface)
    val onSurface = colorResource(id = R.color.md_theme_onSurface)
    val surfaceVariant = colorResource(id = R.color.md_theme_surfaceVariant)
    val onSurfaceVariant = colorResource(id = R.color.md_theme_onSurfaceVariant)
    val outline = colorResource(id = R.color.md_theme_outline)
    val outlineVariant = colorResource(id = R.color.md_theme_outlineVariant)
    val inverseSurface = colorResource(id = R.color.md_theme_inverseSurface)
    val inverseOnSurface = colorResource(id = R.color.md_theme_inverseOnSurface)
    val inversePrimary = colorResource(id = R.color.md_theme_inversePrimary)
    val surfaceDim = colorResource(id = R.color.md_theme_surfaceDim)
    val surfaceBright = colorResource(id = R.color.md_theme_surfaceBright)
    val surfaceContainerLowest = colorResource(id = R.color.md_theme_surfaceContainerLowest)
    val surfaceContainerLow = colorResource(id = R.color.md_theme_surfaceContainerLow)
    val surfaceContainer = colorResource(id = R.color.md_theme_surfaceContainer)
    val surfaceContainerHigh = colorResource(id = R.color.md_theme_surfaceContainerHigh)
    val surfaceContainerHighest = colorResource(id = R.color.md_theme_surfaceContainerHighest)

    return if (darkTheme) {
        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            surfaceDim = surfaceDim,
            surfaceBright = surfaceBright,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            outline = outline,
            outlineVariant = outlineVariant,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            inversePrimary = inversePrimary,
            surfaceDim = surfaceDim,
            surfaceBright = surfaceBright,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
        )
    }
}

/**
 * Material 3 主题
 *
 * @param edgeToEdge 是否开启边到边沉浸式（推荐开启，配合 Scaffold 自动处理安全区域）
 */
@Composable
fun WandererJournalTheme(
    edgeToEdge: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // 决定最终的 ColorScheme
    val themeCode = AppSettingsPreference.getThemeMode(context)
    val darkTheme = when (themeCode) {
        ThemeMode.LIGHT_MODE.ordinal -> false
        ThemeMode.DARK_MODE.ordinal -> true
        else -> isSystemInDarkTheme()
    }
    val dynamicColor = AppSettingsPreference.getDynamicColorStat(context)
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> rememberStaticColorScheme(darkTheme = darkTheme)
    }

    // 2. 控制状态栏和导航栏的外观（对应你 XML 中的 windowLightStatusBar 和 navigationBarColor）
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? ComponentActivity
            if (edgeToEdge && activity != null) {
                // 使用现代 AndroidX 推荐的 enableEdgeToEdge 动态更新系统栏样式
                val barStyle = if (darkTheme) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                activity.enableEdgeToEdge(
                    statusBarStyle = barStyle,
                    navigationBarStyle = barStyle
                )
            } else {
                // 兼容非 EdgeToEdge 模式（严格对齐你原 XML 中的 colorSurface 填充）
                val window = (view.context as Activity).window
                @Suppress("DEPRECATION")
                window.statusBarColor = colorScheme.surface.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = colorScheme.surface.toArgb()

                WindowCompat.getInsetsController(window, view).apply {
                    // 深色模式下为 false（浅色图标），浅色模式下为 true（深色图标）
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(), // 如后续有自定义字体，可在此替换
        content = content
    )
}