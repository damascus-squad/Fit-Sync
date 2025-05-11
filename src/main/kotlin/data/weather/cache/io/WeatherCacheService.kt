package org.damascus.data.weather.cache.io

import org.damascus.data.weather.dto.CsvWeatherModel
import org.damascus.data.weather.mapper.WeatherDataConverter
import org.damascus.domain.model.LocationCoordinate
import org.damascus.domain.model.WeatherInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class WeatherCacheService(
    private val csvFilePath: String = "csvCache.csv",
    private val converter: WeatherDataConverter = WeatherDataConverter(),
    private val fileOperations: FileOperations = FileOperations(csvFilePath),
    private val coordinateTolerance: Double = 0.02
) {
    private fun findMatchingRowData(locationCoordinate: LocationCoordinate): Array<String>? {
        if (!fileOperations.fileExists()) {
            return null
        }
        val allRows = fileOperations.readAllRows(true)

        return allRows
            .asSequence()
            .filter { row ->
                if (row.size < CsvWeatherModel.HEADERS.size) {
                    false
                } else {
                    val latitude = row[0].toDoubleOrNull()
                    val longitude = row[1].toDoubleOrNull()
                    latitude != null && longitude != null
                }
            }
            .mapNotNull { row ->
                val latitude = row[0].toDouble()
                val longitude = row[1].toDouble()
                val latDiff = abs(latitude - locationCoordinate.latitude)
                val lonDiff = abs(longitude - locationCoordinate.longitude)

                if (latDiff < coordinateTolerance && lonDiff < coordinateTolerance) {
                    row
                } else {
                    null
                }
            }
            .firstOrNull()
    }

    fun saveToCache(weatherInfo: WeatherInfo) {
        val csvEntry = converter.weatherInfoToEntry(weatherInfo)
        val rowData = converter.entryToCsvRow(csvEntry)

        val fileIsEmptyOrNonExistent = !fileOperations.fileExists()
                || fileOperations.readAllRows(false).isEmpty()

        if (fileIsEmptyOrNonExistent) {
            fileOperations.writeHeader(CsvWeatherModel.HEADERS.toTypedArray())
        }
        fileOperations.appendRow(rowData)
    }

    fun getFromCache(locationCoordinate: LocationCoordinate): WeatherInfo? {
        val matchingRowData = findMatchingRowData(locationCoordinate) ?: return null
        val csvEntry = converter.csvRowToEntry(matchingRowData) ?: return null
        return converter.entryToWeatherInfo(csvEntry)
    }

    fun getValidFromCache(locationCoordinate: LocationCoordinate, maxAgeMinutes: Long): WeatherInfo? {
        val matchingRowData = findMatchingRowData(locationCoordinate) ?: return null
        val csvEntry = converter.csvRowToEntry(matchingRowData) ?: return null

        return try {
            val timestamp = LocalDateTime.parse(csvEntry.timestamp, DateTimeFormatter.ISO_DATE_TIME)
            val minutesElapsed = ChronoUnit.MINUTES.between(timestamp, LocalDateTime.now())

            if (minutesElapsed in 0..<maxAgeMinutes) {
                converter.entryToWeatherInfo(csvEntry)
            } else {
                null
            }
        } catch (e: DateTimeParseException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun clearCache() {
        fileOperations.clearContent(CsvWeatherModel.HEADERS.toTypedArray())
    }
}