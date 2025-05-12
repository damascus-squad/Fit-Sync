package data.weather.datasource

import com.google.common.truth.Truth.assertThat
import org.damascus.data.weather.datasource.WeatherCacheManager
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

private const val CSV_HEADER =
    "key,timestamp,latitude,longitude,elevation,timezone,temperature,windSpeed,windDirection,isDay,weatherCode,time,temperatureUnit,windSpeedUnit,windDirectionUnit"

class WeatherCacheManagerTest {

    private lateinit var tempFile: File
    private lateinit var cacheManager: WeatherCacheManager

    @BeforeEach
    fun setup() {
        tempFile = File("build/test-cache-${System.nanoTime()}.csv").apply {
            parentFile?.mkdirs()
        }
        tempFile.deleteOnExit()
        if (tempFile.exists()) tempFile.delete()
        cacheManager = WeatherCacheManager(tempFile)
    }

    @Test
    fun `readCache  - returns null if cache file does not exist`() {
        assertThat(cacheManager.readCache("some_key")).isNull()
    }

    @Test
    fun `readCache  - returns null if cache file has matching key but missing required fields (incomplete header)`() {
        tempFile.writeText(
            "key,latitude,longitude,elevation\n" +
                    "some_key,1.0,2.0,3.0"
        )
        assertThat(cacheManager.readCache("some_key")).isNull()
    }

    @Test
    fun `readCache - returns null if cache file contains only the header`() {
        tempFile.writeText("$CSV_HEADER\n")
        assertThat(cacheManager.readCache("some_key")).isNull()
    }

    @Test
    fun `readCache  - uses defaults for missing optional columns (timezone, units, time)`() {
        val minimalHeader =
            "key,timestamp,latitude,longitude,elevation,temperature,windSpeed,windDirection,isDay,weatherCode"
        tempFile.writeText(
            "$minimalHeader\n" +
                    "some_key,0,1.0,2.0,3.0,20.0,5.0,180,1,80"
        )
        assertThat(cacheManager.readCache("some_key")?.timezone).isEqualTo("GMT")
    }
    
    @Test
    fun `writeCache - written entry is readable and matches original object`() {
        val info = dummyWeatherInfo()
        cacheManager.writeCache("some_key", info)
        assertThat(cacheManager.readCache("some_key")).isEqualTo(info)
    }

    @Test
    fun `writeCache - writes isDay=true as 1 and isDay=false as 0`() {
        val infoDay = dummyWeatherInfo().copy(weather = dummyWeather().copy(isDay = true))
        cacheManager.writeCache("day_key", infoDay)
        val infoNight = dummyWeatherInfo().copy(weather = dummyWeather().copy(isDay = false))
        cacheManager.writeCache("night_key", infoNight)

        assertThat(cacheManager.readCache("day_key")?.weather?.isDay).isTrue()
        assertThat(cacheManager.readCache("night_key")?.weather?.isDay).isFalse()
    }


    private fun dummyWeatherInfo() = WeatherInfo(
        latitude = 1.0,
        longitude = 2.0,
        elevation = 3.0,
        timezone = "GMT",
        weather = dummyWeather(),
        units = WeatherUnit("\u00b0C", "km/h", "\u00b0")
    )

    private fun dummyWeather() = Weather(
        temperature = 20.0,
        windSpeed = 5.0,
        windDirection = 180,
        isDay = true,
        weatherCode = 80,
        time = "12:00"
    )
}