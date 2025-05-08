package presentation.io

import org.damascus.presentation.io.Printer

class ConsolePrinter : Printer {
    override fun display(input: Any?) {
        print(input)
    }

    override fun displayLn(input: Any?) {
        println(input)
    }
}