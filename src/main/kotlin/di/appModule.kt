package org.damascus.di

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.damascus.data.clothes.datasource.ClothesDataSource
import org.damascus.data.clothes.datasource.ClothesDataSourceImp
import org.damascus.data.clothes.repository.ClothesRepositoryImpl
import org.damascus.data.location.dataSource.LocationApiClient
import org.damascus.data.location.dataSource.LocationDataSource
import org.damascus.data.location.repository.LocationRepository
import org.damascus.data.location.repository.LocationRepositoryImpl
import org.damascus.data.weather.datasource.WeatherApiClient
import org.damascus.data.weather.datasource.WeatherDataSource
import org.damascus.data.weather.repository.WeatherRepositoryImp
import org.damascus.domain.repository.ClothesRepository
import org.damascus.domain.repository.WeatherRepository
import org.damascus.domain.usecase.*
import org.damascus.presentation.io.ConsoleDisplay
import org.damascus.presentation.ui.ClothesSuggesterByCitySearchCli
import org.damascus.presentation.ui.ClothesSuggesterByIpCli
import org.damascus.presentation.ui.FitSyncApp
import org.koin.dsl.module
import presentation.io.ConsolePrinter
import presentation.io.ConsoleReader
import presentation.io.InputReader
import presentation.ui.ClothesSuggesterByCityNameCli


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
    single<ClothesDataSource> { ClothesDataSourceImp() }
    single<LocationDataSource> { LocationApiClient(get()) }
    single<WeatherDataSource> { WeatherApiClient(get(), get()) }
    single<WeatherRepository> { WeatherRepositoryImp(get()) }
    single<ClothesRepository> { ClothesRepositoryImpl(get()) }
    single<LocationRepository> { LocationRepositoryImpl(get()) }

    single { GetWeatherUseCase(get()) }
    single { SuggestClothesUseCase(get()) }
    single { GetWeatherByIpUseCase(get()) }
    single { GetWeatherBySearchUseCase(get()) }
    single { SearchCityUseCase(get()) }

    //IO
    single<ConsoleDisplay> { ConsolePrinter() }
    single<InputReader> { ConsoleReader() }

    //UI
    single { ClothesSuggesterByCityNameCli(get(), get(), get(), get()) }
    single { ClothesSuggesterByIpCli(get(), get(), get()) }
    single { ClothesSuggesterByCitySearchCli(get(), get(), get(), get(), get()) }

    //app
    single { FitSyncApp(get(), get(), get(), get(), get()) }


}