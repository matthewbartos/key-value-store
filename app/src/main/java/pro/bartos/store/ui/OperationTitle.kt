package pro.bartos.store.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OperationTitle(text: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier
        .height(50.dp)
        .padding(horizontal = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color.Green,
            fontFamily = FontFamily.Monospace,
        )
    }
}
