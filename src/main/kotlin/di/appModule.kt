package org.damascus.di

import org.damascus.data.clothes.repository.ClothesRepositoryImpl
import org.damascus.data.weather.repository.WeatherRepositoryImp
import org.damascus.domain.repository.ClothesRepository
import org.damascus.domain.repository.WeatherRepository
import org.damascus.domain.usecase.GetWeatherUseCase
import org.damascus.presentation.io.Printer
import org.damascus.presentation.ui.ClothesSuggesterByIPCLI
import org.damascus.presentation.ui.FitSyncApp
import org.damascus.domain.usecase.SuggestClothesUseCase
import org.koin.dsl.module
import presentation.io.ConsolePrinter
import presentation.io.ConsoleReader
import presentation.io.InputReader
import presentation.ui.ClothesSuggesterByCityNameCLI

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
                json(get())
            }
        }
    }

    single {
        Json { ignoreUnknownKeys = true }
    }

    single<LocationDataSource> { LocationApiClient(get()) }
    single<WeatherDataSource> { WeatherApiClient(get(), get()) }
    single<WeatherRepository> { WeatherRepositoryImp(get()) }
    single<ClothesRepository> { ClothesRepositoryImpl(get()) }

    single { GetWeatherUseCase(get()) }
    single { SuggestClothesUseCase(get()) }
    single { GetWeatherByIpUseCase(get()) }

    //IO
    single<Printer> { ConsolePrinter() }
    single<InputReader> { ConsoleReader() }

    //UI
    single { ClothesSuggesterByCityNameCLI(get(), get(), get(), get()) }
    single { ClothesSuggesterByIPCLI(get(), get(), get()) }

    //app
    single { FitSyncApp(get(), get(), get(), get()) }
}