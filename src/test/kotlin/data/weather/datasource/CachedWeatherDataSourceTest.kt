package data.weather.datasource

import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.damascus.data.weather.datasource.CachedWeatherDataSource
import org.damascus.data.weather.datasource.WeatherCacheService
import org.damascus.data.weather.datasource.WeatherDataSource
import org.damascus.data.weather.dto.CurrentWeather
import org.damascus.data.weather.dto.CurrentWeatherUnits
import org.damascus.data.weather.dto.WeatherDto
import org.damascus.data.weather.mapper.WeatherCacheEntryConverter
import org.damascus.domain.model.LocationCoordinate
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CachedWeatherDataSourceTest {

 private lateinit var mockApiClient: WeatherDataSource
 private lateinit var mockCacheService: WeatherCacheService
 private lateinit var converter: WeatherCacheEntryConverter // Real instance for testing conversion logic

 private lateinit var cachedWeatherDataSource: CachedWeatherDataSource

 private val testLocation = LocationCoordinate(10.0, 20.0)
 private val cacheDuration = 30L

 private val weatherInfoFromCache = WeatherInfo(
  latitude = 10.0, longitude = 20.0, elevation = 5.0, timezone = "Cache/Zone",
  weather = Weather(
   temperature = 20.0,
   windSpeed = 10.0,
   windDirection = 90,
   isDay = true,
   weatherCode = 1,
   time = "cache_time"
  ),
  units = WeatherUnit(temperatureUnit = "C_cache", windSpeedUnit = "kmh_cache", windDirectionUnit = "deg_cache")
 )
 private val weatherDtoFromApi = WeatherDto(
  latitude = 10.0, longitude = 20.0, generationTimeMs = 1.0, utcOffsetSeconds = 3600, timezone = "Api/Zone",
  timezoneAbbreviation = "Api/Zone", elevation = 10.0,
  currentWeatherUnits = CurrentWeatherUnits("iso8601", "seconds", "C_api", "ms_api", "deg_api", "1/0", "wmo code"),
  currentWeather = CurrentWeather("api_time", 0, 25.0, 15.0, 180, 0, 2)
 )

 @BeforeEach
 fun setup() {
  mockApiClient = mockk()
  mockCacheService = mockk()
  converter = WeatherCacheEntryConverter()

  cachedWeatherDataSource = CachedWeatherDataSource(
   apiClient = mockApiClient,
   cacheService = mockCacheService,
   converter = converter,
   cacheDurationMinutes = cacheDuration
  )
 }

 @Test
 fun `getWeather when cache is valid should return data from cache and not call api`() {
  runTest {
   coEvery { mockCacheService.getValidFromCache(testLocation, cacheDuration) } returns weatherInfoFromCache

   val result: WeatherDto = cachedWeatherDataSource.getWeather(testLocation)

   val expectedDto = WeatherDto(
    latitude = weatherInfoFromCache.latitude,
    longitude = weatherInfoFromCache.longitude,
    elevation = weatherInfoFromCache.elevation,
    timezone = weatherInfoFromCache.timezone,
    generationTimeMs = result.generationTimeMs,
    utcOffsetSeconds = result.utcOffsetSeconds,
    timezoneAbbreviation = result.timezoneAbbreviation,
    currentWeatherUnits = CurrentWeatherUnits(
     time = "iso8601",
     interval = "seconds",
     temperature = weatherInfoFromCache.units.temperatureUnit,
     windSpeed = weatherInfoFromCache.units.windSpeedUnit,
     windDirection = weatherInfoFromCache.units.windDirectionUnit,
     isDay = "1/0",
     weatherCode = "wmo code"
    ),
    currentWeather = CurrentWeather(
     time = weatherInfoFromCache.weather.time,
     interval = 0,
     temperature = weatherInfoFromCache.weather.temperature,
     windSpeed = weatherInfoFromCache.weather.windSpeed,
     windDirection = weatherInfoFromCache.weather.windDirection,
     isDay = if (weatherInfoFromCache.weather.isDay) 1 else 0,
     weatherCode = weatherInfoFromCache.weather.weatherCode
    )
   )

   assertThat(result).isEqualTo(expectedDto)

   coVerify(exactly = 1) { mockCacheService.getValidFromCache(testLocation, cacheDuration) }
   coVerify(exactly = 0) { mockApiClient.getWeather(any()) }
   coVerify(exactly = 0) { mockCacheService.saveToCache(any()) }
  }
 }


// should delete this , no api testing ❌
 @Test
 fun `getWeather when cache is invalid should fetch from api save to cache and return api data`() {
  runTest {
   val expectedInfoToCache = converter.dtoToWeatherInfo(weatherDtoFromApi)
   val weatherInfoSlot = slot<WeatherInfo>()

   coEvery { mockCacheService.getValidFromCache(testLocation, cacheDuration) } returns null
   coEvery { mockApiClient.getWeather(testLocation) } returns weatherDtoFromApi
   coEvery { mockCacheService.saveToCache(capture(weatherInfoSlot)) } just runs

   val result = cachedWeatherDataSource.getWeather(testLocation)

   assertThat(result).isEqualTo(weatherDtoFromApi)
   assertThat(weatherInfoSlot.captured).isEqualTo(expectedInfoToCache)

   coVerify(exactly = 1) { mockCacheService.getValidFromCache(testLocation, cacheDuration) }
   coVerify(exactly = 1) { mockApiClient.getWeather(testLocation) }
   coVerify(exactly = 1) { mockCacheService.saveToCache(expectedInfoToCache) }
  }
 }
}