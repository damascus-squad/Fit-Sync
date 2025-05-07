package org.damascus.data.weather.mapper

import com.google.common.truth.Truth.assertThat
import org.damascus.data.weather.dto.CurrentWeather
import org.damascus.data.weather.dto.CurrentWeatherUnits
import org.damascus.data.weather.dto.WeatherDto
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
}
