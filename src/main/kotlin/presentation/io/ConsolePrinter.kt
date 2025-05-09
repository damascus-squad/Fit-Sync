package presentation.io

import org.damascus.presentation.io.ConsoleDisplay

class ConsolePrinter : ConsoleDisplay {
    override fun display(input: Any?) {
        print(input)
    }

    override fun displayLn(input: Any?) {
        println(input)
    }
}