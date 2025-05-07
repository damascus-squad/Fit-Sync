package org.damascus.data.weather.datasource

import org.damascus.data.weather.dto.WeatherDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.weather.dto.LocationDto
import org.damascus.domain.exception.LocationNotFoundException
import org.damascus.data.weather.dto.LocationResultDto
import org.damascus.domain.exception.WeatherNotFoundException

class WeatherApiClient(
    private val client: HttpClient,
    private val locationDataSource: LocationDataSource
) : WeatherDataSource {
    override suspend fun getWeatherByCity(cityName: String, country: String): WeatherDto {
        return try {
            val location = locationDataSource.getCityCoordinates(cityName, country)
            ?: throw LocationNotFoundException("City not found: $cityName, $country")

        return getWeatherByLocation(location)
    }

    override suspend fun getWeatherByIp(): WeatherDto {
            val ipLocation = locationDataSource.getCurrentLocation()
            ?: throw LocationNotFoundException("Could not determine location from IP")

        return getWeatherByLocation(LocationDto(ipLocation.lat, ipLocation.lon))
    }


    private suspend fun getWeatherByLocation(location: LocationDto): WeatherDto {
        val weatherUrl = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=${location.latitude}" +
                    "&longitude=${location.longitude}" +
                    "&current_weather=true" +
                    "&timezone=auto"

            client.get(weatherUrl).body()
        } catch (e: Exception) {
            throw WeatherNotFoundException(e)
        }
    }
}