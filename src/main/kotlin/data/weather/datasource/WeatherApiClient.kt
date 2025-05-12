package org.damascus.data.weather.datasource

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.weather.dto.LocationDto
import org.damascus.data.weather.dto.WeatherDto
import org.damascus.data.weather.mapper.toDomain
import org.damascus.data.weather.mapper.toDto
import org.damascus.domain.exception.LocationNotFoundException

class WeatherApiClient(
    private val client: HttpClient,
    private val locationDataSource: LocationDataSource,
    private val cacheManager: WeatherCacheManager
) : WeatherDataSource {

    override suspend fun getWeatherByCity(location: LocationDto): WeatherDto {
        return getWeatherByLocation(location)
    }

    override suspend fun getWeatherByIp(): WeatherDto {
        val ipLocation = locationDataSource.getCurrentLocation()
            ?: throw LocationNotFoundException("Could not determine location from IP")

        val locationDto = LocationDto(
            latitude = ipLocation.latitude,
            longitude = ipLocation.longitude
        )

        val cacheKey = "ip:${ipLocation.latitude},${ipLocation.longitude}"

        val cachedData = cacheManager.readCache(cacheKey)
        if (cachedData != null) {
            return cachedData.toDto()
        }

        val dto = getWeatherByLocation(locationDto)
        cacheManager.writeCache(cacheKey, dto.toDomain())
        return dto
    }

    private suspend fun getWeatherByLocation(location: LocationDto): WeatherDto {
        return client.get {
            url(WEATHER_BASE_URL)
            parameter("latitude", location.latitude)
            parameter("longitude", location.longitude)
            parameter("current_weather", true)
            parameter("timezone", "auto")
        }.body()
    }

    private companion object {
        const val WEATHER_BASE_URL = "https://api.open-meteo.com/v1/forecast"
    }
}