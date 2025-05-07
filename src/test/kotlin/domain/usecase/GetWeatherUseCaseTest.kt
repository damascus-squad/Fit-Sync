package domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.damascus.domain.exception.LocationNotFoundException
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
    private lateinit var repository: WeatherRepository
    private lateinit var useCase: GetWeatherUseCase

    @BeforeEach
    fun setup() {
        repository = mockk()
        useCase = GetWeatherUseCase(repository)
    }

    @Test
    fun `should return WeatherInfo when enter city and country`() = runTest {
        //given
        val expected = dummyWeatherInfo()
        coEvery { repository.getWeatherByCity("Cairo", "Egypt") } returns expected

        //when
        val result = useCase("Cairo", "Egypt")

        //then
        assertEquals(expected, result)
    }

    @Test
    fun `should return correct temperature when enter city and country`() = runTest {
        //given
        val expected = dummyWeatherInfo()
        coEvery { repository.getWeatherByCity("Caro", "Egypt") } returns expected

        //when
        val result = useCase("Caro", "Egypt")

        //then
        assertEquals(26.0, result.weather.temperature)
    }

    @Test
    fun `should return throws exception when fail`() = runTest {
        coEvery { repository.getWeatherByCity("Unknown", "Unknown") } throws LocationNotFoundException("City not found")

        assertFailsWith<LocationNotFoundException> {
            useCase("Unknown", "Unknown")
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