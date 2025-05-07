package domain.usecase

import org.damascus.data.clothes.datasource.ClothesDataSourceImp
import org.damascus.data.clothes.repository.ClothesRepositoryImpl
import org.damascus.data.weather.dto.CurrentWeather
import org.damascus.domain.usecase.SuggestClothesUseCase

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class SuggestClothesUseCaseTest {

    private lateinit var suggestClothesUseCase: SuggestClothesUseCase

    @BeforeEach
    fun setup() {
        val clothesDataSource = ClothesDataSourceImp()
        val clothesRepository = ClothesRepositoryImpl(clothesDataSource)
        suggestClothesUseCase = SuggestClothesUseCase(clothesRepository)
    }

    @Test
    fun `getClothesByType should return true when weather gives VERY_HEAVY clothes`() {
        // Given
        val input = CurrentWeather("2025-01-01T08:00", 1, -5.0, 10.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type.name == "VERY_HEAVY" })
    }

    @Test
    fun `getClothesByType should return true when weather gives HEAVY clothes`() {
        // Given
        val input = CurrentWeather("2025-01-01T08:00", 1, 5.0, 5.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type.name == "HEAVY" })
    }

    @Test
    fun `getClothesByType should return true when weather gives MEDIUM clothes`() {
        // Given
        val input = CurrentWeather("2025-04-01T12:00", 1, 15.0, 3.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type.name == "MEDIUM" })
    }

    @Test
    fun `getClothesByType should return true when weather gives LIGHT clothes`() {
        // Given
        val input = CurrentWeather("2025-05-01T15:00", 1, 24.0, 2.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type.name == "LIGHT" })
    }

    @Test
    fun `getClothesByType should return true when weather gives VERY_LIGHT clothes`() {
        // Given
        val input = CurrentWeather("2025-07-01T14:00", 1, 33.0, 1.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type.name == "VERY_LIGHT" })
    }

    @Test
    fun `getClothesByType should return true when exactly 0 degrees gives HEAVY clothes`() {
        // Given
        val input = CurrentWeather("2025-01-01T08:00", 1, 0.0, 8.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type.name == "HEAVY" })
    }

    @Test
    fun `getClothesByType should return true when exactly 10 degrees gives MEDIUM clothes`() {
        // Given
        val input = CurrentWeather("2025-02-01T09:00", 1, 10.0, 6.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.all { it.type.name == "MEDIUM" })
    }

    @Test
    fun `getClothesByType should return true when missing temperature returns empty list`() {
        // Given
        val input = CurrentWeather("2025-05-01T15:00", 1, Double.NaN, 2.0, 180, 1, 1)

        // When
        val result = suggestClothesUseCase(input)

        // Then
        assertTrue(result.isEmpty())
    }


}