package org.damascus.domain.usecase

import org.damascus.data.location.repository.LocationRepository
import org.damascus.domain.model.Location

class SearchCityUseCase(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(city: String): List<Location> {
        return locationRepository.searchCity(city)
    }
}