package org.damascus

import org.damascus.data.weather.dto.CurrentWeather
import org.damascus.di.appModule
import org.damascus.domain.usecase.SuggestClothesUseCase
import org.koin.core.context.GlobalContext.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }
}