package org.damascus.data.weather.repository

import org.damascus.data.location.mapper.toDto
import org.damascus.data.weather.datasource.WeatherDataSource
import org.damascus.data.weather.mapper.toDomain
import org.damascus.domain.model.Location
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.repository.WeatherRepository


class WeatherRepositoryImp(
    private val dataSource: WeatherDataSource
) : WeatherRepository {
    override suspend fun getWeatherByCity(location: Location): WeatherInfo {
        val dto = dataSource.getWeatherByCity(location.toDto())
        return dto.toDomain()
    }

    override suspend fun getWeatherByIp(): WeatherInfo {
        val dto = dataSource.getWeatherByIp()
        return dto.toDomain()
    }

}