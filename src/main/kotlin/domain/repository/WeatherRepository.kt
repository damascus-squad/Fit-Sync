package org.damascus.domain.repository

import org.damascus.domain.model.LocationCoordinate
import org.damascus.domain.model.WeatherInfo

interface WeatherRepository {
    suspend fun getWeather(locationCoordinate: LocationCoordinate): WeatherInfo
}