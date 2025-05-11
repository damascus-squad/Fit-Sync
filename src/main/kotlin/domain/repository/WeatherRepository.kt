package org.damascus.domain.repository

import org.damascus.data.weather.dto.LocationDto
import org.damascus.domain.model.WeatherInfo


interface WeatherRepository {
    suspend fun getWeatherByCity(cityName: String, country: String): WeatherInfo
    suspend fun getWeatherByIp(): WeatherInfo
    suspend fun getWeatherBySearch(location: LocationDto): WeatherInfo
}