package presentation.ui

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.damascus.data.weather.dto.LocationDto
import org.damascus.domain.exception.LocationNotFoundException
import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.usecase.GetWeatherBySearchUseCase
import org.damascus.domain.usecase.SearchCityUseCase
import org.damascus.domain.usecase.SuggestClothesUseCase
import org.damascus.presentation.io.ConsoleDisplay
import org.damascus.presentation.ui.ClothesSuggesterByCitySearchCli
import presentation.io.InputReader
import kotlin.test.BeforeTest
import kotlin.test.Test

class ClothesSuggesterByCitySearchCliTest {

    private lateinit var printer: ConsoleDisplay
    private lateinit var inputReader: InputReader
    private lateinit var searchCityUseCase: SearchCityUseCase
    private lateinit var getWeatherBySearchUseCase: GetWeatherBySearchUseCase
    private lateinit var suggestClothesUseCase: SuggestClothesUseCase
    private lateinit var cli: ClothesSuggesterByCitySearchCli

    private val testCity = "Cairo"

    @BeforeTest
    fun setup() {
        printer = mockk(relaxed = true)
        inputReader = mockk()
        searchCityUseCase = mockk()
        getWeatherBySearchUseCase = mockk()
        suggestClothesUseCase = mockk()

        cli = ClothesSuggesterByCitySearchCli(
            printer,
            inputReader,
            searchCityUseCase,
            getWeatherBySearchUseCase,
            suggestClothesUseCase
        )
    }

    @Test
    fun `should suggest clothes for a valid city with single match`() = runTest {
        // Given
        val location = dummyLocation(name = testCity)
        val weatherInfo = mockk<WeatherInfo>(relaxed = true)
        val clothes = listOf(dummyCloth("T-shirt"))

        every { inputReader.readString(any()) } returns testCity
        coEvery { searchCityUseCase(testCity) } returns listOf(location)
        coEvery { getWeatherBySearchUseCase(location) } returns weatherInfo
        coEvery { suggestClothesUseCase(weatherInfo) } returns clothes

        // When
        cli.start()

        // Then
        verify { printer.displayLn(match { it is String && it.contains("T-shirt") }) }
    }

    @Test
    fun `should return multiple cities and choose second`() = runTest {
        // Given
        val location1 = dummyLocation(name = testCity, region = "Region1")
        val location2 = dummyLocation(name = testCity, region = "Region2")
        val weatherInfo = mockk<WeatherInfo>(relaxed = true)
        val clothes = listOf(dummyCloth("Jacket", ClothType.HEAVY))

        every { inputReader.readString(any()) } returns testCity
        coEvery { searchCityUseCase(testCity) } returns listOf(location1, location2)
        every { inputReader.readInt(any(), min = 1, max = 2) } returns 2
        coEvery { getWeatherBySearchUseCase(location2) } returns weatherInfo
        coEvery { suggestClothesUseCase(weatherInfo) } returns clothes

        // When
        cli.start()

        // Then
        verify { printer.displayLn(match { it is String && it.contains("Jacket") }) }
    }

    @Test
    fun `should return warning when no cities found`() = runTest {
        // Given
        every { inputReader.readString(any()) } returns testCity
        coEvery { searchCityUseCase(testCity) } returns emptyList()

        // When
        cli.start()

        // Then
        verify { printer.displayLn(match { it is String && it.contains("No matching locations") }) }
    }

    @Test
    fun `should theow exception when city not found`() = runTest {
        // Given
        val location = dummyLocation(name = testCity)

        every { inputReader.readString(any()) } returns testCity
        coEvery { searchCityUseCase(testCity) } returns listOf(location)
        coEvery { getWeatherBySearchUseCase(location) } throws LocationNotFoundException("Not found")

        // When
        cli.start()

        // Then
        verify { printer.displayLn(match { it is String && it.contains("Location not found") }) }
    }

    @Test
    fun `should return unexpected exception when weather retrieval`() = runTest {
        // Given
        val location = dummyLocation(name = testCity)

        every { inputReader.readString(any()) } returns testCity
        coEvery { searchCityUseCase(testCity) } returns listOf(location)
        coEvery { getWeatherBySearchUseCase(location) } throws RuntimeException("Server down")

        // When
        cli.start()

        // Then
        verify { printer.displayLn(match { it is String && it.contains("Unexpected error") }) }
    }

    @Test
    fun `should return invalid input when reject input`() = runTest {
        // Given
        every { inputReader.readString(any()) } returnsMany listOf("123", "", testCity)
        coEvery { searchCityUseCase(testCity) } returns emptyList()

        // When
        cli.start()

        // Then
        verify { printer.displayLn(match { it is String && it.contains("Invalid input") }) }
    }

    private fun dummyLocation(
        name: String = "City",
        lat: Double = 30.0,
        lon: Double = 31.0,
        country: String = "Country",
        region: String = "Region"
    ) = LocationDto(name = name, latitude = lat, longitude = lon, country = country, region = region)

    private fun dummyCloth(name: String = "Shirt", type: ClothType = ClothType.LIGHT) =
        Cloth(name = name, type = type)
}