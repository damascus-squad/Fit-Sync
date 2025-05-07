package org.damascus.di

import org.damascus.data.weather.repository.WeatherRepositoryImp
import org.damascus.domain.repository.WeatherRepository
import org.damascus.domain.usecase.GetWeatherUseCase
import org.koin.dsl.module

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.damascus.data.location.dataSource.LocationApiClient
import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.weather.datasource.WeatherApiClient
import org.damascus.data.weather.datasource.WeatherDataSource
import org.damascus.domain.usecase.GetWeatherByIpUseCase


val appModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    single {
        Json { ignoreUnknownKeys = true }
    }

    single<LocationDataSource> { LocationApiClient(get(), get()) }
    single<WeatherDataSource> { WeatherApiClient(get(), get()) }

    single<WeatherRepository> { WeatherRepositoryImp(get()) }

    single { GetWeatherUseCase(get()) }
    single { GetWeatherByIpUseCase(get()) }
}