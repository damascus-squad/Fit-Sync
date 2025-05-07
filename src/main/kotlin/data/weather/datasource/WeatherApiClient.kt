package org.damascus.data.weather.datasource

import org.damascus.data.weather.dto.WeatherDto


import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.damascus.data.weather.dto.LocationResultDto
import org.damascus.domain.exception.WeatherNotFoundException

class WeatherApiClient(private val client: HttpClient) : WeatherDataSource {
    override suspend fun getWeatherByCity(cityName: String, country: String): WeatherDto {
        return try {
            val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$cityName,$country&count=1"
            val locationDto = client.get(geoUrl).body<LocationResultDto>().results.first()

            val weatherUrl = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=${locationDto.latitude}" +
                    "&longitude=${locationDto.longitude}" +
                    "&current_weather=true" +
                    "&timezone=auto"

            client.get(weatherUrl).body()
        } catch (e: Exception) {
            throw WeatherNotFoundException(e)
        }
    }
}