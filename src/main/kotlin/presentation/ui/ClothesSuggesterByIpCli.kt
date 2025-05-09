package org.damascus.presentation.ui

import kotlinx.coroutines.*
import org.damascus.domain.model.Cloth
import org.damascus.domain.usecase.GetWeatherByIpUseCase
import org.damascus.domain.usecase.SuggestClothesUseCase
import org.damascus.presentation.io.ConsoleDisplay
import presentation.TerminalColor
import presentation.ui.UILauncher
import presentation.withStyle

class ClothesSuggesterByIpCli(
    private val printer: ConsoleDisplay,
    private val getWeatherByIpUseCase: GetWeatherByIpUseCase,
    private val suggestClothesUseCase: SuggestClothesUseCase,
) : UILauncher {

    private var suggestedClothes: List<Cloth>? = null
    private val loadingScope = CoroutineScope(Dispatchers.Default)
    override suspend fun start() {
        showLoading()

        suggestClothesByIP(
            onSuccess = { clothes ->
                suggestedClothes = clothes
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

    private fun showLoading() {
        loadingScope.launch {
            val frames = listOf("👗", "🧥", "🧦", "👕", "🧤", "🧣")
            while (isActive) {
                for (frame in frames) {
                    printer.display("\rThinking about your style $frame ")
                    delay(250)
                }
            }
        }
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