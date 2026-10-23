package com.enterprise.expensetracker.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EnterpriseDarkColorScheme = darkColorScheme(
    primary = BrandBlue,
    onPrimary = TextPrimaryDark,
    primaryContainer = SlateDark700,
    onPrimaryContainer = TextPrimaryDark,
    secondary = EmeraldSuccess,
    onSecondary = TextPrimaryDark,
    tertiary = PurpleAi,
    background = SlateDark900,
    onBackground = TextPrimaryDark,
    surface = CardBackgroundDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateDark700,
    onSurfaceVariant = TextSecondaryDark,
    outline = CardBorderDark,
    error = RoseAlert
)

@Composable
fun EnterpriseExpenseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = EnterpriseDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EnterpriseTypography,
        shapes = EnterpriseShapes,
        content = content
    )
}
