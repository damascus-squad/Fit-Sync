package org.damascus.domain.exception


class WeatherNotFoundException(cause: Throwable) : RuntimeException(
    "Failed to fetch weather data. Please check the city or country name.", cause
)
