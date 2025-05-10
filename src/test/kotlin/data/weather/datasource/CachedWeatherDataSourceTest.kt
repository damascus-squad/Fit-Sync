package data.weather.datasource

import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.location.dto.IpLocationDto
import org.damascus.data.weather.cache.io.WeatherCacheService
import org.damascus.data.weather.datasource.CachedWeatherDataSource
import org.damascus.data.weather.datasource.IpNotFoundException
import org.damascus.data.weather.datasource.WeatherDataSource
import org.damascus.data.weather.dto.*
import org.damascus.data.weather.mapper.WeatherDataConverter
import org.damascus.data.weather.datasource.LocationNotFoundException
import org.damascus.domain.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CachedWeatherDataSourceTest {

    private lateinit var mockApiClient: WeatherDataSource
    private lateinit var mockCacheService: WeatherCacheService
    private lateinit var mockLocationDataSource: LocationDataSource
    private lateinit var converter: WeatherDataConverter
    private lateinit var cachedWeatherDataSource: CachedWeatherDataSource

    private val testCity = "Cairo"
    private val testCountry = "EG"
    private val testCityLocation = LocationDto(10.0, 20.0)
    private val testIpLocation = IpLocationDto(10.0, 20.0) // Changed to IpLocationDto
    private val cacheDuration = 30L

    private val weatherInfoFromCache = WeatherInfo(
        latitude = 10.0,
        longitude = 20.0,
        elevation = 5.0,
        timezone = "Cache/Zone",
        weather = Weather(
            temperature = 20.0,
            windSpeed = 10.0,
            windDirection = 90,
            isDay = true,
            weatherCode = 1,
            time = "cache_time"
        ),
        units = WeatherUnit(
            temperatureUnit = "C_cache",
            windSpeedUnit = "kmh_cache",
            windDirectionUnit = "deg_cache"
        )
    )

    private val weatherDtoFromApi = WeatherDto(
        latitude = 10.0,
        longitude = 20.0,
        generationTimeMs = 1.0,
        utcOffsetSeconds = 3600,
        timezone = "Api/Zone",
        timezoneAbbreviation = "Api/Zone",
        elevation = 10.0,
        currentWeatherUnitsDto = CurrentWeatherUnitsDto(
            time = "iso8601",
            interval = "seconds",
            temperature = "C_api",
            windSpeed = "ms_api",
            windDirection = "deg_api",
            isDay = "1/0",
            weatherCode = "wmo code"
        ),
        currentWeatherDto = CurrentWeatherDto(
            time = "api_time",
            interval = 0,
            temperature = 25.0,
            windSpeed = 15.0,
            windDirection = 180,
            isDay = 0,
            weatherCode = 2
        )
    )

    @BeforeEach
    fun setup() {
        mockApiClient = mockk()
        mockCacheService = mockk()
        mockLocationDataSource = mockk()
        converter = WeatherDataConverter()

        cachedWeatherDataSource = CachedWeatherDataSource(
            apiClient = mockApiClient,
            cacheService = mockCacheService,
            converter = converter,
            locationDataSource = mockLocationDataSource,
            cacheDurationMinutes = cacheDuration
        )
    }

    @Test
    fun `getWeatherByCity when cache is valid should return data from cache`() = runTest {
        // Given
        val coordinate = LocationCoordinate(testCityLocation.latitude, testCityLocation.longitude)
        coEvery { mockLocationDataSource.getCityCoordinates(testCity, testCountry) } returns testCityLocation
        coEvery { mockCacheService.getValidFromCache(coordinate, cacheDuration) } returns weatherInfoFromCache

        // When
        val result = cachedWeatherDataSource.getWeatherByCity(testCity, testCountry)

        // Then
        val expectedDto = converter.weatherInfoToDto(weatherInfoFromCache)
        assertThat(result).isEqualTo(expectedDto)

        coVerify(exactly = 1) { mockLocationDataSource.getCityCoordinates(testCity, testCountry) }
        coVerify(exactly = 1) { mockCacheService.getValidFromCache(coordinate, cacheDuration) }
        coVerify(exactly = 0) { mockApiClient.getWeatherByCity(any(), any()) }
        coVerify(exactly = 0) { mockCacheService.saveToCache(any()) }
    }

    @Test
    fun `getWeatherByCity when cache is invalid should fetch from api and cache`() = runTest {
        // Given
        val coordinate = LocationCoordinate(testCityLocation.latitude, testCityLocation.longitude)
        val expectedInfoToCache = converter.dtoToWeatherInfo(weatherDtoFromApi)

        coEvery { mockLocationDataSource.getCityCoordinates(testCity, testCountry) } returns testCityLocation
        coEvery { mockCacheService.getValidFromCache(coordinate, cacheDuration) } returns null
        coEvery {
            mockApiClient.getWeatherByCity(
                testCityLocation.latitude.toString(),
                testCityLocation.longitude.toString()
            )
        } returns weatherDtoFromApi
        coEvery { mockCacheService.saveToCache(any()) } just runs

        // When
        val result = cachedWeatherDataSource.getWeatherByCity(testCity, testCountry)

        // Then
        assertThat(result).isEqualTo(weatherDtoFromApi)

        coVerify(exactly = 1) { mockLocationDataSource.getCityCoordinates(testCity, testCountry) }
        coVerify(exactly = 1) { mockCacheService.getValidFromCache(coordinate, cacheDuration) }
        coVerify(exactly = 1) {
            mockApiClient.getWeatherByCity(
                testCityLocation.latitude.toString(),
                testCityLocation.longitude.toString()
            )
        }
        coVerify(exactly = 1) {
            mockCacheService.saveToCache(match {
                it.latitude == expectedInfoToCache.latitude &&
                        it.longitude == expectedInfoToCache.longitude
            })
        }
    }

    @Test
    fun `getWeatherByIp when cache is valid should return data from cache`() = runTest {
        // Given
        val coordinate = LocationCoordinate(testIpLocation.latitude, testIpLocation.longitude)
        coEvery { mockLocationDataSource.getCurrentLocation() } returns testIpLocation
        coEvery { mockCacheService.getValidFromCache(coordinate, cacheDuration) } returns weatherInfoFromCache

        // When
        val result = cachedWeatherDataSource.getWeatherByIp()

        // Then
        val expectedDto = converter.weatherInfoToDto(weatherInfoFromCache)
        assertThat(result).isEqualTo(expectedDto)

        coVerify(exactly = 1) { mockLocationDataSource.getCurrentLocation() }
        coVerify(exactly = 1) { mockCacheService.getValidFromCache(coordinate, cacheDuration) }
        coVerify(exactly = 0) { mockApiClient.getWeatherByCity(any(), any()) }
        coVerify(exactly = 0) { mockCacheService.saveToCache(any()) }
    }

    @Test
    fun `getWeatherByCity when location not found should throw LocationNotFoundException`() = runTest {
        // Given
        coEvery { mockLocationDataSource.getCityCoordinates(testCity, testCountry) } returns null

        // When & Then
        assertThrows<LocationNotFoundException> {
            cachedWeatherDataSource.getWeatherByCity(testCity, testCountry)
        }

        coVerify(exactly = 1) { mockLocationDataSource.getCityCoordinates(testCity, testCountry) }
        coVerify(exactly = 0) { mockCacheService.getValidFromCache(any(), any()) }
        coVerify(exactly = 0) { mockApiClient.getWeatherByCity(any(), any()) }
    }

    @Test
    fun `getWeatherByIp when location not found should throw IpNotFoundException`() = runTest {
        // Given
        coEvery { mockLocationDataSource.getCurrentLocation() } returns null

        // When / Then
        assertThrows<IpNotFoundException> {
            cachedWeatherDataSource.getWeatherByIp()
        }

        coVerify(exactly = 1) { mockLocationDataSource.getCurrentLocation() }
        coVerify(exactly = 0) { mockCacheService.getValidFromCache(any(), any()) }
        coVerify(exactly = 0) { mockApiClient.getWeatherByCity(any(), any()) }
    }
}