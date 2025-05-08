package org.damascus.di

import org.damascus.data.weather.repository.WeatherRepositoryImp
import org.damascus.domain.repository.WeatherRepository
import org.damascus.domain.usecase.GetWeatherUseCase
import org.damascus.presentation.io.Printer
import org.damascus.presentation.ui.ClothesSuggesterByIPCLI
import org.damascus.presentation.ui.FitSyncApp
import org.koin.dsl.module
import presentation.io.ConsolePrinter
import presentation.io.ConsoleReader
import presentation.io.InputReader
import presentation.ui.ClothesSuggesterByCityNameCLI

val appModule = module {
    single<WeatherRepository> { WeatherRepositoryImp(get()) }
    single { GetWeatherUseCase(get()) }

    //IO
    single<Printer> { ConsolePrinter() }
    single<InputReader> { ConsoleReader() }

    //UI
    single { ClothesSuggesterByCityNameCLI(get(), get(), get(), get()) }
    single { ClothesSuggesterByIPCLI(get(), get(), get()) }

    //app
    single { FitSyncApp(get(), get(), get(), get()) }

}

