package org.damascus.data.weather.datasource

import org.damascus.data.weather.dto.WeatherDto
import org.damascus.domain.model.LocationCoordinate

interface WeatherDataSource {
    suspend fun getWeather(locationCoordinate: LocationCoordinate): WeatherDto
}