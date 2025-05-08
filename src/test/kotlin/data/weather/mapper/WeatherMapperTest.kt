package data.weather.mapper

import com.google.common.truth.Truth.assertThat
import org.damascus.data.weather.dto.CurrentWeather
import org.damascus.data.weather.dto.CurrentWeatherUnits
import org.damascus.data.weather.dto.WeatherDto
import org.damascus.data.weather.mapper.getWeatherDescription
import org.damascus.data.weather.mapper.toWeatherInfo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class WeatherMapperTest {

    @Test
    fun `should map valid WeatherDto with non-blank timezone and units`() {
        val dto = WeatherDto(
            latitude = 31.5,
            longitude = 34.5,
            elevation = 45.0,
            timezone = "Asia/Gaza",
            generationTimeMs = 0.0,
            utcOffsetSeconds = 10800,
            timezoneAbbreviation = "GMT+3",
            currentWeather = CurrentWeather(
                time = "2025-05-08T12:00",
                interval = 900,
                temperature = 25.0,
                windSpeed = 10.0,
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
                weatherCode = "wmo"
            )
        )

        val info = dto.toWeatherInfo()

        assertThat(info.timezone).isEqualTo("Asia/Gaza")
        assertThat(info.weather.isDay).isTrue()
        assertThat(info.units.temperatureUnit).isEqualTo("°C")
    }

    @Test
    fun `should default to GMT when timezone is blank`() {
        val dto = createDto(timezone = "")
        val info = dto.toWeatherInfo()
        assertThat(info.timezone).isEqualTo("GMT")
    }

    @Test
    fun `should default to °C when temperature unit is blank`() {
        val dto = createDto(temperatureUnit = "")
        val info = dto.toWeatherInfo()
        assertThat(info.units.temperatureUnit).isEqualTo("°C")
    }

    @Test
    fun `should return false for isDay when value is 0`() {
        val dto = createDto(isDay = 0)
        val info = dto.toWeatherInfo()
        assertThat(info.weather.isDay).isFalse()
    }

    @Test
    fun `should return empty string when currentWeather time is blank`() {
        val dto = createDto(time = "")
        val info = dto.toWeatherInfo()
        assertThat(info.weather.time).isEqualTo("")
    }

    private fun createDto(
        timezone: String = "Asia/Gaza",
        temperatureUnit: String = "°C",
        isDay: Int = 1,
        time: String = "2025-05-08T12:00"
    ): WeatherDto {
        return WeatherDto(
            latitude = 31.5,
            longitude = 34.5,
            elevation = 45.0,
            timezone = timezone,
            generationTimeMs = 0.0,
            utcOffsetSeconds = 10800,
            timezoneAbbreviation = "GMT+3",
            currentWeather = CurrentWeather(
                time = time,
                interval = 900,
                temperature = 25.0,
                windSpeed = 10.0,
                windDirection = 180,
                isDay = isDay,
                weatherCode = 1
            ),
            currentWeatherUnits = CurrentWeatherUnits(
                time = "iso8601",
                interval = "seconds",
                temperature = temperatureUnit,
                windSpeed = "km/h",
                windDirection = "°",
                isDay = "",
                weatherCode = "wmo"
            )
        )
    }

    @ParameterizedTest
    @CsvSource(
        "0, Clear sky",
        "1, Mainly clear",
        "2, Partly cloudy",
        "3, Overcast",
        "45, Fog",
        "48, Depositing rime fog",
        "51, Light drizzle",
        "53, Moderate drizzle",
        "55, Heavy drizzle",
        "56, Light freezing drizzle",
        "57, Heavy freezing drizzle",
        "61, Light rain",
        "63, Moderate rain",
        "65, Heavy rain",
        "66, Light freezing rain",
        "67, Heavy freezing rain",
        "71, Light snow",
        "73, Moderate snow",
        "75, Heavy snow",
        "77, Snow grains",
        "80, Light rain showers",
        "81, Moderate rain showers",
        "82, Heavy rain showers",
        "85, Light snow showers",
        "86, Heavy snow showers",
        "95, Thunderstorm",
        "96, Thunderstorm with light hail",
        "99, Thunderstorm with heavy hail",
        "999, Unknown weather"
    )
    fun `should return correct weather description for each code`(code: Int, expected: String) {
        val description = getWeatherDescription(code)
        assertThat(description).isEqualTo(expected)
    }
}
