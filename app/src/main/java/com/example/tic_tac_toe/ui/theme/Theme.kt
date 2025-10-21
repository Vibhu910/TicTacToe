package com.example.tic_tac_toe.ui.theme

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

private val NightModeScheme = darkColorScheme(
    primary = LightPurple,
    secondary = LightPurpleGrey,
    tertiary = LightPink
)

private val DayModeScheme = lightColorScheme(
    primary = DarkPurple,
    secondary = DarkPurpleGrey,
    tertiary = DarkPink
)

@Composable
fun TicTacToeTheme(
    nightMode: Boolean = isSystemInDarkTheme(),
    adaptiveColor: Boolean = true,
    displayContent: @Composable () -> Unit
) {
    val selectedScheme = when {
        adaptiveColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val currentContext = LocalContext.current
            if (nightMode) dynamicDarkColorScheme(currentContext) else dynamicLightColorScheme(currentContext)
        }

        nightMode -> NightModeScheme
        else -> DayModeScheme
    }

    MaterialTheme(
        colorScheme = selectedScheme,
        typography = AppTypography,
        content = displayContent
    )
}
