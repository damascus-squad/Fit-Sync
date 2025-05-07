package org.damascus.domain.model

data class WeatherInfo(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val timezone: String,
    val weather: Weather,
    val units: WeatherUnit
)
