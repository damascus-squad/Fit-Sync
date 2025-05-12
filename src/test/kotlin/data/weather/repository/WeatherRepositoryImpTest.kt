//package data.weather.repository
//
//import com.google.common.truth.Truth.assertThat
//import io.mockk.coEvery
//import io.mockk.coVerify
//import io.mockk.mockk
//import kotlinx.coroutines.test.runTest
//import org.damascus.data.location.mapper.toDto
//import org.damascus.data.weather.datasource.WeatherDataSource
//import org.damascus.data.weather.datasource.WeatherCacheManager
//import org.damascus.data.weather.dto.*
//import org.damascus.data.weather.mapper.toDomain
//import org.damascus.data.weather.repository.WeatherRepositoryImp
//import org.damascus.domain.exception.LocationNotFoundException
//import org.damascus.domain.model.*
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertThrows
//
//class WeatherRepositoryImpTest {
//
//    private lateinit var weatherDataSource: WeatherDataSource
//    private lateinit var cacheManager: WeatherCacheManager
//    private lateinit var weatherRepository: WeatherRepositoryImp
//
//    private val location = Location(
//        name = "Cairo",
//        region = "Cairo",
//        country = "EG",
//        latitude = 30.0,
//        longitude = 31.0
//    )
//
//    @BeforeEach
//    fun setup() {
//        weatherDataSource = mockk()
//        cacheManager = mockk(relaxed = true)
//        weatherRepository = WeatherRepositoryImp(weatherDataSource, cacheManager)
//    }
//
//    @Test
//    fun `getWeatherByCity returns cached data when available`() = runTest {
//        // Given
//        val cachedWeather = dummyWeatherDto().toDomain()
//        coEvery { cacheManager.readCache(any()) } returns cachedWeather
//
//        // When
//        val result = weatherRepository.getWeatherByCity(location)
//
//        // Then
//        assertThat(result).isEqualTo(cachedWeather)
//        coVerify(exactly = 0) { weatherDataSource.getWeatherByCity(any()) }
//    }
//
//    @Test
//    fun `getWeatherByCity fetches and caches fresh data when cache is empty`() = runTest {
//        // Given
//        val freshDto = dummyWeatherDto()
//        coEvery { cacheManager.readCache(any()) } returns null
//        coEvery { weatherDataSource.getWeatherByCity(location.toDto()) } returns freshDto
//
//        // When
//        val result = weatherRepository.getWeatherByCity(location)
//
//        // Then
//        assertThat(result).isEqualTo(freshDto.toDomain())
//        coVerify { cacheManager.writeCache(any(), freshDto.toDomain()) }
//    }
//
//    @Test
//    fun `getWeatherByCity throws exception when data source fails`() = runTest {
//        // Given
//        coEvery { cacheManager.readCache(any()) } returns null
//        coEvery { weatherDataSource.getWeatherByCity(any()) } throws
//                LocationNotFoundException("City not found")
//
//        // When & Then
//        assertThrows<LocationNotFoundException> {
//            weatherRepository.getWeatherByCity(location)
//        }
//    }
//
//    @Test
//    fun `getWeatherByIp returns cached data when available`() = runTest {
//        // Given
//        val cachedWeather = dummyWeatherDto().toDomain()
//        coEvery { cacheManager.readCache("by_ip") } returns cachedWeather
//
//        // When
//        val result = weatherRepository.getWeatherByIp()
//
//        // Then
//        assertThat(result).isEqualTo(cachedWeather)
//        coVerify(exactly = 0) { weatherDataSource.getWeatherByIp() }
//    }
//
//    @Test
//    fun `getWeatherByIp fetches and caches fresh data when cache is empty`() = runTest {
//        // Given
//        val freshDto = dummyWeatherDto()
//        coEvery { cacheManager.readCache("by_ip") } returns null
//        coEvery { weatherDataSource.getWeatherByIp() } returns freshDto
//
//        // When
//        val result = weatherRepository.getWeatherByIp()
//
//        // Then
//        assertThat(result).isEqualTo(freshDto.toDomain())
//        coVerify { cacheManager.writeCache("by_ip", freshDto.toDomain()) }
//    }
//
//    @Test
//    fun `getWeatherByIp throws exception when data source fails`() = runTest {
//        // Given
//        coEvery { cacheManager.readCache("by_ip") } returns null
//        coEvery { weatherDataSource.getWeatherByIp() } throws
//                LocationNotFoundException("IP failed")
//
//        // When & Then
//        assertThrows<LocationNotFoundException> {
//            weatherRepository.getWeatherByIp()
//        }
//    }
//
//    @Test
//    fun `should return default weather info if current weather is missing`() = runTest {
//        // Given
//        val incompleteDto = dummyWeatherDto().copy(
//            timezone = "",
//            currentWeatherDto = CurrentWeatherDto(
//                temperature = 0.0,
//                windSpeed = 0.0,
//                time = "",
//                interval = 1,
//                windDirection = 0,
//                isDay = 0,
//                weatherCode = -1
//            ),
//            currentWeatherUnitsDto = dummyWeatherDto().currentWeatherUnitsDto.copy(
//                temperature = ""
//            )
//        )
//        val expected = WeatherInfo(
//            latitude = 30.0,
//            longitude = 31.0,
//            elevation = 10.0,
//            timezone = "GMT",
//            weather = Weather(
//                temperature = 0.0,
//                windSpeed = 0.0,
//                windDirection = 0,
//                isDay = false,
//                weatherCode = -1,
//                time = ""
//            ),
//            units = WeatherUnit(
//                temperatureUnit = "°C",
//                windSpeedUnit = "km/h",
//                windDirectionUnit = "°"
//            )
//        )
//
//        coEvery { cacheManager.readCache(any()) } returns null
//        coEvery { weatherDataSource.getWeatherByCity(any()) } returns incompleteDto
//
//        // When
//        val result = weatherRepository.getWeatherByCity(location)
//
//        // Then
//        assertThat(result).isEqualTo(expected)
//        coVerify { cacheManager.writeCache(any(), expected) }
//    }
//
//    @Test
//    fun `should map isDay to false when currentWeather isDay is 0`() = runTest {
//        // Given
//        val dto = dummyWeatherDto().copy(
//            currentWeatherDto = dummyWeatherDto().currentWeatherDto.copy(isDay = 0)
//        )
//
//        coEvery { cacheManager.readCache(any()) } returns null
//        coEvery { weatherDataSource.getWeatherByCity(any()) } returns dto
//
//        // When
//        val result = weatherRepository.getWeatherByCity(location)
//
//        // Then
//        assertThat(result.weather.isDay).isFalse()
//        coVerify { cacheManager.writeCache(any(), result) }
//    }
//
//    private fun dummyWeatherDto(): WeatherDto = WeatherDto(
//        latitude = 30.0,
//        longitude = 31.0,
//        generationTimeMs = 12.3,
//        utcOffsetSeconds = 7200,
//        timezone = "Africa/Cairo",
//        timezoneAbbreviation = "EET",
//        elevation = 10.0,
//        currentWeatherUnitsDto = CurrentWeatherUnitsDto(
//            time = "iso8601",
//            interval = "int",
//            temperature = "°C",
//            windSpeed = "km/h",
//            windDirection = "°",
//            isDay = "bool",
//            weatherCode = "int"
//        ),
//        currentWeatherDto = CurrentWeatherDto(
//            temperature = 26.0,
//            windSpeed = 12.0,
//            time = "2025-05-05T12:00",
//            interval = 1,
//            windDirection = 90,
//            isDay = 1,
//            weatherCode = 0,
//        )
//    )
//}
package data.weather.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.damascus.data.location.mapper.toDto
import org.damascus.data.weather.datasource.WeatherCacheManager
import org.damascus.data.weather.datasource.WeatherDataSource
import org.damascus.data.weather.dto.*
import org.damascus.data.weather.mapper.toDomain
import org.damascus.data.weather.repository.WeatherRepositoryImp
import org.damascus.domain.exception.LocationNotFoundException
import org.damascus.domain.model.Location
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WeatherRepositoryImpTest {

    private lateinit var weatherDataSource: WeatherDataSource
    private lateinit var cacheManager: WeatherCacheManager
    private lateinit var weatherRepository: WeatherRepositoryImp

    private val location = Location(
        name = "Cairo",
        region = "Cairo",
        country = "EG",
        latitude = 30.0,
        longitude = 31.0
    )

    @BeforeEach
    fun setup() {
        weatherDataSource = mockk()
        cacheManager = mockk(relaxed = true)
        weatherRepository = WeatherRepositoryImp(weatherDataSource, cacheManager)
    }

    // Cache miss scenarios (will call dataSource)
    @Test
    fun `getWeatherByCity should fetch from source and cache when no cached data`() = runTest {
        // Given - no cached data
        val freshDto = dummyWeatherDto()
        coEvery { cacheManager.readCache(any()) } returns null
        coEvery { weatherDataSource.getWeatherByCity(location.toDto()) } returns freshDto

        // When
        val result = weatherRepository.getWeatherByCity(location)

        // Then
        assertThat(result).isEqualTo(freshDto.toDomain())
        coVerify(exactly = 1) {
            weatherDataSource.getWeatherByCity(location.toDto())
        }
        coVerify(exactly = 1) {
            cacheManager.writeCache(any(), freshDto.toDomain())
        }
    }

    @Test
    fun `getWeatherByIp should fetch from source and cache when no cached data`() = runTest {
        // Given - no cached data
        val freshDto = dummyWeatherDto()
        coEvery { cacheManager.readCache("by_ip") } returns null
        coEvery { weatherDataSource.getWeatherByIp() } returns freshDto

        // When
        val result = weatherRepository.getWeatherByIp()

        // Then
        assertThat(result).isEqualTo(freshDto.toDomain())
        coVerify(exactly = 1) { weatherDataSource.getWeatherByIp() }
        coVerify(exactly = 1) { cacheManager.writeCache("by_ip", freshDto.toDomain()) }
    }

    // Cache hit scenarios (won't call dataSource)
    @Test
    fun `getWeatherByCity should return cached data when available`() = runTest {
        // Given - cached data exists
        val cachedWeather = dummyWeatherDto().toDomain()
        coEvery { cacheManager.readCache(any()) } returns cachedWeather

        // When
        val result = weatherRepository.getWeatherByCity(location)

        // Then
        assertThat(result).isEqualTo(cachedWeather)
        coVerify(exactly = 0) { weatherDataSource.getWeatherByCity(any()) }
        coVerify(exactly = 0) { cacheManager.writeCache(any(), any()) }
    }

    @Test
    fun `getWeatherByIp should return cached data when available`() = runTest {
        // Given - cached data exists
        val cachedWeather = dummyWeatherDto().toDomain()
        coEvery { cacheManager.readCache("by_ip") } returns cachedWeather

        // When
        val result = weatherRepository.getWeatherByIp()

        // Then
        assertThat(result).isEqualTo(cachedWeather)
        coVerify(exactly = 0) { weatherDataSource.getWeatherByIp() }
        coVerify(exactly = 0) { cacheManager.writeCache(any(), any()) }
    }

    // Error scenarios
    @Test
    fun `getWeatherByCity should throw when source fails and no cache`() = runTest {
        // Given - no cache and source fails
        coEvery { cacheManager.readCache(any()) } returns null
        coEvery { weatherDataSource.getWeatherByCity(any()) } throws
                LocationNotFoundException("City not found")

        // When & Then
        assertThrows<LocationNotFoundException> {
            weatherRepository.getWeatherByCity(location)
        }
        coVerify(exactly = 1) { weatherDataSource.getWeatherByCity(any()) }
    }

    @Test
    fun `getWeatherByIp should throw when source fails and no cache`() = runTest {
        // Given - no cache and source fails
        coEvery { cacheManager.readCache("by_ip") } returns null
        coEvery { weatherDataSource.getWeatherByIp() } throws
                LocationNotFoundException("IP failed")

        // When & Then
        assertThrows<LocationNotFoundException> {
            weatherRepository.getWeatherByIp()
        }
        coVerify(exactly = 1) { weatherDataSource.getWeatherByIp() }
    }

    // Edge cases
    @Test
    fun `getWeatherByCity should handle incomplete data`() = runTest {
        // Given - incomplete data from source
        val incompleteDto = dummyWeatherDto().copy(
            timezone = "",
            currentWeatherDto = CurrentWeatherDto(
                temperature = 0.0,
                windSpeed = 0.0,
                time = "",
                interval = 1,
                windDirection = 0,
                isDay = 0,
                weatherCode = -1
            ),
            currentWeatherUnitsDto = dummyWeatherDto().currentWeatherUnitsDto.copy(
                temperature = ""
            )
        )
        coEvery { cacheManager.readCache(any()) } returns null
        coEvery { weatherDataSource.getWeatherByCity(any()) } returns incompleteDto

        // When
        val result = weatherRepository.getWeatherByCity(location)

        // Then - should still cache the result
        coVerify(exactly = 1) { cacheManager.writeCache(any(), any()) }
    }

    private fun dummyWeatherDto(): WeatherDto = WeatherDto(
        latitude = 30.0,
        longitude = 31.0,
        generationTimeMs = 12.3,
        utcOffsetSeconds = 7200,
        timezone = "Africa/Cairo",
        timezoneAbbreviation = "EET",
        elevation = 10.0,
        currentWeatherUnitsDto = CurrentWeatherUnitsDto(
            time = "iso8601",
            interval = "int",
            temperature = "°C",
            windSpeed = "km/h",
            windDirection = "°",
            isDay = "bool",
            weatherCode = "int"
        ),
        currentWeatherDto = CurrentWeatherDto(
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