package org.damascus.domain.usecase

import org.damascus.data.location.repository.LocationRepository
import org.damascus.data.weather.dto.LocationDto

class SearchCityUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(city: String): List<LocationDto> {
        return locationRepository.searchCity(city)
    }
}