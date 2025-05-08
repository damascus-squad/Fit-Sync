package presentation.ui

import kotlinx.coroutines.*
import org.damascus.domain.model.Cloth
import org.damascus.domain.usecase.GetWeatherUseCase
import org.damascus.presentation.io.Printer
import presentation.TerminalColor
import presentation.io.InputReader
import presentation.withStyle

class ClothesSuggesterByCityNameCLI(
    private val printer: Printer,
    private val inputReader: InputReader,
    private val getWeatherUseCase: GetWeatherUseCase,
    private val suggestClothesUseCase: SuggestClothesUseCase
) : UILauncher {

    private var suggestedClothes: List<Cloth>? = null
    private lateinit var loadingScope: CoroutineScope

    override fun start() {
        val cityName = inputReader.readString("🌆 What's your city? ")
        val countryName = inputReader.readString("🌍 Which country? ")

        loadingScope = CoroutineScope(Dispatchers.Default)
        showLoading()

        runBlocking {
            suggestClothesByCityAndCountry(cityName, countryName)

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
                    printer.display("\rThinking about your style $frame ".withStyle(TerminalColor.Yellow))
                    delay(250)
                }
            }
        }
    }

    private suspend fun suggestClothesByCityAndCountry(cityName: String, countryName: String) {
        try {
            val dailyWeather = getWeatherUseCase(cityName, countryName)
            suggestedClothes = suggestClothesUseCase(dailyWeather)
        } catch (exception: Exception) {
            printer.display("\r".withStyle(TerminalColor.Reset))
            suggestedClothes = null
            printer.displayLn("⚠️ Error: ${exception.message}".withStyle(TerminalColor.Red))
        }
    }

    private fun showClothingSuggestions(clothes: List<Cloth>) {
        printer.displayLn("\n👚 Based on our high-tech fashion sensors, we suggest:\n".withStyle(TerminalColor.Green))

        clothes.forEachIndexed { index, cloth ->
            printer.displayLn("${index + 1}. ${cloth.name}".withStyle(TerminalColor.Cyan))
        }

        printer.displayLn("\n🕺 Go rule the streets. Or at least your hallway.".withStyle(TerminalColor.Magenta))
    }
}
