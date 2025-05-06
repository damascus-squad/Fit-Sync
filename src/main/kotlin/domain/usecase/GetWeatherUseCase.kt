package org.damascus.domain.usecase

import org.damascus.domain.repository.WeatherRepository

class GetWeatherUseCase(
    private val repository: WeatherRepository
) {

}