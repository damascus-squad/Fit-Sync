package org.damascus.data.weather.repository

import org.damascus.data.location.mapper.toDto
import org.damascus.data.weather.datasource.WeatherDataSource
import org.damascus.data.weather.mapper.toDomain
import org.damascus.domain.model.Location
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.repository.WeatherRepository
import org.damascus.data.weather.datasource.WeatherCacheManager


class WeatherRepositoryImp(
    private val dataSource: WeatherDataSource,
    private val cacheManager: WeatherCacheManager
) : WeatherRepository {
    override suspend fun getWeatherByCity(location: Location): WeatherInfo {
        val cacheKey =
            "city:${location.name},${location.region},${location.country},${location.latitude},${location.longitude}"
        val cachedData = cacheManager.readCache(cacheKey)

        return if (cachedData != null) {
            cachedData
        } else {
            val dto = dataSource.getWeatherByCity(location.toDto())
            val freshData = dto.toDomain()
            cacheManager.writeCache(cacheKey, freshData)
            freshData
        }
    }

    override suspend fun getWeatherByIp(): WeatherInfo {
        val cacheKey = "by_ip"
        val cachedData = cacheManager.readCache(cacheKey)

        return if (cachedData != null) {
            cachedData
        } else {
            val dto = dataSource.getWeatherByIp()
            val freshData = dto.toDomain()
            cacheManager.writeCache(cacheKey, freshData)
            freshData
        }
    }

}
