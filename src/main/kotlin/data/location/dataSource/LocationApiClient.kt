package org.damascus.data.location.dataSource

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import org.damascus.data.location.dto.AutoLocationDto
import org.damascus.data.weather.dto.LocationDto
import org.damascus.data.weather.dto.LocationResultDto

class LocationApiClient(
    private val client: HttpClient,
    private val json: Json,
) : LocationDataSource {
    override suspend fun getCityCoordinates(city: String, country: String): LocationDto? {
        val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$city,$country&count=1"
        val response = client.get(geoUrl)
        val result = json.decodeFromString<LocationResultDto>(response.bodyAsText())
        return result.results.firstOrNull()
    }

    override suspend fun getCurrentLocation(): AutoLocationDto? {
        val ipUrl = "http://ip-api.com/json/"
        val response = client.get(ipUrl)
        val result = json.decodeFromString<AutoLocationDto>(response.bodyAsText())
        return result
    }
}