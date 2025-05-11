package data.weather.cache.io

import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.damascus.data.weather.cache.io.FileOperations
import org.damascus.data.weather.cache.io.WeatherCacheService
import org.damascus.data.weather.dto.CsvWeatherModel
import org.damascus.data.weather.mapper.WeatherDataConverter
import org.damascus.domain.model.LocationCoordinate
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherCacheServiceTest {
    private lateinit var mockFileOps: FileOperations
    private lateinit var realConverter: WeatherDataConverter
    private lateinit var cacheService: WeatherCacheService

    private val testCsvFilePath = "test_cache.csv"
    private val testTolerance = 0.01
    private val testLocation = LocationCoordinate(10.0, 20.0)
    private val fixedTestTime = LocalDateTime.of(2023, 10, 26, 10, 0, 0)
    private val testWeatherInfo = WeatherInfo(
        latitude = 10.0,
        longitude = 20.0,
        elevation = 5.0,
        timezone = "Test/Zone",
        weather = Weather(
            temperature = 25.0,
            windSpeed = 15.0,
            windDirection = 180,
            isDay = true,
            weatherCode = 1,
            time = "test_time"
        ),
        units = WeatherUnit("C", "kmh", "deg")
    )

    @BeforeEach
    fun setUp() {
        mockFileOps = mockk(relaxed = true)
        realConverter = WeatherDataConverter()

        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixedTestTime

        every { mockFileOps.fileExists() } returns false
        every { mockFileOps.readAllRows(any()) } returns emptyList()

        cacheService = WeatherCacheService(
            csvFilePath = testCsvFilePath,
            converter = realConverter,
            fileOperations = mockFileOps,
            coordinateTolerance = testTolerance
        )
    }

    @Test
    fun `saveToCache - when file does not exist - writes header then appends row`() {
        val expectedEntry = realConverter.weatherInfoToEntry(testWeatherInfo)
        val expectedRowData = realConverter.entryToCsvRow(expectedEntry)
        val headersArray = CsvWeatherModel.HEADERS.toTypedArray()

        every { mockFileOps.fileExists() } returns false
        every { mockFileOps.writeHeader(headersArray) } returns false
        every { mockFileOps.appendRow(expectedRowData) } returns false

        cacheService.saveToCache(testWeatherInfo)

        verify(exactly = 1) { mockFileOps.fileExists() }
        verify(exactly = 1) { mockFileOps.writeHeader(headersArray) }
        verify(exactly = 1) { mockFileOps.appendRow(expectedRowData) }
    }

    @Test
    fun `saveToCache - when file exists and has data - only appends row`() {
        val expectedEntry = realConverter.weatherInfoToEntry(testWeatherInfo)
        val expectedRowData = realConverter.entryToCsvRow(expectedEntry)

        every { mockFileOps.fileExists() } returns true
        every { mockFileOps.readAllRows(false) } returns listOf(CsvWeatherModel.HEADERS.toTypedArray())
        every { mockFileOps.appendRow(expectedRowData) } returns true  // Fixed

        cacheService.saveToCache(testWeatherInfo)

        verify(exactly = 1) { mockFileOps.fileExists() }
        verify(exactly = 1) { mockFileOps.readAllRows(false) }
        verify(exactly = 0) { mockFileOps.writeHeader(any()) }
        verify(exactly = 1) { mockFileOps.appendRow(expectedRowData) }
    }

    @Test
    fun `getFromCache - when file does not exist - returns null`() {
        every { mockFileOps.fileExists() } returns false

        val result = cacheService.getFromCache(testLocation)

        assertThat(result).isNull()
        verify(exactly = 1) { mockFileOps.fileExists() }
    }

    @Test
    fun `getFromCache - when no matching row - returns null`() {
        every { mockFileOps.fileExists() } returns true
        every { mockFileOps.readAllRows(true) } returns emptyList()

        val result = cacheService.getFromCache(testLocation)

        assertThat(result).isNull()
    }

    @Test
    fun `getFromCache - when matching row exists - returns WeatherInfo`() {
        val now = fixedTestTime
        val entry = realConverter.weatherInfoToEntry(testWeatherInfo, now.format(DateTimeFormatter.ISO_DATE_TIME))
        val rowData = realConverter.entryToCsvRow(entry)

        every { mockFileOps.fileExists() } returns true
        every { mockFileOps.readAllRows(true) } returns listOf(rowData)

        val result = cacheService.getFromCache(testLocation)

        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(testWeatherInfo)
    }

    @Test
    fun `getValidFromCache - when entry is valid - returns WeatherInfo`() {
        val validTimestamp = fixedTestTime.minusMinutes(10).format(DateTimeFormatter.ISO_DATE_TIME)
        val entry = realConverter.weatherInfoToEntry(testWeatherInfo, validTimestamp)
        val rowData = realConverter.entryToCsvRow(entry)

        every { mockFileOps.fileExists() } returns true
        every { mockFileOps.readAllRows(true) } returns listOf(rowData)

        val result = cacheService.getValidFromCache(testLocation, 30L)

        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(testWeatherInfo)
    }

    @Test
    fun `getValidFromCache - when entry is too old - returns null`() {
        val oldTimestamp = fixedTestTime.minusMinutes(60).format(DateTimeFormatter.ISO_DATE_TIME)
        val entry = realConverter.weatherInfoToEntry(testWeatherInfo, oldTimestamp)
        val rowData = realConverter.entryToCsvRow(entry)

        every { mockFileOps.fileExists() } returns true
        every { mockFileOps.readAllRows(true) } returns listOf(rowData)

        val result = cacheService.getValidFromCache(testLocation, 30L)

        assertThat(result).isNull()
    }

    @Test
    fun `clearCache - when calls fileOps_clearContent`() {
        val headersArray = CsvWeatherModel.HEADERS.toTypedArray()
        every { mockFileOps.clearContent(headersArray) } returns true

        cacheService.clearCache()

        verify(exactly = 1) { mockFileOps.clearContent(headersArray) }
    }

    @Test
    fun `findMatchingRowData - when rows exist but out of tolerance - returns null`() {
        val farWeatherInfo = testWeatherInfo.copy(
            latitude = 999.0,
            longitude = 999.0
        )
        val farEntry = realConverter.weatherInfoToEntry(farWeatherInfo)
        val row = realConverter.entryToCsvRow(farEntry)

        every { mockFileOps.fileExists() } returns true
        every { mockFileOps.readAllRows(true) } returns listOf(row)

        val result = cacheService.getFromCache(
            LocationCoordinate(
                latitude = testWeatherInfo.latitude,
                longitude = testWeatherInfo.longitude
            )
        )

        assertThat(result).isNull()
    }



}
