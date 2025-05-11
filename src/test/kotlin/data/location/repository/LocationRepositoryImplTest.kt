package data.location.repository

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.location.repository.LocationRepositoryImpl
import org.damascus.data.weather.dto.LocationDto
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LocationRepositoryImplTest {

    private lateinit var locationDataSource: LocationDataSource
    private lateinit var locationRepository: LocationRepositoryImpl

    @BeforeTest
    fun setup() {
        locationDataSource = mockk()
        locationRepository = LocationRepositoryImpl(locationDataSource)
    }

    @Test
    fun `should returns list of locations`() = runTest {
        // Given
        val city = "Cairo"
        val expected = listOf(LocationDto(name = "Cairo", latitude = 30.0, longitude = 31.0))
        coEvery { locationDataSource.searchCity(city) } returns expected

        // When
        val result = locationRepository.searchCity(city)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `should returns empty list when no cities found`() = runTest {
        // Given
        val city = "UnknownCity"
        coEvery { locationDataSource.searchCity(city) } returns emptyList()

        // When
        val result = locationRepository.searchCity(city)

        // Then
        assertEquals(emptyList(), result)
    }
}