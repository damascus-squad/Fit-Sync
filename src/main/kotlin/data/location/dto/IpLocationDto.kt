package org.damascus.data.location.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IpLocationDto(
    @SerialName("lat")
    val latitude: Double,
    @SerialName("lon")
    val longitude: Double
)