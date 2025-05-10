package org.damascus.data.weather.datasource

import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.weather.cache.io.WeatherCacheService
import org.damascus.data.weather.mapper.WeatherDataConverter
import org.damascus.data.weather.dto.WeatherDto
import org.damascus.domain.model.LocationCoordinate
import org.damascus.domain.model.WeatherInfo

class CachedWeatherDataSource(
    private val apiClient: WeatherDataSource,
    private val cacheService: WeatherCacheService,
    private val converter: WeatherDataConverter,
    private val locationDataSource: LocationDataSource,
    private val cacheDurationMinutes: Long = 30
) : WeatherDataSource {

    override suspend fun getWeatherByCity(cityName: String, country: String): WeatherDto {
        val location = locationDataSource.getCityCoordinates(cityName, country)
            ?: throw LocationNotFoundException(cityName, country)
        return getCachedWeather(LocationCoordinate(location.latitude, location.longitude))
    }

    override suspend fun getWeatherByIp(): WeatherDto {
        val location = locationDataSource.getCurrentLocation()
            ?: throw IpNotFoundException()
        return getCachedWeather(LocationCoordinate(location.latitude, location.longitude))
    }

    private suspend fun getCachedWeather(locationCoordinate: LocationCoordinate): WeatherDto {
        val cachedWeatherInfo = cacheService.getValidFromCache(locationCoordinate, cacheDurationMinutes)
        if (cachedWeatherInfo != null)
            return converter.weatherInfoToDto(cachedWeatherInfo)

        val weatherDtoFromApi = apiClient.getWeatherByCity(locationCoordinate.latitude.toString(), locationCoordinate.longitude.toString())
        val weatherInfoToCache: WeatherInfo = converter.dtoToWeatherInfo(weatherDtoFromApi)
        cacheService.saveToCache(weatherInfoToCache)
        return weatherDtoFromApi
    }
}