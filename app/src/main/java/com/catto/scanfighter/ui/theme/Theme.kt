package com.catto.scanfighter.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val GameColorScheme = lightColorScheme(
    primary = ScanFighterYellow,
    onPrimary = ScanFighterDarkGray,
    secondary = ScanFighterGold,
    onSecondary = ScanFighterDarkGray,
    background = ScanFighterBlue,
    onBackground = White,
    surface = ScanFighterDarkGray,
    onSurface = White,
    error = ScanFighterRed,
    onError = White
)

@Composable
fun ScanFighterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is turned off to enforce the game's theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> GameColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
