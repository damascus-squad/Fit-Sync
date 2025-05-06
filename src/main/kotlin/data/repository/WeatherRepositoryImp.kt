package org.damascus.data.repository

import org.damascus.data.datasource.WeatherRemoteDataSource
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.repository.WeatherRepository

class WeatherRepositoryImp(
    private val remoteDataSource: WeatherRemoteDataSource
) : WeatherRepository {

    override suspend fun getWeather(city: String): WeatherInfo {
        val dto = remoteDataSource.getWeather(city)
        return WeatherInfo(
            temperature = dto.temperature,
            condition = dto.condition
        )
    }
}
