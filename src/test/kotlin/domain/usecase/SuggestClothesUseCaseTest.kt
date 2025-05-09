package domain.usecase

import io.mockk.every
import io.mockk.mockk
import org.checkerframework.checker.units.qual.Temperature
import org.damascus.data.clothes.datasource.ClothesDataSource
import org.damascus.data.clothes.datasource.ClothesDataSourceImp
import org.damascus.data.clothes.repository.ClothesRepositoryImpl
import org.damascus.data.weather.dto.CurrentWeather
import org.damascus.domain.exception.IllegalTemperatureException
import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.damascus.domain.usecase.SuggestClothesUseCase

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test

class SuggestClothesUseCaseTest {

    private lateinit var suggestClothesUseCase: SuggestClothesUseCase
    private lateinit var clothesDataSource: ClothesDataSourceImp
    private lateinit var clothesRepository: ClothesRepositoryImpl

    @BeforeEach
    fun setup() {
        clothesDataSource = mockk()
        clothesRepository = ClothesRepositoryImpl(clothesDataSource)

        every { clothesDataSource.getClothesByType(ClothType.VERY_HEAVY) } returns listOf(
            Cloth(name = "Winter Jacket", type = ClothType.VERY_HEAVY),
            Cloth(name = "Thick Sweater", type = ClothType.VERY_HEAVY)
        )

        every { clothesDataSource.getClothesByType(ClothType.HEAVY) } returns listOf(
            Cloth(name = "Light Jacket", type = ClothType.HEAVY),
            Cloth(name = "Sweater", type = ClothType.HEAVY)
        )

        every { clothesDataSource.getClothesByType(ClothType.MEDIUM) } returns listOf(
            Cloth(name = "Long Sleeve Shirt", type = ClothType.MEDIUM),
            Cloth(name = "Light Sweater", type = ClothType.MEDIUM)
        )

        every { clothesDataSource.getClothesByType(ClothType.LIGHT) } returns listOf(
            Cloth(name = "T-Shirt", type = ClothType.LIGHT),
            Cloth(name = "Short Sleeve Shirt", type = ClothType.LIGHT)
        )

        every { clothesDataSource.getClothesByType(ClothType.VERY_LIGHT) } returns listOf(
            Cloth(name = "Tank Top", type = ClothType.VERY_LIGHT),
            Cloth(name = "Light T-Shirt", type = ClothType.VERY_LIGHT)
        )

        suggestClothesUseCase = SuggestClothesUseCase(clothesRepository)
    }

    @ParameterizedTest
    @CsvSource(
        "5.0, C",
        "0.0, C",
        "41.0, F",
        "32.0, F"
    )
    fun `getClothesByType should return list of type VERY_HEAVY when temperature within very heavy range`(
        temperature: Double,
        temperatureUnit: String
    ) {
        // Given
        val input = helper(temperature, temperatureUnit)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type == ClothType.VERY_HEAVY })
    }

    @ParameterizedTest
    @CsvSource(
        "12.5, C",
        "8.0, C",
        "54.5, F",
        "46.4, F"
    )
    fun `getClothesByType should return list of type HEAVY when temperature within heavy range`(
        temperature: Double,
        temperatureUnit: String
    ) {
        // Given
        val input = helper(temperature, temperatureUnit)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type == ClothType.HEAVY })
    }

    @ParameterizedTest
    @CsvSource(
        "20.0, C",
        "17.0, C",
        "68.0, F",
        "62.6, F"
    )
    fun `getClothesByType should return list of type MEDIUM when temperature within medium range`(
        temperature: Double,
        temperatureUnit: String
    ) {
        // Given
        val input = helper(temperature, temperatureUnit)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type == ClothType.MEDIUM })
    }

    @ParameterizedTest
    @CsvSource(
        "28.0, C",
        "24.0, C",
        "82.4, F",
        "75.2, F"
    )
    fun `getClothesByType should return list of type LIGHT when temperature within light range`(
        temperature: Double,
        temperatureUnit: String
    ) {
        // Given
        val input = helper(temperature, temperatureUnit)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type == ClothType.LIGHT })
    }


    @Test
    fun `getClothesByType should return list of type VERY_LIGHT when temperature within very light range`() {
        // Given
        val input = helper(32.0, "C")

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type == ClothType.VERY_LIGHT })
    }

    @Test
    fun `getClothesByType should throw exception when temperature is NAN`() {
        // Given
        val input = helper(Double.NaN, "C")

        // When & Then
        assertThrows<IllegalTemperatureException> {
            suggestClothesUseCase(input)
        }
    }

}

fun helper(temperature: Double, temperatureUnit: String): WeatherInfo {
    return WeatherInfo(
        latitude = 1.1,
        longitude = 1.1,
        elevation = 1.1,
        timezone = "timezone1",
        weather = Weather(
            temperature = temperature,
            windSpeed = 1.1,
            windDirection = 1,
            isDay = true,
            weatherCode = 1,
            time = "time1"
        ),
        units = WeatherUnit(
            temperatureUnit = temperatureUnit,
            windSpeedUnit = "windSpeedUnit1",
            windDirectionUnit = "windDirectionUnit1"
        )
    )
}