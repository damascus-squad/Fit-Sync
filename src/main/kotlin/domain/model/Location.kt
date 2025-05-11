package org.damascus.domain.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val region: String,
    val country: String
)