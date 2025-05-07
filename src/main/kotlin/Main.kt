package org.damascus

import kotlinx.coroutines.runBlocking
import org.damascus.data.weather.datasource.WeatherApiClient
import org.damascus.data.weather.repository.WeatherRepositoryImp
import org.damascus.data.remote.HttpClientProvider
import org.damascus.data.weather.mapper.getWeatherDescription
import org.damascus.domain.usecase.GetWeatherUseCase

fun main() = runBlocking {
    val httpClient = HttpClientProvider.client

    val dataSource = WeatherApiClient(httpClient)
    val repository = WeatherRepositoryImp(dataSource)
    val getWeatherUseCase = GetWeatherUseCase(repository)

    val city = "Amman"
    val country = "Jordan"

    try {
        val weatherInfo = getWeatherUseCase(city, country)
        println("\nWeather in $city, $country:")
        println("Temperature: ${weatherInfo.weather.temperature} ${weatherInfo.units.temperatureUnit}")
        println("Wind: ${weatherInfo.weather.windSpeed} ${weatherInfo.units.windSpeedUnit}")
        println("Condition Code: ${weatherInfo.weather.weatherCode}")
        println("Condition: ${getWeatherDescription(weatherInfo.weather.weatherCode)}")
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}