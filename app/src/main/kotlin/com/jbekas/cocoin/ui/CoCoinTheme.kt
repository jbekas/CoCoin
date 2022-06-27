package com.jbekas.cocoin.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val cocoin_caption = Color.DarkGray
val cocoin_divider_color = Color.LightGray
private val cocoin_red = Color(0xFFE30425)
private val cocoin_white = Color.White
private val cocoin_purple_700 = Color(0xFF720D5D)
private val cocoin_purple_800 = Color(0xFF5D1049)
private val cocoin_purple_900 = Color(0xFF4E0D3A)

val cocoinColors = lightColors(
    primary = cocoin_purple_800,
    secondary = cocoin_red,
    surface = cocoin_purple_900,
    onSurface = cocoin_white,
    primaryVariant = cocoin_purple_700
)

val BottomSheetShape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

@Composable
fun CoCoinTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = cocoinColors, typography = cocoinTypography) {
        content()
    }
}
