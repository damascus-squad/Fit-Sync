package org.damascus.data.weather.mapper

import org.damascus.data.weather.dto.WeatherDto
import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit

fun WeatherDto.toDomain(): WeatherInfo {
    return WeatherInfo(
        latitude = latitude,
        longitude = longitude,
        elevation = elevation,
        timezone = timezone.ifBlank { "GMT" },
        weather = Weather(
            temperature = currentWeatherDto.temperature,
            windSpeed = currentWeatherDto.windSpeed,
            windDirection = currentWeatherDto.windDirection,
            isDay = currentWeatherDto.isDay == 1,
            weatherCode = currentWeatherDto.weatherCode,
            time = currentWeatherDto.time.ifBlank { "" }
        ),
        units = WeatherUnit(
            temperatureUnit = currentWeatherUnitsDto.temperature.ifBlank { "°C" },
            windSpeedUnit = currentWeatherUnitsDto.windSpeed.ifBlank { "km/h" },
            windDirectionUnit = currentWeatherUnitsDto.windDirection.ifBlank { "°" }
        )
    )

}
fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45 -> "Fog"
        48 -> "Depositing rime fog"
        51 -> "Light drizzle"
        53 -> "Moderate drizzle"
        55 -> "Heavy drizzle"
        56 -> "Light freezing drizzle"
        57 -> "Heavy freezing drizzle"
        61 -> "Light rain"
        63 -> "Moderate rain"
        65 -> "Heavy rain"
        66 -> "Light freezing rain"
        67 -> "Heavy freezing rain"
        71 -> "Light snow"
        73 -> "Moderate snow"
        75 -> "Heavy snow"
        77 -> "Snow grains"
        80 -> "Light rain showers"
        81 -> "Moderate rain showers"
        82 -> "Heavy rain showers"
        85 -> "Light snow showers"
        86 -> "Heavy snow showers"
        95 -> "Thunderstorm"
        96 -> "Thunderstorm with light hail"
        99 -> "Thunderstorm with heavy hail"
        else -> "Unknown weather"
    }
}


