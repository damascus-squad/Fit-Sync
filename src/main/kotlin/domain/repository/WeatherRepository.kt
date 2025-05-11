package org.damascus.domain.repository

import org.damascus.domain.model.Location
import org.damascus.domain.model.WeatherInfo

interface WeatherRepository {
    suspend fun getWeatherByCity(location: Location): WeatherInfo
    suspend fun getWeatherByIp(): WeatherInfo
}