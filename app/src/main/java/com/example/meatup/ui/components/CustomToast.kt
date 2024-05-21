package com.example.meatup.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup

@Composable
fun CustomToast(message: String, onDismiss: () -> Unit) {
    Popup(alignment = Alignment.TopCenter, onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.padding(16.dp),
            color = Color.Black,
            shape = MaterialTheme.shapes.small,
            tonalElevation = 8.dp
        ) {
            Text(
                text = message,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
