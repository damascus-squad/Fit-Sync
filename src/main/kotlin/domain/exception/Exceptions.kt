package org.damascus.domain.exception

import org.koin.core.logger.MESSAGE

class WeatherNotFoundException : Exception("No weather found")
class LocationNotFoundException(message: MESSAGE) : Exception(message)

class WeatherNotFoundException(cause: Throwable) : RuntimeException(
    "Failed to fetch weather data. Please check the city or country name.", cause
)
