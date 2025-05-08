package data.weather.mapper

import com.google.common.truth.Truth.assertThat
import org.damascus.data.weather.dto.CurrentWeather
import org.damascus.data.weather.dto.CurrentWeatherUnits
import org.damascus.data.weather.dto.WeatherDto
import org.damascus.data.weather.mapper.getWeatherDescription
import org.damascus.data.weather.mapper.toWeatherInfo
import org.junit.jupiter.api.Test

class WeatherMapperTest {

    @Test
    fun `should map WeatherDto to WeatherInfo correctly when dto is valid`() {
        val dto = WeatherDto(
            latitude = 31.95,
            longitude = 35.9,
            elevation = 750.0,
            timezone = "Asia/Amman",
            generationTimeMs = 0.0,
            utcOffsetSeconds = 10800,
            timezoneAbbreviation = "GMT+3",
            currentWeather = CurrentWeather(
                time = "2025-05-07T12:00",
                interval = 900,
                temperature = 28.5,
                windSpeed = 12.0,
                windDirection = 180,
                isDay = 1,
                weatherCode = 1
            ),
            currentWeatherUnits = CurrentWeatherUnits(
                time = "iso8601",
                interval = "seconds",
                temperature = "°C",
                windSpeed = "km/h",
                windDirection = "°",
                isDay = "",
                weatherCode = "wmo code"
            )
        )

        val weatherInfo = dto.toWeatherInfo()

        assertThat(weatherInfo.latitude).isEqualTo(31.95)
        assertThat(weatherInfo.weather.temperature).isEqualTo(28.5)
        assertThat(weatherInfo.weather.isDay).isTrue()
        assertThat(weatherInfo.units.temperatureUnit).isEqualTo("°C")
    }

    @Test
    fun `should return clear sky description when weather code is 0`() {
        val description = getWeatherDescription(0)
        assertThat(description).isEqualTo("Clear sky")
    }

    @Test
    fun `should return unknown weather description when weather code is not recognized`() {
        val description = getWeatherDescription(999)
        assertThat(description).isEqualTo("Unknown weather")
    }
    @Test
    fun `should return correct descriptions for all known weather codes`() {
        val cases = mapOf(
            0 to "Clear sky",
            1 to "Mainly clear",
            2 to "Partly cloudy",
            3 to "Overcast",
            45 to "Fog",
            48 to "Depositing rime fog",
            51 to "Light drizzle",
            53 to "Moderate drizzle",
            55 to "Heavy drizzle",
            56 to "Light freezing drizzle",
            57 to "Heavy freezing drizzle",
            61 to "Light rain",
            63 to "Moderate rain",
            65 to "Heavy rain",
            66 to "Light freezing rain",
            67 to "Heavy freezing rain",
            71 to "Light snow",
            73 to "Moderate snow",
            75 to "Heavy snow",
            77 to "Snow grains",
            80 to "Light rain showers",
            81 to "Moderate rain showers",
            82 to "Heavy rain showers",
            85 to "Light snow showers",
            86 to "Heavy snow showers",
            95 to "Thunderstorm",
            96 to "Thunderstorm with light hail",
            99 to "Thunderstorm with heavy hail"
        )

        for ((code, expected) in cases) {
            val result = getWeatherDescription(code)
            assertThat(result).isEqualTo(expected)
        }
    }

}
