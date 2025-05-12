package org.damascus.data.weather.datasource

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import java.io.File

class WeatherCachingManager(private val cacheFile: File) {

    enum class CacheColumn {
        KEY,
        TIMESTAMP,
        LATITUDE,
        LONGITUDE,
        ELEVATION,
        TIMEZONE,
        TEMPERATURE,
        WIND_SPEED,
        WIND_DIRECTION,
        IS_DAY,
        WEATHER_CODE,
        TIME,
        TEMPERATURE_UNIT,
        WIND_SPEED_UNIT,
        WIND_DIRECTION_UNIT
    }

    fun readCache(key: String): WeatherInfo? {
        if (!cacheFile.exists()) return null

        val rows = csvReader().readAllWithHeader(cacheFile)
        val row = rows.find { it[CacheColumn.KEY.name.lowercase()] == key } ?: return null

        return WeatherInfo(
            latitude = row[CacheColumn.LATITUDE.name.lowercase()]!!.toDouble(),
            longitude = row[CacheColumn.LONGITUDE.name.lowercase()]!!.toDouble(),
            elevation = row[CacheColumn.ELEVATION.name.lowercase()]!!.toDouble(),
            timezone = row[CacheColumn.TIMEZONE.name.lowercase()]!!,
            weather = Weather(
                temperature   = row[CacheColumn.TEMPERATURE.name.lowercase()]!!.toDouble(),
                windSpeed     = row[CacheColumn.WIND_SPEED.name.lowercase()]!!.toDouble(),
                windDirection = row[CacheColumn.WIND_DIRECTION.name.lowercase()]!!.toInt(),
                isDay         = row[CacheColumn.IS_DAY.name.lowercase()] == "1",
                weatherCode   = row[CacheColumn.WEATHER_CODE.name.lowercase()]!!.toInt(),
                time          = row[CacheColumn.TIME.name.lowercase()]!!
            ),
            units = WeatherUnit(
                temperatureUnit   = row[CacheColumn.TEMPERATURE_UNIT.name.lowercase()]!!,
                windSpeedUnit     = row[CacheColumn.WIND_SPEED_UNIT.name.lowercase()]!!,
                windDirectionUnit = row[CacheColumn.WIND_DIRECTION_UNIT.name.lowercase()]!!
            )
        )
    }

    fun writeCache(key: String, info: WeatherInfo) {
        val existing = if (cacheFile.exists()) {
            csvReader().readAllWithHeader(cacheFile)
                .filterNot { it[CacheColumn.KEY.name.lowercase()] == key }
        } else {
            emptyList()
        }

        val newRow = CacheColumn.entries.associate { column ->
            column.name.lowercase() to when (column) {
                CacheColumn.KEY                -> key
                CacheColumn.TIMESTAMP          -> System.currentTimeMillis().toString()
                CacheColumn.LATITUDE           -> info.latitude.toString()
                CacheColumn.LONGITUDE          -> info.longitude.toString()
                CacheColumn.ELEVATION          -> info.elevation.toString()
                CacheColumn.TIMEZONE           -> info.timezone
                CacheColumn.TEMPERATURE        -> info.weather.temperature.toString()
                CacheColumn.WIND_SPEED         -> info.weather.windSpeed.toString()
                CacheColumn.WIND_DIRECTION     -> info.weather.windDirection.toString()
                CacheColumn.IS_DAY             -> if (info.weather.isDay) "1" else "0"
                CacheColumn.WEATHER_CODE       -> info.weather.weatherCode.toString()
                CacheColumn.TIME               -> info.weather.time
                CacheColumn.TEMPERATURE_UNIT   -> info.units.temperatureUnit
                CacheColumn.WIND_SPEED_UNIT    -> info.units.windSpeedUnit
                CacheColumn.WIND_DIRECTION_UNIT-> info.units.windDirectionUnit
            }
        }

        val allRows = existing + newRow

        val header = CacheColumn.entries.map { it.name.lowercase() }
        val dataRows = allRows.map { rowMap ->
            header.map { rowMap[it].orEmpty() }
        }

        csvWriter().writeAll(listOf(header) + dataRows, cacheFile)
    }
}
