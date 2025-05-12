package data.weather.datasource

import org.damascus.data.weather.datasource.WeatherCacheManager
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.io.File
import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WeatherCacheManagerTest {

    private lateinit var tempFile: File
    private lateinit var cacheManager: WeatherCacheManager

    @BeforeEach
    fun setup() {
        tempFile = File.createTempFile("weather_test", ".csv")
        cacheManager = WeatherCacheManager(tempFile)
    }

    @AfterEach
    fun cleanup() {
        tempFile.delete()
    }

    @Test
    fun `readCache returns null when file does not exist`() {
        tempFile.delete() // simulate missing file

        val result = cacheManager.readCache("any-key")
        assertEquals(result, null)
    }

    @Test
    fun `readCache returns null when key is not found`() {
        tempFile.writeText(
            "key,latitude,longitude,elevation,timezone,temperature,windSpeed,windDirection,isDay,weatherCode,time,temperatureUnit,windSpeedUnit,windDirectionUnit\n" +
                    "wrong-key,36.0,42.0,300.0,Asia/Baghdad,25.0,5.0,90,1,2,12:00,°C,km/h,°"
        )

        val result = cacheManager.readCache("correct-key")

        assertThat(result).isNull()
    }

    @Test
    fun `readCache returns valid WeatherInfo when data is correct`() {
        val line = "test-key,36.0,42.0,300.0,Asia/Baghdad,25.0,5.0,90,1,2,12:00,°C,km/h,°"
        tempFile.writeText(
            "key,latitude,longitude,elevation,timezone,temperature,windSpeed,windDirection,isDay,weatherCode,time,temperatureUnit,windSpeedUnit,windDirectionUnit\n$line"
        )

        val result = cacheManager.readCache("test-key")

        assertThat(result?.latitude).isEqualTo(36.0)
    }

    @Test
    fun `readCache returns null when latitude is missing`() {
        val line = "test-key,,42.0,300.0,Asia/Baghdad,25.0,5.0,90,1,2,12:00,°C,km/h,°"
        tempFile.writeText(
            "key,latitude,longitude,elevation,timezone,temperature,windSpeed,windDirection,isDay,weatherCode,time,temperatureUnit,windSpeedUnit,windDirectionUnit\n$line"
        )

        val result = cacheManager.readCache("test-key")

        assertThat(result).isNull()
    }

    @Test
    fun `writeCache writes a row and can read it back`() {
        val info = WeatherInfo(
            latitude = 36.0,
            longitude = 42.0,
            elevation = 300.0,
            timezone = "Asia/Baghdad",
            weather = Weather(
                temperature = 25.0,
                windSpeed = 5.0,
                windDirection = 90,
                isDay = true,
                weatherCode = 2,
                time = "12:00"
            ),
            units = WeatherUnit(
                temperatureUnit = "°C",
                windSpeedUnit = "km/h",
                windDirectionUnit = "°"
            )
        )

        cacheManager.writeCache("key1", info)
        val result = cacheManager.readCache("key1")

        assertThat(result).isNotNull()
    }

    @Test
    fun `writeCache overrides old key entry`() {
        val first = WeatherInfo(
            latitude = 36.0,
            longitude = 42.0,
            elevation = 300.0,
            timezone = "Asia/Baghdad",
            weather = Weather(
                temperature = 25.0,
                windSpeed = 5.0,
                windDirection = 90,
                isDay = true,
                weatherCode = 2,
                time = "12:00"
            ),
            units = WeatherUnit("°C", "km/h", "°")
        )

        val second = first.copy(latitude = 37.5)

        cacheManager.writeCache("same-key", first)
        cacheManager.writeCache("same-key", second)

        val result = cacheManager.readCache("same-key")

        assertThat(result?.latitude).isEqualTo(37.5)
    }
}
