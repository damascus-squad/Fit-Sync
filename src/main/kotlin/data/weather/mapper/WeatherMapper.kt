package org.damascus.data.weather.mapper

import org.damascus.data.weather.dto.WeatherDto
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit

fun WeatherDto.toWeatherInfo(): WeatherInfo {
    return WeatherInfo(
        latitude = latitude ,
        longitude = longitude,
        elevation = elevation,
        timezone = timezone.ifBlank { "GMT" },
        weather = Weather(
            temperature = currentWeather.temperature,
            windSpeed = currentWeather.windspeed,
            windDirection = currentWeather.winddirection ,
            isDay = currentWeather.isDay == 1,
            weatherCode = currentWeather.weathercode,
            time = currentWeather.time.ifBlank { "" }
        ),
        units = WeatherUnit(
            temperatureUnit = currentWeatherUnits.temperature.ifBlank { "°C" },
            windSpeedUnit = currentWeatherUnits.windspeed.ifBlank { "km/h" },
            windDirectionUnit = currentWeatherUnits.winddirection.ifBlank { "°" }
        )
    )
}
