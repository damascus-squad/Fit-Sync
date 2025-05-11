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
    private lateinit var fileOps: FileOperations
    private lateinit var converter: WeatherDataConverter
    private lateinit var service: WeatherCacheService

    private val csvPath = "test_cache.csv"
    private val tolerance = 0.01
    private val baseLocation = LocationCoordinate(10.0, 20.0)
    private val fixedNow = LocalDateTime.of(2023, 10, 26, 10, 0)
    private val baseWeather = WeatherInfo(
        latitude = 10.0,
        longitude = 20.0,
        elevation = 5.0,
        timezone = "Test/Zone",
        weather = Weather(25.0, 15.0, 180, true, 1, "test_time"),
        units = WeatherUnit("C", "kmh", "deg")
    )

    @BeforeEach
    fun setUp() {
        fileOps = mockk(relaxed = true)
        converter = WeatherDataConverter()
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixedNow
        every { fileOps.fileExists() } returns false
        every { fileOps.readAllRows(any()) } returns emptyList()

        service = WeatherCacheService(
            csvFilePath = csvPath,
            converter = converter,
            fileOperations = fileOps,
            coordinateTolerance = tolerance
        )
    }

    // saveToCache tests
    @Test
    fun `saveToCache when file does not exist writes header and appends`() {
        val entry = converter.weatherInfoToEntry(baseWeather)
        val row = converter.entryToCsvRow(entry)
        val headers = CsvWeatherModel.HEADERS.toTypedArray()

        every { fileOps.fileExists() } returns false
        every { fileOps.writeHeader(headers) } returns true
        every { fileOps.appendRow(row) } returns true

        service.saveToCache(baseWeather)

        verifySequence {
            fileOps.fileExists()
            fileOps.writeHeader(headers)
            fileOps.appendRow(row)
        }
    }

    @Test
    fun `saveToCache when file exists but empty writes header and appends`() {
        val entry = converter.weatherInfoToEntry(baseWeather)
        val row = converter.entryToCsvRow(entry)
        val headers = CsvWeatherModel.HEADERS.toTypedArray()

        every { fileOps.fileExists() } returns true
        every { fileOps.readAllRows(false) } returns emptyList()
        every { fileOps.writeHeader(headers) } returns true
        every { fileOps.appendRow(row) } returns true

        service.saveToCache(baseWeather)

        verifySequence {
            fileOps.fileExists()
            fileOps.readAllRows(false)
            fileOps.writeHeader(headers)
            fileOps.appendRow(row)
        }
    }

    @Test
    fun `saveToCache when file exists with data only appends`() {
        val entry = converter.weatherInfoToEntry(baseWeather)
        val row = converter.entryToCsvRow(entry)

        every { fileOps.fileExists() } returns true
        every { fileOps.readAllRows(false) } returns listOf(CsvWeatherModel.HEADERS.toTypedArray())
        every { fileOps.appendRow(row) } returns true

        service.saveToCache(baseWeather)

        verify {
            fileOps.fileExists(); fileOps.readAllRows(false)
            fileOps.appendRow(row)
        }
        verify(exactly = 0) { fileOps.writeHeader(any()) }
    }

    @Test
    fun `getFromCache returns null when file missing`() {
        every { fileOps.fileExists() } returns false
        assertThat(service.getFromCache(baseLocation)).isNull()
    }

    @Test
    fun `getFromCache returns null when only header present`() {
        every { fileOps.fileExists() } returns true
        every { fileOps.readAllRows(true) } returns listOf(CsvWeatherModel.HEADERS.toTypedArray())
        assertThat(service.getFromCache(baseLocation)).isNull()
    }

    @Test
    fun `getFromCache returns null when row too short`() {
        every { fileOps.fileExists() } returns true
        every { fileOps.readAllRows(true) } returns listOf(arrayOf("10.0", "20.0"))
        assertThat(service.getFromCache(baseLocation)).isNull()
    }

    @Test
    fun `getFromCache returns null when lat lon non-numeric`() {
        val bad = arrayOf(
            "x",
            "y",
            "5",
            "Zone",
            "25",
            "15",
            "180",
            "true",
            "1",
            "t",
            "C",
            "kmh",
            "deg",
            "2023-10-26T10:00:00"
        )
        every { fileOps.fileExists() } returns true
        every { fileOps.readAllRows(true) } returns listOf(bad)
        assertThat(service.getFromCache(baseLocation)).isNull()
    }

    @Test
    fun `getFromCache returns null when out of tolerance`() {
        val far = baseWeather.copy(latitude = 999.0, longitude = 999.0)
        val row = converter.entryToCsvRow(converter.weatherInfoToEntry(far))
        every { fileOps.fileExists() } returns true
        every { fileOps.readAllRows(true) } returns listOf(row)
        assertThat(service.getFromCache(baseLocation)).isNull()
    }

    @Test
    fun `getFromCache returns WeatherInfo when exact or near match`() {
        val near = baseWeather.copy(latitude = 10.005, longitude = 19.995)
        val ts = fixedNow.format(DateTimeFormatter.ISO_DATE_TIME)
        val row = converter.entryToCsvRow(converter.weatherInfoToEntry(near, ts))
        every { fileOps.fileExists() } returns true
        every { fileOps.readAllRows(true) } returns listOf(row)
        assertThat(service.getFromCache(baseLocation)).isEqualTo(near)
    }

    // getValidFromCache tests
    @Test
    fun `getValidFromCache returns null when no match`() {
        every { fileOps.fileExists() } returns true
        every { fileOps.readAllRows(true) } returns emptyList()
        assertThat(service.getValidFromCache(baseLocation, 30L)).isNull()
    }

    @Test
    fun `getValidFromCache returns WeatherInfo when timestamp within limit`() {
        val ts = fixedNow.minusMinutes(10).format(DateTimeFormatter.ISO_DATE_TIME)
        val row = converter.entryToCsvRow(converter.weatherInfoToEntry(baseWeather, ts))
        every { fileOps.fileExists() } returns true
        every { fileOps.readAllRows(true) } returns listOf(row)
        assertThat(service.getValidFromCache(baseLocation, 30L)).isEqualTo(baseWeather)
    }

    @Test
    fun `getValidFromCache returns null when timestamp too old`() {
        val ts = fixedNow.minusMinutes(60).format(DateTimeFormatter.ISO_DATE_TIME)
        val row = converter.entryToCsvRow(converter.weatherInfoToEntry(baseWeather, ts))
        every { fileOps.fileExists() } returns true
        every { fileOps.readAllRows(true) } returns listOf(row)
        assertThat(service.getValidFromCache(baseLocation, 30L)).isNull()
    }

    @Test
    fun `getValidFromCache returns null when timestamp malformed`() {
        val row = converter.entryToCsvRow(converter.weatherInfoToEntry(baseWeather, "bad-ts"))
        every { fileOps.fileExists() } returns true
        every { fileOps.readAllRows(true) } returns listOf(row)
        assertThat(service.getValidFromCache(baseLocation, 30L)).isNull()
    }

    @Test
    fun `clearCache calls clearContent with headers`() {
        val headers = CsvWeatherModel.HEADERS.toTypedArray()
        every { fileOps.clearContent(headers) } returns true
        service.clearCache()
        verify { fileOps.clearContent(headers) }
    }
}
