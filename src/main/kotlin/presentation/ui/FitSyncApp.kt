package org.damascus.presentation.ui

import org.damascus.presentation.io.ConsoleDisplay
import presentation.io.InputReader
import presentation.ui.ClothesSuggesterByCityNameCli
import presentation.ui.UILauncher
import presentation.utils.TerminalColor
import presentation.utils.withStyle

class FitSyncApp(
    private val printer: ConsoleDisplay,
    private val inputReader: InputReader,
    private val clothesSuggesterByCityNameCli: ClothesSuggesterByCityNameCli,
    private val clothesSuggesterByIpCli: ClothesSuggesterByIpCli
) : UILauncher {

    override suspend fun start() {
        showMenu(
            uiActionList = listOf(
                UiAction(
                    name = "🌆 Suggest Clothes Based on City",
                    action = { clothesSuggesterByCityNameCli.start() }
                ),
                UiAction(
                    name = "🌍 Suggest Clothes Based on Your IP Location",
                    action = { clothesSuggesterByIpCli.start() }
                ),
                UiAction(
                    name = "❌ Exit",
                    action = { exitApp() }
                )
            )
        )
    }

    private suspend fun showMenu(uiActionList: List<UiAction>) {
        while (true) {

            printer.displayLn("=".repeat(35).withStyle(TerminalColor.Blue))
            printer.displayLn("\n👚 Welcome to FitSync App! 👕\n".withStyle(TerminalColor.Green))
            printer.displayLn("=".repeat(35).withStyle(TerminalColor.Blue))

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
