package presentation.ui

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType
import org.damascus.domain.usecase.GetWeatherUseCase
import org.damascus.domain.usecase.SuggestClothesUSeCase
import org.damascus.presentation.io.Printer
import presentation.io.InputReader
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClothesSuggesterByCityNameCLITest {

    private lateinit var printer: Printer
    private lateinit var inputReader: InputReader
    private lateinit var getWeatherUseCase: GetWeatherUseCase
    private lateinit var suggestClothesUseCase: SuggestClothesUSeCase
    private lateinit var clothesSuggesterByCityNameCLI: ClothesSuggesterByCityNameCLI

    @BeforeTest
    fun setup() {
        printer = mockk(relaxed = true)
        inputReader = mockk(relaxed = true)
        getWeatherUseCase = mockk(relaxed = true)
        suggestClothesUseCase = mockk(relaxed = true)
        clothesSuggesterByCityNameCLI = ClothesSuggesterByCityNameCLI(
            printer,
            inputReader,
            getWeatherUseCase,
            suggestClothesUseCase
        )
    }


    @Test
    fun `should display suggested clothes when inputs are valid`() {
        // given
        val city = "London"
        val country = "UK"
        val fakeClothes = listOf(
            Cloth(name = "🧥Jacket", type = ClothType.HEAVY),
        )

        every { inputReader.readString(any()) } returns city andThen country
        coEvery { suggestClothesUseCase(any()) } returns fakeClothes

        // when
        clothesSuggesterByCityNameCLI.start()

        // then
        verify {
            printer.displayLn(match { it.contains("1-🧥Jacket") })
        }
    }


    @Test
    fun `should display error when getWeatherUseCase throws exception`() {
        // given
        val city = "Paris"
        val country = "France"
        val errorMessage = "Network error"

        every { inputReader.readString(any()) } returns city andThen country
        coEvery { getWeatherUseCase(city, country) } throws Exception(errorMessage)

        // when
        clothesSuggesterByCityNameCLI.start()

        // then
        verify {
            printer.displayLn(match { it.contains("Error: $errorMessage") })
        }
    }


    @Test
    fun `should display error when suggestClothesUseCase throws exception`() {
        // given
        val city = "Berlin"
        val country = "Germany"
        val weatherMock = mockk<Any>()
        val errorMessage = "Failed to suggest clothes"

        every { inputReader.readString(any()) } returns city andThen country
        coEvery { getWeatherUseCase(city, country) } returns weatherMock
        coEvery { suggestClothesUseCase(weatherMock) } throws Exception(errorMessage)

        // when
        clothesSuggesterByCityNameCLI.start()

        // then
        verify {
            printer.displayLn(match { it.contains("Error: $errorMessage") })
        }
    }


}