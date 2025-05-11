package org.damascus.data.location.repository

import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.location.mapper.toDomain
import org.damascus.domain.model.Location

class LocationRepositoryImpl(
    private val locationDataSource: LocationDataSource
) : LocationRepository {

    override suspend fun searchCity(city: String): List<Location> {
        return locationDataSource.searchCity(city).map { it.toDomain() }
    }
}
