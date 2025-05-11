package presentation.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.damascus.domain.exception.LocationNotFoundException
import org.damascus.domain.model.Cloth
import org.damascus.domain.model.Location
import org.damascus.domain.usecase.GetWeatherUseCase
import org.damascus.domain.usecase.SearchCityUseCase
import org.damascus.domain.usecase.SuggestClothesUseCase
import org.damascus.presentation.io.ConsoleDisplay
import org.damascus.presentation.utils.showLoading
import presentation.io.InputReader
import presentation.utils.TerminalColor
import presentation.utils.withStyle

class ClothesSuggesterByCityNameCli(
    private val printer: ConsoleDisplay,
    private val inputReader: InputReader,
    private val searchCityUseCase: SearchCityUseCase,
    private val getWeatherByCityUseCase: GetWeatherUseCase,
    private val suggestClothesUseCase: SuggestClothesUseCase
) : UILauncher {

    private val loadingScope = CoroutineScope(Dispatchers.Default)

    override suspend fun start() {
        val cityName = inputReader.readString("🌆 What's your city? ")

        val matchingLocations = searchCityUseCase(cityName)

        if (matchingLocations.isEmpty()) {
            printer.displayLn("⚠️ No matching locations found.".withStyle(TerminalColor.Red))
            return
        }

        val selectedLocation = if (matchingLocations.size == 1) {
            matchingLocations.first()
        } else {
            printer.displayLn("🔍 Multiple matches found. Please choose one:")
            matchingLocations.forEachIndexed { index, location ->
                printer.displayLn(
                    "${index + 1}. ${location.name} - ${location.region} - ${location.country}".withStyle(
                        TerminalColor.Cyan
                    )
                )
            }

            val selectedIndex = inputReader.readInt(
                prompt = "👉 Enter the number of your location: ".withStyle(TerminalColor.Yellow),
                min = 1,
                max = matchingLocations.size
            )

            matchingLocations[selectedIndex - 1]
        }

        showLoading(loadingScope, printer)

        suggestClothesByLocation(
            selectedLocation,
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

    private suspend fun suggestClothesByLocation(
        location: Location,
        onSuccess: (clothes: List<Cloth>) -> Unit,
        onFailure: (message: String) -> Unit
    ){
        try {
            val dailyWeather = getWeatherByCityUseCase(location)
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
}
