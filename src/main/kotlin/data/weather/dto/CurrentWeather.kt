package org.damascus.data.weather.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeather(
    val time: String,
    val interval: Int,
    val temperature: Double,
    @SerialName("windspeed")
    val windSpeed: Double,
    @SerialName("winddirection")
    val windDirection: Int,
    @SerialName("is_day")
    val isDay: Int,
    @SerialName("weathercode")
    val weatherCode: Int
)
