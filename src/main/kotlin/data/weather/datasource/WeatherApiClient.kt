package org.damascus.data.weather.datasource

import org.damascus.data.weather.dto.WeatherDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.url
import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.weather.dto.LocationDto
import org.damascus.domain.exception.LocationNotFoundException

class WeatherApiClient(
    private val client: HttpClient,
    private val locationDataSource: LocationDataSource
) : WeatherDataSource {
    companion object {
        private const val WEATHER_BASE_URL = "https://api.open-meteo.com/v1/forecast"
    }
    override suspend fun getWeatherByCity(cityName: String, country: String): WeatherDto {
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
        return client.get {
            url(WEATHER_BASE_URL)
            parameter("latitude", location.latitude)
            parameter("longitude", location.longitude)
            parameter("current_weather", true)
            parameter("timezone", "auto")
        }.body()
    }}