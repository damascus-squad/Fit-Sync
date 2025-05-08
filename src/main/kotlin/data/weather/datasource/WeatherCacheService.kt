package org.damascus.data.weather.datasource

import org.damascus.domain.model.LocationCoordinate
import org.damascus.domain.model.WeatherInfo

interface WeatherCacheService {
    fun saveToCache(weatherInfo: WeatherInfo)
    fun getFromCache(locationCoordinate: LocationCoordinate): WeatherInfo?
    fun isCacheValid(locationCoordinate: LocationCoordinate): Boolean
    fun getValidFromCache(locationCoordinate: LocationCoordinate, maxAgeMinutes: Long): WeatherInfo?
}