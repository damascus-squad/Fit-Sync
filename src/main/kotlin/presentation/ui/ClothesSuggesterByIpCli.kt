package org.damascus.presentation.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.damascus.domain.model.Cloth
import org.damascus.domain.usecase.GetWeatherByIpUseCase
import org.damascus.domain.usecase.SuggestClothesUseCase
import org.damascus.presentation.io.ConsoleDisplay
import org.damascus.presentation.utils.showLoading
import presentation.ui.UILauncher
import presentation.utils.TerminalColor
import presentation.utils.withStyle

class ClothesSuggesterByIpCli(
    private val printer: ConsoleDisplay,
    private val getWeatherByIpUseCase: GetWeatherByIpUseCase,
    private val suggestClothesUseCase: SuggestClothesUseCase,
) : UILauncher {

    private val loadingScope = CoroutineScope(Dispatchers.Default)
    override suspend fun start() {
        showLoading(loadingScope, printer)

        suggestClothesByIP(
            onSuccess = { clothes ->
                printer.display("\r".withStyle(TerminalColor.Reset))
                showClothingSuggestions(clothes)
                loadingScope.cancel()
            },
            onFailure = { message ->
                printer.display("\r".withStyle(TerminalColor.Reset))
                printer.displayLn("❌ $message")
                loadingScope.cancel()
            }
        )
    }

    private suspend fun suggestClothesByIP(
        onSuccess: (clothes: List<Cloth>) -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        try {
            val dailyWeather = getWeatherByIpUseCase()
            val clothes = suggestClothesUseCase(dailyWeather)
            onSuccess(clothes)
        } catch (exception: Exception) {
            onFailure("${exception.message} An unexpected error occurred.".withStyle(TerminalColor.Red))
        }
    }

    private fun showClothingSuggestions(clothes: List<Cloth>) {
        printer.displayLn("\n👚 Based on our high-tech fashion sensors, we suggest:\n")

        clothes.forEachIndexed { index, cloth ->
            printer.displayLn("${index + 1}. ${cloth.name}")
        }

        printer.displayLn("\n🕺 Go rule the streets. Or at least your hallway.")
    }


}