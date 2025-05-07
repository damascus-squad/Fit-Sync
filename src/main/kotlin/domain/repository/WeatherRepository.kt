package org.damascus.domain.repository

import org.damascus.domain.model.WeatherInfo


interface WeatherRepository {
    suspend fun getWeatherByCity(cityName: String, country: String): WeatherInfo

    suspend fun getWeatherByIp(): WeatherInfo
}