package data.clothes.datasource

import org.damascus.data.clothes.datasource.ClothesDataSourceImp
import org.damascus.domain.model.ClothType
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClothesDataSourceImpTest {

    private lateinit var clothesDataSource: ClothesDataSourceImp

    @BeforeEach
    fun setUp() {
        clothesDataSource = ClothesDataSourceImp()
    }
    @Test
    fun `getClothesByType returns only VERY_HEAVY clothes`() {
        val result = clothesDataSource.getClothesByType(ClothType.VERY_HEAVY)
        assertEquals(6, result.size)
        assertTrue(result.all { it.type == ClothType.VERY_HEAVY })
    }

    @Test
    fun `getClothesByType returns correct items for MEDIUM`() {
        val result = clothesDataSource.getClothesByType(ClothType.MEDIUM)
        assertEquals(6, result.size)
        assertTrue(result.all { it.type == ClothType.MEDIUM })
    }

    @Test
    fun `getClothesByType returns correct items for VERY_LIGHT`() {
        val result = clothesDataSource.getClothesByType(ClothType.VERY_LIGHT)
        assertEquals(6, result.size)
        assertTrue(result.all { it.type == ClothType.VERY_LIGHT })
    }
}