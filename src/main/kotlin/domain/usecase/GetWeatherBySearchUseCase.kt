package org.damascus.domain.usecase

import org.damascus.data.weather.dto.LocationDto
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.repository.WeatherRepository

class GetWeatherBySearchUseCase(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(location: LocationDto): WeatherInfo {
        return weatherRepository.getWeatherBySearch(location)
    }
}