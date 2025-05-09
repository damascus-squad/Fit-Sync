package org.damascus.data.weather.cache.io

import org.damascus.data.weather.datasource.WeatherCacheService
import org.damascus.data.weather.dto.WeatherCacheCsvEntry
import org.damascus.data.weather.mapper.WeatherCacheEntryConverter
import org.damascus.domain.model.LocationCoordinate
import org.damascus.domain.model.WeatherInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class CsvWeatherCacheService(
    private val csvFilePath: String = "csvCache.csv",
    private val converter: WeatherCacheEntryConverter = WeatherCacheEntryConverter(),
    private val fileOps: CsvFileOperations = OpenCsvFileOperations(),
    private val coordinateTolerance: Double = 0.01
) : WeatherCacheService {
    private fun findMatchingRowData(locationCoordinate: LocationCoordinate): Array<String>? {
        if (!fileOps.fileExists(csvFilePath)) {
            return null
        }

        val allRows = fileOps.readAllRows(filePath = csvFilePath, skipHeader = true)

        return allRows
            .asSequence()
            .filter { row ->
                if (row.size < WeatherCacheCsvEntry.HEADERS.size) {
                    false
                } else {
                    val lat = row[0].toDoubleOrNull()
                    val lon = row[1].toDoubleOrNull()
                    lat != null && lon != null
                }
            }
            .mapNotNull { row ->
                val lat = row[0].toDouble()
                val lon = row[1].toDouble()
                val latDiff = abs(lat - locationCoordinate.latitude)
                val lonDiff = abs(lon - locationCoordinate.longitude)

                if (latDiff < coordinateTolerance && lonDiff < coordinateTolerance) {
                    row
                } else {
                    null
                }
            }
            .firstOrNull()
    }
    override fun saveToCache(weatherInfo: WeatherInfo) {
        val csvEntry = converter.weatherInfoToEntry(weatherInfo)
        val rowData = converter.entryToCsvRow(csvEntry)

        val fileIsEmptyOrNonExistent = !fileOps.fileExists(csvFilePath) ||
                (fileOps.fileExists(csvFilePath) && fileOps.readAllRows(csvFilePath, false).isEmpty())

        if (fileIsEmptyOrNonExistent) {
            fileOps.writeHeader(csvFilePath, WeatherCacheCsvEntry.HEADERS.toTypedArray())
        }
        fileOps.appendRow(csvFilePath, rowData)
    }
    override fun getFromCache(locationCoordinate: LocationCoordinate): WeatherInfo? {
        val matchingRowData = findMatchingRowData(locationCoordinate) ?: return null
        val csvEntry = converter.csvRowToEntry(matchingRowData) ?: return null
        return converter.entryToWeatherInfo(csvEntry)
    }
    override fun getValidFromCache(locationCoordinate: LocationCoordinate, maxAgeMinutes: Long): WeatherInfo? {
        val matchingRowData = findMatchingRowData(locationCoordinate) ?: return null
        val csvEntry = converter.csvRowToEntry(matchingRowData) ?: return null

        return try {
            val timestamp = LocalDateTime.parse(csvEntry.timestamp, DateTimeFormatter.ISO_DATE_TIME)
            val now = LocalDateTime.now()
            val minutesElapsed = ChronoUnit.MINUTES.between(timestamp, now)

            if (minutesElapsed >= 0 && minutesElapsed < maxAgeMinutes) {
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
    override fun clearCache() {
        fileOps.clearFileContent(csvFilePath, WeatherCacheCsvEntry.HEADERS.toTypedArray())
    }
}