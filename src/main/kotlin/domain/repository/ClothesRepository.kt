package org.damascus.domain.repository

import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType

interface ClothesRepository {
    fun getClothByType(clothType: ClothType): List<Cloth>
}