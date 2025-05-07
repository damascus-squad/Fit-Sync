package org.damascus.data.weather.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class CurrentWeatherUnits(
    val time: String,
    val interval: String,
    val temperature: String,
    @SerialName("windspeed") val windSpeed: String,
    @SerialName("winddirection") val windDirection: String,
    @SerialName("is_day") val isDay: String,
    @SerialName("weathercode") val weatherCode: String
)
