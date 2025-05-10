package org.damascus.data.location.dataSource

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json
import org.damascus.data.location.dto.IpLocationDto
import org.damascus.data.weather.dto.LocationDto
import org.damascus.data.weather.dto.LocationResultDto

class LocationApiClient(
    private val client: HttpClient,
    private val jsonParser: Json
) : LocationDataSource {

    override suspend fun getCityCoordinates(city: String, country: String): LocationDto? {
        val response = client.get {
            url(GEO_BASE_URL)
            parameter("name", "$city,$country")
        }

        val resultDto = jsonParser.decodeFromString(LocationResultDto.serializer(), response.bodyAsText())
        return resultDto.results?.firstOrNull()
    }


    override suspend fun getCurrentLocation(): IpLocationDto? {
        return client.get(IP_BASE_URL).body()
    }

    private companion object {
        const val GEO_BASE_URL = "https://geocoding-api.open-meteo.com/v1/search"
        const val IP_BASE_URL = "http://ip-api.com/json/"
    }

}
