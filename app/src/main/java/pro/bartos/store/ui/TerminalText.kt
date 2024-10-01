package pro.bartos.store.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TerminalText(modifier: Modifier = Modifier, text: String, index: Int, charAnimationDelay: Long = 25L) {
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(index) {
        displayedText = ""
        text.forEachIndexed { _, char ->
            delay(charAnimationDelay)
            displayedText += char
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = displayedText,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = Color.Green,
            )
        )
    }
}

@Preview
@Composable
private fun TerminalTextPreview() {
    TerminalText(text = "testing long example text", index = 0)
}