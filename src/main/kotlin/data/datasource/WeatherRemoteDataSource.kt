package org.damascus.data.datasource

import org.damascus.data.api.dto.WeatherDto

interface WeatherRemoteDataSource {
    suspend fun getWeather(city: String): WeatherDto
}