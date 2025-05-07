package org.damascus.di

import org.damascus.data.weather.repository.WeatherRepositoryImp
import org.damascus.domain.repository.WeatherRepository
import org.damascus.domain.usecase.GetWeatherUseCase
import org.koin.dsl.module

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import org.damascus.data.weather.datasource.WeatherApiClient
import org.damascus.data.weather.datasource.WeatherDataSource


val appModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
    }
    single<WeatherDataSource> { WeatherApiClient(get()) }
    single<WeatherRepository> { WeatherRepositoryImp(get()) }
    single { GetWeatherUseCase(get()) }
}
