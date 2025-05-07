package org.damascus.di

import org.damascus.data.clothes.repository.ClothesRepositoryImpl
import org.damascus.data.weather.repository.WeatherRepositoryImp
import org.damascus.domain.repository.ClothesRepository
import org.damascus.domain.repository.WeatherRepository
import org.damascus.domain.usecase.GetWeatherUseCase
import org.damascus.domain.usecase.SuggestClothesUseCase
import org.koin.dsl.module

val appModule = module {
    single<WeatherRepository> { WeatherRepositoryImp(get()) }
    single<ClothesRepository> { ClothesRepositoryImpl(get()) }

    single { GetWeatherUseCase(get()) }
    single { SuggestClothesUseCase(get()) }

}

