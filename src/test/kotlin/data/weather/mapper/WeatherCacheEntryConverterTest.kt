package org.damascus.data.weather.mapper

import com.google.common.truth.Truth.assertThat
import org.damascus.data.weather.dto.CurrentWeather
import org.damascus.data.weather.dto.CurrentWeatherUnits
import org.damascus.data.weather.dto.WeatherCacheCsvEntry
import org.damascus.data.weather.dto.WeatherDto
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class WeatherCacheEntryConverterTest {

 private lateinit var converter: WeatherCacheEntryConverter

 @BeforeEach
 fun setUp() {
  converter = WeatherCacheEntryConverter()
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
  currentWeatherUnits = CurrentWeatherUnits(
   time = "iso8601",
   interval = "seconds",
   temperature = "°C",
   windSpeed = "m/s",
   windDirection = "°",
   isDay = "1/0",
   weatherCode = "wmo code"
  ),
  currentWeather = CurrentWeather(
   time = "2023-10-28T12:00:00Z",
   interval = 0,
   temperature = 22.0,
   windSpeed = 5.0,
   windDirection = 90,
   isDay = 1,
   weatherCode = 801
  )
 )

 private val testWeatherDtoNight = testWeatherDto.copy(
  currentWeather = testWeatherDto.currentWeather.copy(isDay = 0)
 )

 private val testWeatherDtoBlankFields = WeatherDto(
  latitude = 30.0, longitude = 40.0, generationTimeMs = 0.0, utcOffsetSeconds = 0,
  timezone = "", timezoneAbbreviation = "", elevation = 100.0,
  currentWeatherUnits = CurrentWeatherUnits("", "", "", "", "", "", ""),
  currentWeather = CurrentWeather("", 0, 0.0, 0.0, 0, 0, 0)
 )

 private val testTimestampString = "2023-01-01T12:00:00"
 private val testCacheEntry = WeatherCacheCsvEntry(
  latitude = 10.0, longitude = 20.0, elevation = 5.0, timezone = "Europe/Berlin",
  temperature = 25.5, windSpeed = 15.2, windDirection = 180, isDay = true, weatherCode = 800,
  time = "2023-10-28T10:00:00Z", temperatureUnit = "°C", windSpeedUnit = "km/h",
  windDirectionUnit = "°", timestamp = testTimestampString
 )

 @Test
 fun `weatherInfoToEntry - converts correctly and generates timestamp`() {
  val result = converter.weatherInfoToEntry(testWeatherInfo)

  val expectedEntry = WeatherCacheCsvEntry(
   latitude = testWeatherInfo.latitude,
   longitude = testWeatherInfo.longitude,
   elevation = testWeatherInfo.elevation,
   timezone = testWeatherInfo.timezone,
   temperature = testWeatherInfo.weather.temperature,
   windSpeed = testWeatherInfo.weather.windSpeed,
   windDirection = testWeatherInfo.weather.windDirection,
   isDay = testWeatherInfo.weather.isDay,
   weatherCode = testWeatherInfo.weather.weatherCode,
   time = testWeatherInfo.weather.time,
   temperatureUnit = testWeatherInfo.units.temperatureUnit,
   windSpeedUnit = testWeatherInfo.units.windSpeedUnit,
   windDirectionUnit = testWeatherInfo.units.windDirectionUnit,
   timestamp = result.timestamp
  )
  assertThat(result).isEqualTo(expectedEntry)

  assertThat(result.timestamp).isNotEmpty()

  try {
   LocalDateTime.parse(result.timestamp, DateTimeFormatter.ISO_DATE_TIME)
  } catch (e: DateTimeParseException) {
   throw AssertionError("Generated timestamp is not in ISO_DATE_TIME format: ${result.timestamp}", e)
  }
 }

 @Test
 fun `weatherInfoToEntry - uses provided timestamp`() {
  val specificTimestamp = "2024-01-01T00:00:00"
  val result = converter.weatherInfoToEntry(testWeatherInfo, specificTimestamp)
  val expectedEntry = WeatherCacheCsvEntry(
   latitude = testWeatherInfo.latitude,
   longitude = testWeatherInfo.longitude,
   elevation = testWeatherInfo.elevation,
   timezone = testWeatherInfo.timezone,
   temperature = testWeatherInfo.weather.temperature,
   windSpeed = testWeatherInfo.weather.windSpeed,
   windDirection = testWeatherInfo.weather.windDirection,
   isDay = testWeatherInfo.weather.isDay,
   weatherCode = testWeatherInfo.weather.weatherCode,
   time = testWeatherInfo.weather.time,
   temperatureUnit = testWeatherInfo.units.temperatureUnit,
   windSpeedUnit = testWeatherInfo.units.windSpeedUnit,
   windDirectionUnit = testWeatherInfo.units.windDirectionUnit,
   timestamp = specificTimestamp
  )
  assertThat(result).isEqualTo(expectedEntry)
 }

 @Test
 fun `entryToWeatherInfo - converts correctly`() {
  val result = converter.entryToWeatherInfo(testCacheEntry)

  val expectedWeatherInfo = WeatherInfo(
   latitude = testCacheEntry.latitude,
   longitude = testCacheEntry.longitude,
   elevation = testCacheEntry.elevation,
   timezone = testCacheEntry.timezone,
   weather = Weather(
    temperature = testCacheEntry.temperature,
    windSpeed = testCacheEntry.windSpeed,
    windDirection = testCacheEntry.windDirection,
    isDay = testCacheEntry.isDay,
    weatherCode = testCacheEntry.weatherCode,
    time = testCacheEntry.time
   ),
   units = WeatherUnit(
    temperatureUnit = testCacheEntry.temperatureUnit,
    windSpeedUnit = testCacheEntry.windSpeedUnit,
    windDirectionUnit = testCacheEntry.windDirectionUnit
   )
  )
  assertThat(result).isEqualTo(expectedWeatherInfo)
 }

 @Test
 fun `dtoToWeatherInfo - converts correctly for day`() {
  val result = converter.dtoToWeatherInfo(testWeatherDto)

  val expectedWeatherInfo = WeatherInfo(
   latitude = testWeatherDto.latitude,
   longitude = testWeatherDto.longitude,
   elevation = testWeatherDto.elevation,
   timezone = testWeatherDto.timezone,
   weather = Weather(
    temperature = testWeatherDto.currentWeather.temperature,
    windSpeed = testWeatherDto.currentWeather.windSpeed,
    windDirection = testWeatherDto.currentWeather.windDirection,
    isDay = true,
    weatherCode = testWeatherDto.currentWeather.weatherCode,
    time = testWeatherDto.currentWeather.time
   ),
   units = WeatherUnit(
    temperatureUnit = testWeatherDto.currentWeatherUnits.temperature,
    windSpeedUnit = testWeatherDto.currentWeatherUnits.windSpeed,
    windDirectionUnit = testWeatherDto.currentWeatherUnits.windDirection
   )
  )
  assertThat(result).isEqualTo(expectedWeatherInfo)
 }

 @Test
 fun `dtoToWeatherInfo - converts correctly for night`() {
  val result = converter.dtoToWeatherInfo(testWeatherDtoNight)
  val expectedWeatherInfo = WeatherInfo(
   latitude = testWeatherDtoNight.latitude,
   longitude = testWeatherDtoNight.longitude,
   elevation = testWeatherDtoNight.elevation,
   timezone = testWeatherDtoNight.timezone,
   weather = Weather(
    temperature = testWeatherDtoNight.currentWeather.temperature,
    windSpeed = testWeatherDtoNight.currentWeather.windSpeed,
    windDirection = testWeatherDtoNight.currentWeather.windDirection,
    isDay = false,
    weatherCode = testWeatherDtoNight.currentWeather.weatherCode,
    time = testWeatherDtoNight.currentWeather.time
   ),
   units = WeatherUnit(
    temperatureUnit = testWeatherDtoNight.currentWeatherUnits.temperature,
    windSpeedUnit = testWeatherDtoNight.currentWeatherUnits.windSpeed,
    windDirectionUnit = testWeatherDtoNight.currentWeatherUnits.windDirection
   )
  )
  assertThat(result).isEqualTo(expectedWeatherInfo)
 }

 @Test
 fun `dtoToWeatherInfo - handles blank fields with defaults`() {
  val result = converter.dtoToWeatherInfo(testWeatherDtoBlankFields)

  val expectedWeatherInfo = WeatherInfo(
   latitude = testWeatherDtoBlankFields.latitude,
   longitude = testWeatherDtoBlankFields.longitude,
   elevation = testWeatherDtoBlankFields.elevation,
   timezone = "GMT",
   weather = Weather(
    temperature = 0.0,
    windSpeed = 0.0,
    windDirection = 0,
    isDay = false,
    weatherCode = 0,
    time = ""
   ),
   units = WeatherUnit(
    temperatureUnit = "°C",
    windSpeedUnit = "km/h",
    windDirectionUnit = "°"
   )
  )
  assertThat(result).isEqualTo(expectedWeatherInfo)
 }

 @Test
 fun `weatherInfoToDto - converts correctly`() {
  val result = converter.weatherInfoToDto(testWeatherInfo)

  val expectedDto = WeatherDto(
   latitude = testWeatherInfo.latitude,
   longitude = testWeatherInfo.longitude,
   elevation = testWeatherInfo.elevation,
   timezone = testWeatherInfo.timezone,
   generationTimeMs = result.generationTimeMs,
   timezoneAbbreviation = "Europe/Berlin",
   utcOffsetSeconds = result.utcOffsetSeconds,
   currentWeatherUnits = CurrentWeatherUnits(
    time = "iso8601",
    interval = "seconds",
    temperature = testWeatherInfo.units.temperatureUnit,
    windSpeed = testWeatherInfo.units.windSpeedUnit,
    windDirection = testWeatherInfo.units.windDirectionUnit,
    isDay = "1/0",
    weatherCode = "wmo code"
   ),
   currentWeather = CurrentWeather(
    time = testWeatherInfo.weather.time,
    interval = 0,
    temperature = testWeatherInfo.weather.temperature,
    windSpeed = testWeatherInfo.weather.windSpeed,
    windDirection = testWeatherInfo.weather.windDirection,
    isDay = 1,
    weatherCode = testWeatherInfo.weather.weatherCode
   )
  )

  assertThat(result).isEqualTo(expectedDto)
 }

 @Test
 fun `weatherInfoToDto - handles invalid timezone in WeatherInfo with UTC default for abbreviation and offset`() {
  val infoWithInvalidTimezone = testWeatherInfo.copy(timezone = "Invalid/Timezone")
  val result = converter.weatherInfoToDto(infoWithInvalidTimezone)

  val expectedDto = WeatherDto(
   latitude = infoWithInvalidTimezone.latitude,
   longitude = infoWithInvalidTimezone.longitude,
   elevation = infoWithInvalidTimezone.elevation,
   timezone = "Invalid/Timezone",
   generationTimeMs = result.generationTimeMs,
   timezoneAbbreviation = "UTC",
   utcOffsetSeconds = 0,
   currentWeatherUnits = CurrentWeatherUnits(
    time = "iso8601",
    interval = "seconds",
    temperature = infoWithInvalidTimezone.units.temperatureUnit,
    windSpeed = infoWithInvalidTimezone.units.windSpeedUnit,
    windDirection = infoWithInvalidTimezone.units.windDirectionUnit,
    isDay = "1/0",
    weatherCode = "wmo code"
   ),
   currentWeather = CurrentWeather(
    time = infoWithInvalidTimezone.weather.time,
    interval = 0,
    temperature = infoWithInvalidTimezone.weather.temperature,
    windSpeed = infoWithInvalidTimezone.weather.windSpeed,
    windDirection = infoWithInvalidTimezone.weather.windDirection,
    isDay = 1,
    weatherCode = infoWithInvalidTimezone.weather.weatherCode
   )
  )
  assertThat(result).isEqualTo(expectedDto)
  assertThat(result.generationTimeMs).isGreaterThan(0.0)
 }

 @Test
 fun `entryToCsvRow - converts correctly to string array`() {
  val result = converter.entryToCsvRow(testCacheEntry)
  val expectedArray = arrayOf(
   "10.0", "20.0", "5.0", "Europe/Berlin", "25.5", "15.2", "180", "true", "800",
   "2023-10-28T10:00:00Z", "°C", "km/h", "°", testTimestampString
  )
  assertThat(result.size).isEqualTo(WeatherCacheCsvEntry.HEADERS.size)
  assertThat(result).isEqualTo(expectedArray)
 }

 @Test
 fun `csvRowToEntry - converts valid row correctly`() {
  val validRow = arrayOf(
   "10.5", "20.5", "5.5", "America/New_York", "26.5", "16.2", "190", "false", "802",
   "2023-10-29T11:00:00Z", "°F", "mph", "deg", "2023-10-29T11:05:00"
  )
  val result = converter.csvRowToEntry(validRow)

  val expectedEntry = WeatherCacheCsvEntry(
   latitude = 10.5,
   longitude = 20.5,
   elevation = 5.5,
   timezone = "America/New_York",
   temperature = 26.5,
   windSpeed = 16.2,
   windDirection = 190,
   isDay = false,
   weatherCode = 802,
   time = "2023-10-29T11:00:00Z",
   temperatureUnit = "°F",
   windSpeedUnit = "mph",
   windDirectionUnit = "deg",
   timestamp = "2023-10-29T11:05:00"
  )
  assertThat(result).isEqualTo(expectedEntry)
 }

 @Test
 fun `csvRowToEntry - returns null for row with too few columns`() {
  val shortRow = arrayOf("10.0", "20.0")
  val result = converter.csvRowToEntry(shortRow)
  assertThat(result).isNull()
 }

 @Test
 fun `csvRowToEntry - returns null for row with non-numeric latitude`() {
  val badLatRow = arrayOf(
   "abc", "20.5", "5.5", "America/New_York", "26.5", "16.2", "190", "false", "802",
   "2023-10-29T11:00:00Z", "°F", "mph", "deg", "2023-10-29T11:05:00"
  )
  val result = converter.csvRowToEntry(badLatRow)
  assertThat(result).isNull()
 }
}