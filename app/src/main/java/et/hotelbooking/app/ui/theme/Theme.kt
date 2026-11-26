package et.hotelbooking.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import et.hotelbooking.app.R
import et.hotelbooking.app.util.ThemeMode

// Ethiopian-inspired palette: deep highland green, Meskel gold, and a warm coffee-red accent.
object Brand {
    val Green = Color(0xFF0B6B45)
    val GreenDark = Color(0xFF064A30)
    val Gold = Color(0xFFE0A526)
    val GoldSoft = Color(0xFFFFE3A3)
    val Red = Color(0xFFB83A2E)
    val Ink = Color(0xFF14201A)
    val Sand = Color(0xFFFBF8F1)
}

private val Light = lightColorScheme(
    primary = Brand.Green, onPrimary = Color.White, primaryContainer = Color(0xFFB4F1CF), onPrimaryContainer = Color(0xFF002113),
    secondary = Color(0xFF8A6100), onSecondary = Color.White, secondaryContainer = Color(0xFFFFDFA0), onSecondaryContainer = Color(0xFF2B1C00),
    tertiary = Brand.Red, onTertiary = Color.White, tertiaryContainer = Color(0xFFFFDAD4), onTertiaryContainer = Color(0xFF410001),
    background = Brand.Sand, onBackground = Brand.Ink, surface = Color.White, onSurface = Brand.Ink,
    surfaceVariant = Color(0xFFE6EEE8), onSurfaceVariant = Color(0xFF404943), outline = Color(0xFF707973), outlineVariant = Color(0xFFC0C9C1),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF6F4EE), surfaceContainer = Color(0xFFF1EFE8),
    surfaceContainerHigh = Color(0xFFEBE9E2), surfaceContainerHighest = Color(0xFFE5E3DC), error = Color(0xFFBA1A1A)
)

private val Dark = darkColorScheme(
    primary = Color(0xFF82DBAA), onPrimary = Color(0xFF003822), primaryContainer = Color(0xFF005234), onPrimaryContainer = Color(0xFFB4F1CF),
    secondary = Color(0xFFF3BF48), onSecondary = Color(0xFF412D00), secondaryContainer = Color(0xFF5E4200), onSecondaryContainer = Color(0xFFFFDFA0),
    tertiary = Color(0xFFFFB4A8), onTertiary = Color(0xFF690003), tertiaryContainer = Color(0xFF8E1F17), onTertiaryContainer = Color(0xFFFFDAD4),
    background = Color(0xFF0E1511), onBackground = Color(0xFFDEE4DE), surface = Color(0xFF131A16), onSurface = Color(0xFFDEE4DE),
    surfaceVariant = Color(0xFF404943), onSurfaceVariant = Color(0xFFC0C9C1), outline = Color(0xFF8A938C), outlineVariant = Color(0xFF404943),
    surfaceContainerLowest = Color(0xFF09100C), surfaceContainerLow = Color(0xFF171E1A), surfaceContainer = Color(0xFF1B221E),
    surfaceContainerHigh = Color(0xFF252C28), surfaceContainerHighest = Color(0xFF303733)
)

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun ethiopic(weight: FontWeight) = Font(R.font.noto_sans_ethiopic, weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun playfair(weight: FontWeight) = Font(R.font.playfair_display, weight, variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)))

val EthiopicFamily = FontFamily(ethiopic(FontWeight.Normal), ethiopic(FontWeight.Medium), ethiopic(FontWeight.SemiBold), ethiopic(FontWeight.Bold))
val DisplayFamily = FontFamily(playfair(FontWeight.Normal), playfair(FontWeight.SemiBold), playfair(FontWeight.Bold))

private fun typography(lang: String): Typography {
    val ethiopicScript = lang == "am" || lang == "ti"
    val latinDisplay = lang in setOf("en", "fr", "es", "om")
    val body: FontFamily = if (ethiopicScript) EthiopicFamily else FontFamily.Default
    val display: FontFamily = when { ethiopicScript -> EthiopicFamily; latinDisplay -> DisplayFamily; else -> FontFamily.Default }
    val base = Typography()
    fun TextStyle.b() = copy(fontFamily = body)
    fun TextStyle.d(w: FontWeight = FontWeight.Bold) = copy(fontFamily = display, fontWeight = w)
    return base.copy(
        displayLarge = base.displayLarge.d(), displayMedium = base.displayMedium.d(), displaySmall = base.displaySmall.d(),
        headlineLarge = base.headlineLarge.d(), headlineMedium = base.headlineMedium.d(), headlineSmall = base.headlineSmall.d(),
        titleLarge = base.titleLarge.copy(fontFamily = body, fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontFamily = body, fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontFamily = body, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.b(), bodyMedium = base.bodyMedium.b(), bodySmall = base.bodySmall.b(),
        labelLarge = base.labelLarge.copy(fontFamily = body, fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.b(), labelSmall = base.labelSmall.copy(fontFamily = body, fontSize = 11.sp),
    )
}

@Immutable data class ExtraColors(val gold: Color, val success: Color, val warning: Color, val info: Color)
val LocalExtraColors = staticCompositionLocalOf { ExtraColors(Brand.Gold, Color(0xFF1B7F4B), Color(0xFFB26A00), Color(0xFF1F5FAF)) }

@Composable
fun EthioTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (themeMode) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.LIGHT -> false; ThemeMode.DARK -> true }
    val lang = LocalConfiguration.current.locales[0]?.language ?: "en"
    val extra = if (dark) ExtraColors(Color(0xFFF3BF48), Color(0xFF6FD69C), Color(0xFFFFB955), Color(0xFF9CC3FF))
        else ExtraColors(Brand.Gold, Color(0xFF1B7F4B), Color(0xFFB26A00), Color(0xFF1F5FAF))
    androidx.compose.runtime.CompositionLocalProvider(LocalExtraColors provides extra) {
        MaterialTheme(colorScheme = if (dark) Dark else Light, typography = typography(lang), shapes = Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp), small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), large = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)), content = content)
    }
}
