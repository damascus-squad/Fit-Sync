package org.damascus.data.weather.dto

import kotlinx.serialization.Serializable


@Serializable
data class LocationResultDto(val results: List<LocationDto>)

@Serializable
data class LocationDto(val latitude: Double, val longitude: Double)
