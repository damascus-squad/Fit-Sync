package domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.damascus.data.location.repository.LocationRepository
import org.damascus.data.weather.dto.LocationDto
import org.damascus.domain.usecase.SearchCityUseCase
import kotlin.collections.emptyList
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchCityUseCaseTest {

    private lateinit var locationRepository: LocationRepository
    private lateinit var searchCityUseCase: SearchCityUseCase

    @BeforeTest
    fun setup() {
        locationRepository = mockk()
        searchCityUseCase = SearchCityUseCase(locationRepository)
    }

    @Test
    fun `should returns list of locations`() = runTest {
        // Given
        val city = "Paris"
        val expected = listOf(LocationDto(name = "Paris", latitude = 48.8, longitude = 2.3))
        coEvery { locationRepository.searchCity(city) } returns expected

        // When
        val result = searchCityUseCase(city)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `should returns empty list when no match`() = runTest {
        // Given
        val city = "UnknownCity"
        coEvery { locationRepository.searchCity(city) } returns emptyList()

        // When
        val result = searchCityUseCase(city)

        // Then
        assertEquals(emptyList(), result)
    }
}