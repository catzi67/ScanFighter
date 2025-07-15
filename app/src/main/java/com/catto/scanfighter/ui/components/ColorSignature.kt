package com.catto.scanfighter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun ColorSignature(
    colors: List<Color>,
    onColorBarClick: (Int) -> Unit
) {
    var lastPlayedIndex by remember { mutableStateOf(-1) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .pointerInput(Unit) {
                forEachGesture {
                    awaitPointerEventScope {
                        // Wait for the user to press down.
                        val down = awaitFirstDown()

                        // Calculate which bar was pressed and play the note.
                        val index = (down.position.x / size.width * colors.size).toInt().coerceIn(0, colors.lastIndex)
                        if (index != lastPlayedIndex) {
                            onColorBarClick(index)
                            lastPlayedIndex = index
                        }

                        // Continuously process drag events after the initial press.
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                // Calculate which bar the finger is currently over.
                                val currentIndex = (change.position.x / size.width * colors.size).toInt().coerceIn(0, colors.lastIndex)
                                // If it's a new bar, play the new note.
                                if (currentIndex != lastPlayedIndex) {
                                    onColorBarClick(currentIndex)
                                    lastPlayedIndex = currentIndex
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        // Reset the last played index when the finger is lifted.
                        lastPlayedIndex = -1
                    }
                }
            }
    ) {
        colors.forEachIndexed { index, color ->
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color)
            )
            if (index < colors.size - 1) {
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}
