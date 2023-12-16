package com.dxl.androidscaffold.ui.compose.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


@Composable
fun WanTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) {
            darkColorScheme(
                primary = primary500,
                secondary = primary300,
                tertiary = primary100
            )
        } else {
            lightColorScheme(
                primary = primary500,
                secondary = primary300,
                tertiary = primary100
            )
        },
        content = content
    )
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = primary500.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
}