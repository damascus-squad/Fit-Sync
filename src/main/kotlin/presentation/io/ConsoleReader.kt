package presentation.io

import presentation.TerminalColor
import presentation.withStyle

class ConsoleReader: InputReader {

    override fun readString(prompt: String): String {
        print("$prompt ".withStyle(TerminalColor.Blue))
        while (true) {
            val input = readln().trim()

            if (input.isEmpty()) {
                println("❌ Input cannot be empty. Please try again.".withStyle(TerminalColor.Red))
                print("$prompt ".withStyle(TerminalColor.Blue))
            } else return input
        }
    }
    override fun readInt(prompt: String, min: Int?, max: Int?): Int {
        print("$prompt ".withStyle(TerminalColor.Blue))
        while (true) {
            val input = readlnOrNull()?.trim()?.toIntOrNull() ?: throw Exception("Wrong input: ")

            if ((min != null && input < min) || (max != null && input > max)) {
                println("❌ Invalid input. Try again.\n".withStyle(TerminalColor.Red))
            } else return input
        }
    }
}