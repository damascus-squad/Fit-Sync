package org.damascus.data.weather.datasource

import org.damascus.data.weather.dto.WeatherDto
import org.damascus.domain.model.LocationCoordinate

class WeatherApiClient : WeatherDataSource {

    override suspend fun getWeather(locationCoordinate: LocationCoordinate): WeatherDto {
        TODO("Not yet implemented")
    }
}