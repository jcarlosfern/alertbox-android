package app.alertbox.io.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AlertOrange = Color(0xFFF26A2E)
val AlertYellow = Color(0xFFF7BF3C)
val AlertBackground = Color(0xFFFFF9F4)
val AlertInk = Color(0xFF222126)
val AlertMuted = Color(0xFF736D69)

private val LightColors = lightColorScheme(
    primary = AlertOrange,
    onPrimary = Color.White,
    secondary = AlertYellow,
    background = AlertBackground,
    surface = Color.White,
    onBackground = AlertInk,
    onSurface = AlertInk,
    outline = Color(0xFFE5DCD5),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF8A52),
    secondary = Color(0xFFFFCB56),
    background = Color(0xFF171513),
    surface = Color(0xFF24211F),
    onBackground = Color(0xFFF8F1EC),
    onSurface = Color(0xFFF8F1EC),
    outline = Color(0xFF514943),
)

@Composable
fun AlertBoxTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
