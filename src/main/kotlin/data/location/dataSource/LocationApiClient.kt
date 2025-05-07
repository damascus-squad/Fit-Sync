package org.damascus.data.location.dataSource

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.damascus.data.location.dto.AutoLocationDto
import org.damascus.data.weather.dto.LocationDto
import org.damascus.data.weather.dto.LocationResultDto

class LocationApiClient(
    private val client: HttpClient
) : LocationDataSource {

    override suspend fun getCityCoordinates(city: String, country: String): LocationDto? {
        val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$city,$country&count=1"
        return client.get(geoUrl).body<LocationResultDto>().results.firstOrNull()
    }

    override suspend fun getCurrentLocation(): AutoLocationDto? {
        val ipUrl = "http://ip-api.com/json/"
        return client.get(ipUrl).body()
    }
}
