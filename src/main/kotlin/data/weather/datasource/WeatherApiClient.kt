package org.damascus.data.weather.datasource

import org.damascus.data.weather.dto.WeatherDto


import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.damascus.data.weather.dto.LocationResultDto

class WeatherApiClient(private val client: HttpClient) : WeatherDataSource {
    override suspend fun getWeatherByCity(cityName: String, country: String): WeatherDto {
        val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$cityName,$country&count=1"
        val locationDto = client.get(geoUrl).body<LocationResultDto>().results.first()

        val weatherUrl = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${locationDto.latitude}" +
                "&longitude=${locationDto.longitude}" +
                "&current_weather=true" +
                "&timezone=auto"

        return client.get(weatherUrl).body()
    }
}