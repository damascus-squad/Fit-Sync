package org.damascus.data.location.repository

import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.weather.dto.LocationDto

class LocationRepositoryImpl(
    private val locationDataSource: LocationDataSource
) : LocationRepository {

    override suspend fun searchCity(city: String): List<LocationDto> {
        return locationDataSource.searchCity(city)
    }
}
