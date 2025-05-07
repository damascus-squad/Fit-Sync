package org.damascus.data.clothes.datasource

import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType

class ClothesDataSourceImp : ClothesDataSource {
    override fun getClothesByType(clothType: ClothType): List<Cloth> {
        return clothes.filter { it.type == clothType }
    }

    companion object {
        private val clothes = listOf(
            // VERY_HEAVY
            Cloth(name = "Thermal Underwear", type = ClothType.VERY_HEAVY),
            Cloth(name = "Heavy Winter Coat", type = ClothType.VERY_HEAVY),
            Cloth(name = "Insulated Gloves", type = ClothType.VERY_HEAVY),
            Cloth(name = "Wool Scarf", type = ClothType.VERY_HEAVY),
            Cloth(name = "Snow Boots", type = ClothType.VERY_HEAVY),
            Cloth(name = "Fleece-Lined Pants", type = ClothType.VERY_HEAVY),

            // HEAVY
            Cloth(name = "Thick Jacket", type = ClothType.HEAVY),
            Cloth(name = "Wool Sweater", type = ClothType.HEAVY),
            Cloth(name = "Beanie Hat", type = ClothType.HEAVY),
            Cloth(name = "Padded Vest", type = ClothType.HEAVY),
            Cloth(name = "Winter Boots", type = ClothType.HEAVY),

            // MEDIUM
            Cloth(name = "Hoodie", type = ClothType.MEDIUM),
            Cloth(name = "Lightweight Jacket", type = ClothType.MEDIUM),
            Cloth(name = "Pullover Sweater", type = ClothType.MEDIUM),
            Cloth(name = "Long-Sleeve Shirt", type = ClothType.MEDIUM),
            Cloth(name = "Jeans", type = ClothType.MEDIUM),
            Cloth(name = "Cargo Pants", type = ClothType.MEDIUM),

            // LIGHT
            Cloth(name = "T-Shirt", type = ClothType.LIGHT),
            Cloth(name = "Chinos", type = ClothType.LIGHT),
            Cloth(name = "Polo Shirt", type = ClothType.LIGHT),
            Cloth(name = "Denim Shirt", type = ClothType.LIGHT),
            Cloth(name = "Cotton Shirt", type = ClothType.LIGHT),

            // VERY_LIGHT
            Cloth(name = "Tank Top", type = ClothType.VERY_LIGHT),
            Cloth(name = "Shorts", type = ClothType.VERY_LIGHT),
            Cloth(name = "Sleeveless Shirt", type = ClothType.VERY_LIGHT),
            Cloth(name = "Flip-Flops", type = ClothType.VERY_LIGHT),
            Cloth(name = "Sun Hat", type = ClothType.VERY_LIGHT),
            Cloth(name = "Linen Pants", type = ClothType.VERY_LIGHT),
        )
    }
}