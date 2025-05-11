package data.location.repository

import kotlinx.coroutines.test.runTest
import org.damascus.data.location.repository.LocationRepositoryImpl
import org.damascus.data.weather.dto.LocationDto
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

class LocationRepositoryImplTest {

    private lateinit var locationRepository: LocationRepositoryImpl

    @BeforeEach
    fun setup() {
        val fakeDataSource = FakeLocationDataSource()
        locationRepository = LocationRepositoryImpl(fakeDataSource)
    }

    @Test
    fun `should return matching city`() = runTest {
        // given
        val city = "Cairo"

        // when
        val result = locationRepository.searchCity(city)

        // then
        assertEquals("Cairo", result.first().name)
    }

    @Test
    fun `should return empty list when city not found`() = runTest {
        // given
        val city = "UnknownCity"

        // when
        val result = locationRepository.searchCity(city)

        // then
        assertEquals(emptyList(), result)
    }

}

class FakeLocationDataSource : org.damascus.data.location.dataSource.LocationDataSource {

    private val fakeLocations = listOf(
        LocationDto(name = "Cairo", latitude = 30.0, longitude = 31.0, country = "EG", region = "Cairo"),
        LocationDto(name = "Alexandria", latitude = 31.2, longitude = 29.9, country = "EG", region = "Alex"),
        LocationDto(name = "Paris", latitude = 48.8, longitude = 2.3, country = "FR", region = "Ile-de-France")
    )

    override suspend fun searchCity(city: String): List<LocationDto> {
        return fakeLocations.filter { it.name.equals(city, ignoreCase = true) }
    }

    override suspend fun getCityCoordinates(city: String, country: String): LocationDto? {
        return fakeLocations.find {
            it.name.equals(city, ignoreCase = true) &&
                    it.country.equals(country, ignoreCase = true)
        }
    }

    override suspend fun getCurrentLocation() = null
}