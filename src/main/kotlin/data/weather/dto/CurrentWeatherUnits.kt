package org.damascus.data.weather.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrentWeatherUnits(
    val time: String,
    val interval: String,
    val temperature: String,
    val windSpeed: String,
    val windDirection: String,
    @SerialName("is_day") val isDay: String,
    val weatherCode: String
)