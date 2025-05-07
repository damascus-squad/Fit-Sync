package org.damascus.data.location.dataSource

import org.damascus.data.location.dto.AutoLocationDto
import org.damascus.data.weather.dto.LocationDto

interface LocationDataSource {
    suspend fun getCityCoordinates(city: String, country: String): LocationDto?
    suspend fun getCurrentLocation(): AutoLocationDto?
}