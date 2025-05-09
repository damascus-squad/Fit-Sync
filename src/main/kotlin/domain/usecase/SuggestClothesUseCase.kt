package org.damascus.domain.usecase

import org.damascus.domain.exception.IllegalTemperatureException
import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType
import org.damascus.domain.model.WeatherInfo
import org.damascus.domain.repository.ClothesRepository

class SuggestClothesUseCase(
    private val clothesRepository: ClothesRepository
) {
    operator fun invoke(currentWeather: WeatherInfo): List<Cloth> {
        val temperature = currentWeather.weather.temperature
        val weatherUnit = currentWeather.units.temperatureUnit

        if (temperature.isNaN()) throw IllegalTemperatureException()

        val clothType: ClothType = if (weatherUnit == "C") {
            when {
                temperature <= VERY_HEAVY_TEMPERATURE_THRESHOLDS_C -> ClothType.VERY_HEAVY
                temperature <= HEAVY_TEMPERATURE_THRESHOLDS_C -> ClothType.HEAVY
                temperature <= MEDIUM_TEMPERATURE_THRESHOLDS_C -> ClothType.MEDIUM
                temperature <= LIGHT_TEMPERATURE_THRESHOLDS_C -> ClothType.LIGHT
                else -> ClothType.VERY_LIGHT
            }
        } else {
            when {
                temperature <= VERY_HEAVY_TEMPERATURE_THRESHOLDS_F -> ClothType.VERY_HEAVY
                temperature <= HEAVY_TEMPERATURE_THRESHOLDS_F -> ClothType.HEAVY
                temperature <= MEDIUM_TEMPERATURE_THRESHOLDS_F -> ClothType.MEDIUM
                temperature <= LIGHT_TEMPERATURE_THRESHOLDS_F -> ClothType.LIGHT
                else -> ClothType.VERY_LIGHT
            }
        }

        return clothesRepository.getClothsByType(clothType)
    }

    private companion object TemperatureThresholdsCelsius {
        const val VERY_HEAVY_TEMPERATURE_THRESHOLDS_C = 5.0
        const val HEAVY_TEMPERATURE_THRESHOLDS_C = 12.5
        const val MEDIUM_TEMPERATURE_THRESHOLDS_C = 20.0
        const val LIGHT_TEMPERATURE_THRESHOLDS_C = 28.0

        const val VERY_HEAVY_TEMPERATURE_THRESHOLDS_F = 41.0
        const val HEAVY_TEMPERATURE_THRESHOLDS_F = 54.5
        const val MEDIUM_TEMPERATURE_THRESHOLDS_F = 68.0
        const val LIGHT_TEMPERATURE_THRESHOLDS_F = 82.4

    }
}
