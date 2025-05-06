package org.damascus.di

import org.damascus.data.repository.WeatherRepositoryImp
import org.damascus.domain.repository.OutfitSuggestionRepository
import org.damascus.domain.repository.WeatherRepository
import org.damascus.domain.usecase.GetWeatherUseCase
import org.damascus.domain.usecase.SuggestOutfitUseCase
import org.koin.dsl.module

val appModule = module {
    single<WeatherRepository> { WeatherRepositoryImp(get()) }
    single<OutfitSuggestionRepository> { error("OutfitSuggestionRepository not implemented yet") }

    factory { GetWeatherUseCase(get()) }
    factory { SuggestOutfitUseCase(get()) }
}

