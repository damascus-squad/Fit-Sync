package org.damascus.data.weather.fake

import org.damascus.domain.model.Weather
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.model.WeatherUnit

class WeatherDomyData {
    fun getWeatherInfoDomyData(): List<WeatherInfo> {
        return listOf(
            weatherInfoDomy1,
//            weatherInfoDomy2,
//            weatherInfoDomy3
        )
    }
}

val weatherInfoDomy1 = WeatherInfo(
    latitude = 1.1,
    longitude = 1.1,
    elevation = 1.1,
    timezone = "timezone1",
    weather = Weather(
        temperature = 1.1,
        windSpeed = 1.1,
        windDirection = 1,
        isDay = true,
        weatherCode = 1,
        time = "time1"
    ),
    units = WeatherUnit(
        temperatureUnit = "temperatureUnit1",
        windSpeedUnit = "windSpeedUnit1",
        windDirectionUnit = "windDirectionUnit1"
    )
)

val weatherInfoDomy2 = WeatherInfo(
    latitude = 2.2,
    longitude = 2.2,
    elevation = 2.2,
    timezone = "timezone2",
    weather = Weather(
        temperature = 2.2,
        windSpeed = 2.2,
        windDirection = 2,
        isDay = true,
        weatherCode = 2,
        time = "time2"
    ),
    units = WeatherUnit(
        temperatureUnit = "temperatureUnit2",
        windSpeedUnit = "windSpeedUnit2",
        windDirectionUnit = "windDirectionUnit2"
    )
)

val weatherInfoDomy3 = WeatherInfo(
    latitude = 3.3,
    longitude = 3.3,
    elevation = 3.3,
    timezone = "timezone3",
    weather = Weather(
        temperature = 3.3,
        windSpeed = 3.3,
        windDirection = 3,
        isDay = true,
        weatherCode = 3,
        time = "time3"
    ),
    units = WeatherUnit(
        temperatureUnit = "temperatureUnit3",
        windSpeedUnit = "windSpeedUnit3",
        windDirectionUnit = "windDirectionUnit3"
    )
)