package data.weather.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.damascus.data.weather.datasource.WeatherCacheManager
import org.damascus.data.weather.datasource.WeatherDataSource
import org.damascus.data.weather.dto.CurrentWeatherDto
import org.damascus.data.weather.dto.CurrentWeatherUnitsDto
import org.damascus.data.weather.dto.LocationDto
import org.damascus.data.weather.dto.WeatherDto
import org.damascus.data.weather.repository.WeatherRepositoryImp
import org.damascus.domain.model.Location
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

class WeatherRepositoryImpTest {

    private lateinit var dataSource: WeatherDataSource
    private lateinit var cacheManager: WeatherCacheManager
    private lateinit var weatherRepository: WeatherRepositoryImp

    private val dummyLocation = Location(
        name = "Cairo",
        region = "Cairo Governorate",
        country = "Egypt",
        latitude = 30.0444,
        longitude = 31.2357
    )

    private val dummyLocationDto = LocationDto(
        name = "Cairo",
        region = "Cairo Governorate",
        country = "Egypt",
        latitude = 30.0444,
        longitude = 31.2357
    )

    private val dummyWeatherInfo = WeatherInfo(
        latitude = 30.0444,
        longitude = 31.2357,
        elevation = 50.0,
        timezone = "Africa/Cairo",
        weather = Weather(
            temperature = 25.0,
            windSpeed = 10.0,
            windDirection = 180,
            isDay = true,
            weatherCode = 1,
            time = "2023-10-26T12:00"
        ),
        units = WeatherUnit(
            temperatureUnit = "°C",
            windSpeedUnit = "km/h",
            windDirectionUnit = "°"
        )
    )

    private val dummyWeatherDto = WeatherDto(
        latitude = 30.0444,
        longitude = 31.2357,
        generationTimeMs = 0.0,
        utcOffsetSeconds = 0,
        timezone = "Africa/Cairo",
        timezoneAbbreviation = "",
        elevation = 50.0,
        currentWeatherUnitsDto = CurrentWeatherUnitsDto(
            time = "°C",
            interval = "km/h",
            temperature = "°C",
            windSpeed = "km/h",
            windDirection = "°",
            isDay = "bool",
            weatherCode = "int"
        ),
        currentWeatherDto = CurrentWeatherDto(
            temperature = 25.0,
            windSpeed = 10.0,
            time = "2023-10-26T12:00",
            windDirection = 180,
            isDay = 1,
            weatherCode = 1,
            interval = 1
        )
    )


    @BeforeEach
    fun setup() {
        dataSource = mockk()
        cacheManager = mockk(relaxed = true)
        weatherRepository = WeatherRepositoryImp(dataSource, cacheManager)

    }

    @Test
    fun `getWeatherByCity should return cached data if available`() = runTest {
        val cacheKey = "city:Cairo,Cairo Governorate,Egypt,30.0444,31.2357"
        coEvery { cacheManager.readCache(cacheKey) } returns dummyWeatherInfo

        val result = weatherRepository.getWeatherByCity(dummyLocation)

        assertThat(result).isEqualTo(dummyWeatherInfo)
        coVerify(exactly = 1) { cacheManager.readCache(cacheKey) }
        coVerify(exactly = 0) { dataSource.getWeatherByCity(any()) }
        coVerify(exactly = 0) { cacheManager.writeCache(any(), any()) }
    }

    @Test
    fun `getWeatherByCity should fetch from data source and cache if not in cache`() = runTest {
        val cacheKey = "city:Cairo,Cairo Governorate,Egypt,30.0444,31.2357"

        coEvery { cacheManager.readCache(cacheKey) } returns null
        coEvery { dataSource.getWeatherByCity(dummyLocationDto) } returns dummyWeatherDto
        coEvery { cacheManager.writeCache(cacheKey, dummyWeatherInfo) } returns Unit

        val result = weatherRepository.getWeatherByCity(dummyLocation)

        assertThat(result).isEqualTo(dummyWeatherInfo)
        coVerify(exactly = 1) { cacheManager.readCache(cacheKey) }
        coVerify(exactly = 1) { dataSource.getWeatherByCity(dummyLocationDto) }
        coVerify(exactly = 1) { cacheManager.writeCache(cacheKey, dummyWeatherInfo) }
    }

    @Test
    fun `getWeatherByCity should propagate exceptions from dataSource and not write to cache`() = runTest {
        val cacheKey = "city:Cairo,Cairo Governorate,Egypt,30.0444,31.2357"
        val dataSourceException = IOException("Network error")

        coEvery { cacheManager.readCache(cacheKey) } returns null
        coEvery { dataSource.getWeatherByCity(dummyLocationDto) } throws dataSourceException

        val thrownException = assertThrows<IOException> {
            weatherRepository.getWeatherByCity(dummyLocation)
        }

        assertThat(thrownException).isEqualTo(dataSourceException)
        coVerify(exactly = 1) { cacheManager.readCache(cacheKey) }
        coVerify(exactly = 1) { dataSource.getWeatherByCity(dummyLocationDto) }
        coVerify(exactly = 0) { cacheManager.writeCache(any(), any()) }
    }

    @Test
    fun `getWeatherByIp should fetch from data source and return domain model`() = runTest {
        coEvery { dataSource.getWeatherByIp() } returns dummyWeatherDto

        val result = weatherRepository.getWeatherByIp()

        assertThat(result).isEqualTo(dummyWeatherInfo)
        coVerify(exactly = 1) { dataSource.getWeatherByIp() }
    }


    @Test
    fun `getWeatherByIp should return cached data if data source fails but cache has data`() = runTest {
        val dataSourceException = IOException("Data source failure")

        coEvery { dataSource.getWeatherByIp() } throws dataSourceException
        coEvery { cacheManager.readCache("ip_last") } returns dummyWeatherInfo

        val result = weatherRepository.getWeatherByIp()

        assertThat(result).isEqualTo(dummyWeatherInfo)
        coVerify(exactly = 1) { dataSource.getWeatherByIp() }
        coVerify(exactly = 1) { cacheManager.readCache("ip_last") }
        coVerify(exactly = 0) { cacheManager.writeCache(any(), any()) }
    }


}