package org.damascus.domain.usecase

import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.repository.WeatherRepository



class GetWeatherUseCase(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(city: String, country: String): WeatherInfo {
        return weatherRepository.getWeatherByCity(city, country)
    }
}
