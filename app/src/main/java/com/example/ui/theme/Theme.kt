package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.data.local.ThemeMode

val LocalAdaptiveAccent = staticCompositionLocalOf { SalimNeutralAccent }
val LocalIsDarkTheme = staticCompositionLocalOf { true }

private val SalimDarkColorScheme = darkColorScheme(
    primary = SalimNeutralAccent,
    onPrimary = Color.Black,
    surface = SalimSurface1,
    onSurface = SalimTextPrimaryDark,
    surfaceVariant = SalimSurface2,
    onSurfaceVariant = SalimTextSecondaryDark,
    background = SalimBlack,
    onBackground = SalimTextPrimaryDark,
    outline = SalimBorderSubtleDark
)

private val SalimLightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    surface = Color(0xFFFFFFFF),
    onSurface = SalimTextPrimaryLight,
    surfaceVariant = Color(0xFFF2F2F7),
    onSurfaceVariant = SalimTextSecondaryLight,
    background = Color(0xFFF9F9FB),
    onBackground = SalimTextPrimaryLight,
    outline = SalimBorderSubtleLight
)

@Composable
fun SalimTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    adaptiveAccent: Color = SalimNeutralAccent,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) SalimDarkColorScheme else SalimLightColorScheme

    CompositionLocalProvider(
        LocalAdaptiveAccent provides adaptiveAccent,
        LocalIsDarkTheme provides isDark
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
