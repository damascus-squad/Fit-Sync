package org.damascus.domain.exception

class WeatherNotFoundException : Exception("No weather found")

class IllegalTemperatureException : IllegalArgumentException("NAN: The temperature is not valid")

class LocationNotFoundException(message: String) : Exception(message)

class CountryAndCityNotFoundException(cityName: String, country: String) :
    Exception("There is no cache for: $country $cityName")

class IpNotFoundException : Exception("This IP not found")

