package domain.usecase

import io.mockk.every
import io.mockk.mockk
import org.damascus.data.clothes.datasource.ClothesDataSource
import org.damascus.data.clothes.repository.ClothesRepositoryImpl
import org.damascus.data.weather.dto.CurrentWeather
import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType
import org.damascus.domain.usecase.IllegalTemperatureException
import org.damascus.domain.usecase.SuggestClothesUseCase

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test

class SuggestClothesUseCaseTest {

    private lateinit var suggestClothesUseCase: SuggestClothesUseCase
    private var clothesDataSource: ClothesDataSource = mockk()

    @BeforeEach
    fun setup() {

        every { clothesDataSource.getClothesByType(any()) } answers {
            val type = firstArg<ClothType>()
            listOf(
                Cloth(name = "Mocked $type Cloth 1", type = type),
                Cloth(name = "Mocked $type Cloth 2", type = type),
            )
        }

        val clothesRepository = ClothesRepositoryImpl(clothesDataSource)
        suggestClothesUseCase = SuggestClothesUseCase(clothesRepository)
    }

    @ParameterizedTest
    @CsvSource("5.0", "0.0")
    fun `getClothesByType should return list of type VERY_HEAVY when temperature within very heavy range`(temperature: Double) {
        // Given
        val input = CurrentWeather("2025-01-01T08:00", 1, temperature, 10.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type == ClothType.VERY_HEAVY })
    }

    @ParameterizedTest
    @CsvSource("12.5", "8.0")
    fun `getClothesByType should return list of type HEAVY when temperature within heavy range`(temperature: Double) {
        // Given
        val input = CurrentWeather("2025-01-01T08:00", 1, temperature, 10.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type == ClothType.HEAVY })
    }

    @ParameterizedTest
    @CsvSource("20.0", "17.0")
    fun `getClothesByType should return list of type MEDIUM when temperature within medium range`(temperature: Double) {
        // Given
        val input = CurrentWeather("2025-01-01T08:00", 1, temperature, 10.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type == ClothType.MEDIUM })
    }

    @ParameterizedTest
    @CsvSource("28.0", "24.0")
    fun `getClothesByType should return list of type LIGHT when temperature within light range`(temperature: Double) {
        // Given
        val input = CurrentWeather("2025-01-01T08:00", 1, temperature, 10.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type == ClothType.LIGHT })
    }


    @Test
    fun `getClothesByType should return list of type VERY_LIGHT when temperature within very light range`() {
        // Given
        val input = CurrentWeather("2025-01-01T08:00", 1, 32.0, 10.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type == ClothType.VERY_LIGHT })
    }

    @Test
    fun `getClothesByType should throw exception when temperature is NAN`() {
        // Given
        val input = CurrentWeather("2025-01-01T08:00", 1, Double.NaN, 10.0, 180, 1, 1)

        // When & Then
        assertThrows<IllegalTemperatureException> {
            suggestClothesUseCase(input)
        }
    }


}