package presentation.ui

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.damascus.presentation.io.ConsoleDisplay
import org.damascus.presentation.ui.ClothesSuggesterByIpCli
import org.damascus.presentation.ui.UiAction
import org.junit.jupiter.api.BeforeEach
import presentation.io.InputReader
import kotlin.test.Test

class FitSyncAppTest {

    private lateinit var consoleDisplay: ConsoleDisplay
    private lateinit var inputReader: InputReader
    private lateinit var clothesSuggesterByCityNameCli: ClothesSuggesterByCityNameCli
    private lateinit var clothesSuggesterByIpCli: ClothesSuggesterByIpCli
    private lateinit var fitSyncApp: FitSyncApp

    @BeforeEach
    fun setUp() {
        consoleDisplay = mockk(relaxed = true)
        inputReader = mockk(relaxed = true)
        clothesSuggesterByCityNameCli = mockk(relaxed = true)
        clothesSuggesterByIpCli = mockk(relaxed = true)

        fitSyncApp = FitSyncApp(
            consoleDisplay,
            inputReader,
            clothesSuggesterByCityNameCli,
            clothesSuggesterByIpCli
        )
    }


    @Test
    fun `start should call showMenu with expected UiActions`() = runTest {
        // given
        every { inputReader.readInt(any(), any(), any()) } returns 1
        every { inputReader.readInt(any(), any(), any()) } returnsMany listOf(1, 0)

        // when
        fitSyncApp.start()

        // then
        coVerify {
            clothesSuggesterByCityNameCli.start()
        }
    }


    @Test
    fun `IP action should call clothesSuggesterByIpCli start`() = runTest {
        // given
        val action = UiAction(
            name = "🌍 Suggest Clothes Based on Your IP Location",
            action = { clothesSuggesterByIpCli.start() }
        )

        // when
        action.action.invoke()

        // then
        coVerify { clothesSuggesterByIpCli.start() }
    }


    @Test
    fun `Exit action should print exit message`() = runTest {
        // given
        every { inputReader.readInt(any(), any(), any()) } returns 3
        every { inputReader.readInt(any(), any(), any()) } returnsMany listOf(3, 0)

        // when
        fitSyncApp.start()

        // then
        verify {
            consoleDisplay.displayLn(match { it.toString().contains("Exiting FitSyncApp") })
        }
    }


    @Test
    fun `should  handle out of range input correctly`() = runTest {
        //given
        every { inputReader.readInt(any(), 0, 3) } returnsMany listOf(99, 0)

        //when
        fitSyncApp.start()

        //then
        verify(atLeast = 0) { consoleDisplay.displayLn(match { it.toString().contains("⚠️") }) }
    }


    @Test
    fun `should select multiple options sequentially`() = runTest {
        //given
        every { inputReader.readInt(any(), any(), any()) } returnsMany listOf(1, 2, 0)

        //when
        fitSyncApp.start()

        //then
        coVerify(exactly = 1) { clothesSuggesterByCityNameCli.start() }
        coVerify(exactly = 1) { clothesSuggesterByIpCli.start() }
    }


    @Test
    fun `should suggest clothes by city is called when option 1 selected`() = runTest {
        //given
        every { inputReader.readInt(any(), any(), any()) } returnsMany listOf(1, 0)

        //when
        fitSyncApp.start()

        //then

        coVerify(exactly = 1) { clothesSuggesterByCityNameCli.start() }
        coVerify(exactly = 0) { clothesSuggesterByIpCli.start() }
    }


    @Test
    fun `test suggest clothes by IP is called when option 2 selected`() = runTest {
        //given
        every { inputReader.readInt(any(), any(), any()) } returnsMany listOf(2, 0)

        //when
        fitSyncApp.start()

        //then
        coVerify { clothesSuggesterByIpCli.start() }
    }


    @Test
    fun `start should execute UiAction when valid input is given`() = runTest {
        //given
        every { inputReader.readInt(any(), any(), any()) } returnsMany listOf(1, 0)

        // when
        fitSyncApp.start()

        // then
        coVerify(exactly = 1) { clothesSuggesterByCityNameCli.start() }
    }

}



