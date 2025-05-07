package org.damascus.domain.model

import java.util.*

data class Cloth(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val type: ClothType,
    val isFavourite: Boolean = false,
)