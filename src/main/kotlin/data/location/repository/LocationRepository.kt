package org.damascus.data.location.repository

import org.damascus.domain.model.Location

interface LocationRepository {
    suspend fun searchCity(city: String): List<Location>
}