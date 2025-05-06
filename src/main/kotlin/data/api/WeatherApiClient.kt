package org.damascus.data.api

import org.damascus.data.api.dto.WeatherDto

interface WeatherApiClient {
    suspend fun fetchWeather(city: String): WeatherDto
}