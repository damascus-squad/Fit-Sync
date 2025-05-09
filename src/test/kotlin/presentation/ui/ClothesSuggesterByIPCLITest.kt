package presentation.ui

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType
import org.damascus.presentation.io.Printer
import org.damascus.presentation.ui.ClothesSuggesterByIPCLI
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClothesSuggesterByIPCLITest {

    private lateinit var printer: Printer
    private lateinit var getWeatherByIpUseCase: GetWeatherByIpUseCase
    private lateinit var suggestClothesUseCase: SuggestClothesUseCase
    private lateinit var clothesSuggesterByIPCLI: ClothesSuggesterByIPCLI

    @BeforeEach
    fun setup() {
        printer = mockk(relaxed = true)
        getWeatherByIpUseCase = mockk(relaxed = true)
        suggestClothesUseCase = mockk(relaxed = true)
        clothesSuggesterByIPCLI = ClothesSuggesterByIPCLI(printer, getWeatherByIpUseCase, suggestClothesUseCase)
    }


    @Test
    fun `should display suggested clothes when IP-based weather is available`() {
        // given
        val clothes = listOf(
            Cloth(name = "👕 T-Shirt", type = ClothType.LIGHT),
        )

        coEvery { suggestClothesUseCase(any()) } returns clothes

        // when
        clothesSuggesterByIPCLI.start()

        // then
        verify {
            printer.displayLn(match { it.contains("1.👕 T-Shirt") })
        }

    }

    @Test
    fun `should print error message when weather fetching by IP fails`() {
        // given
        val errorMessage = "Unable to determine location"

        coEvery { getWeatherByIpUseCase() } throws Exception(errorMessage)

        // when
        clothesSuggesterByIPCLI.start()

        // then
        verify {
            printer.displayLn(errorMessage)
        }

    }


    @Test
    fun `should print error when suggestClothesUseCase fails`() {
        // given
        val errorMessage = "Failed to analyze clothes"

        coEvery { suggestClothesUseCase(any()) } throws Exception(errorMessage)

        // when
        clothesSuggesterByIPCLI.start()

        // then
        verify {
            printer.displayLn(errorMessage)
        }

    }


}