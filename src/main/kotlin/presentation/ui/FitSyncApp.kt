package org.damascus.presentation.ui

import org.damascus.presentation.io.Printer
import presentation.TerminalColor
import presentation.io.InputReader
import presentation.ui.ClothesSuggesterByCityNameCLI
import presentation.ui.UILauncher
import presentation.withStyle

class FitSyncApp(
    private val printer: Printer,
    private val inputReader: InputReader,
    private val clothesSuggesterByCityNameCLI: ClothesSuggesterByCityNameCLI,
    private val clothesSuggesterByIPCLI: ClothesSuggesterByIPCLI
) : UILauncher {

    override fun start() {
        showMenu(
            uiActionList = listOf(
                UiAction(
                    name = "🌆 Suggest Clothes Based on City",
                    action = { clothesSuggesterByCityNameCLI.start() }
                ),
                UiAction(
                    name = "🌍 Suggest Clothes Based on Your IP Location",
                    action = { clothesSuggesterByIPCLI.start() }
                ),
                UiAction(
                    name = "❌ Exit",
                    action = { exitApp() }
                )
            )
        )
    }

    private fun showMenu(uiActionList: List<UiAction>) {
        while (true) {

            printer.displayLn("=".repeat(40))
            printer.displayLn("\n👚 Welcome to FitSync App! 👕\n".withStyle(TerminalColor.Green))
            printer.displayLn("=".repeat(40))

            uiActionList.forEachIndexed { index, action ->
                val choice = (index + 1).toString().padStart(2, '0')
                printer.displayLn("${choice}. ${action.name}".withStyle(TerminalColor.entries.random()))
            }

            try {
                val input = inputReader.readInt(
                    prompt = "\n👉 Enter your choice (0 to Exit): ".withStyle(TerminalColor.Yellow),
                    min = 0,
                    max = uiActionList.size
                )

                if (input == 0) {
                    println("\n👋 Exiting... Stay fashionable!".withStyle(TerminalColor.Green))
                    return
                }

                printer.displayLn("\n✨ You selected: ${uiActionList[input - 1].name}".withStyle(TerminalColor.Cyan))
                uiActionList[input - 1].action()

            } catch (e: Exception) {
                printer.displayLn("⚠️ ${e.message}".withStyle(TerminalColor.Red))
            }

            printer.displayLn("\n🔄 Press Enter to return to menu...".withStyle(TerminalColor.Reset))
            readlnOrNull()
        }
    }

    private fun exitApp() {
        println("\n👋 Exiting FitSyncApp... See you next time!".withStyle(TerminalColor.Green))
    }
}
