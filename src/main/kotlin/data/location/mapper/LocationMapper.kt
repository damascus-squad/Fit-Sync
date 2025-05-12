package org.damascus.data.location.mapper

import org.damascus.data.weather.dto.LocationDto
import org.damascus.domain.model.Location

fun LocationDto.toDomain(): Location {
    return Location(
        name = name,
        region = region,
        country = country,
        latitude = latitude,
        longitude = longitude
    )
}

fun Location.toDto(): LocationDto {
    return LocationDto(
        name = name,
        region = region,
        country = country,
        latitude = latitude,
        longitude = longitude
    )
}