package presentation.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.damascus.domain.exception.LocationNotFoundException
import org.damascus.domain.model.Cloth
import org.damascus.domain.usecase.GetWeatherUseCase
import org.damascus.domain.usecase.SuggestClothesUseCase
import org.damascus.presentation.io.ConsoleDisplay
import org.damascus.presentation.utils.showLoading
import presentation.io.InputReader
import presentation.utils.TerminalColor
import presentation.utils.withStyle

class ClothesSuggesterByCityNameCli(
    private val printer: ConsoleDisplay,
    private val inputReader: InputReader,
    private val getWeatherUseCase: GetWeatherUseCase,
    private val suggestClothesUseCase: SuggestClothesUseCase,
) : UILauncher {

    private val loadingScope = CoroutineScope(Dispatchers.Default)
    override suspend fun start() {
        val cityName = readValidInput("🌆 What's your city? ")
        val countryName = readValidInput("🌍 Which country? ")

        showLoading(loadingScope, printer)

        suggestClothesByCityAndCountry(
            cityName,
            countryName,
            onSuccess = { clothes ->
                printer.display("\r".withStyle(TerminalColor.Reset))
                showClothingSuggestions(clothes)
                loadingScope.cancel()
            },
            onFailure = { message ->
                printer.display("\r".withStyle(TerminalColor.Reset))
                printer.displayLn("⚠️ Error: $message".withStyle(TerminalColor.Red))
                loadingScope.cancel()
            }
        )
    }

    private suspend fun suggestClothesByCityAndCountry(
        cityName: String,
        countryName: String,
        onSuccess: (clothes: List<Cloth>) -> Unit,
        onFailure: (message: String) -> Unit
    ) {
        try {

            val dailyWeather = getWeatherUseCase(cityName, countryName)

            val clothes = suggestClothesUseCase(dailyWeather)
            onSuccess(clothes)
        } catch (exception: Exception) {
            val message = when (exception) {
                is LocationNotFoundException -> "Location not found. Please check the spelling."
                else -> "Unexpected error: ${exception.message}"
            }

            onFailure(message.withStyle(TerminalColor.Red))
        }
    }

    private fun showClothingSuggestions(clothes: List<Cloth>) {
        printer.displayLn("\n👚 Based on our high-tech fashion sensors, we suggest:\n".withStyle(TerminalColor.Green))

        clothes.forEachIndexed { index, cloth ->
            printer.displayLn("${index + 1}. ${cloth.name}".withStyle(TerminalColor.Cyan))
        }

        printer.displayLn("\n🕺 Go rule the streets. Or at least your hallway.".withStyle(TerminalColor.Magenta))
    }

    private fun readValidInput(prompt: String): String {
        while (true) {
            val input = inputReader.readString(prompt)

            if (!isValidName(input)) {
                printer.displayLn("Invalid input! Please enter letters only.".withStyle(TerminalColor.Red))
            } else {
                return input
            }
        }
    }

    private fun isValidName(input: String): Boolean {
        return input.matches(Regex("^[a-zA-Z\\s'-]{2,}$"))
    }
}
