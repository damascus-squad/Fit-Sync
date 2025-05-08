package org.damascus.domain.usecase

import org.damascus.data.weather.dto.CurrentWeather
import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType
import org.damascus.domain.repository.ClothesRepository

class SuggestClothesUseCase(
    private val clothesRepository: ClothesRepository
) {
    operator fun invoke(currentWeather: CurrentWeather): List<Cloth> {
        val temperature = currentWeather.temperature

        if (temperature.isNaN()) throw IllegalTemperatureException()


        var clothType = ClothType.VERY_LIGHT

        if (temperature <= VERY_HEAVY) {
            clothType = ClothType.VERY_HEAVY
        } else if (temperature <= HEAVY) {
            clothType = ClothType.HEAVY
        } else if (temperature <= MEDIUM) {
            clothType = ClothType.MEDIUM
        } else if (temperature <= LIGHT) {
            clothType = ClothType.LIGHT
        }

        return clothesRepository.getClothByType(clothType)
    }

    private companion object TemperatureThresholds {
        const val VERY_HEAVY = 5.0
        const val HEAVY = 12.5
        const val MEDIUM = 20.0
        const val LIGHT = 28.0
    }
}

class IllegalTemperatureException() : IllegalArgumentException("NAN: The temperature is not valid")


