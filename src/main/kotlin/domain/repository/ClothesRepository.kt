package org.damascus.domain.repository

import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType

interface ClothesRepository {
    fun getClothesByType(clothType: ClothType): List<Cloth>
    fun getAllClothes(): List<Cloth>
}
