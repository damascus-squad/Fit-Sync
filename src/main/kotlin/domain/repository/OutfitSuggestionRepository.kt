package org.damascus.domain.repository

import org.damascus.domain.model.OutfitSuggestion
import org.damascus.domain.model.WeatherInfo

interface OutfitSuggestionRepository {
    fun suggest(weather: WeatherInfo, gender: String? = null): OutfitSuggestion
}