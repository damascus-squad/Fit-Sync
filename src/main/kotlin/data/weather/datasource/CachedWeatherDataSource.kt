package org.damascus.data.weather.datasource

import org.damascus.data.weather.mapper.WeatherCacheEntryConverter
import org.damascus.data.weather.dto.WeatherDto
import org.damascus.domain.model.LocationCoordinate
import org.damascus.domain.model.WeatherInfo

class CachedWeatherDataSource(
    private val apiClient: WeatherDataSource,
    private val cacheService: WeatherCacheService,
    private val converter: WeatherCacheEntryConverter,
    private val cacheDurationMinutes: Long = 30
) : WeatherDataSource {

    override suspend fun getWeather(locationCoordinate: LocationCoordinate): WeatherDto {
        val cachedWeatherInfo = cacheService.getValidFromCache(locationCoordinate, cacheDurationMinutes)
        if (cachedWeatherInfo != null)
            return converter.weatherInfoToDto(cachedWeatherInfo)

        val weatherDtoFromApi = apiClient.getWeather(locationCoordinate)
        val weatherInfoToCache: WeatherInfo = converter.dtoToWeatherInfo(weatherDtoFromApi)
        cacheService.saveToCache(weatherInfoToCache)
        return weatherDtoFromApi
    }
}