package presentation.ui

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.damascus.domain.model.Cloth
import org.damascus.domain.model.ClothType
import org.damascus.domain.usecase.GetWeatherUseCase
import org.damascus.domain.usecase.SuggestClothesUseCase
import org.damascus.presentation.io.ConsoleDisplay
import presentation.io.InputReader
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClothesSuggesterByCityNameCliTest {

    private lateinit var printer: ConsoleDisplay
    private lateinit var inputReader: InputReader
    private lateinit var getWeatherUseCase: GetWeatherUseCase
    private lateinit var suggestClothesUseCase: SuggestClothesUseCase
    private lateinit var clothesSuggesterByCityNameCli: ClothesSuggesterByCityNameCli

    @BeforeTest
    fun setup() {
        printer = mockk(relaxed = true)
        inputReader = mockk(relaxed = true)
        getWeatherUseCase = mockk(relaxed = true)
        suggestClothesUseCase = mockk(relaxed = true)
        clothesSuggesterByCityNameCli = ClothesSuggesterByCityNameCli(
            printer,
            inputReader,
            getWeatherUseCase,
            suggestClothesUseCase
        )
    }


    @Test
    fun `should display suggested clothes when inputs are valid`() = runTest {
        // given
        val city = "London"
        val country = "UK"
        val fakeClothes = listOf(
            Cloth(name = "🧥Jacket", type = ClothType.HEAVY),
        )

        every { inputReader.readString(any()) } returns city andThen country
        coEvery { suggestClothesUseCase(any()) } returns fakeClothes

        // when
        clothesSuggesterByCityNameCli.start()

        // the
        verify {
            printer.displayLn(match { it.toString().contains("🧥Jacket") })
        }
    }


    @Test
    fun `should display error when getWeatherUseCase throws exception`() = runTest {
        // given
        val city = "Paris"
        val country = "France"
        val errorMessage = "An unexpected error occurred."

        every { inputReader.readString(any()) } returns city andThen country
        coEvery { getWeatherUseCase(city, country) } throws Exception(errorMessage)

        // when
        clothesSuggesterByCityNameCli.start()

        // then
        verify {
            printer.displayLn(match { it.toString().contains(errorMessage) })
        }
    }


    @Test
    fun `should display error when suggestClothesUseCase throws exception`() = runTest {
        // given
        val city = "Berlin"
        val country = "Germany"
        val errorMessage = "An unexpected error occurred."

        every { inputReader.readString(any()) } returns city andThen country
        coEvery { suggestClothesUseCase(any()) } throws Exception(errorMessage)

        // when
        clothesSuggesterByCityNameCli.start()

        // then
        verify {
            printer.displayLn(match { it.toString().contains(errorMessage) })
        }
    }


}