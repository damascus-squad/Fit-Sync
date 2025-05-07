package org.damascus.data.clothes.repository

import org.damascus.data.clothes.datasource.ClothesDataSource
import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType
import org.damascus.domain.repository.ClothesRepository

class ClothesRepositoryImpl(
    private val dataSource: ClothesDataSource,
) : ClothesRepository {
    override fun getClothByType(clothType: ClothType): List<Cloth> {
        return dataSource.getClothesByType(clothType)
    }
}