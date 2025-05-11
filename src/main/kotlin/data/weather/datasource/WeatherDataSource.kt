package org.damascus.data.weather.datasource

import org.damascus.data.weather.dto.LocationDto
import org.damascus.data.weather.dto.WeatherDto

interface WeatherDataSource {
    suspend fun getWeatherByCity(location: LocationDto): WeatherDto
    suspend fun getWeatherByIp(): WeatherDto
}
