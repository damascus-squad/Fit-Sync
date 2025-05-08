package data.weather.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.damascus.data.weather.datasource.WeatherDataSource
import org.damascus.data.weather.dto.CurrentWeather
import org.damascus.data.weather.dto.CurrentWeatherUnits
import org.damascus.data.weather.dto.WeatherDto
import org.damascus.data.weather.mapper.toWeatherInfo
import org.damascus.data.weather.repository.WeatherRepositoryImp
import org.damascus.domain.exception.LocationNotFoundException
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WeatherRepositoryImpTest {

    private lateinit var weatherDataSource: WeatherDataSource
    private lateinit var weatherRepository: WeatherRepositoryImp

    @BeforeEach
    fun setup() {
        weatherDataSource = mockk(relaxed = true)
        weatherRepository = WeatherRepositoryImp(weatherDataSource)
    }

    @Test
    fun `should return weather info by city when data source succeeds`() = runTest {
        // Given
        val dto = dummyWeatherDto()
        coEvery { weatherDataSource.getWeatherByCity("Cairo", "EG") } returns dto

        // When
        val result = weatherRepository.getWeatherByCity("Cairo", "EG")

        // Then
        assertThat(result).isEqualTo(dto.toWeatherInfo())
    }


    @Test
    fun `should throw exception when data source fails`() = runTest {
        // Given
        coEvery {
            weatherDataSource.getWeatherByCity(
                "Unknown",
                "Unknown"
            )
        } throws LocationNotFoundException("City not found")

        // When & Then
        assertThrows<LocationNotFoundException> {
            weatherRepository.getWeatherByCity("Unknown", "Unknown")
        }
    }

    @Test
    fun `should return weather info by ip when data source succeeds`() = runTest {
        // Given
        val dto = dummyWeatherDto()
        coEvery { weatherDataSource.getWeatherByIp() } returns dto

        // When
        val result = weatherRepository.getWeatherByIp()

        // Then
        assertThat(result).isEqualTo(dto.toWeatherInfo())
    }


    @Test
    fun `getWeatherByIp should throw exception when data source fails`() = runTest {
        // Given
        coEvery { weatherDataSource.getWeatherByIp() } throws LocationNotFoundException("IP failed")

        // When & Then
        assertThrows<LocationNotFoundException> {
            weatherRepository.getWeatherByIp()
        }
    }

    @Test
    fun `should return default weather info if current weather is missing`() = runTest {
        // Given
        val incompleteDto = dummyWeatherDto().copy(
            timezone = "",
            currentWeather = CurrentWeather(
                temperature = 0.0,
                windSpeed = 0.0,
                time = "",
                interval = 1,
                windDirection = 0,
                isDay = 0,
                weatherCode = -1
            ),
            currentWeatherUnits = dummyWeatherDto().currentWeatherUnits.copy(
                temperature = ""
            )
        )
        val expected = WeatherInfo(
            latitude = 30.0,
            longitude = 31.0,
            elevation = 10.0,
            timezone = "GMT",
            weather = Weather(
                temperature = 0.0,
                windSpeed = 0.0,
                windDirection = 0,
                isDay = false,
                weatherCode = -1,
                time = ""
            ),
            units = WeatherUnit(
                temperatureUnit = "°C",
                windSpeedUnit = "km/h",
                windDirectionUnit = "°"
            )
        )

        coEvery { weatherDataSource.getWeatherByCity("DesertCity", "EG") } returns incompleteDto

        // When
        val result = weatherRepository.getWeatherByCity("DesertCity", "EG")

        // Then
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `should map isDay to false when currentWeather isDay is 0`() = runTest {
        // Given
        val dto = dummyWeatherDto().copy(
            currentWeather = dummyWeatherDto().currentWeather.copy(isDay = 0)
        )

        coEvery { weatherDataSource.getWeatherByCity("NightCity", "EG") } returns dto

        // When
        val result = weatherRepository.getWeatherByCity("NightCity", "EG")

        // Then
        assertThat(result.weather.isDay).isFalse()
    }

    private fun dummyWeatherDto(): WeatherDto = WeatherDto(
        latitude = 30.0,
        longitude = 31.0,
        generationTimeMs = 12.3,
        utcOffsetSeconds = 7200,
        timezone = "Africa/Cairo",
        timezoneAbbreviation = "EET",
        elevation = 10.0,
        currentWeatherUnits = CurrentWeatherUnits(
            time = "iso8601",
            interval = "int",
            temperature = "°C",
            windSpeed = "km/h",
            windDirection = "°",
            isDay = "bool",
            weatherCode = "int"
        ),
        currentWeather = CurrentWeather(
            temperature = 26.0,
            windSpeed = 12.0,
            time = "2025-05-05T12:00",
            interval = 1,
            windDirection = 90,
            isDay = 1,
            weatherCode = 0,
        )
    )
}