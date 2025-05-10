package presentation.ui

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType
import org.damascus.domain.usecase.GetWeatherByIpUseCase
import org.damascus.domain.usecase.SuggestClothesUseCase
import org.damascus.presentation.io.ConsoleDisplay
import org.damascus.presentation.ui.ClothesSuggesterByIpCli
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClothesSuggesterByIpCliTest {

    private lateinit var printer: ConsoleDisplay
    private lateinit var getWeatherByIpUseCase: GetWeatherByIpUseCase
    private lateinit var suggestClothesUseCase: SuggestClothesUseCase
    private lateinit var clothesSuggesterByIpCli: ClothesSuggesterByIpCli

    @BeforeEach
    fun setup() {
        printer = mockk(relaxed = true)
        getWeatherByIpUseCase = mockk(relaxed = true)
        suggestClothesUseCase = mockk(relaxed = true)
        clothesSuggesterByIpCli = ClothesSuggesterByIpCli(printer, getWeatherByIpUseCase, suggestClothesUseCase)
    }


    @Test
    fun `should display suggested clothes when IP-based weather is available`() = runTest {
        // given
        val clothes = listOf(
            Cloth(name = "👕 T-Shirt", type = ClothType.LIGHT),
        )

        coEvery { suggestClothesUseCase(any()) } returns clothes

        // when
        clothesSuggesterByIpCli.start()

        // then
        verify {
            printer.displayLn(match {
                it.toString().contains("\n👚 Based on our high-tech fashion sensors, we suggest:")
            })
        }

    }

    @Test
    fun `should print error message when weather fetching by IP fails`() = runTest {
        // given
        val errorMessage = "Unable to determine location"

        coEvery { getWeatherByIpUseCase() } throws Exception(errorMessage)

        // when
        clothesSuggesterByIpCli.start()

        // then
        verify {
            printer.displayLn(match { it.toString().contains("Unable to determine location") })
        }

    }


    @Test
    fun `should print error when suggestClothesUseCase fails`() = runTest {
        // given
        val errorMessage = "An unexpected error occurred."

        coEvery { suggestClothesUseCase(any()) } throws Exception(errorMessage)

        // when
        clothesSuggesterByIpCli.start()

        // then
        verify {
            printer.displayLn(match { it.toString().contains("An unexpected error occurred") })
        }

    }


}