package org.damascus.domain.usecase

import org.damascus.domain.model.Location
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.repository.WeatherRepository

class GetWeatherUseCase(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(location: Location): WeatherInfo {
        return weatherRepository.getWeatherByCity(location)
    }
}
