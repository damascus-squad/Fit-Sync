package org.damascus.di

import org.damascus.data.weather.repository.WeatherRepositoryImp
import org.damascus.domain.repository.WeatherRepository
import org.damascus.domain.usecase.GetWeatherUseCase
import org.koin.dsl.module

val appModule = module {
    single<WeatherRepository> { WeatherRepositoryImp(get()) }
    single { GetWeatherUseCase(get()) }
}

