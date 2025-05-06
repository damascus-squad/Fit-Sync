package org.damascus.data.datasource

import org.damascus.data.api.WeatherApiClient
import org.damascus.data.api.dto.WeatherDto

class WeatherRemoteDataSourceImpl(
    private val apiClient: WeatherApiClient
) : WeatherRemoteDataSource {

    override suspend fun getWeather(city: String): WeatherDto {
        return apiClient.fetchWeather(city)
    }
}