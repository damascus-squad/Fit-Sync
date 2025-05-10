package org.damascus.data.weather.mapper

import org.damascus.data.weather.dto.*
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class WeatherDataConverter  {

    fun weatherInfoToEntry(weatherInfo: WeatherInfo, timestamp: String? = null): CsvWeatherModel {
        val actualTimestamp = timestamp ?: LocalDateTime.now()
            .format(DateTimeFormatter.ISO_DATE_TIME)

        return CsvWeatherModel(
            latitude = weatherInfo.latitude,
            longitude = weatherInfo.longitude,
            elevation = weatherInfo.elevation,
            timezone = weatherInfo.timezone,
            temperature = weatherInfo.weather.temperature,
            windSpeed = weatherInfo.weather.windSpeed,
            windDirection = weatherInfo.weather.windDirection,
            isDay = weatherInfo.weather.isDay,
            weatherCode = weatherInfo.weather.weatherCode,
            time = weatherInfo.weather.time,
            temperatureUnit = weatherInfo.units.temperatureUnit,
            windSpeedUnit = weatherInfo.units.windSpeedUnit,
            windDirectionUnit = weatherInfo.units.windDirectionUnit,
            timestamp = actualTimestamp
        )
    }

    fun entryToWeatherInfo(entry: CsvWeatherModel): WeatherInfo {
        return WeatherInfo(
            latitude = entry.latitude,
            longitude = entry.longitude,
            elevation = entry.elevation,
            timezone = entry.timezone,
            weather = Weather(
                temperature = entry.temperature,
                windSpeed = entry.windSpeed,
                windDirection = entry.windDirection,
                isDay = entry.isDay,
                weatherCode = entry.weatherCode,
                time = entry.time
            ),
            units = WeatherUnit(
                temperatureUnit = entry.temperatureUnit,
                windSpeedUnit = entry.windSpeedUnit,
                windDirectionUnit = entry.windDirectionUnit
            )
        )
    }

    fun dtoToWeatherInfo(dto: WeatherDto): WeatherInfo {
        return WeatherInfo(
            latitude = dto.latitude,
            longitude = dto.longitude,
            elevation = dto.elevation,
            timezone = dto.timezone.ifBlank { "GMT" },
            weather = Weather(
                temperature = dto.currentWeatherDto.temperature,
                windSpeed = dto.currentWeatherDto.windSpeed,
                windDirection = dto.currentWeatherDto.windDirection,
                isDay = dto.currentWeatherDto.isDay == 1,
                weatherCode = dto.currentWeatherDto.weatherCode,
                time = dto.currentWeatherDto.time.ifBlank { "" }
            ),
            units = WeatherUnit(
                temperatureUnit = dto.currentWeatherUnitsDto.temperature.ifBlank { "°C" },
                windSpeedUnit = dto.currentWeatherUnitsDto.windSpeed.ifBlank { "km/h" },
                windDirectionUnit = dto.currentWeatherUnitsDto.windDirection.ifBlank { "°" }
            )
        )
    }

    fun weatherInfoToDto(weatherInfo: WeatherInfo): WeatherDto {
        val zoneId = try {
            ZoneId.of(weatherInfo.timezone)
        } catch (e: Exception) {
            ZoneId.of("UTC")
        }

        return WeatherDto(
            latitude = weatherInfo.latitude,
            longitude = weatherInfo.longitude,
            generationTimeMs = System.currentTimeMillis().toDouble(),
            utcOffsetSeconds = zoneId.rules.getOffset(Instant.now()).totalSeconds,
            timezone = weatherInfo.timezone,
            timezoneAbbreviation = zoneId.id,
            elevation = weatherInfo.elevation,
            currentWeatherUnitsDto = CurrentWeatherUnitsDto(
                time = "iso8601",
                interval = "seconds",
                temperature = weatherInfo.units.temperatureUnit,
                windSpeed = weatherInfo.units.windSpeedUnit,
                windDirection = weatherInfo.units.windDirectionUnit,
                isDay = "1/0",
                weatherCode = "wmo code"
            ),
            currentWeatherDto = CurrentWeatherDto(
                time = weatherInfo.weather.time,
                interval = 0,
                temperature = weatherInfo.weather.temperature,
                windSpeed = weatherInfo.weather.windSpeed,
                windDirection = weatherInfo.weather.windDirection,
                isDay = if (weatherInfo.weather.isDay) 1 else 0,
                weatherCode = weatherInfo.weather.weatherCode
            )
        )
    }

    fun entryToCsvRow(entry: CsvWeatherModel): Array<String> {
        return arrayOf(
            entry.latitude.toString(),
            entry.longitude.toString(),
            entry.elevation.toString(),
            entry.timezone,
            entry.temperature.toString(),
            entry.windSpeed.toString(),
            entry.windDirection.toString(),
            entry.isDay.toString(),
            entry.weatherCode.toString(),
            entry.time,
            entry.temperatureUnit,
            entry.windSpeedUnit,
            entry.windDirectionUnit,
            entry.timestamp
        )
    }

    fun csvRowToEntry(csvRow: Array<String>): CsvWeatherModel? =
        runCatching {
            CsvWeatherModel(
                latitude = csvRow[0].toDouble(),
                longitude = csvRow[1].toDouble(),
                elevation = csvRow[2].toDouble(),
                timezone = csvRow[3],
                temperature = csvRow[4].toDouble(),
                windSpeed = csvRow[5].toDouble(),
                windDirection = csvRow[6].toInt(),
                isDay = csvRow[7].toBooleanStrict(),
                weatherCode = csvRow[8].toInt(),
                time = csvRow[9],
                temperatureUnit = csvRow[10],
                windSpeedUnit = csvRow[11],
                windDirectionUnit = csvRow[12],
                timestamp = csvRow[13]
            )
        }.getOrNull()
}