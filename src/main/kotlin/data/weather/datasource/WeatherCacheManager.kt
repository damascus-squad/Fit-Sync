package org.damascus.data.weather.datasource

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import java.io.File

class WeatherCacheManager(private val cacheFile: File) {

    private data class CacheEntry(val data: Map<String, String>) {
        val key: String? get() = data["key"]
    }

    fun readCache(key: String): WeatherInfo? {
        if (!cacheFile.exists()) return null

        val rows = csvReader().readAllWithHeader(cacheFile).map { CacheEntry(it) }
        val row = rows.find { it.key == key }?.data ?: return null

        return try {
            WeatherInfo(
                latitude = row["latitude"]?.toDouble() ?: return null,
                longitude = row["longitude"]?.toDouble() ?: return null,
                elevation = row["elevation"]?.toDouble() ?: return null,
                timezone = row["timezone"] ?: "GMT",
                weather = Weather(
                    temperature = row["temperature"]?.toDouble() ?: return null,
                    windSpeed = row["windSpeed"]?.toDouble() ?: return null,
                    windDirection = row["windDirection"]?.toInt() ?: return null,
                    isDay = row["isDay"] == "1",
                    weatherCode = row["weatherCode"]?.toInt() ?: return null,
                    time = row["time"] ?: ""
                ),
                units = WeatherUnit(
                    temperatureUnit = row["temperatureUnit"] ?: "°C",
                    windSpeedUnit = row["windSpeedUnit"] ?: "km/h",
                    windDirectionUnit = row["windDirectionUnit"] ?: "°"
                )
            )
        } catch (e: Exception) {
            null
        }
    }

    fun writeCache(key: String, info: WeatherInfo) {
        val rows = mutableListOf<Map<String, String>>()

        if (cacheFile.exists()) {
            rows += csvReader().readAllWithHeader(cacheFile).filterNot { it["key"] == key }
        }

        val newRow = mapOf(
            "key" to key,
            "timestamp" to System.currentTimeMillis().toString(),
            "latitude" to info.latitude.toString(),
            "longitude" to info.longitude.toString(),
            "elevation" to info.elevation.toString(),
            "timezone" to info.timezone,
            "temperature" to info.weather.temperature.toString(),
            "windSpeed" to info.weather.windSpeed.toString(),
            "windDirection" to info.weather.windDirection.toString(),
            "isDay" to if (info.weather.isDay) "1" else "0",
            "weatherCode" to info.weather.weatherCode.toString(),
            "time" to info.weather.time,
            "temperatureUnit" to info.units.temperatureUnit,
            "windSpeedUnit" to info.units.windSpeedUnit,
            "windDirectionUnit" to info.units.windDirectionUnit
        )

        rows += newRow

        csvWriter().writeAll(
            listOf(newRow.keys.toList()) + rows.map { it.values.toList() },
            cacheFile
        )
    }
}
