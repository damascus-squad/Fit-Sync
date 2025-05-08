package org.damascus.presentation.ui

import kotlinx.coroutines.*
import org.damascus.domain.model.Cloth
import org.damascus.presentation.io.Printer
import presentation.ui.UILauncher

class ClothesSuggesterByIPCLI(
    private val printer: Printer,
    private val getWeatherByIpUseCase: GetWeatherByIpUseCase,
    private val suggestClothesUseCase: SuggestClothesUseCase
) : UILauncher {

    private var suggestedClothes: List<Cloth>? = null
    private lateinit var loadingScope: CoroutineScope


    override fun start() {

        loadingScope = CoroutineScope(Dispatchers.Default)
        showLoading()

        runBlocking {
            suggestClothesByIP()

            suggestedClothes?.let { cloths ->
                showClothingSuggestions(cloths)
            }

            loadingScope.cancel()
        }
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

    private suspend fun suggestClothesByIP() {
        try {
            val dailyWeather = getWeatherByIpUseCase()
            suggestedClothes = suggestClothesUseCase(dailyWeather)
        } catch (exception: Exception) {
            printer.display("\r")
            suggestedClothes = null
            printer.displayLn(exception.message)
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