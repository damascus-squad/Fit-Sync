package org.damascus.data.weather.repository

import org.damascus.data.weather.datasource.WeatherDataSource
import org.damascus.data.weather.mapper.toWeatherInfo
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.repository.WeatherRepository


class WeatherRepositoryImp(
    private val dataSource: WeatherDataSource
) : WeatherRepository {
    override suspend fun getWeatherByCity(cityName: String, country: String): WeatherInfo {
        val dto = dataSource.getWeatherByCity(cityName, country)
        return dto.toWeatherInfo()
    }
}