package pro.bartos.store.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pro.bartos.store.cli.CliOperation

@Composable
fun OperationButton(modifier: Modifier = Modifier, text: String, selectedOperation: CliOperation?, onClick: () -> Unit) {
    val isSelected = selectedOperation?.message == text

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(50.dp)
            .border(2.dp, Color.Green, RectangleShape)
            .clickable { onClick() }
            .background(if (isSelected) Color.Green else Color.Transparent)
            .padding(horizontal = 15.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isSelected) Color.Black else Color.Green,
            fontFamily = FontFamily.Monospace,
        )
    }
}