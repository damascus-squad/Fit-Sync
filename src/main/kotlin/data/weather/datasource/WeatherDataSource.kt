package org.damascus.data.weather.datasource


import org.damascus.data.weather.dto.WeatherDto

interface WeatherDataSource {
    suspend fun getWeatherByCity(cityName: String, country: String): WeatherDto
}
