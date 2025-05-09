package org.damascus.data.clothes.datasource

import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType
interface ClothesDataSource {
    fun getAllClothes(): List<Cloth>
}
