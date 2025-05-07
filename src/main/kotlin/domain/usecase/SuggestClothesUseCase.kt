package org.damascus.domain.usecase

import org.damascus.data.weather.dto.CurrentWeather
import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType
import org.damascus.domain.repository.ClothesRepository

class SuggestClothesUseCase(
    private val clothesRepository: ClothesRepository
) {
    operator fun invoke(currentWeather: CurrentWeather): List<Cloth> {
        val clothType = when {
            currentWeather.temperature <= -5 -> ClothType.VERY_HEAVY
            currentWeather.temperature in -5.0..5.0 -> ClothType.HEAVY
            currentWeather.temperature in 6.0..15.0 -> ClothType.MEDIUM
            currentWeather.temperature in 16.0..24.0 -> ClothType.LIGHT
            else -> ClothType.VERY_LIGHT
        }

        return clothesRepository.getClothByType(clothType)
    }
}

