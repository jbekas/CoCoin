package com.redgecko.expensetracker.ui.compose.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val expense_tracker_caption = Color.DarkGray
val expense_tracker_divider_color = Color.LightGray
private val expense_tracker_red = Color(0xFFE30425)
private val expense_tracker_white = Color.White
private val expense_tracker_purple_700 = Color(0xFF720D5D)
private val expense_tracker_purple_800 = Color(0xFF5D1049)
private val expense_tracker_purple_900 = Color(0xFF4E0D3A)

val expenseTrackerColors = lightColors(
    primary = expense_tracker_purple_800,
    secondary = expense_tracker_red,
    surface = expense_tracker_purple_900,
    onSurface = expense_tracker_white,
    primaryVariant = expense_tracker_purple_700
)

val BottomSheetShape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

@Composable
fun ExpenseTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = expenseTrackerColors, typography = expenseTrackerTypography) {
        content()
    }
}
