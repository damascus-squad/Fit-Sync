package org.damascus.data.location.repository

import org.damascus.data.weather.dto.LocationDto

interface LocationRepository {
    suspend fun searchCity(city: String): List<LocationDto>
}
