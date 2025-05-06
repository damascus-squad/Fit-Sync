package org.damascus.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    val temperature: Double,
    val condition: String,
    val windSpeed: Double
)