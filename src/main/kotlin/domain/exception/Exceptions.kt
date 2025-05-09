package org.damascus.domain.exception

class WeatherNotFoundException : Exception("No weather found")

class IllegalTemperatureException() : IllegalArgumentException("NAN: The temperature is not valid")

class LocationNotFoundException(message: String) : Exception(message)
