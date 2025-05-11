package domain.usecase

import kotlinx.coroutines.test.runTest
import org.damascus.data.location.repository.LocationRepository
import org.damascus.domain.model.Location
import org.damascus.domain.usecase.SearchCityUseCase
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchCityUseCaseTest {

    private lateinit var searchCityUseCase: SearchCityUseCase

    @BeforeEach
    fun setup() {
        val fakeRepo = FakeLocationRepository()
        searchCityUseCase = SearchCityUseCase(fakeRepo)
    }

    @Test
    fun `should return location when enter city`() = runTest {
        // Given
        val city = "Cairo"
        // When
        val result = searchCityUseCase(city)
        // Then
        assertEquals("Cairo", result[0].name)
    }

    @Test
    fun `should return multiple city when enter city have more than one similarity`() = runTest {
        // Given
        val city = "wast"
        // When
        val result = searchCityUseCase(city)
        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `should return empty list when enter unknown city`() = runTest {
        // Given
        val city = "Atlantis"
        // When
        val result = searchCityUseCase(city)
        // Then
        assertEquals(emptyList(), result)
    }

}

class FakeLocationRepository(
    private val cities: Map<String, List<Location>> = defaultCities
) : LocationRepository {

    override suspend fun searchCity(city: String): List<Location> {
        return cities[city] ?: emptyList()
    }

    companion object {
        val defaultCities = mapOf(
            "Cairo" to listOf(
                Location(
                    name = "Cairo",
                    region = "Cairo",
                    country = "EG",
                    latitude = 30.0444,
                    longitude = 31.2357
                )
            ),
            "Paris" to listOf(
                Location(
                    name = "Paris",
                    region = "Paris",
                    country = "France",
                    latitude = 48.8566,
                    longitude = 2.3522
                )
            ),
            "wast" to listOf(
                Location(
                    name = "wast",
                    region = "New York",
                    country = "USA",
                    latitude = 39.78,
                    longitude = -89.64
                ),
                Location(
                    name = "wast",
                    region = "Los Angeles",
                    country = "USA",
                    latitude = 37.20,
                    longitude = -93.29
                )
            )
        )
    }
}
