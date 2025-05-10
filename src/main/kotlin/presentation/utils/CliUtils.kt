package org.damascus.presentation.utils

import kotlinx.coroutines.*
import org.damascus.presentation.io.ConsoleDisplay
import presentation.utils.TerminalColor
import presentation.utils.withStyle


fun showLoading(
    scope: CoroutineScope,
    printer: ConsoleDisplay,
    color: TerminalColor = TerminalColor.Yellow
): Job {
    val frames = listOf("👗", "🧥", "🧦", "👕", "🧤", "🧣")

    return scope.launch {
        while (isActive) {
            for (frame in frames) {
                printer.display("\rThinking about your style $frame ".withStyle(color))
                delay(250)
            }
        }
    }

}