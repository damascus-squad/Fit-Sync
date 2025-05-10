package org.damascus.data.weather.datasource

class LocationNotFoundException(cityName: String, country: String) : Exception("City not found: $cityName, $country")
class IpNotFoundException(): Exception("Ip not found")

