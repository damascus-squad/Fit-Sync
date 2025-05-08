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
