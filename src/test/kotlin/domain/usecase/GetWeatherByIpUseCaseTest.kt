package domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import org.damascus.domain.repository.WeatherRepository
import org.damascus.domain.usecase.GetWeatherByIpUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest
import org.damascus.domain.exception.LocationNotFoundException
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetWeatherByIpUseCaseTest {

    private lateinit var repository: WeatherRepository
    private lateinit var useCase: GetWeatherByIpUseCase

    @BeforeEach
    fun setup() {
        repository = mockk()
        useCase = GetWeatherByIpUseCase(repository)
    }

    @Test
    fun `should return weather info from IP location`() = runTest {
        // Given
        val expected = dummyWeatherInfo()
        coEvery { repository.getWeatherByIp() } returns expected

        // When
        val result = useCase()

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `should return correct temperature from IP location`() = runTest {
        // Given
        val expected = dummyWeatherInfo()
        coEvery { repository.getWeatherByIp() } returns expected

        // When
        val result = useCase()

        // Then
        assertEquals(26.0, result.weather.temperature)
    }

    @Test
    fun `should return throws exception when fail`() = runTest {
        coEvery { repository.getWeatherByIp() } throws LocationNotFoundException("Could not determine location from IP")

        assertFailsWith<LocationNotFoundException> {
            useCase()
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

