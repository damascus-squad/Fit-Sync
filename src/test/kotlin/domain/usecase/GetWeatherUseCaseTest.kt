package domain.usecase

import kotlinx.coroutines.test.runTest
import org.damascus.domain.exception.LocationNotFoundException
import org.damascus.domain.model.Location
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.damascus.domain.repository.WeatherRepository
import org.damascus.domain.usecase.GetWeatherUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetWeatherUseCaseTest {

    private lateinit var weatherRepository: WeatherRepository
    private lateinit var weatherUseCase: GetWeatherUseCase

    @BeforeEach
    fun setup() {
        weatherRepository = FakeWeatherRepository()
        weatherUseCase = GetWeatherUseCase(weatherRepository)
    }

    @Test
    fun `should return weather info for given location`() = runTest {
        // Given
        val location = Location(
            name = "Cairo",
            region = "Cairo",
            country = "EG",
            latitude = 30.0,
            longitude = 31.0,
        )
        // When
        val result = weatherUseCase(location)

        // Then
        assertEquals(
            expected = location,
            actual = Location(
                latitude = result.latitude,
                longitude = result.longitude,
                name = "Cairo",
                region = "Cairo",
                country = "EG",
            ),
        )
    }

    @Test
    fun `should throw exception when location not found`() = runTest {
        // Given
        val unknownLocation = Location(
            name = "UnknownCity",
            region = "",
            country = "",
            latitude = 0.0,
            longitude = 0.0
        )
        // When & Then
        assertFailsWith<LocationNotFoundException> {
            weatherUseCase(unknownLocation)
        }
    }
}

class FakeWeatherRepository(
    private val returnError: Boolean = false
) : WeatherRepository {

    override suspend fun getWeatherByCity(location: Location): WeatherInfo {
        if (returnError || location.name == "UnknownCity") {
            throw LocationNotFoundException("not found city")
        }

        return WeatherInfo(
            latitude = 30.0,
            longitude = 31.0,
            elevation = 10.0,
            timezone = "Africa/Cairo",
            weather = Weather(
                temperature = 26.0,
                windSpeed = 12.0,
                windDirection = 90,
                isDay = true,
                weatherCode = 0,
                time = "2025-05-05T12:00"
            ),
            units = WeatherUnit(
                temperatureUnit = "°C",
                windSpeedUnit = "km/h",
                windDirectionUnit = "°"
            )
        )
    }

    override suspend fun getWeatherByIp(): WeatherInfo {
        if (returnError) {
            throw LocationNotFoundException("IP location not found")
        }

        return WeatherInfo(
            latitude = 10.0,
            longitude = 20.0,
            elevation = 5.0,
            timezone = "UTC",
            weather = Weather(
                temperature = 22.0,
                windSpeed = 10.0,
                windDirection = 180,
                isDay = false,
                weatherCode = 1,
                time = "2025-05-11T06:00"
            ),
            units = WeatherUnit(
                temperatureUnit = "°C",
                windSpeedUnit = "km/h",
                windDirectionUnit = "°"
            )
        )
    }
}