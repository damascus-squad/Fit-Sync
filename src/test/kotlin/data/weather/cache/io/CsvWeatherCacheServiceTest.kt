package data.weather.cache.io

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import org.damascus.data.weather.cache.io.CsvFileOperations
import org.damascus.data.weather.cache.io.CsvWeatherCacheService
import org.damascus.data.weather.dto.WeatherCacheCsvEntry
import org.damascus.data.weather.mapper.WeatherCacheEntryConverter
import org.damascus.domain.model.LocationCoordinate
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CsvWeatherCacheServiceTest {
    private lateinit var mockFileOps: CsvFileOperations
    private lateinit var realConverter: WeatherCacheEntryConverter
    private lateinit var cacheService: CsvWeatherCacheService

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
        mockFileOps = mockk()
        realConverter = WeatherCacheEntryConverter()

        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixedTestTime

        cacheService = CsvWeatherCacheService(
            csvFilePath = testCsvFilePath,
            converter = realConverter,
            fileOps = mockFileOps,
            coordinateTolerance = testTolerance
        )
    }

    @Test
    fun `saveToCache - when file does not exist - writes header then appends row`() {
        val expectedEntry = realConverter.weatherInfoToEntry(testWeatherInfo)
        val expectedRowData = realConverter.entryToCsvRow(expectedEntry)
        val headersArray = WeatherCacheCsvEntry.HEADERS.toTypedArray()

        every { mockFileOps.fileExists(testCsvFilePath) } returns false
        every { mockFileOps.writeHeader(testCsvFilePath, eq(headersArray)) } just runs
        every { mockFileOps.appendRow(testCsvFilePath, eq(expectedRowData)) } just runs

        cacheService.saveToCache(testWeatherInfo)

        verify(exactly = 1) { mockFileOps.fileExists(testCsvFilePath) }
        verify(exactly = 1) { mockFileOps.writeHeader(testCsvFilePath, eq(headersArray)) }
        verify(exactly = 1) { mockFileOps.appendRow(testCsvFilePath, eq(expectedRowData)) }
    }

    @Test
    fun `saveToCache - when file exists but is empty - writes header then appends row`() {
        val expectedEntry = realConverter.weatherInfoToEntry(testWeatherInfo)
        val expectedRowData = realConverter.entryToCsvRow(expectedEntry)
        val headersArray = WeatherCacheCsvEntry.HEADERS.toTypedArray()

        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(testCsvFilePath, false) } returns emptyList()
        every { mockFileOps.writeHeader(testCsvFilePath, eq(headersArray)) } just runs
        every { mockFileOps.appendRow(testCsvFilePath, eq(expectedRowData)) } just runs

        cacheService.saveToCache(testWeatherInfo)

        verify(exactly = 2) { mockFileOps.fileExists(testCsvFilePath) }
        verify(exactly = 1) { mockFileOps.readAllRows(testCsvFilePath, false) }
        verify(exactly = 1) { mockFileOps.writeHeader(testCsvFilePath, eq(headersArray)) }
        verify(exactly = 1) { mockFileOps.appendRow(testCsvFilePath, eq(expectedRowData)) }
    }

    @Test
    fun `saveToCache - when file exists and has data - only appends row`() {
        val expectedEntry = realConverter.weatherInfoToEntry(testWeatherInfo)
        val expectedRowData = realConverter.entryToCsvRow(expectedEntry)

        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every {
            mockFileOps.readAllRows(
                testCsvFilePath,
                false
            )
        } returns listOf(WeatherCacheCsvEntry.HEADERS.toTypedArray())
        every { mockFileOps.appendRow(testCsvFilePath, eq(expectedRowData)) } just runs

        cacheService.saveToCache(testWeatherInfo)

        verify(exactly = 2) { mockFileOps.fileExists(testCsvFilePath) }
        verify(exactly = 1) { mockFileOps.readAllRows(testCsvFilePath, false) }
        verify(exactly = 0) { mockFileOps.writeHeader(any(), any()) }
        verify(exactly = 1) { mockFileOps.appendRow(testCsvFilePath, eq(expectedRowData)) }
    }

    @Test
    fun `saveToCache - when fileExists is true then false in condition - only appends row`() {
        val expectedEntry = realConverter.weatherInfoToEntry(testWeatherInfo)
        val expectedRowData = realConverter.entryToCsvRow(expectedEntry)

        every { mockFileOps.fileExists(testCsvFilePath) } returnsMany listOf(true, false)
        every { mockFileOps.appendRow(testCsvFilePath, eq(expectedRowData)) } just runs

        cacheService.saveToCache(testWeatherInfo)

        verify(exactly = 2) { mockFileOps.fileExists(testCsvFilePath) }
        verify(exactly = 0) { mockFileOps.readAllRows(any(), any()) }
        verify(exactly = 0) { mockFileOps.writeHeader(any(), any()) }
        verify(exactly = 1) { mockFileOps.appendRow(testCsvFilePath, eq(expectedRowData)) }
    }

    @Test
    fun `getFromCache - when file does not exist - returns null`() {
        every { mockFileOps.fileExists(testCsvFilePath) } returns false

        val result = cacheService.getFromCache(testLocation)

        assertThat(result).isNull()
        verify(exactly = 1) { mockFileOps.fileExists(testCsvFilePath) }
    }

    @Test
    fun `getFromCache - when no matching row - returns null`() {
        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(testCsvFilePath, true) } returns emptyList()

        val result = cacheService.getFromCache(testLocation)

        assertThat(result).isNull()
    }

    @Test
    fun `getFromCache - when matching row exists - returns WeatherInfo`() {
        val now = fixedTestTime
        val entry = realConverter.weatherInfoToEntry(testWeatherInfo, now.format(DateTimeFormatter.ISO_DATE_TIME))
        val rowData = realConverter.entryToCsvRow(entry)

        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(testCsvFilePath, true) } returns listOf(rowData)

        val result = cacheService.getFromCache(testLocation)

        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(testWeatherInfo)
    }

    @Test
    fun `getFromCache - when matching row is malformed by converter - returns null`() {
        val rowThatCouldPassFindButFailConverter = arrayOf(
            testLocation.latitude.toString(), testLocation.longitude.toString(), "0", "TZ",
            "bad_temp", "0", "0", "true", "0", "time", "C", "kmh", "deg", "timestamp_ignored_by_getFromCache"
        )
        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(testCsvFilePath, true) } returns listOf(rowThatCouldPassFindButFailConverter)

        val resultStrict = cacheService.getFromCache(testLocation)
        assertThat(resultStrict).isNull()
    }

    @Test
    fun `getValidFromCache - when entry is valid - returns WeatherInfo`() {
        val validTimestamp = fixedTestTime.minusMinutes(10).format(DateTimeFormatter.ISO_DATE_TIME)
        val entry = realConverter.weatherInfoToEntry(testWeatherInfo, validTimestamp)
        val rowData = realConverter.entryToCsvRow(entry)

        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(testCsvFilePath, true) } returns listOf(rowData)

        val result = cacheService.getValidFromCache(testLocation, 30L)

        assertThat(result).isNotNull()
        assertThat(result).isEqualTo(testWeatherInfo)
    }

    @Test
    fun `getValidFromCache - when entry is too old - returns null`() {
        val oldTimestamp = fixedTestTime.minusMinutes(60).format(DateTimeFormatter.ISO_DATE_TIME)
        val entry = realConverter.weatherInfoToEntry(testWeatherInfo, oldTimestamp)
        val rowData = realConverter.entryToCsvRow(entry)

        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(testCsvFilePath, true) } returns listOf(rowData)

        val result = cacheService.getValidFromCache(testLocation, 30L)

        assertThat(result).isNull()
    }

    @Test
    fun `getValidFromCache - when entry timestamp is in future - returns null`() {
        val futureTimestamp = fixedTestTime.plusMinutes(60).format(DateTimeFormatter.ISO_DATE_TIME)
        val entry = realConverter.weatherInfoToEntry(testWeatherInfo, futureTimestamp)
        val rowData = realConverter.entryToCsvRow(entry)

        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(testCsvFilePath, true) } returns listOf(rowData)

        val result = cacheService.getValidFromCache(testLocation, 30L)

        assertThat(result).isNull()
    }

    @Test
    fun `getValidFromCache - when entry timestamp is malformed - returns null`() {
        val malformedTimestamp = "not-a-valid-timestamp"
        val entryWithMalformedTimestamp = testWeatherInfo.copy(
            weather = testWeatherInfo.weather.copy(),
            units = testWeatherInfo.units.copy()
        )
        val entryForCsv = realConverter.weatherInfoToEntry(entryWithMalformedTimestamp, malformedTimestamp)
        val rowData = realConverter.entryToCsvRow(entryForCsv)


        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(testCsvFilePath, true) } returns listOf(rowData)

        val result = cacheService.getValidFromCache(testLocation, 30L)

        assertThat(result).isNull()
    }

    @Test
    fun `getValidFromCache - when no matching row - returns null`() {
        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(testCsvFilePath, true) } returns emptyList()

        val result = cacheService.getValidFromCache(testLocation, 30L)
        assertThat(result).isNull()
    }

    @Test
    fun `getValidFromCache - when converter returns null for matching row - returns null`() {
        val matchingRowDataThatWillFailConversion = arrayOf(
            testLocation.latitude.toString(), testLocation.longitude.toString(), "5.0", "Test/Zone",
            "NOT_A_DOUBLE_TEMP",
            "15.0", "180", "true", "1", "test_time",
            "C", "kmh", "deg",
            fixedTestTime.format(DateTimeFormatter.ISO_DATE_TIME)
        )

        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(filePath = testCsvFilePath, skipHeader = true) } returns listOf(
            matchingRowDataThatWillFailConversion
        )

        val result = cacheService.getValidFromCache(testLocation, 30L)

        assertThat(result).isNull()

        verify(exactly = 1) { mockFileOps.fileExists(testCsvFilePath) }
        verify(exactly = 1) { mockFileOps.readAllRows(filePath = testCsvFilePath, skipHeader = true) }
    }

    @Test
    fun `clearCache - calls fileOps_clearFileContent`() {
        val headersArray = WeatherCacheCsvEntry.HEADERS.toTypedArray()
        every { mockFileOps.clearFileContent(testCsvFilePath, headersArray) } returns true

        cacheService.clearCache()

        verify(exactly = 1) { mockFileOps.clearFileContent(testCsvFilePath, headersArray) }
    }

    @Test
    fun `findMatchingRowData - when row has insufficient columns - filters out row`() {
        val insufficientColumnsRow = arrayOf("10.0")

        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(filePath = testCsvFilePath, skipHeader = true) } returns listOf(
            insufficientColumnsRow
        )

        val result = cacheService.getFromCache(testLocation)

        assertThat(result).isNull()
        verify(exactly = 1) { mockFileOps.readAllRows(filePath = testCsvFilePath, skipHeader = true) }
    }

    @Test
    fun `findMatchingRowData - when row has non-numeric latitude - filters out row`() {
        val nonNumericLatRow = Array(WeatherCacheCsvEntry.HEADERS.size) { "valid_data" }
        nonNumericLatRow[0] = "not_a_latitude"
        nonNumericLatRow[1] = testLocation.longitude.toString()

        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every {
            mockFileOps.readAllRows(
                filePath = testCsvFilePath,
                skipHeader = true
            )
        } returns listOf(nonNumericLatRow)

        val result = cacheService.getFromCache(testLocation)

        assertThat(result).isNull()
        verify(exactly = 1) { mockFileOps.readAllRows(filePath = testCsvFilePath, skipHeader = true) }
    }

    @Test
    fun `findMatchingRowData - when row has non-numeric longitude - filters out row`() {
        val nonNumericLonRow = Array(WeatherCacheCsvEntry.HEADERS.size) { "valid_data" }
        nonNumericLonRow[0] = testLocation.latitude.toString()
        nonNumericLonRow[1] = "not_a_longitude"

        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every {
            mockFileOps.readAllRows(
                filePath = testCsvFilePath,
                skipHeader = true
            )
        } returns listOf(nonNumericLonRow)

        val result = cacheService.getFromCache(testLocation)

        assertThat(result).isNull()
        verify(exactly = 1) { mockFileOps.readAllRows(filePath = testCsvFilePath, skipHeader = true) }
    }

    @Test
    fun `findMatchingRowData - latitude outside tolerance, longitude inside - returns null`() {
        val latOutsideTolerance = testLocation.latitude + (testTolerance * 2)
        val lonInsideTolerance = testLocation.longitude + (testTolerance / 2)

        val entry = realConverter.weatherInfoToEntry(
            testWeatherInfo.copy(latitude = latOutsideTolerance, longitude = lonInsideTolerance)
        )
        val rowData = realConverter.entryToCsvRow(entry)


        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(filePath = testCsvFilePath, skipHeader = true) } returns listOf(rowData)

        val result = cacheService.getFromCache(LocationCoordinate(testLocation.latitude, testLocation.longitude))

        assertThat(result).isNull()
    }

    @Test
    fun `findMatchingRowData - latitude inside tolerance, longitude outside - returns null`() {
        val latInsideTolerance = testLocation.latitude + (testTolerance / 2)
        val lonOutsideTolerance = testLocation.longitude + (testTolerance * 2)

        val entry = realConverter.weatherInfoToEntry(
            testWeatherInfo.copy(latitude = latInsideTolerance, longitude = lonOutsideTolerance)
        )
        val rowData = realConverter.entryToCsvRow(entry)

        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(filePath = testCsvFilePath, skipHeader = true) } returns listOf(rowData)

        val result = cacheService.getFromCache(LocationCoordinate(testLocation.latitude, testLocation.longitude))

        assertThat(result).isNull()
    }

    @Test
    fun `findMatchingRowData - both latitude and longitude outside tolerance - returns null`() {
        val latOutsideTolerance = testLocation.latitude + (testTolerance * 2)
        val lonOutsideTolerance = testLocation.longitude + (testTolerance * 2)

        val entry = realConverter.weatherInfoToEntry(
            testWeatherInfo.copy(latitude = latOutsideTolerance, longitude = lonOutsideTolerance)
        )
        val rowData = realConverter.entryToCsvRow(entry)

        every { mockFileOps.fileExists(testCsvFilePath) } returns true
        every { mockFileOps.readAllRows(filePath = testCsvFilePath, skipHeader = true) } returns listOf(rowData)

        val result = cacheService.getFromCache(LocationCoordinate(testLocation.latitude, testLocation.longitude))

        assertThat(result).isNull()
    }

}