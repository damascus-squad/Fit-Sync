package data.weather.mapper

import com.google.common.truth.Truth.assertThat
import org.damascus.data.weather.dto.CurrentWeatherDto
import org.damascus.data.weather.dto.CurrentWeatherUnitsDto
import org.damascus.data.weather.dto.CsvWeatherModel
import org.damascus.data.weather.dto.WeatherDto
import org.damascus.data.weather.mapper.WeatherDataConverter
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class WeatherDataConverterTest {

    private lateinit var converter: WeatherDataConverter

    @BeforeEach
    fun setUp() {
        converter = WeatherDataConverter()
    }

    private val testWeatherInfo = WeatherInfo(
        latitude = 10.0,
        longitude = 20.0,
        elevation = 5.0,
        timezone = "Europe/Berlin",
        weather = Weather(
            temperature = 25.5,
            windSpeed = 15.2,
            windDirection = 180,
            isDay = true,
            weatherCode = 800,
            time = "2023-10-28T10:00:00Z"
        ),
        units = WeatherUnit(
            temperatureUnit = "°C",
            windSpeedUnit = "km/h",
            windDirectionUnit = "°"
        )
    )

    private val testWeatherDto = WeatherDto(
        latitude = 30.0,
        longitude = 40.0,
        generationTimeMs = 1672531200000.0,
        utcOffsetSeconds = 7200,
        timezone = "Africa/Cairo",
        timezoneAbbreviation = "EET",
        elevation = 100.0,
        currentWeatherUnitsDto = CurrentWeatherUnitsDto(
            time = "iso8601",
            interval = "seconds",
            temperature = "°C",
            windSpeed = "m/s",
            windDirection = "°",
            isDay = "1/0",
            weatherCode = "wmo code"
        ),
        currentWeatherDto = CurrentWeatherDto(
            time = "2023-10-28T12:00:00Z",
            interval = 0,
            temperature = 22.0,
            windSpeed = 5.0,
            windDirection = 90,
            isDay = 1,
            weatherCode = 801
        )
    )

    private val testWeatherDtoBlankFields = WeatherDto(
        latitude = 30.0,
        longitude = 40.0,
        generationTimeMs = 0.0,
        utcOffsetSeconds = 0,
        timezone = "",
        timezoneAbbreviation = "",
        elevation = 100.0,
        currentWeatherUnitsDto = CurrentWeatherUnitsDto("", "", "", "", "", "", ""),
        currentWeatherDto = CurrentWeatherDto("", 0, 0.0, 0.0, 0, 0, 0)
    )

    private val testTimestampString = "2023-01-01T12:00:00"
    private val testCacheEntry = CsvWeatherModel(
        latitude = 10.0,
        longitude = 20.0,
        elevation = 5.0,
        timezone = "Europe/Berlin",
        temperature = 25.5,
        windSpeed = 15.2,
        windDirection = 180,
        isDay = true,
        weatherCode = 800,
        time = "2023-10-28T10:00:00Z",
        temperatureUnit = "°C",
        windSpeedUnit = "km/h",
        windDirectionUnit = "°",
        timestamp = testTimestampString
    )

    @Test
    fun `weatherInfoToEntry - converts correctly and generates timestamp`() {
        val result = converter.weatherInfoToEntry(testWeatherInfo)

        val expected = result.copy(timestamp = result.timestamp)
        assertThat(result).isEqualTo(expected)

        // Check timestamp format
        try {
            LocalDateTime.parse(result.timestamp, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: DateTimeParseException) {
            throw AssertionError("Timestamp format is invalid: ${result.timestamp}", e)
        }
    }

    @Test
    fun `weatherInfoToEntry - uses provided timestamp`() {
        val timestamp = "2024-01-01T00:00:00"
        val result = converter.weatherInfoToEntry(testWeatherInfo, timestamp)

        assertThat(result.timestamp).isEqualTo(timestamp)
    }

    @Test
    fun `entryToWeatherInfo - converts correctly`() {
        val result = converter.entryToWeatherInfo(testCacheEntry)
        assertThat(result).isEqualTo(testWeatherInfo)
    }

    @Test
    fun `dtoToWeatherInfo - converts correctly`() {
        val result = converter.dtoToWeatherInfo(testWeatherDto)
        assertThat(result.latitude).isEqualTo(testWeatherDto.latitude)
        assertThat(result.weather.isDay).isTrue()
        assertThat(result.units.temperatureUnit).isEqualTo("°C")
    }

    @Test
    fun `dtoToWeatherInfo - blank fields fallback to defaults`() {
        val result = converter.dtoToWeatherInfo(testWeatherDtoBlankFields)
        assertThat(result.timezone).isEqualTo("GMT")
        assertThat(result.units.temperatureUnit).isEqualTo("°C")
    }

    @Test
    fun `weatherInfoToDto - converts correctly`() {
        val result = converter.weatherInfoToDto(testWeatherInfo)
        assertThat(result.latitude).isEqualTo(testWeatherInfo.latitude)
        assertThat(result.currentWeatherDto.isDay).isEqualTo(1)
    }

    @Test
    fun `entryToCsvRow and csvRowToEntry - work correctly`() {
        val csvRow = converter.entryToCsvRow(testCacheEntry)
        val result = converter.csvRowToEntry(csvRow)
        assertThat(result).isEqualTo(testCacheEntry)
    }

    @Test
    fun `csvRowToEntry - invalid row returns null`() {
        val badRow = arrayOf("not_a_number", "20.0")
        val result = converter.csvRowToEntry(badRow)
        assertThat(result).isNull()
    }
}
