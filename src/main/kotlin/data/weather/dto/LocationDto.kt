package org.damascus.data.weather.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("name")
    val name: String = "",
    @SerialName("admin1")
    val region: String= "",
    @SerialName("country")
    val country: String= "",
)
