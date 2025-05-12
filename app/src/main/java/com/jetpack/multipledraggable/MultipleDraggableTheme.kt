package com.jetpack.multipledraggable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// your existing Shapes & Typography
val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)

val Typography = Typography(
    defaultFontFamily = FontFamily.Default
)

// your accent colors
internal val Purple500 = Color(0xFF97B385)
private val Purple700 = Color(0xF892B3FF)

private val DarkColorPalette = darkColors(
    primary         = Purple500,
    primaryVariant  = Purple700,
    secondary       = Purple700,          // <<< swap out the green
    background      = Color.Black,
    surface         = Color(0xFF121212),  // subtle dark gray
    onPrimary       = Color.Black,
    onSecondary     = Color.Black,
    onBackground    = Color.White,
    onSurface       = Color.White
)

private val LightColorPalette = lightColors(
    primary         = Purple500,
    primaryVariant  = Purple700,
    secondary       = Purple700,          // keep everything in the same family
    background      = Color.White,
    surface         = Color(0xFFF5F5F5),  // subtle light gray
    onPrimary       = Color.White,
    onSecondary     = Color.White,
    onBackground    = Color.Black,
    onSurface       = Color.Black
)

@Composable
fun MultipleDraggableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors     = colors,
        typography = Typography,
        shapes     = Shapes,
        content    = content
    )
}
