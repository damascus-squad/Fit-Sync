package domain.usecase

import org.damascus.domain.usecase.GetWeatherBySearchUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.damascus.data.weather.dto.LocationDto
import org.damascus.domain.exception.LocationNotFoundException
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.damascus.domain.repository.WeatherRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetWeatherBySearchUseCaseTest {

    private lateinit var weatherRepository: WeatherRepository
    private lateinit var getWeatherBySearchUseCase: GetWeatherBySearchUseCase

    @BeforeTest
    fun setup() {
        weatherRepository = mockk()
        getWeatherBySearchUseCase = GetWeatherBySearchUseCase(weatherRepository)
    }

    @Test
    fun `should returns weather info for given location`() = runTest {
        // Given
        val location = LocationDto(
            name = "Cairo",
            latitude = 30.0444,
            longitude = 31.2357
        )
        val expected = dummyWeatherInfo()
        coEvery { weatherRepository.getWeatherBySearch(location) } returns expected

        // When
        val result = getWeatherBySearchUseCase(location)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `should throws exception when fails`() = runTest {
        // Given
        val location = LocationDto(name = "UnknownCity", latitude = 0.0, longitude = 0.0)
        val exception = LocationNotFoundException("not found city")

        coEvery { weatherRepository.getWeatherBySearch(location) } throws exception

        // When + Then
        assertFailsWith<LocationNotFoundException> {
            getWeatherBySearchUseCase(location)
        }
    }

    private fun dummyWeatherInfo() = WeatherInfo(
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
