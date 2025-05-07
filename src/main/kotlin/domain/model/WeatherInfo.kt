package org.damascus.domain.model

data class Weather(
    val temperature: Double,
    val windSpeed: Double,
    val windDirection: Int,
    val isDay: Boolean,
    val weatherCode: Int,
    val time: String
)

data class WeatherUnit(
    val temperatureUnit: String,
    val windSpeedUnit: String,
    val windDirectionUnit: String
)

data class WeatherInfo(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val timezone: String,
    val weather: Weather,
    val units: WeatherUnit
)
