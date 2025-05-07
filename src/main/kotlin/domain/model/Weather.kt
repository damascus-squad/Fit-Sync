package org.damascus.domain.model

data class Weather(
    val temperature: Double,
    val windSpeed: Double,
    val windDirection: Int,
    val isDay: Boolean,
    val weatherCode: Int,
    val time: String
)