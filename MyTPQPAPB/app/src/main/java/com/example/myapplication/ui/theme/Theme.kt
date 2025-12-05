package com.example.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * DarkColorScheme - Skema warna untuk mode gelap
 * Menggunakan warna hijau tua dengan kontras putih
 */
private val DarkColorScheme = darkColorScheme(
    primary = IslamicGreen, // Hijau Islami untuk primary
    secondary = SoftGold, // Emas lembut untuk secondary
    tertiary = GoldAccent, // Emas gelap untuk tertiary
    background = DarkGreen, // Background hijau tua
    surface = DarkGreen, // Surface hijau tua
    onPrimary = Color.White, // Text putih di atas primary
    onSecondary = DarkGreen, // Text hijau tua di atas secondary
    onTertiary = DarkGreen, // Text hijau tua di atas tertiary
    onBackground = Color.White, // Text putih di atas background
    onSurface = Color.White // Text putih di atas surface
)

/**
 * LightColorScheme - Skema warna untuk mode terang
 * Menggunakan warna hijau dengan background putih
 */
private val LightColorScheme = lightColorScheme(
    primary = IslamicGreen, // Hijau Islami untuk primary
    secondary = SoftGold, // Emas lembut untuk secondary
    tertiary = GoldAccent, // Emas gelap untuk tertiary
    background = IvoryWhite, // Background putih gading
    surface = CreamWhite, // Surface krim putih
    onPrimary = Color.White, // Text putih di atas primary
    onSecondary = DarkGreen, // Text hijau tua di atas secondary
    onTertiary = DarkGreen, // Text hijau tua di atas tertiary
    onBackground = DarkGreen, // Text hijau tua di atas background
    onSurface = DarkGreen // Text hijau tua di atas surface
)

/**
 * MyApplicationTheme - Tema utama aplikasi TPQ
 * Mengatur skema warna Islami dengan dukungan light/dark mode
 * @param darkTheme Apakah menggunakan mode gelap
 * @param dynamicColor Apakah menggunakan dynamic color (disabled untuk konsistensi)
 * @param content Konten yang akan ditampilkan dengan tema ini
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable dynamic color untuk konsistensi tema Islami
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Dynamic color untuk Android 12+ (disabled untuk konsistensi tema Islami)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme // Mode gelap dengan tema Islami
        else -> LightColorScheme // Mode terang dengan tema Islami
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color sesuai dengan primary color
            window.statusBarColor = colorScheme.primary.toArgb()
            // Set status bar appearance berdasarkan mode gelap/terang
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme, // Skema warna Islami
        typography = Typography, // Tipografi kustom
        content = content
    )
}