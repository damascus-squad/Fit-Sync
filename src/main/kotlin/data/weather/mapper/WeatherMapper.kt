package org.damascus.data.weather.mapper

import org.damascus.data.weather.dto.WeatherDto
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit

// TODO should delete , merged in @WeatherCacheEntryConverter in line 61
fun WeatherDto.toWeatherInfo(): WeatherInfo {
    return WeatherInfo(
        latitude = latitude ,
        longitude = longitude,
        elevation = elevation,
        timezone = timezone.ifBlank { "GMT" },
        weather = Weather(
            temperature = currentWeather.temperature,
            windSpeed = currentWeather.windSpeed,
            windDirection = currentWeather.windDirection ,
            isDay = currentWeather.isDay == 1,
            weatherCode = currentWeather.weatherCode,
            time = currentWeather.time.ifBlank { "" }
        ),
        units = WeatherUnit(
            temperatureUnit = currentWeatherUnits.temperature.ifBlank { "°C" },
            windSpeedUnit = currentWeatherUnits.windSpeed.ifBlank { "km/h" },
            windDirectionUnit = currentWeatherUnits.windDirection.ifBlank { "°" }
        )
    )
}
