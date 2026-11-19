package com.shezik.drawanywhere.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.shezik.drawanywhere.R

enum class ThemeMode(val labelRes: Int) {
    SYSTEM(R.string.theme_mode_system),
    LIGHT(R.string.theme_mode_light),
    DARK(R.string.theme_mode_dark),
}

/**
 * A small set of hand-tuned accent palettes. Dynamic color (Monet) overrides these on
 * Android 12+, but the presets keep the app usable and identifiable on every device.
 */
enum class ThemeAccent(
    val labelRes: Int,
    val preview: Color,
    val lightPrimary: Color,
    val lightPrimaryContainer: Color,
    val lightOnPrimaryContainer: Color,
    val darkPrimary: Color,
    val darkPrimaryContainer: Color,
    val darkOnPrimaryContainer: Color,
) {
    BLUE(
        R.string.accent_blue,
        Color(0xFF4D6FB7),
        Color(0xFF4D6FB7),
        Color(0xFFD8E2FF),
        Color(0xFF001A41),
        Color(0xFFB7CBFF),
        Color(0xFF294A70),
        Color(0xFFD8E2FF),
    ),
    TEAL(
        R.string.accent_teal,
        Color(0xFF476E6D),
        Color(0xFF476E6D),
        Color(0xFFD3ECEB),
        Color(0xFF00201F),
        Color(0xFF9DD5D4),
        Color(0xFF2A4B4A),
        Color(0xFFD3ECEB),
    ),
    PURPLE(
        R.string.accent_purple,
        Color(0xFF6B4E9B),
        Color(0xFF6B4E9B),
        Color(0xFFE9DDFF),
        Color(0xFF23005B),
        Color(0xFFD0BCFF),
        Color(0xFF4F378B),
        Color(0xFFE9DDFF),
    ),
    GREEN(
        R.string.accent_green,
        Color(0xFF3B6E4F),
        Color(0xFF3B6E4F),
        Color(0xFFC8E6D0),
        Color(0xFF00210F),
        Color(0xFF9BD4B0),
        Color(0xFF24513A),
        Color(0xFFC8E6D0),
    ),
    ORANGE(
        R.string.accent_orange,
        Color(0xFF8B5A2B),
        Color(0xFF8B5A2B),
        Color(0xFFFFDCC2),
        Color(0xFF2E1500),
        Color(0xFFFFB77A),
        Color(0xFF5B3A1A),
        Color(0xFFFFDCC2),
    ),
    ROSE(
        R.string.accent_rose,
        Color(0xFF8E4B44),
        Color(0xFF8E4B44),
        Color(0xFFFFDAD4),
        Color(0xFF3B0907),
        Color(0xFFFFB4AB),
        Color(0xFF653B36),
        Color(0xFFFFDAD4),
    ),
}

fun ColorScheme.withAccent(accent: ThemeAccent, dark: Boolean): ColorScheme = copy(
    primary = if (dark) accent.darkPrimary else accent.lightPrimary,
    onPrimary = if (dark) Color(0xFF0E1B2D) else Color.White,
    primaryContainer = if (dark) accent.darkPrimaryContainer else accent.lightPrimaryContainer,
    onPrimaryContainer = if (dark) accent.darkOnPrimaryContainer else accent.lightOnPrimaryContainer,
)
