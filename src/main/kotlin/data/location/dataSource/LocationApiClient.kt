package org.damascus.data.location.dataSource

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.damascus.data.location.dto.IpLocationDto
import org.damascus.data.weather.dto.LocationDto
import org.damascus.data.weather.dto.LocationResultDto

class LocationApiClient(
    private val client: HttpClient
) : LocationDataSource {

    private companion object {
        private const val GEO_BASE_URL = "https://geocoding-api.open-meteo.com/v1/search"
        private const val IP_BASE_URL = "http://ip-api.com/json/"
    }

    override suspend fun getCityCoordinates(city: String, country: String): LocationDto? {
        val response = client.get {
            url(GEO_BASE_URL)
            parameter("name", "$city,$country")
            parameter("count", 1)
        }
        return response.body<LocationResultDto>().results.firstOrNull()
    }

    override suspend fun getCurrentLocation(): IpLocationDto? {
        return client.get(IP_BASE_URL).body()
    }
}
