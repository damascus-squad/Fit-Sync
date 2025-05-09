package data.clothes.datasource

import org.damascus.data.clothes.datasource.ClothesDataSourceImp
import org.damascus.domain.model.ClothType
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ClothesDataSourceImpTest {

    private val dataSource = ClothesDataSourceImp()

    @ParameterizedTest(name = "should return {1} items for type {0}")
    @CsvSource(
        "VERY_HEAVY, 6",
        "HEAVY, 5",
        "MEDIUM, 6",
        "LIGHT, 5",
        "VERY_LIGHT, 6"
    )
    fun `should return correct number of clothes for each type`(type: ClothType, expectedCount: Int) {
        // Given is already covered by CsvSource input

        // When
        val result = dataSource.getClothesByType(type)

        // Then
        assertEquals(expectedCount, result.size)
    }

    @ParameterizedTest(name = "should return only items of type {0}")
    @CsvSource(
        "VERY_HEAVY",
        "HEAVY",
        "MEDIUM",
        "LIGHT",
        "VERY_LIGHT"
    )
    fun `should return only items of the specified type`(type: ClothType) {
        // When
        val result = dataSource.getClothesByType(type)

        // Then
        assertTrue(result.all { it.type == type })
    }

    @ParameterizedTest
    @CsvSource("28") // Total items: 6 + 5 + 6 + 5 + 6 = 28
    fun `should return all clothes correctly`(expectedTotal: Int) {
        // When
        val allClothes = dataSource.getAllClothes()

        // Then
        assertEquals(expectedTotal, allClothes.size)
    }
}
