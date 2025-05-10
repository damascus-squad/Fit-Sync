package org.damascus.data.weather.dto

import kotlinx.serialization.Serializable

@Serializable
data class CsvWeatherModel(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val timezone: String,
    val temperature: Double,
    val windSpeed: Double,
    val windDirection: Int,
    val isDay: Boolean,
    val weatherCode: Int,
    val time: String,
    val temperatureUnit: String,
    val windSpeedUnit: String,
    val windDirectionUnit: String,
    val timestamp: String
) {
    companion object {
        val HEADERS = listOf(
            "latitude", "longitude", "elevation", "timezone",
            "temperature", "windSpeed", "windDirection", "isDay", "weatherCode", "time",
            "temperatureUnit", "windSpeedUnit", "windDirectionUnit", "timestamp"
        )
    }
}