package org.damascus.domain.usecase

import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.repository.WeatherRepository

class GetWeatherByIpUseCase(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(): WeatherInfo {
        return weatherRepository.getWeatherByIp()
    }
}